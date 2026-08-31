# Simulated Integrations & Production Readiness Playbook

This document details all third-party integrations, external university systems, and payment services that are currently **simulated in development/sandbox mode**, along with the exact architectural hooks, API endpoints, and configuration required when wiring real production providers.

---

## 1. Summary of Simulated Integrations

| Feature / Subsystem | Development / Sandbox Behavior | Production Architecture & Live Provider |
| :--- | :--- | :--- |
| **M-Pesa Fee Payment** (`POST /fees/pay/mpesa`) | Immediately returns HTTP 200 OK with a generated receipt reference (e.g. `MPESA-QK892...`), records the payment transaction in the PostgreSQL `fee_transactions` ledger, updates the outstanding balance, and clears fees. | **Safaricom Daraja API (Lipa na M-Pesa Online STK Push)** or **eCitizen GOK Paybill 300059 API**. Asynchronous callback handler listening for `C2B` / `STK Callback` webhooks. |
| **HELB / HEF Loan Disbursement** | Seeded and ledger-balanced under `fee_transactions` with `TransactionType.HELB_DISBURSEMENT` / `HEF_SCHOLARSHIP`. | **Higher Education Financing (HEF) & HELB REST Integration**. Batched automated disbursements triggered via government webhook or CSV reconciliation queue. |
| **SMIS Course Registration** (`POST /units/register`) | Validates that registered units belong to the student's current year of study and semester, stores records in `unit_registrations`, and marks them as `APPROVED` with an academic receipt. | **UoN SMIS ERP (Oracle / Microsoft SQL Server)**. Two-phase registration committing to local Lakebase Postgres and calling SMIS SOAP/REST student information API with rollback support. |
| **Hostel Booking & Room Allocation** (`POST /hostels/book`, `/hostels/pay`) | Automatically reserves the requested room space in `hostel_bookings` and marks status `CONFIRMED` / `PAID`. | **UoN SWA (Student Welfare Authority) Room Management API**. Real-time lock / optimistic concurrency on bed space availability with automated release timer. |
| **Unified Student Identity (SMIS vs AD)** (`POST /auth/signup`, `/auth/login`) | Creates account in Neon Auth and links both `student_email` (`...@students.uonbi.ac.ke`) and `reg_no` (`P15/...`) to the same `user_id`. Logins via SMIS or AD resolve to the exact same student profile. | **Neon Auth (Better-Auth) with Microsoft Entra ID / Azure AD SAML Single Sign-On (SSO)** for University of Nairobi Active Directory accounts. |

---

## 2. Where Simulated Logic Lives & How to Swap for Production

### A. M-Pesa & Fee Payments
- **Current File:** `server/src/main/kotlin/com/wolfeleo2/myuon/server/routes/FeeRoutes.kt`
- **Production Implementation:**
  1. Add Safaricom Daraja credentials to environment:
     ```env
     DARAJA_CONSUMER_KEY=your_consumer_key
     DARAJA_CONSUMER_SECRET=your_consumer_secret
     DARAJA_PASSKEY=your_passkey
     DARAJA_SHORTCODE=300059
     DARAJA_CALLBACK_URL=https://api.myuon.uonbi.ac.ke/fees/callbacks/mpesa
     ```
  2. In `FeeRoutes.kt`, replace the instantaneous ledger insert with an asynchronous STK Push request (`https://api.safaricom.co.ke/mpesa/stkpush/v1/processrequest`).
  3. Register a webhook route `post("/fees/callbacks/mpesa")` that receives the Daraja confirmation JSON payload, parses the result code, and completes the `FeeTransactionsTable` record.

### B. Course Registration & SMIS Validation
- **Current File:** `server/src/main/kotlin/com/wolfeleo2/myuon/server/db/ExposedAcademicRepository.kt`
- **Production Implementation:**
  1. Add SMIS connector service in `server/src/main/kotlin/com/wolfeleo2/myuon/server/integration/SmisConnector.kt`.
  2. When `registerUnits` is called, submit a transactional RPC to SMIS ERP.
  3. Store the confirmed SMIS registration reference in `unit_registrations`.

### C. Active Directory & Microsoft 365 OAuth SSO
- **Current File:** `server/src/main/kotlin/com/wolfeleo2/myuon/server/auth/NeonAuthClient.kt`
- **Production Implementation:**
  1. Enable Microsoft OAuth provider in Neon Auth using `add_auth_oauth_provider`.
  2. Configure Azure AD Client ID and Tenant ID for `uonbi.ac.ke`.
  3. Client navigates to Neon Auth OAuth flow for institutional Microsoft SSO.

---

## 3. Fresh Student Account Model (First Year Baseline)

When a new student signs up via `POST /auth/signup`:
1. **Initial Year & Semester:** `yearOfStudy = 1`, `semester = 1`.
2. **Financial Baseline:** Fee statement initialized with `outstandingBalance = 36000.0`, `isFeeCleared = false`, `totalPaid = 0.0`.
3. **Accommodation Baseline:** No room bookings (`/hostels/booking` returns `null`).
4. **Academic Baseline:** No registered units (`/units/registered` returns `[]`), available units scoped strictly to Year 1 Semester 1.

# myUON Backend: Ktor Project Shape & Dual-Mode Auth — Design

Status: approved, not yet implemented.

## Context

myUON currently runs entirely on mock data (`app/.../MockDataProvider.kt`) and
an unused local Room scaffold (`data/db/Entities.kt` / `Daos.kt` /
`MyUonDatabase.kt`). No backend exists. This spec covers two decisions needed
before backend work starts:

1. How the Ktor project is shaped (module layout).
2. How the app's two login modes — SMIS reg-no/password and Active Directory
   (AD) email/password — map to one underlying identity.

Stack context (decided previously, unchanged here): **Neon (Postgres) +
Neon Auth (built on Better Auth) + Ktor (API layer) + Cloudflare R2 (object
storage)**. Domain data (units, grades, fees, timetable, attendance) is
relational, which is why Postgres/Neon won over Firebase. Ktor was chosen
over Spring Boot for `kotlinx.serialization` alignment, coroutine-native fit,
and lighter cold starts against Neon's scale-to-zero. Supabase remains a
noted faster-to-ship alternative if a custom Ktor backend is ever dropped in
favor of it — not chosen.

Neither SMIS nor Active Directory are real external systems myUON talks to
today — there is nothing to integrate against yet. This design treats both
as *mock/local* until a real integration is scoped separately.

## Decision 1: Ktor project shape

**Single Gradle module**, not a multi-module KMP `:shared` split. The
justification for `:shared` — reusing models/repos across a future
iOS/web/desktop KMP client — is a live but *unfirm* goal ("maybe soon-ish").
A real `:shared` module doesn't pay for itself until a second client exists
to consume it.

Instead, enforce the module boundary that `:shared` would eventually own via
package layering inside the single module:

```
server/
  domain/     # plain Kotlin: models (kotlinx.serialization), repo
              # interfaces, business logic. No Ktor-server types
              # (Application, ApplicationCall, routing) and no
              # Exposed/DB-entity types allowed here.
  routes/     # Ktor routing/handlers. Calls into domain/, never db/ directly.
  db/         # Exposed entities + repo implementations. Maps to/from
              # domain/ models at the boundary.
```

When a second client is actually being built, `domain/` is lifted into a
real `:shared` Gradle module — a module-boundary move along an existing
package boundary, not a rewrite, because nothing in `domain/` ever imported
a Ktor-server or Exposed type.

**Discipline this design depends on:** DB entities are never returned
directly from a route handler, and `ApplicationCall`/routing types are never
passed into `domain/` functions. This is the one rule that keeps the future
extraction cheap; violating it anywhere reintroduces the coupling the
layering exists to avoid.

## Decision 2: Dual-mode login → single identity

**Requirement:** a user who signs in via AD email in one session and via
SMIS reg-no in another must resolve to the same account.

**Neon Auth is a hosted REST API**, not an in-process library — Ktor talks
to it over HTTP like any external service (verified: Neon Auth, aka
"Managed Better Auth," has no Kotlin/JVM SDK; any backend language calls it
via HTTP). It also has no schema-level custom-field mechanism worth relying
on for `regNo` — custom data on a hosted user is an untyped `serverMetadata`
JSON blob with no unique index and no "look up user by custom field"
endpoint. So `regNo` is kept entirely on our own side, not pushed into Neon
Auth at all.

**Design:**

- Neon Auth owns exactly **one credential pair per user**: email + password.
  It is the only component that ever checks a password, reached from Ktor
  via its REST API.
- The app's actual student data (regNo, faculty, department, program,
  yearOfStudy, semester, campus, nationalId, mobileNumber, photoUrl,
  fee-cleared status, etc. — the existing `StudentProfile` model in
  `data/model/StudentProfile.kt`, already driving the mock UI) lives in its
  own `students` table in `db/`, one row per Neon Auth user id (FK), mapped
  to/from a `domain/` `StudentProfile` model. This isn't new scope — it's
  the same domain data the stack decision already named (units, grades,
  fees, timetable, attendance all key off it) and the app already models;
  the auth design just needed to say where it's keyed from.
- `students.regNo` is unique-indexed. It's also where the SMIS-login lookup
  happens — no separate mapping table, no data duplicated into Neon Auth.
- **Signup** is a single flow that collects email, password, and regNo
  together: creates one Neon Auth user (email + password) via its REST API,
  then creates one `students` row (regNo + that user's id + studentEmail).
  This is the one deliberate moment the AD identity and the SMIS identity
  are linked — there is no later inference step, because nothing exists yet
  to infer the link from. (The rest of `StudentProfile` — faculty, program,
  year, etc. — is filled in separately, e.g. seeded alongside signup for
  mock purposes; a real integration would populate it from SMIS instead.)
- **AD-mode login**: routes straight to Neon Auth's REST sign-in with the
  email/password the user typed.
- **SMIS-mode login**: the Ktor route looks up `regNo → studentEmail` in our
  own `students` table, then calls Neon Auth's REST sign-in with that email
  and the password the user typed. Same credential check, same session
  issuance, different lookup key. No second password is ever stored or
  checked by us.

This is intentionally shaped so that a future real SMIS/AD integration only
replaces the *signup* step (verify against real SMIS/AD instead of trusting
what the user typed) — the login-mode routing above doesn't change.

## Out of scope / explicitly deferred

- Real SMIS API / Active Directory (LDAP) integration — both modes are
  self-issued/mocked for now.
- A `:shared` KMP module — deferred until a second client is actually being
  built.
- M-Pesa Daraja webhook endpoint — noted as a future reason a real public
  Ktor endpoint is needed, not designed here.

## Open questions

None remaining from this round. See project memory
(`myuon-backend-stack-decision`) for the running record of this decision
thread.

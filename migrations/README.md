# myUON Database Migrations

This folder tracks SQL schema migrations for the **myUON** backend running on **Neon Lakebase Postgres**.

---

## Migration History

| Version | File | Description | Applied Status |
| :--- | :--- | :--- | :--- |
| `0001` | [`0001_initial_schema.sql`](./0001_initial_schema.sql) | Core domain tables: `students`, `units`, `unit_registrations`, `grade_records`, `fee_statements`, `fee_transactions`, `timetable_items`, `exam_timetable_items`, `attendance_sessions`, `student_attendance`, `hostels`, `hostel_bookings`, `academic_requests` | Applied (`production`) |

---

## Applying Migrations to Neon

### Option 1: Via Neon MCP
The agent can apply migrations directly or prepare a branch migration using Neon MCP tools:
- `prepare_database_migration`
- `complete_database_migration`
- `run_sql`

### Option 2: Via psql or Neon CLI
```bash
# Connect using the DATABASE_URL from .env.local
psql "$DATABASE_URL" -f migrations/0001_initial_schema.sql
```

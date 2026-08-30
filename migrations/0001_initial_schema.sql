-- ==============================================================================
-- Migration: 0001_initial_schema.sql
-- Description: Core schema for myUON portal
-- Includes: students, course units, unit registrations, grade records,
--           fee accounts & transactions, timetables, attendance, hostel bookings,
--           and academic requests.
-- ==============================================================================

-- 1. Enable UUID Extension if not enabled
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Students Table (Maps 1-to-1 with Neon Auth users in neon_auth.user)
CREATE TABLE IF NOT EXISTS students (
    user_id VARCHAR(64) PRIMARY KEY,
    reg_no VARCHAR(64) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    student_email VARCHAR(255) NOT NULL UNIQUE,
    faculty VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    program VARCHAR(255) NOT NULL,
    year_of_study INT NOT NULL CHECK (year_of_study >= 1),
    semester INT NOT NULL CHECK (semester IN (1, 2, 3)),
    campus VARCHAR(100) NOT NULL,
    national_id VARCHAR(50) NOT NULL,
    mobile_number VARCHAR(50) NOT NULL,
    photo_url TEXT,
    is_fee_cleared BOOLEAN NOT NULL DEFAULT TRUE,
    is_registered_for_semester BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_students_reg_no ON students(reg_no);
CREATE INDEX IF NOT EXISTS idx_students_student_email ON students(student_email);

-- 3. Course Units Table
CREATE TABLE IF NOT EXISTS units (
    code VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    credits INT NOT NULL DEFAULT 3,
    lecturer_name VARCHAR(255) NOT NULL,
    lecturer_email VARCHAR(255),
    department VARCHAR(255) NOT NULL,
    year_of_study INT NOT NULL,
    semester INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. Unit Registrations
CREATE TABLE IF NOT EXISTS unit_registrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id VARCHAR(64) NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    unit_code VARCHAR(20) NOT NULL REFERENCES units(code) ON DELETE CASCADE,
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
    registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_student_unit_registration UNIQUE (student_id, unit_code, academic_year, semester)
);

CREATE INDEX IF NOT EXISTS idx_unit_reg_student ON unit_registrations(student_id);

-- 5. Grade Records (Composite key: student + unit + year + semester)
CREATE TABLE IF NOT EXISTS grade_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id VARCHAR(64) NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    unit_code VARCHAR(20) NOT NULL REFERENCES units(code) ON DELETE CASCADE,
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    cat_mark DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    exam_mark DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    grade_letter VARCHAR(5) NOT NULL,
    is_pass BOOLEAN NOT NULL DEFAULT TRUE,
    is_supplementary BOOLEAN NOT NULL DEFAULT FALSE,
    is_special BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_grade_record UNIQUE (student_id, unit_code, academic_year, semester)
);

CREATE INDEX IF NOT EXISTS idx_grade_records_student ON grade_records(student_id);

-- 6. Fee Statements & Transactions
CREATE TABLE IF NOT EXISTS fee_statements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id VARCHAR(64) NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    total_invoiced NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    total_paid NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    current_balance NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_fee_statement UNIQUE (student_id, academic_year, semester)
);

CREATE TABLE IF NOT EXISTS fee_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id VARCHAR(64) NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    transaction_type VARCHAR(30) NOT NULL, -- INVOICE, MPESA_PAYMENT, HELB_DISBURSEMENT, BURSARY
    description VARCHAR(255) NOT NULL,
    reference_no VARCHAR(100),
    amount NUMERIC(12, 2) NOT NULL,
    balance_after NUMERIC(12, 2) NOT NULL,
    transaction_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fee_transactions_student ON fee_transactions(student_id);

-- 7. Timetable Items
CREATE TABLE IF NOT EXISTS timetable_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_code VARCHAR(20) NOT NULL REFERENCES units(code) ON DELETE CASCADE,
    day_of_week VARCHAR(15) NOT NULL, -- Monday, Tuesday, etc.
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(100) NOT NULL,
    building VARCHAR(100) NOT NULL,
    session_type VARCHAR(50) NOT NULL DEFAULT 'LECTURE'
);

-- 8. Exam Timetable Items
CREATE TABLE IF NOT EXISTS exam_timetable_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_code VARCHAR(20) NOT NULL REFERENCES units(code) ON DELETE CASCADE,
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    exam_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    venue VARCHAR(100) NOT NULL,
    seat_number VARCHAR(50)
);

-- 9. Attendance Schedule & Sessions (Weekly sessions matching real UoN timetables)
CREATE TABLE IF NOT EXISTS attendance_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_code VARCHAR(20) NOT NULL REFERENCES units(code) ON DELETE CASCADE,
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    week_number INT NOT NULL,
    session_number INT NOT NULL, -- 1 or 2 per week
    session_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_attendance_session UNIQUE (unit_code, academic_year, semester, week_number, session_number)
);

CREATE TABLE IF NOT EXISTS student_attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    student_id VARCHAR(64) NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    is_attended BOOLEAN NOT NULL DEFAULT FALSE,
    marked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_student_attendance UNIQUE (session_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_student_attendance_student ON student_attendance(student_id);

-- 10. Hostels & Bookings
CREATE TABLE IF NOT EXISTS hostels (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    campus VARCHAR(100) NOT NULL,
    gender VARCHAR(20) NOT NULL, -- MALE, FEMALE, MIXED
    total_rooms INT NOT NULL,
    available_rooms INT NOT NULL,
    rate_per_semester NUMERIC(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS hostel_bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id VARCHAR(64) NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    hostel_id VARCHAR(50) NOT NULL REFERENCES hostels(id),
    room_number VARCHAR(20) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, CANCELLED, CHECKED_OUT
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,
    booked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_hostel_booking UNIQUE (student_id, academic_year, semester)
);

-- 11. Academic Requests (Special Exams, Supplementary, Missing Marks)
CREATE TABLE IF NOT EXISTS academic_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id VARCHAR(64) NOT NULL REFERENCES students(user_id) ON DELETE CASCADE,
    request_type VARCHAR(30) NOT NULL, -- SPECIAL_EXAM, SUPPLEMENTARY, MISSING_MARKS
    unit_code VARCHAR(20) NOT NULL REFERENCES units(code),
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    reason TEXT NOT NULL,
    document_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    reviewer_comments TEXT,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reviewed_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_academic_requests_student ON academic_requests(student_id);

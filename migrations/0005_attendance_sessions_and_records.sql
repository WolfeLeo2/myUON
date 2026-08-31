-- ==============================================================================
-- Migration: 0005_attendance_sessions_and_records.sql
-- Description: Seeds attendance sessions and attendance records for student user_demo_1
-- ==============================================================================

-- Seed attendance sessions for Year 1 units (CSC 111, CSC 112) and Year 3 units (CSC 311, CSC 315, CSC 321)
DO $$
DECLARE
    u_code TEXT;
    w INT;
    s INT;
    sess_id UUID;
    is_att BOOLEAN;
BEGIN
    FOR u_code IN SELECT unnest(ARRAY['CSC 111', 'CSC 112', 'CSC 121', 'CSC 122', 'CSC 311', 'CSC 315', 'CSC 321']) LOOP
        FOR w IN 1..12 LOOP
            FOR s IN 1..2 LOOP
                sess_id := gen_random_uuid();
                
                INSERT INTO attendance_sessions (id, unit_code, academic_year, semester, week_number, session_number, session_date)
                VALUES (
                    sess_id,
                    u_code,
                    '2025/2026',
                    2,
                    w,
                    s,
                    (DATE '2026-05-04' + ((w - 1) * 7 + (s * 2)) * INTERVAL '1 day')::DATE::TEXT
                )
                ON CONFLICT (id) DO NOTHING;

                -- Mark ~90% attendance for user_demo_1
                is_att := (w != 4 OR s != 2) AND (w != 9 OR s != 1);
                INSERT INTO student_attendance (id, session_id, student_id, is_attended, marked_at)
                VALUES (
                    gen_random_uuid(),
                    sess_id,
                    'user_demo_1',
                    is_att,
                    (DATE '2026-05-04' + ((w - 1) * 7 + (s * 2)) * INTERVAL '1 day')::DATE::TEXT || ' 09:05:00'
                )
                ON CONFLICT (id) DO NOTHING;
            END LOOP;
        END LOOP;
    END LOOP;
END $$;

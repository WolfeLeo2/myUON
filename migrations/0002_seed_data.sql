-- ==============================================================================
-- Migration: 0002_seed_data.sql
-- Description: Initial test and development seed data for University of Nairobi
-- ==============================================================================

-- 1. Demo Student Profile
INSERT INTO students (
    user_id, reg_no, full_name, student_email, faculty, department, program,
    year_of_study, semester, campus, national_id, mobile_number, photo_url,
    is_fee_cleared, is_registered_for_semester
) VALUES (
    'user_demo_1', 'P15/12345/2022', 'Leo K.', 'leo@students.uonbi.ac.ke',
    'Faculty of Science & Technology', 'Department of Computer Science',
    'Bachelor of Science in Computer Science', 3, 2, 'Chiromo',
    '38920194', '+254 712 345 678', NULL, TRUE, TRUE
) ON CONFLICT (user_id) DO UPDATE SET
    reg_no = EXCLUDED.reg_no,
    full_name = EXCLUDED.full_name,
    student_email = EXCLUDED.student_email;

-- 2. Units
INSERT INTO units (code, title, credits, lecturer_name, lecturer_email, department, year_of_study, semester) VALUES
    ('CSC 311', 'Advanced Database Systems', 3, 'Prof. Peter Wagacha', 'pwagacha@uonbi.ac.ke', 'Department of Computer Science', 3, 2),
    ('CSC 315', 'Operating Systems Principles', 3, 'Dr. Andrew Mwangi', 'amwangi@uonbi.ac.ke', 'Department of Computer Science', 3, 2),
    ('CSC 321', 'Distributed Systems & Cloud Computing', 3, 'Dr. Lawrence Muchemi', 'lmuchemi@uonbi.ac.ke', 'Department of Computer Science', 3, 2),
    ('CSC 323', 'Artificial Intelligence & Machine Learning', 3, 'Dr. Richard M. Rimiru', 'rrimiru@uonbi.ac.ke', 'Department of Computer Science', 3, 2),
    ('CSC 327', 'Compiler Construction', 3, 'Prof. Christopher Chepken', 'cchepken@uonbi.ac.ke', 'Department of Computer Science', 3, 2),
    ('CSC 331', 'Computer Graphics & Multimedia', 3, 'Dr. Elisha Opiyo', 'eopiyo@uonbi.ac.ke', 'Department of Computer Science', 3, 2),
    ('CSC 341', 'Software Engineering Methodologies', 3, 'Dr. Agnes Mindila', 'amindila@uonbi.ac.ke', 'Department of Computer Science', 3, 2),
    -- Historical units
    ('CSC 111', 'Introduction to Computer Systems', 3, 'Dr. Andrew Mwangi', 'amwangi@uonbi.ac.ke', 'Department of Computer Science', 1, 1),
    ('CSC 112', 'Structured Programming', 3, 'Prof. Christopher Chepken', 'cchepken@uonbi.ac.ke', 'Department of Computer Science', 1, 1),
    ('CSC 113', 'Discrete Mathematics', 3, 'Prof. Peter Wagacha', 'pwagacha@uonbi.ac.ke', 'Department of Computer Science', 1, 1),
    ('CSC 114', 'Calculus I for CS', 3, 'Dr. Richard M. Rimiru', 'rrimiru@uonbi.ac.ke', 'Department of Computer Science', 1, 1),
    ('CSC 115', 'Communication Skills', 3, 'Dr. Agnes Mindila', 'amindila@uonbi.ac.ke', 'Department of Computer Science', 1, 1),
    ('CSC 121', 'Object Oriented Programming', 3, 'Dr. Lawrence Muchemi', 'lmuchemi@uonbi.ac.ke', 'Department of Computer Science', 1, 2),
    ('CSC 122', 'Data Structures & Algorithms', 3, 'Prof. Christopher Chepken', 'cchepken@uonbi.ac.ke', 'Department of Computer Science', 1, 2),
    ('CSC 123', 'Digital Electronics', 3, 'Dr. Andrew Mwangi', 'amwangi@uonbi.ac.ke', 'Department of Computer Science', 1, 2),
    ('CSC 124', 'Linear Algebra for CS', 3, 'Prof. Peter Wagacha', 'pwagacha@uonbi.ac.ke', 'Department of Computer Science', 1, 2),
    ('CSC 125', 'Probability & Statistics', 3, 'Dr. Richard M. Rimiru', 'rrimiru@uonbi.ac.ke', 'Department of Computer Science', 1, 2),
    ('CSC 211', 'Database Systems', 3, 'Prof. Peter Wagacha', 'pwagacha@uonbi.ac.ke', 'Department of Computer Science', 2, 1),
    ('CSC 212', 'Computer Architecture', 3, 'Dr. Andrew Mwangi', 'amwangi@uonbi.ac.ke', 'Department of Computer Science', 2, 1),
    ('CSC 213', 'Object Oriented Analysis & Design', 3, 'Dr. Lawrence Muchemi', 'lmuchemi@uonbi.ac.ke', 'Department of Computer Science', 2, 1),
    ('CSC 214', 'Calculus II', 3, 'Dr. Richard M. Rimiru', 'rrimiru@uonbi.ac.ke', 'Department of Computer Science', 2, 1),
    ('CSC 215', 'Economics for CS', 3, 'Dr. Agnes Mindila', 'amindila@uonbi.ac.ke', 'Department of Computer Science', 2, 1),
    ('CSC 221', 'Computer Networks', 3, 'Dr. Andrew Mwangi', 'amwangi@uonbi.ac.ke', 'Department of Computer Science', 2, 2),
    ('CSC 222', 'Design & Analysis of Algorithms', 3, 'Prof. Christopher Chepken', 'cchepken@uonbi.ac.ke', 'Department of Computer Science', 2, 2),
    ('CSC 223', 'Web Technologies', 3, 'Dr. Lawrence Muchemi', 'lmuchemi@uonbi.ac.ke', 'Department of Computer Science', 2, 2),
    ('CSC 224', 'Numerical Analysis', 3, 'Prof. Peter Wagacha', 'pwagacha@uonbi.ac.ke', 'Department of Computer Science', 2, 2),
    ('CSC 225', 'Research Methodologies', 3, 'Dr. Agnes Mindila', 'amindila@uonbi.ac.ke', 'Department of Computer Science', 2, 2),
    ('CSC 301', 'Automata Theory', 3, 'Prof. Christopher Chepken', 'cchepken@uonbi.ac.ke', 'Department of Computer Science', 3, 1),
    ('CSC 303', 'Mobile Application Development', 3, 'Dr. Lawrence Muchemi', 'lmuchemi@uonbi.ac.ke', 'Department of Computer Science', 3, 1),
    ('CSC 305', 'Human Computer Interaction', 3, 'Dr. Agnes Mindila', 'amindila@uonbi.ac.ke', 'Department of Computer Science', 3, 1),
    ('CSC 307', 'Computer Security & Cryptography', 3, 'Dr. Andrew Mwangi', 'amwangi@uonbi.ac.ke', 'Department of Computer Science', 3, 1),
    ('CSC 309', 'Data Science Fundamentals', 3, 'Dr. Richard M. Rimiru', 'rrimiru@uonbi.ac.ke', 'Department of Computer Science', 3, 1)
ON CONFLICT (code) DO NOTHING;

-- 3. Unit Registrations for Demo Student
INSERT INTO unit_registrations (student_id, unit_code, academic_year, semester, status) VALUES
    ('user_demo_1', 'CSC 311', '2025/2026', 2, 'APPROVED'),
    ('user_demo_1', 'CSC 315', '2025/2026', 2, 'APPROVED'),
    ('user_demo_1', 'CSC 321', '2025/2026', 2, 'APPROVED'),
    ('user_demo_1', 'CSC 323', '2025/2026', 2, 'APPROVED'),
    ('user_demo_1', 'CSC 327', '2025/2026', 2, 'APPROVED'),
    ('user_demo_1', 'CSC 331', '2025/2026', 2, 'APPROVED')
ON CONFLICT (student_id, unit_code, academic_year, semester) DO NOTHING;

-- 4. Historical Grade Records
INSERT INTO grade_records (student_id, unit_code, academic_year, semester, cat_mark, exam_mark, total_score, grade_letter, is_pass, is_supplementary, is_special) VALUES
    ('user_demo_1', 'CSC 111', '2023/2024', 1, 24.6, 57.4, 82.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 112', '2023/2024', 1, 22.8, 53.2, 76.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 113', '2023/2024', 1, 20.4, 47.6, 68.0, 'B', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 114', '2023/2024', 1, 22.2, 51.8, 74.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 115', '2023/2024', 1, 21.0, 49.0, 70.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 121', '2023/2024', 2, 23.4, 54.6, 78.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 122', '2023/2024', 2, 21.3, 49.7, 71.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 123', '2023/2024', 2, 19.5, 45.5, 65.0, 'B', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 124', '2023/2024', 2, 24.0, 56.0, 80.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 125', '2023/2024', 2, 22.5, 52.5, 75.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 211', '2024/2025', 1, 21.6, 50.4, 72.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 212', '2024/2025', 1, 20.4, 47.6, 68.0, 'B', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 213', '2024/2025', 1, 25.5, 59.5, 85.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 214', '2024/2025', 1, 21.0, 49.0, 70.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 215', '2024/2025', 1, 19.8, 46.2, 66.0, 'B', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 221', '2024/2025', 2, 23.7, 55.3, 79.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 222', '2024/2025', 2, 22.2, 51.8, 74.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 223', '2024/2025', 2, 24.3, 56.7, 81.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 224', '2024/2025', 2, 20.7, 48.3, 69.0, 'B', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 225', '2024/2025', 2, 23.1, 53.9, 77.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 301', '2025/2026', 1, 22.5, 52.5, 75.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 303', '2025/2026', 1, 24.9, 58.1, 83.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 305', '2025/2026', 1, 21.3, 49.7, 71.0, 'A', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 307', '2025/2026', 1, 20.1, 46.9, 67.0, 'B', TRUE, FALSE, FALSE),
    ('user_demo_1', 'CSC 309', '2025/2026', 1, 23.4, 54.6, 78.0, 'A', TRUE, FALSE, FALSE)
ON CONFLICT (student_id, unit_code, academic_year, semester) DO NOTHING;

-- 5. Timetable Slots
INSERT INTO timetable_items (unit_code, day_of_week, start_time, end_time, room, building, session_type) VALUES
    ('CSC 311', 'Monday', '09:00:00', '11:00:00', 'Lab 02', 'Chiromo Science Complex', 'LECTURE'),
    ('CSC 321', 'Monday', '14:00:00', '16:00:00', 'Lab 01', 'Chiromo Science Complex', 'LECTURE'),
    ('CSC 315', 'Tuesday', '11:00:00', '13:00:00', 'MLT 01', 'Main Lecture Theatre', 'LECTURE'),
    ('CSC 323', 'Wednesday', '08:00:00', '10:00:00', 'Lab 03', 'Chiromo Science Complex', 'LECTURE'),
    ('CSC 327', 'Thursday', '10:00:00', '12:00:00', 'Room 204', 'Chiromo Science Complex', 'LECTURE'),
    ('CSC 331', 'Friday', '14:00:00', '16:00:00', 'Graphics Lab', 'Chiromo Science Complex', 'LECTURE');

-- 6. Exam Timetable
INSERT INTO exam_timetable_items (unit_code, academic_year, semester, exam_date, start_time, end_time, venue, seat_number) VALUES
    ('CSC 311', '2025/2026', 2, '2026-09-08', '08:30:00', '11:30:00', 'Chiromo Examination Hall 1', 'DK-14'),
    ('CSC 315', '2025/2026', 2, '2026-09-10', '14:00:00', '17:00:00', 'Main Lecture Theatre (MLT)', 'DK-28'),
    ('CSC 321', '2025/2026', 2, '2026-09-12', '08:30:00', '11:30:00', 'Chiromo Science Lab 02', 'DK-05'),
    ('CSC 323', '2025/2026', 2, '2026-09-15', '14:00:00', '17:00:00', '8-4-4 Building Ground Floor', 'DK-42'),
    ('CSC 327', '2025/2026', 2, '2026-09-17', '08:30:00', '11:30:00', 'Taifa Hall', 'DK-19');

-- 7. Hostels & Bookings
INSERT INTO hostels (id, name, campus, gender, total_rooms, available_rooms, rate_per_semester) VALUES
    ('hall-1', 'Hall 9', 'Main Campus', 'MALE', 120, 14, 6500.00),
    ('hall-2', 'Hall 10', 'Main Campus', 'MALE', 140, 8, 6500.00),
    ('hall-3', 'Box (Women''s Hall)', 'Main Campus', 'FEMALE', 200, 22, 7000.00),
    ('hall-4', 'Chiromo Hall 1', 'Chiromo Campus', 'MALE', 80, 5, 8000.00),
    ('hall-5', 'Chiromo Hall 2', 'Chiromo Campus', 'FEMALE', 80, 11, 8000.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO hostel_bookings (student_id, hostel_id, room_number, academic_year, semester, status, is_paid) VALUES
    ('user_demo_1', 'hall-4', '204', '2025/2026', 2, 'ACTIVE', TRUE)
ON CONFLICT (student_id, academic_year, semester) DO NOTHING;

-- 8. Fee Statements & Transactions
INSERT INTO fee_statements (student_id, academic_year, semester, total_invoiced, total_paid, current_balance) VALUES
    ('user_demo_1', '2025/2026', 2, 36000.00, 36000.00, 0.00)
ON CONFLICT (student_id, academic_year, semester) DO NOTHING;

INSERT INTO fee_transactions (student_id, academic_year, semester, transaction_type, description, reference_no, amount, balance_after, transaction_date) VALUES
    ('user_demo_1', '2025/2026', 2, 'INVOICE', 'Tuition Fees 2025/2026 Semester 2', 'INV-2026-0021', 36000.00, 36000.00, '2026-01-10'),
    ('user_demo_1', '2025/2026', 2, 'PAYMENT_MPESA', 'M-Pesa Tuition Payment QK82947193', 'QK82947193', 36000.00, 0.00, '2026-01-15');

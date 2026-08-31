-- ==============================================================================
-- Migration: 0004_year1_syllabus_timetables_and_student_isolation.sql
-- Description: Seeds Year 1 Course Syllabi, Timetables, and Leo K. Year 1 Academic History
-- ==============================================================================

-- ==============================================================================
-- 1. YEAR 1 SEMESTER 1 UNITS (Syllabi & Descriptions)
-- ==============================================================================

UPDATE units SET
    lecturer_office = 'Chiromo Science Complex, Rm 101',
    venue_name = 'LT 01 (Chiromo)',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Mon 08:00 - 10:00',
    description = 'Foundations of computer systems, von Neumann architecture, binary representations, digital logic gates, hardware abstractions, operating systems overview, computer networks, and societal impacts of computing.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"History of Computing & Number Systems","subtopics":["Evolution from vacuum tubes to VLSI","Binary, octal, hexadecimal representations","Floating-point IEEE 754 standard"]},{"weekNumber":2,"title":"Computer Hardware Architecture","subtopics":["CPU components: ALU, Control Unit, Registers","Instruction execution cycle: Fetch, Decode, Execute","Memory hierarchy: Cache, RAM, ROM, Secondary storage"]},{"weekNumber":3,"title":"Operating Systems & Software Systems","subtopics":["System software vs Application software","OS functions: Process scheduling, memory management","File systems and Command Line Interface (CLI) basics"]},{"weekNumber":4,"title":"Networking & The Internet","subtopics":["OSI 7-layer model vs TCP/IP model","IP addressing, Subnetting, and DNS","Client-server architecture and HTTP/HTTPS protocols"]}]',
    learning_outcomes_json = '["Understand binary representation, arithmetic, and floating point computations.","Identify core hardware components and explain the instruction execution cycle.","Use Linux CLI for basic system administration and development tasks."]',
    recommended_textbooks_json = '["Computer Science: An Overview (13th Ed.) — J. Glenn Brookshear & Dennis Brylow","Structured Computer Organization (6th Ed.) — Andrew S. Tanenbaum"]',
    prerequisites_json = '[]'
WHERE code = 'CSC 111';

UPDATE units SET
    lecturer_office = 'Chiromo Science Complex, Rm 205',
    venue_name = 'Chiromo Lab 01',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Tue 10:00 - 12:00',
    description = 'Procedural and structured programming in C. Covers data types, control flow, functions, recursion, arrays, string manipulation, pointers, dynamic memory allocation (malloc/free), structs, unions, and file I/O operations.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"C Language Fundamentals & Compilation","subtopics":["GCC compiler toolchain: Preprocessing, compilation, assembly, linking","Data types, type qualifiers, format specifiers","Control structures: if-else, switch-case, for, while, do-while"]},{"weekNumber":2,"title":"Functions, Scope & Recursion","subtopics":["Function prototypes, call stack, activation records","Pass by value vs Pass by reference simulation","Recursive problem solving and base cases"]},{"weekNumber":3,"title":"Pointers & Dynamic Memory Allocation","subtopics":["Pointer arithmetic, dereferencing, and void pointers","Dynamic memory: malloc, calloc, realloc, and free","Memory leaks, buffer overflows, and Valgrind memory debugging"]},{"weekNumber":4,"title":"Structures, Unions & File I/O","subtopics":["Struct alignment, padding, and nested structs","File pointers, fopen, fread, fwrite, fprintf, fscanf","Command line arguments argc and argv"]}]',
    learning_outcomes_json = '["Develop robust, memory-safe structured programs in ANSI C.","Master pointers, memory layout, and dynamic memory allocation.","Read and write structured binary and text data from files."]',
    recommended_textbooks_json = '["The C Programming Language (2nd Ed.) — Brian W. Kernighan & Dennis M. Ritchie","C Programming: A Modern Approach (2nd Ed.) — K. N. King"]',
    prerequisites_json = '[]'
WHERE code = 'CSC 112';

UPDATE units SET
    lecturer_office = 'Chiromo Science Complex, Rm 315',
    venue_name = 'Chiromo Rm 204',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Wed 14:00 - 16:00',
    description = 'Mathematical foundations for computer science: propositional and predicate logic, proof techniques (induction, contradiction), set theory, relations, functions, combinatorics, recurrence relations, and graph theory basics.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Propositional & Predicate Logic","subtopics":["Truth tables, logical equivalences, and normal forms (CNF/DNF)","Quantifiers (universal, existential) and predicate calculus","Rules of inference and valid argument structures"]},{"weekNumber":2,"title":"Proof Techniques & Mathematical Induction","subtopics":["Direct proofs, proof by contraposition, proof by contradiction","Weak mathematical induction and strong induction","Well-ordering principle and structural induction"]},{"weekNumber":3,"title":"Sets, Relations & Functions","subtopics":["Set operations, power sets, and Cartesian products","Equivalence relations, partitions, and partial orders","Injective, surjective, and bijective functions"]},{"weekNumber":4,"title":"Combinatorics & Graph Theory Basics","subtopics":["Permutations, combinations, Pigeonhole Principle","Binomial Theorem and generating functions","Graphs: Vertices, edges, paths, cycles, trees, Euler and Hamiltonian paths"]}]',
    learning_outcomes_json = '["Construct rigorous mathematical proofs using direct, contradiction, and inductive methods.","Model computer science problems using discrete structures like sets, relations, and graphs.","Apply combinatorics principles to analyze algorithm complexity bounds."]',
    recommended_textbooks_json = '["Discrete Mathematics and its Applications (8th Ed.) — Kenneth H. Rosen","Mathematics for Computer Science — Eric Lehman, F. Thomson Leighton, Albert R. Meyer"]',
    prerequisites_json = '[]'
WHERE code = 'CSC 113';

UPDATE units SET
    lecturer_office = 'Chiromo Science Complex, Rm 118',
    venue_name = 'LT 02 (Chiromo)',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Thu 08:00 - 10:00',
    description = 'Differential and integral calculus with applications in computer science: limits, continuity, derivatives, optimization problems, Taylor series, numerical integration, and differential equations.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Limits, Continuity & Differentiation","subtopics":["Formal epsilon-delta definition of limits","Techniques of differentiation: Product, quotient, and chain rules","Implicit differentiation and related rates"]},{"weekNumber":2,"title":"Applications of Derivatives in Computing","subtopics":["Maxima, minima, and critical points","Optimization problems in machine learning cost functions","Taylor and Maclaurin polynomial series approximations"]},{"weekNumber":3,"title":"Definite & Indefinite Integrals","subtopics":["Riemann sums and the Fundamental Theorem of Calculus","Integration by parts, partial fractions, and trigonometric substitution","Numerical integration: Trapezoidal and Simpson rules"]}]',
    learning_outcomes_json = '["Calculate limits, derivatives, and definite integrals accurately.","Apply derivative optimization techniques to computational and machine learning models."]',
    recommended_textbooks_json = '["Calculus: Early Transcendentals (8th Ed.) — James Stewart","Thomas Calculus (14th Ed.) — Hass, Heil, Weir"]',
    prerequisites_json = '[]'
WHERE code = 'CSC 114';

UPDATE units SET
    lecturer_office = 'Education Building, Rm 204',
    venue_name = 'ED 01',
    campus = 'Main Campus',
    is_core = FALSE,
    schedule_time = 'Fri 10:00 - 12:00',
    description = 'Development of critical thinking, academic writing, presentation skills, technical report writing, professional communication, and digital research ethics.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Academic Writing & Citation Styles","subtopics":["APA, IEEE, and Chicago referencing styles","Avoiding plagiarism and academic integrity","Structuring technical reports, abstracts, and executive summaries"]},{"weekNumber":2,"title":"Oral & Visual Technical Presentations","subtopics":["Audience analysis and message structuring","Slide design principles for technical presentations","Handling Q&A sessions and scientific peer reviews"]}]',
    learning_outcomes_json = '["Author structured technical reports following IEEE/APA standards.","Deliver effective verbal presentations on computational topics."]',
    recommended_textbooks_json = '["Technical Communication (12th Ed.) — Mike Markel & Stuart A. Selber"]',
    prerequisites_json = '[]'
WHERE code = 'CSC 115';

-- ==============================================================================
-- 2. YEAR 1 SEMESTER 2 UNITS (Syllabi & Descriptions)
-- ==============================================================================

UPDATE units SET
    lecturer_office = 'Chiromo Science Complex, Rm 210',
    venue_name = 'Chiromo Lab 02',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Mon 10:00 - 12:00',
    description = 'Object-oriented software development using Java / Kotlin. Principles of encapsulation, inheritance, polymorphism, abstraction, interface design, generics, exception handling, and Java Collections Framework.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Classes, Objects & Encapsulation","subtopics":["Constructors, instance variables, access modifiers (public, private, protected)","Getters/setters, immutability, and data integrity","Memory model: JVM Stack vs Heap allocation"]},{"weekNumber":2,"title":"Inheritance, Polymorphism & Interfaces","subtopics":["Method overriding vs overloading","Abstract classes vs Interfaces (Default and static methods)","Runtime polymorphism and dynamic method dispatch"]},{"weekNumber":3,"title":"Generics & Java Collections Framework","subtopics":["List, Set, Map, and Queue hierarchy","ArrayList vs LinkedList, HashMap vs TreeMap time complexities","Generic classes, bounded type parameters, and wildcards"]},{"weekNumber":4,"title":"Exception Handling & Modern I/O Streams","subtopics":["Checked vs Unchecked exceptions, try-catch-finally, try-with-resources","Custom exception hierarchy design","NIO.2 file path operations and object serialization"]}]',
    learning_outcomes_json = '["Design object-oriented systems applying SOLID and OOP principles.","Utilize the Collections framework effectively for complex data processing.","Implement robust error handling and stream-based data I/O."]',
    recommended_textbooks_json = '["Effective Java (3rd Ed.) — Joshua Bloch","Core Java Volume I: Fundamentals (12th Ed.) — Cay S. Horstmann"]',
    prerequisites_json = '["CSC 112 Structured Programming"]'
WHERE code = 'CSC 121';

UPDATE units SET
    lecturer_office = 'Chiromo Science Complex, Rm 308',
    venue_name = 'Chiromo Lab 01',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Tue 14:00 - 16:00',
    description = 'Design, implementation, and asymptotic complexity analysis of fundamental data structures: dynamic arrays, singly/doubly linked lists, stacks, queues, binary search trees (BST), heaps/priority queues, hash tables, and sorting algorithms.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Asymptotic Analysis & Linear Data Structures","subtopics":["Big-O, Big-Omega, Big-Theta notation","Dynamic array amortized constant time resizing","Singly and Doubly linked list node manipulations"]},{"weekNumber":2,"title":"Stacks, Queues & Priority Queues","subtopics":["Stack LIFO operations: Expression evaluation (postfix conversion)","Queue FIFO operations: Circular buffer implementation","Binary Heaps: Min/Max heapify, priority queue scheduling"]},{"weekNumber":3,"title":"Trees & Binary Search Trees (BST)","subtopics":["Tree traversals: In-order, Pre-order, Post-order, Level-order","BST insertion, search, and node deletion algorithms","Tree balance concepts and introduction to AVL trees"]},{"weekNumber":4,"title":"Hash Tables & Sorting Algorithms","subtopics":["Hash functions, collision resolution: Chaining vs Open Addressing","Sorting: MergeSort, QuickSort (Lomuto/Hoare partition), HeapSort","Counting sort and Radix sort linear time algorithms"]}]',
    learning_outcomes_json = '["Analyze time and space complexity of algorithms using asymptotic notations.","Select and implement optimal data structures for algorithmic challenges.","Implement and evaluate divide-and-conquer sorting and searching algorithms."]',
    recommended_textbooks_json = '["Introduction to Algorithms (4th Ed.) — Cormen, Leiserson, Rivest, Stein (CLRS)","Algorithms (4th Ed.) — Robert Sedgewick & Kevin Wayne"]',
    prerequisites_json = '["CSC 112 Structured Programming"]'
WHERE code = 'CSC 122';

UPDATE units SET
    lecturer_office = 'Chiromo Science Complex, Rm 104',
    venue_name = 'Physics Lab 03',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Wed 10:00 - 12:00',
    description = 'Boolean algebra, logic gate minimization (K-maps), combinational logic (adders, multiplexers, decoders), sequential logic (latches, flip-flops, registers, counters), and finite state machine (FSM) hardware design.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Boolean Algebra & Logic Simplification","subtopics":["Boolean laws, De Morgan theorems","Karnaugh Maps (K-Maps) 2, 3, 4 variables and don''t care conditions","NAND and NOR universal gate implementations"]},{"weekNumber":2,"title":"Combinational Circuit Design","subtopics":["Half adders, Full adders, Ripple-carry adders","Multiplexers (MUX), Demultiplexers (DEMUX), Encoders, Decoders","Arithmetic Logic Unit (ALU) 1-bit bit-slice architecture"]},{"weekNumber":3,"title":"Sequential Circuits & Flip-Flops","subtopics":["SR, D, JK, and T flip-flops and timing diagrams","Master-slave flip-flops and clock jitter","Synchronous and asynchronous counter design"]},{"weekNumber":4,"title":"Finite State Machines (FSM)","subtopics":["Moore vs Mealy state machines","State transition tables, state reduction, and excitation tables","FPGA hardware description introduction using Verilog"]}]',
    learning_outcomes_json = '["Minimize Boolean expressions and design combinational logic circuits.","Design synchronous sequential logic circuits and state counters.","Model finite state machines for digital hardware controllers."]',
    recommended_textbooks_json = '["Digital Design (6th Ed.) — M. Morris Mano & Michael D. Ciletti"]',
    prerequisites_json = '["CSC 111 Introduction to Computer Systems"]'
WHERE code = 'CSC 123';

UPDATE units SET
    lecturer_office = 'Chiromo Science Complex, Rm 316',
    venue_name = 'Chiromo Rm 204',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Thu 14:00 - 16:00',
    description = 'Vector spaces, linear transformations, matrices, systems of linear equations (Gaussian elimination), determinants, eigenvalues, eigenvectors, singular value decomposition (SVD), and geometric computer vision applications.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Vector Spaces & Matrix Operations","subtopics":["Vector operations in R^n, dot product, cross product, norm","Matrix multiplication, transpose, invertibility","Linear combinations, span, linear independence, basis and dimension"]},{"weekNumber":2,"title":"Systems of Linear Equations & Gaussian Elimination","subtopics":["Augmented matrices and Row Reduced Echelon Form (RREF)","Rank-Nullity theorem and solution spaces","LU Decomposition and numerical stability"]},{"weekNumber":3,"title":"Eigenvalues, Eigenvectors & Diagonalization","subtopics":["Characteristic polynomials and eigenvalue computation","Matrix diagonalization and geometric interpretation","Principal Component Analysis (PCA) introduction"]},{"weekNumber":4,"title":"Orthogonality & Singular Value Decomposition","subtopics":["Gram-Schmidt orthogonalization process and QR decomposition","Least squares approximations and linear regression derivation","Singular Value Decomposition (SVD) and image compression"]}]',
    learning_outcomes_json = '["Solve large systems of linear equations using matrix decomposition techniques.","Compute eigenvalues and eigenvectors for dimensionality reduction in machine learning.","Apply linear algebra transformations to 3D computer graphics and vision."]',
    recommended_textbooks_json = '["Linear Algebra and Its Applications (5th Ed.) — David C. Lay","Introduction to Linear Algebra (5th Ed.) — Gilbert Strang"]',
    prerequisites_json = '["CSC 113 Discrete Mathematics"]'
WHERE code = 'CSC 124';

UPDATE units SET
    lecturer_office = 'Chiromo Science Complex, Rm 214',
    venue_name = 'LT 02 (Chiromo)',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Fri 08:00 - 10:00',
    description = 'Probability axioms, random variables (discrete and continuous), probability distributions (Binomial, Poisson, Normal, Exponential), joint distributions, central limit theorem, hypothesis testing, and maximum likelihood estimation.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Probability Axioms & Conditional Probability","subtopics":["Sample spaces, events, Kolmogorov axioms","Conditional probability, Law of Total Probability, Bayes Theorem","Independence of random events"]},{"weekNumber":2,"title":"Discrete & Continuous Random Variables","subtopics":["Probability Mass Functions (PMF) and Cumulative Distribution Functions (CDF)","Probability Density Functions (PDF) and expectation/variance","Distributions: Bernoulli, Binomial, Poisson, Uniform, Exponential, Gaussian"]},{"weekNumber":3,"title":"Joint Distributions & Central Limit Theorem","subtopics":["Joint PMFs/PDFs, marginals, and conditional distributions","Covariance, correlation coefficient, and independence","Law of Large Numbers and Central Limit Theorem (CLT)"]},{"weekNumber":4,"title":"Statistical Inference & Hypothesis Testing","subtopics":["Point estimation: Maximum Likelihood Estimation (MLE)","Confidence intervals for population means","Null hypothesis significance testing (p-values, t-test, z-test)"]}]',
    learning_outcomes_json = '["Calculate probabilities using Bayes theorem and probability distributions.","Perform statistical hypothesis testing and interpret confidence intervals.","Derive Maximum Likelihood Estimators for probabilistic machine learning models."]',
    recommended_textbooks_json = '["Probability and Statistics for Computer Scientists (3rd Ed.) — Michael Baron","Introduction to Probability (2nd Ed.) — Dimitri P. Bertsekas & John N. Tsitsiklis"]',
    prerequisites_json = '["CSC 114 Calculus I for CS"]'
WHERE code = 'CSC 125';

-- ==============================================================================
-- 3. YEAR 1 TIMETABLE ITEMS
-- ==============================================================================

INSERT INTO timetable_items (id, unit_code, day_of_week, start_time, end_time, room, building, session_type)
VALUES
    -- Year 1 Sem 1
    (gen_random_uuid(), 'CSC 111', 'Monday', '08:00:00', '10:00:00', 'LT 01', 'Chiromo Science Complex', 'LECTURE'),
    (gen_random_uuid(), 'CSC 112', 'Tuesday', '10:00:00', '12:00:00', 'Lab 01', 'Chiromo Science Complex', 'LABORATORY'),
    (gen_random_uuid(), 'CSC 113', 'Wednesday', '14:00:00', '16:00:00', 'Room 204', 'Chiromo Science Complex', 'LECTURE'),
    (gen_random_uuid(), 'CSC 114', 'Thursday', '08:00:00', '10:00:00', 'LT 02', 'Chiromo Science Complex', 'LECTURE'),
    (gen_random_uuid(), 'CSC 115', 'Friday', '10:00:00', '12:00:00', 'ED 01', 'Education Building', 'LECTURE'),

    -- Year 1 Sem 2
    (gen_random_uuid(), 'CSC 121', 'Monday', '10:00:00', '12:00:00', 'Lab 02', 'Chiromo Science Complex', 'LABORATORY'),
    (gen_random_uuid(), 'CSC 122', 'Tuesday', '14:00:00', '16:00:00', 'Lab 01', 'Chiromo Science Complex', 'LECTURE'),
    (gen_random_uuid(), 'CSC 123', 'Wednesday', '10:00:00', '12:00:00', 'Physics Lab 03', 'Chiromo Science Complex', 'LABORATORY'),
    (gen_random_uuid(), 'CSC 124', 'Thursday', '14:00:00', '16:00:00', 'Room 204', 'Chiromo Science Complex', 'LECTURE'),
    (gen_random_uuid(), 'CSC 125', 'Friday', '08:00:00', '10:00:00', 'LT 02', 'Chiromo Science Complex', 'LECTURE')
ON CONFLICT (id) DO NOTHING;

-- ==============================================================================
-- Migration: 0003_unit_syllabus_and_details.sql
-- Description: Adds syllabus, overview, learning outcomes, and course metadata to units table
-- ==============================================================================

ALTER TABLE units ADD COLUMN IF NOT EXISTS lecturer_office VARCHAR(255) DEFAULT 'Department of Computer Science, Chiromo';
ALTER TABLE units ADD COLUMN IF NOT EXISTS venue_name VARCHAR(100) DEFAULT 'Lecture Theatre';
ALTER TABLE units ADD COLUMN IF NOT EXISTS campus VARCHAR(100) DEFAULT 'Chiromo';
ALTER TABLE units ADD COLUMN IF NOT EXISTS is_core BOOLEAN DEFAULT TRUE;
ALTER TABLE units ADD COLUMN IF NOT EXISTS schedule_time VARCHAR(100) DEFAULT 'Mon 09:00 - 11:00';
ALTER TABLE units ADD COLUMN IF NOT EXISTS description TEXT DEFAULT '';
ALTER TABLE units ADD COLUMN IF NOT EXISTS syllabus_topics_json TEXT DEFAULT '[]';
ALTER TABLE units ADD COLUMN IF NOT EXISTS learning_outcomes_json TEXT DEFAULT '[]';
ALTER TABLE units ADD COLUMN IF NOT EXISTS recommended_textbooks_json TEXT DEFAULT '[]';
ALTER TABLE units ADD COLUMN IF NOT EXISTS prerequisites_json TEXT DEFAULT '[]';

-- Update CSC 311
UPDATE units SET
    lecturer_office = 'Chiromo Science Complex, Rm 304',
    venue_name = 'Chiromo Lab 02',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Mon 09:00 - 11:00',
    description = 'Covers relational algebra query optimization, multi-version concurrency control (MVCC), transaction isolation levels (ACID), distributed storage engines, write-ahead logging (WAL), B+ tree indexing strategies, and modern NoSQL column/graph database architectures.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Database Engine Architectures & Storage Engines","subtopics":["Buffer pool management and page replacement algorithms","Row-oriented (Postgres) vs Columnar (Parquet/DuckDB) storage","Disk allocation, slotted pages, and tuple headers"]},{"weekNumber":2,"title":"Indexing Strategies & Tree Data Structures","subtopics":["B+ Tree depth, fan-out, and concurrent node splits","LSM-Trees (Log-Structured Merge-Trees) in Cassandra/RocksDB","Hash indexes, GiST, GIN, and spatial indices"]},{"weekNumber":3,"title":"Query Execution & Cost-Based Optimizer (CBO)","subtopics":["Query parse trees and logical-to-physical plan generation","Join algorithms: Nested loop, Block Nested, Hash Join, Merge Join","Histogram statistics and cardinality estimation"]},{"weekNumber":4,"title":"Concurrency Control & Isolation Levels","subtopics":["Two-Phase Locking (2PL) and deadlock detection graph","Multi-Version Concurrency Control (MVCC) in PostgreSQL","Anomalies: Dirty reads, non-repeatable reads, phantom reads, write skew"]},{"weekNumber":5,"title":"Crash Recovery & Write-Ahead Logging (WAL)","subtopics":["ARIES recovery algorithm: Analysis, Redo, and Undo passes","Checkpoints, fuzzy checkpoints, and dirty page tables","WAL replication and synchronous vs asynchronous commit"]},{"weekNumber":6,"title":"Distributed Databases & Consensus","subtopics":["Two-Phase Commit (2PC) protocol and coordinator failure","CAP Theorem and PACELC trade-offs","Horizontal sharding, consistent hashing, and replica sets"]}]',
    learning_outcomes_json = '["Evaluate query execution plans using EXPLAIN ANALYZE and implement appropriate indexing strategies.","Design and configure high-concurrency database systems avoiding transactional anomalies.","Implement distributed partitioning, replication, and failover architectures for enterprise workloads."]',
    recommendedTextbooks_json = '["Database Management Systems (3rd Ed.) — Raghu Ramakrishnan & Johannes Gehrke","Designing Data-Intensive Applications — Martin Kleppmann","Database Internals: A Deep Dive into How Distributed Systems Work — Alex Petrov"]',
    prerequisites_json = '["CSC 211 Database Systems","CSC 122 Data Structures & Algorithms"]'
WHERE code = 'CSC 311';

-- Update CSC 315
UPDATE units SET
    lecturer_office = 'Department of Computer Science, Rm 208',
    venue_name = 'MLT 01 (Main Lecture Theatre)',
    campus = 'Main Campus',
    is_core = TRUE,
    schedule_time = 'Tue 11:00 - 13:00',
    description = 'Detailed exploration of kernel architecture, hardware interrupts, process life cycles, preemptive scheduling, POSIX threads, synchronization primitives (mutexes, semaphores, monitors), virtual memory paging, page replacement algorithms, file systems (ext4, NTFS), and Linux kernel module programming.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Kernel Architecture & Hardware Traps","subtopics":["Dual-mode CPU operation: User mode vs Kernel mode","System call dispatch table and trap handlers","Monolithic kernels vs Microkernels (Linux vs seL4)"]},{"weekNumber":2,"title":"Process Scheduling & Thread Management","subtopics":["PCB structure, context switching, and fork/exec semantics","Completely Fair Scheduler (CFS) and real-time scheduling","User-level vs kernel-level threads, pthread programming"]},{"weekNumber":3,"title":"Synchronization & Concurrency Primitives","subtopics":["Race conditions, critical sections, and Peterson algorithm","Hardware atomic instructions: CAS, Test-and-Set","Semaphores, condition variables, and reader-writer lock implementations"]},{"weekNumber":4,"title":"Deadlocks & Resource Allocation","subtopics":["Coffman conditions for deadlock occurrence","Resource allocation graphs (RAG) and cycle detection","Banker algorithm for deadlock avoidance"]},{"weekNumber":5,"title":"Virtual Memory & Paging Mechanics","subtopics":["Page tables, multi-level paging, and Inverted Page Tables","Translation Lookaside Buffer (TLB) hits, misses, and shootdowns","Page replacement algorithms: Clock, LRU, Second-chance"]},{"weekNumber":6,"title":"Storage Systems & Linux File System VFS","subtopics":["Inodes, direct/indirect blocks, and ext4 extents","Journaling modes and crash consistency","Virtual File System (VFS) abstraction and device drivers"]}]',
    learning_outcomes_json = '["Write thread-safe concurrent C/POSIX programs using mutexes, semaphores, and condition variables.","Analyze and debug virtual memory allocation bottlenecks, page faults, and cache misses.","Develop loadable kernel modules and understand system call interception mechanisms."]',
    recommendedTextbooks_json = '["Operating Systems: Three Easy Pieces (OSTEP) — Remzi H. Arpaci-Dusseau & Andrea C. Arpaci-Dusseau","Operating System Concepts (10th Ed.) — Silberschatz, Galvin & Gagne","Understanding the Linux Kernel (3rd Ed.) — Daniel P. Bovet & Marco Cesati"]',
    prerequisites_json = '["CSC 212 Computer Architecture","CSC 112 Structured Programming"]'
WHERE code = 'CSC 315';

-- Update CSC 321
UPDATE units SET
    lecturer_office = 'Chiromo Science Complex, Rm 312',
    venue_name = 'Chiromo Lab 01',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Mon 14:00 - 16:00',
    description = 'Covers distributed systems principles, Remote Procedure Calls (gRPC/Protobuf), vector clocks, distributed consensus algorithms (Raft, Paxos), cloud computing virtualization, container orchestration with Kubernetes, serverless paradigms, and distributed tracing.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Distributed Systems Fundamentals & RPC","subtopics":["Network fallacies and failure modes in distributed environments","Protocol Buffers schema definitions and gRPC client/server codegen","Idempotency keys and at-least-once vs exactly-once semantics"]},{"weekNumber":2,"title":"Time, Clocks & Distributed State Ordering","subtopics":["Physical clock drift, NTP, and TrueTime (Google Spanner)","Lamport logical timestamps and partial ordering","Vector clocks for causal dependency tracking and conflict detection"]},{"weekNumber":3,"title":"Distributed Consensus & Leader Election","subtopics":["FLP Impossibility theorem for asynchronous systems","Raft consensus: Leader election, log replication, safety invariants","Paxos overview and Multi-Paxos leader leases"]},{"weekNumber":4,"title":"Cloud Infrastructure & Virtualization","subtopics":["Hypervisors Type 1 vs Type 2, SR-IOV network virtualization","Linux cgroups, namespaces, and OCI container runtimes","Kubernetes control plane architecture: etcd, API Server, Kubelet"]},{"weekNumber":5,"title":"Microservices Design Patterns & Resilience","subtopics":["Circuit breakers, retry with exponential backoff and jitter","API Gateways, service meshes (Envoy/Istio), and mTLS","Distributed tracing with OpenTelemetry and Jaeger"]},{"weekNumber":6,"title":"Serverless Computing & Edge Architectures","subtopics":["FaaS execution lifecycles and cold start mitigation","Event-driven messaging: Kafka partition offsets vs SQS","Edge workers, CDN caches, and geo-distributed read replicas"]}]',
    learning_outcomes_json = '["Build production-ready microservices using gRPC and Protobuf with distributed tracing.","Implement and debug distributed consensus protocols and leader election algorithms.","Architect and deploy multi-tier containerized applications to Kubernetes clusters."]',
    recommendedTextbooks_json = '["Distributed Systems (4th Ed.) — Maarten van Steen & Andrew S. Tanenbaum","Designing Data-Intensive Applications — Martin Kleppmann","Cloud Native Infrastructure — Justin Garrison & Kris Nova"]',
    prerequisites_json = '["CSC 221 Computer Networks","CSC 211 Database Systems"]'
WHERE code = 'CSC 321';

-- Update CSC 323
UPDATE units SET
    lecturer_office = 'Department of Computer Science, Rm 302',
    venue_name = 'Chiromo Lab 03',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Wed 08:00 - 10:00',
    description = 'Covers classical AI search algorithms, constraint satisfaction, adversarial search, supervised learning (linear models, decision trees, SVMs), unsupervised learning (k-means, PCA), neural networks backpropagation, and deep learning architectures (CNNs, Transformers) using PyTorch.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Informed Search & Heuristic Optimization","subtopics":["A* search admissibility, consistency, and graph search invariants","Minimax algorithm and Alpha-Beta pruning in game trees","Constraint Satisfaction Problems (CSP): Arc consistency AC-3"]},{"weekNumber":2,"title":"Probabilistic Reasoning & Bayesian Networks","subtopics":["Conditional independence and Bayes rule derivations","Bayesian Belief Networks (BBN) inference and sampling","Hidden Markov Models (HMM) and Viterbi algorithm"]},{"weekNumber":3,"title":"Supervised Machine Learning Foundations","subtopics":["Linear regression, cost functions, gradient descent variants","Logistic regression, cross-entropy loss, and ROC-AUC metrics","Decision Trees, entropy, Gini impurity, and Random Forests"]},{"weekNumber":4,"title":"Support Vector Machines & Regularization","subtopics":["Maximal margin hyperplanes and kernel trick (RBF, Polynomial)","L1 (Lasso) vs L2 (Ridge) regularization and feature selection","Bias-variance tradeoff, cross-validation, and hyperparameter tuning"]},{"weekNumber":5,"title":"Neural Networks & Backpropagation Engine","subtopics":["Multi-Layer Perceptron (MLP) architecture and activation functions","Matrix calculus derivation of the backpropagation algorithm","Optimization algorithms: Adam, RMSprop, learning rate schedules"]},{"weekNumber":6,"title":"Deep Learning: CNNs & Transformer Architectures","subtopics":["Convolutional filters, pooling, stride, and ResNet skip connections","Self-attention mechanism, Multi-Head Attention, and Transformer encoder","Transfer learning and fine-tuning pretrained LLMs"]}]',
    learning_outcomes_json = '["Implement search and optimization algorithms for combinatorial problem solving.","Train, evaluate, and tune classical machine learning classifiers using scikit-learn.","Build and train deep neural networks with PyTorch for computer vision and NLP tasks."]',
    recommendedTextbooks_json = '["Artificial Intelligence: A Modern Approach (4th Ed.) — Stuart Russell & Peter Norvig","Pattern Recognition and Machine Learning — Christopher M. Bishop","Deep Learning — Ian Goodfellow, Yoshua Bengio & Aaron Courville"]',
    prerequisites_json = '["CSC 124 Linear Algebra for CS","CSC 125 Probability & Statistics"]'
WHERE code = 'CSC 323';

-- Update CSC 327
UPDATE units SET
    lecturer_office = 'Chiromo Science Complex, Rm 204',
    venue_name = 'Chiromo Rm 204',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Thu 10:00 - 12:00',
    description = 'Covers lexical analysis (regular expressions, DFA/NFA), syntax analysis (LL, LR, LALR parsers), abstract syntax tree (AST) construction, semantic analysis, symbol tables, type checking, intermediate code generation (TAC, SSA form), and target code generation for LLVM.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Lexical Analysis & Finite State Automata","subtopics":["Regular definitions, lexical tokens, and lexeme identification","Thompson construction: Regular expressions to Non-deterministic Finite Automata (NFA)","Subset construction (Powerset algorithm) for NFA-to-DFA minimization"]},{"weekNumber":2,"title":"Context-Free Grammars & Top-Down Parsing","subtopics":["Grammar ambiguities, left recursion elimination, and left factoring","First and Follow set computation algorithms","Recursive descent parser implementation and LL(1) parse table construction"]},{"weekNumber":3,"title":"Bottom-Up LR Parsing Engines","subtopics":["Shift-reduce conflicts and handle pruning mechanics","LR(0) and SLR(1) item sets and parsing tables","Canonical LR(1) and LALR(1) parsing table compression (Yacc/Bison)"]},{"weekNumber":4,"title":"Syntax-Directed Translation & AST Construction","subtopics":["Synthesized vs Inherited attributes in attribute grammars","Abstract Syntax Tree (AST) node representations and visitor pattern","Symbol table scopes, lexical nesting, and declaration resolution"]},{"weekNumber":5,"title":"Semantic Analysis & Type Systems","subtopics":["Static type inference and unification algorithms (Hindley-Milner)","Type checking rules for polymorphic functions and array indices","Error recovery, warning reporting, and diagnostics formatting"]},{"weekNumber":6,"title":"Intermediate Representations & Code Generation","subtopics":["Three-Address Code (TAC) and Static Single Assignment (SSA) form","Control flow graphs (CFG), basic blocks, and dominance frontiers","LLVM IR emission, register allocation (Chaitin graph coloring), and peephole optimizations"]}]',
    learning_outcomes_json = '["Construct lexical analyzers and LR parsers from grammar specifications using tools like Flex/Bison or ANTLR.","Design symbol tables and write type checkers enforcing static language semantics.","Generate intermediate code (LLVM IR) and apply fundamental compiler optimization passes."]',
    recommendedTextbooks_json = '["Compilers: Principles, Techniques, and Tools (Dragon Book, 2nd Ed.) — Aho, Lam, Sethi, Ullman","Engineering a Compiler (3rd Ed.) — Keith Cooper & Linda Torczon","Modern Compiler Implementation in C/Java — Andrew W. Appel"]',
    prerequisites_json = '["CSC 301 Automata Theory","CSC 122 Data Structures & Algorithms"]'
WHERE code = 'CSC 327';

-- Update CSC 331
UPDATE units SET
    lecturer_office = 'Department of Computer Science, Rm 310',
    venue_name = 'Graphics Lab',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Fri 14:00 - 16:00',
    description = 'Covers 2D/3D geometric transformations, viewing pipelines, projection matrices, graphics hardware pipeline, OpenGL/Vulkan rendering, shader programming (GLSL), rasterization, lighting and shading models (Phong, PBR), ray tracing, and audio/video compression algorithms.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Geometric Transformations & Homogeneous Coordinates","subtopics":["2D & 3D affine transformations: Translation, rotation matrices, scaling","Homogeneous coordinates and 4x4 transformation matrix composition","Euler angles, gimbal lock, and quaternion rotation representations"]},{"weekNumber":2,"title":"The Modern Graphics Pipeline & Shader Architecture","subtopics":["Vertex Buffer Objects (VBO), Vertex Array Objects (VAO), and draw calls","Programmable pipeline stages: Vertex, Geometry, and Fragment shaders","GLSL uniform variables, attribute vectors, and interpolation"]},{"weekNumber":3,"title":"Camera Models & Projection Mathematics","subtopics":["View transformation matrix (LookAt implementation)","Orthographic vs Perspective projection frustums and clipping planes","Normalized Device Coordinates (NDC) to viewport mapping"]},{"weekNumber":4,"title":"Rasterization, Clipping & Depth Buffering","subtopics":["Bresenham line drawing and midpoint circle algorithms","Sutherland-Hodgman polygon clipping against frustum planes","Early-Z testing, depth buffer precision, and shadow mapping"]},{"weekNumber":5,"title":"Illumination Models & Shading Techniques","subtopics":["Ambient, Diffuse (Lambert), and Specular (Phong/Blinn-Phong) reflections","Flat shading vs Gouraud shading vs Phong fragment interpolation","Physically Based Rendering (PBR): Cook-Torrance BRDF model"]},{"weekNumber":6,"title":"Ray Tracing & Multimedia Compression","subtopics":["Ray-sphere and ray-triangle (Moller-Trumbore) intersection tests","Recursive reflection, refraction (Snell Law), and BVH acceleration structures","Discrete Cosine Transform (DCT) in JPEG/MPEG and audio psychoacoustics"]}]',
    learning_outcomes_json = '["Write GPU shader programs in GLSL for real-time 3D rendering in OpenGL/WebGL.","Derive and apply matrix transformations for complex 3D camera systems and skeletal animations.","Implement ray tracers calculating realistic lighting, reflections, refractions, and soft shadows."]',
    recommendedTextbooks_json = '["Fundamentals of Computer Graphics (5th Ed.) — Steve Marschner & Peter Shirley","Real-Time Rendering (4th Ed.) — Tomas Akenine-Möller et al.","LearnOpenGL: Learn Modern OpenGL — Joey de Vries"]',
    prerequisites_json = '["CSC 124 Linear Algebra for CS","CSC 112 Structured Programming"]'
WHERE code = 'CSC 331';

-- Update CSC 341
UPDATE units SET
    lecturer_office = 'Chiromo Science Complex, Rm 218',
    venue_name = 'Chiromo Lab 01',
    campus = 'Chiromo',
    is_core = TRUE,
    schedule_time = 'Tue 14:00 - 16:00',
    description = 'Covers software engineering lifecycle models (Agile, Scrum, Kanban), requirements engineering, domain-driven design (DDD), UML modeling, architectural styles (hexagonal, event-driven), clean code principles, test-driven development (TDD), CI/CD pipelines, and software project management.',
    syllabus_topics_json = '[{"weekNumber":1,"title":"Software Lifecycles & Agile Frameworks","subtopics":["Waterfall vs Scrum vs Kanban methodology trade-offs","Sprint planning, backlog grooming, velocity tracking, and burndown charts","User stories, acceptance criteria, and INVEST guidelines"]},{"weekNumber":2,"title":"Requirements Engineering & Domain-Driven Design","subtopics":["Functional vs Non-functional requirements and SMART metrics","DDD Strategic Design: Bounded Contexts, Ubiquitous Language, Context Maps","DDD Tactical Patterns: Entities, Value Objects, Aggregates, Repositories"]},{"weekNumber":3,"title":"Architectural Patterns & Clean Architecture","subtopics":["Monolith vs Microservices vs Serverless architectural trade-offs","Hexagonal Architecture (Ports and Adapters) and Onion Architecture","Event-Driven Architecture: Pub/Sub, Event Sourcing, and CQRS"]},{"weekNumber":4,"title":"Object-Oriented Design Principles (SOLID)","subtopics":["Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion","Gang of Four Design Patterns: Factory, Strategy, Observer, Decorator","Refactoring code smells, technical debt management"]},{"weekNumber":5,"title":"Testing Strategies & Test-Driven Development (TDD)","subtopics":["Testing pyramid: Unit, integration, end-to-end, and contract tests","TDD Red-Green-Refactor cycle and mutation testing","Mocking, stubbing, and test doubles with MockK/Mockito"]},{"weekNumber":6,"title":"DevOps, CI/CD & Production Observability","subtopics":["Automated CI/CD pipelines: GitHub Actions, automated test suites, artifact promotion","Blue-Green deployments, canary releases, and feature flags","Observability pillars: Structured logging, distributed tracing, metrics, SLIs/SLOs"]}]',
    learning_outcomes_json = '["Analyze system requirements and model robust domain architectures using UML and DDD.","Apply SOLID design principles and design patterns to eliminate technical debt.","Configure automated CI/CD pipelines with comprehensive unit, integration, and security testing."]',
    recommendedTextbooks_json = '["Clean Architecture: A Craftsman Guide to Software Structure — Robert C. Martin","Domain-Driven Design: Tackling Complexity in the Heart of Software — Eric Evans","Accelerate: Building and Scaling High Performing Technology Organizations — Nicole Forsgren et al."]',
    prerequisites_json = '["CSC 213 Object Oriented Analysis & Design","CSC 121 Object Oriented Programming"]'
WHERE code = 'CSC 341';

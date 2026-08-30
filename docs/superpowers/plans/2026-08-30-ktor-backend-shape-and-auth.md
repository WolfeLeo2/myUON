# Ktor Backend Shape & Dual-Mode Auth Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stand up the `:server` Ktor module for myUON with the package layering, `students` table, and dual-mode (AD email / SMIS regNo) login the spec calls for — as working, tested code, wired to real Neon Postgres + Neon Auth via env vars.

**Architecture:** A new single Gradle module `:server` (JVM, Ktor + Netty), internally layered into `domain/` (plain Kotlin models, repo/client interfaces, business logic), `db/` (Exposed, Postgres/Neon), `infra/` (outbound HTTP to Neon Auth), and `routes/` (Ktor routing). `domain/` never imports a Ktor-server or Exposed type — that's the boundary a future KMP `:shared` extraction would use.

**Tech Stack:** Kotlin 2.4.0 (matches root project), Ktor 3.0.3 (server: Netty, ContentNegotiation; client: CIO), kotlinx.serialization 1.9.0 (matches root), Exposed 0.56.0 + PostgreSQL JDBC driver 42.7.4, H2 2.3.232 (test-only, Postgres-compat mode) , kotlin.test + kotlinx-coroutines-test 1.9.0, ktor-client-mock (test-only).

**Spec:** `docs/superpowers/specs/2026-08-30-ktor-backend-shape-and-auth-design.md`

## Global Constraints

- Single Gradle module (`:server`) — no `:shared` KMP module. (Spec Decision 1.)
- `domain/` package: zero imports of `io.ktor.server.*` or `org.jetbrains.exposed.*`. Only plain Kotlin + `kotlinx.serialization`. (Spec Decision 1, "Discipline this design depends on".)
- `routes/` calls into `domain/` only, never `db/` or `infra/` directly. (Spec Decision 1.)
- Neon Auth is a hosted REST API, called over HTTP from `infra/` — no Kotlin/JVM SDK exists for it. (Spec Decision 2 preamble.)
- Exactly one password check ever happens, inside Neon Auth. Nothing in `:server` stores or verifies a second password for SMIS-mode login. (Spec Decision 2.)
- `regNo` lives only in our own `students` table (unique-indexed), never pushed to Neon Auth as custom user data. (Spec Decision 2.)

---

## File Structure

```
settings.gradle.kts                                   # add include(":server")
server/
  build.gradle.kts
  src/main/kotlin/com/wolfeleo2/myuon/server/
    Application.kt                                     # main(), Application.module(), requireEnv()
    domain/
      StudentProfile.kt                                 # domain model (Task 2)
      StudentRepository.kt                               # repo interface (Task 3)
      NeonAuthClient.kt                                  # client interface + NeonAuthResult (Task 4)
      AuthService.kt                                     # signup/login business logic (Task 5)
    db/
      DatabaseFactory.kt                                 # Exposed Database.connect + schema create (Task 3)
      StudentsTable.kt                                    # Exposed table (Task 3)
      ExposedStudentRepository.kt                         # StudentRepository impl (Task 3)
    infra/
      NeonAuthHttpClient.kt                               # NeonAuthClient impl over HTTP (Task 4)
    routes/
      AuthRoutes.kt                                       # /auth/signup, /auth/login (Task 6)
  src/main/resources/
    logback.xml                                           # (Task 1)
  src/test/kotlin/com/wolfeleo2/myuon/server/
    ApplicationTest.kt                                    # health check (Task 1)
    ApplicationConfigTest.kt                              # requireEnv (Task 7)
    domain/
      StudentProfileTest.kt                               # (Task 2)
      AuthServiceTest.kt                                  # (Task 5)
      FakeNeonAuthClient.kt                               # test double (Task 5)
    db/
      ExposedStudentRepositoryTest.kt                     # (Task 3)
    infra/
      NeonAuthHttpClientTest.kt                           # (Task 4)
    routes/
      AuthRoutesTest.kt                                   # (Task 6)
  .env.example                                             # (Task 7)
  README.md                                                # (Task 7)
```

---

### Task 1: Bootstrap `:server` module + health check

**Files:**
- Modify: `settings.gradle.kts`
- Create: `server/build.gradle.kts`
- Create: `server/src/main/kotlin/com/wolfeleo2/myuon/server/Application.kt`
- Create: `server/src/main/resources/logback.xml`
- Test: `server/src/test/kotlin/com/wolfeleo2/myuon/server/ApplicationTest.kt`

**Interfaces:**
- Produces: `fun main()`, `fun Application.module()` — later tasks extend `module()`, don't replace it.

- [ ] **Step 1: Add the module to the Gradle build**

Modify `settings.gradle.kts`:

```kotlin
rootProject.name = "myUON"
include(":app")
include(":server")
```

- [ ] **Step 2: Create `server/build.gradle.kts`**

```kotlin
plugins {
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
    application
}

group = "com.wolfeleo2.myuon.server"
version = "0.1.0"

repositories {
    mavenCentral()
}

val ktorVersion = "3.0.3"
val exposedVersion = "0.56.0"

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.postgresql:postgresql:42.7.4")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("ch.qos.logback:logback-classic:1.5.12")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("com.h2database:h2:2.3.232")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation(kotlin("test-junit5"))
}

application {
    mainClass.set("com.wolfeleo2.myuon.server.ApplicationKt")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
```

*(Version note: pin these, but confirm no newer patch releases exist for `io.ktor`, `org.jetbrains.exposed`, and `org.postgresql:postgresql` on Maven Central before running — hosted registries move faster than this plan can track.)*

- [ ] **Step 3: Write the failing health-check test**

```kotlin
// server/src/test/kotlin/com/wolfeleo2/myuon/server/ApplicationTest.kt
package com.wolfeleo2.myuon.server

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun `health check responds OK`() = testApplication {
        application {
            routing { get("/health") { call.respondText("OK") } }
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
```

This test installs its own minimal routing rather than calling `Application.module()` directly, so later tasks are free to make `module()` require real env vars without breaking this test.

- [ ] **Step 4: Run the test, verify it fails**

Run: `./gradlew :server:test --tests "com.wolfeleo2.myuon.server.ApplicationTest"`
Expected: FAIL — module doesn't compile yet (`Application.kt` doesn't exist).

- [ ] **Step 5: Create `Application.kt` and `logback.xml`**

```kotlin
// server/src/main/kotlin/com/wolfeleo2/myuon/server/Application.kt
package com.wolfeleo2.myuon.server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) { json() }
    routing {
        get("/health") { call.respondText("OK") }
    }
}

internal fun requireEnv(name: String): String =
    System.getenv(name) ?: error("Missing required environment variable: $name")
```

```xml
<!-- server/src/main/resources/logback.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

`requireEnv` is added now (unused) because Task 7 needs it and Task 7's test lives in this same file's package — defining it here keeps `Application.kt` as the one place env-reading happens.

- [ ] **Step 6: Run the test, verify it passes**

Run: `./gradlew :server:test --tests "com.wolfeleo2.myuon.server.ApplicationTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add settings.gradle.kts server/
git commit -m "feat(server): bootstrap :server Ktor module with health check"
```

---

### Task 2: `domain/StudentProfile` model

**Files:**
- Create: `server/src/main/kotlin/com/wolfeleo2/myuon/server/domain/StudentProfile.kt`
- Test: `server/src/test/kotlin/com/wolfeleo2/myuon/server/domain/StudentProfileTest.kt`

**Interfaces:**
- Produces: `data class StudentProfile(userId, regNo, fullName, studentEmail, faculty, department, program, yearOfStudy, semester, campus, nationalId, mobileNumber, photoUrl, isFeeCleared)` — Tasks 3, 5, 6 all construct/consume this exact shape.

- [ ] **Step 1: Write the failing serialization round-trip test**

```kotlin
// server/src/test/kotlin/com/wolfeleo2/myuon/server/domain/StudentProfileTest.kt
package com.wolfeleo2.myuon.server.domain

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class StudentProfileTest {
    private val profile = StudentProfile(
        userId = "user-1",
        regNo = "P15/12345/2022",
        fullName = "Jane Student",
        studentEmail = "jane.student@students.uonbi.ac.ke",
        faculty = "Science",
        department = "Computing",
        program = "BSc Computer Science",
        yearOfStudy = 3,
        semester = 1,
        campus = "Main",
        nationalId = "12345678",
        mobileNumber = "0700000000",
        photoUrl = null,
        isFeeCleared = true,
    )

    @Test
    fun `round-trips through JSON unchanged`() {
        val json = Json.encodeToString(profile)
        val decoded = Json.decodeFromString<StudentProfile>(json)
        assertEquals(profile, decoded)
    }
}
```

- [ ] **Step 2: Run test, verify it fails**

Run: `./gradlew :server:test --tests "com.wolfeleo2.myuon.server.domain.StudentProfileTest"`
Expected: FAIL — `StudentProfile` doesn't exist.

- [ ] **Step 3: Create the model**

```kotlin
// server/src/main/kotlin/com/wolfeleo2/myuon/server/domain/StudentProfile.kt
package com.wolfeleo2.myuon.server.domain

import kotlinx.serialization.Serializable

@Serializable
data class StudentProfile(
    val userId: String,
    val regNo: String,
    val fullName: String,
    val studentEmail: String,
    val faculty: String,
    val department: String,
    val program: String,
    val yearOfStudy: Int,
    val semester: Int,
    val campus: String,
    val nationalId: String,
    val mobileNumber: String,
    val photoUrl: String? = null,
    val isFeeCleared: Boolean = true,
)
```

`userId` is the Neon Auth user id (FK target); this mirrors the app's existing `data/model/StudentProfile.kt` minus client-only fields (`isBiometricEnabled` is an Android device setting, not server data).

- [ ] **Step 4: Run test, verify it passes**

Run: `./gradlew :server:test --tests "com.wolfeleo2.myuon.server.domain.StudentProfileTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add server/src/main/kotlin/com/wolfeleo2/myuon/server/domain/StudentProfile.kt server/src/test/kotlin/com/wolfeleo2/myuon/server/domain/StudentProfileTest.kt
git commit -m "feat(server): add StudentProfile domain model"
```

---

### Task 3: `students` table — repo interface + Exposed implementation

**Files:**
- Create: `server/src/main/kotlin/com/wolfeleo2/myuon/server/domain/StudentRepository.kt`
- Create: `server/src/main/kotlin/com/wolfeleo2/myuon/server/db/StudentsTable.kt`
- Create: `server/src/main/kotlin/com/wolfeleo2/myuon/server/db/DatabaseFactory.kt`
- Create: `server/src/main/kotlin/com/wolfeleo2/myuon/server/db/ExposedStudentRepository.kt`
- Test: `server/src/test/kotlin/com/wolfeleo2/myuon/server/db/ExposedStudentRepositoryTest.kt`

**Interfaces:**
- Consumes: `StudentProfile` (Task 2).
- Produces: `interface StudentRepository { suspend fun create(profile: StudentProfile): StudentProfile; suspend fun findByUserId(userId: String): StudentProfile?; suspend fun findByRegNo(regNo: String): StudentProfile? }`, `object DatabaseFactory { fun connect(url: String, driver: String, user: String = "", password: String = ""): Database }`, `class ExposedStudentRepository(database: Database) : StudentRepository`. Task 5 consumes `StudentRepository` (interface only).

- [ ] **Step 1: Write the failing repository test**

```kotlin
// server/src/test/kotlin/com/wolfeleo2/myuon/server/db/ExposedStudentRepositoryTest.kt
package com.wolfeleo2.myuon.server.db

import com.wolfeleo2.myuon.server.domain.StudentProfile
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.exceptions.ExposedSQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ExposedStudentRepositoryTest {

    private fun freshRepository(): ExposedStudentRepository {
        val db = DatabaseFactory.connect(
            url = "jdbc:h2:mem:test-${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
        )
        return ExposedStudentRepository(db)
    }

    private val sample = StudentProfile(
        userId = "user-1",
        regNo = "P15/12345/2022",
        fullName = "Jane Student",
        studentEmail = "jane.student@students.uonbi.ac.ke",
        faculty = "Science",
        department = "Computing",
        program = "BSc Computer Science",
        yearOfStudy = 3,
        semester = 1,
        campus = "Main",
        nationalId = "12345678",
        mobileNumber = "0700000000",
        photoUrl = null,
        isFeeCleared = true,
    )

    @Test
    fun `create then findByUserId returns the same profile`() = runTest {
        val repo = freshRepository()
        repo.create(sample)

        assertEquals(sample, repo.findByUserId("user-1"))
    }

    @Test
    fun `findByRegNo resolves the same row`() = runTest {
        val repo = freshRepository()
        repo.create(sample)

        assertEquals(sample, repo.findByRegNo("P15/12345/2022"))
    }

    @Test
    fun `findByUserId returns null when the user has no student row`() = runTest {
        val repo = freshRepository()

        assertNull(repo.findByUserId("missing"))
    }

    @Test
    fun `regNo must be unique`() = runTest {
        val repo = freshRepository()
        repo.create(sample)

        assertFailsWith<ExposedSQLException> {
            repo.create(sample.copy(userId = "user-2"))
        }
    }
}
```

- [ ] **Step 2: Run test, verify it fails**

Run: `./gradlew :server:test --tests "com.wolfeleo2.myuon.server.db.ExposedStudentRepositoryTest"`
Expected: FAIL — none of `DatabaseFactory`/`ExposedStudentRepository`/`StudentsTable` exist.

- [ ] **Step 3: Create the repo interface**

```kotlin
// server/src/main/kotlin/com/wolfeleo2/myuon/server/domain/StudentRepository.kt
package com.wolfeleo2.myuon.server.domain

interface StudentRepository {
    suspend fun create(profile: StudentProfile): StudentProfile
    suspend fun findByUserId(userId: String): StudentProfile?
    suspend fun findByRegNo(regNo: String): StudentProfile?
}
```

- [ ] **Step 4: Create the Exposed table**

```kotlin
// server/src/main/kotlin/com/wolfeleo2/myuon/server/db/StudentsTable.kt
package com.wolfeleo2.myuon.server.db

import org.jetbrains.exposed.sql.Table

object StudentsTable : Table("students") {
    val userId = varchar("user_id", 64)
    val regNo = varchar("reg_no", 32).uniqueIndex()
    val fullName = varchar("full_name", 128)
    val studentEmail = varchar("student_email", 128)
    val faculty = varchar("faculty", 128)
    val department = varchar("department", 128)
    val program = varchar("program", 128)
    val yearOfStudy = integer("year_of_study")
    val semester = integer("semester")
    val campus = varchar("campus", 64)
    val nationalId = varchar("national_id", 32)
    val mobileNumber = varchar("mobile_number", 32)
    val photoUrl = varchar("photo_url", 512).nullable()
    val isFeeCleared = bool("is_fee_cleared").default(true)

    override val primaryKey = PrimaryKey(userId)
}
```

- [ ] **Step 5: Create `DatabaseFactory`**

```kotlin
// server/src/main/kotlin/com/wolfeleo2/myuon/server/db/DatabaseFactory.kt
package com.wolfeleo2.myuon.server.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun connect(url: String, driver: String, user: String = "", password: String = ""): Database {
        val database = Database.connect(url = url, driver = driver, user = user, password = password)
        transaction(database) {
            SchemaUtils.create(StudentsTable)
        }
        return database
    }
}
```

- [ ] **Step 6: Create `ExposedStudentRepository`**

```kotlin
// server/src/main/kotlin/com/wolfeleo2/myuon/server/db/ExposedStudentRepository.kt
package com.wolfeleo2.myuon.server.db

import com.wolfeleo2.myuon.server.domain.StudentProfile
import com.wolfeleo2.myuon.server.domain.StudentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedStudentRepository(private val database: Database) : StudentRepository {

    override suspend fun create(profile: StudentProfile): StudentProfile = dbQuery {
        StudentsTable.insert {
            it[userId] = profile.userId
            it[regNo] = profile.regNo
            it[fullName] = profile.fullName
            it[studentEmail] = profile.studentEmail
            it[faculty] = profile.faculty
            it[department] = profile.department
            it[program] = profile.program
            it[yearOfStudy] = profile.yearOfStudy
            it[semester] = profile.semester
            it[campus] = profile.campus
            it[nationalId] = profile.nationalId
            it[mobileNumber] = profile.mobileNumber
            it[photoUrl] = profile.photoUrl
            it[isFeeCleared] = profile.isFeeCleared
        }
        profile
    }

    override suspend fun findByUserId(userId: String): StudentProfile? = dbQuery {
        StudentsTable.selectAll().where { StudentsTable.userId eq userId }
            .map { it.toStudentProfile() }
            .singleOrNull()
    }

    override suspend fun findByRegNo(regNo: String): StudentProfile? = dbQuery {
        StudentsTable.selectAll().where { StudentsTable.regNo eq regNo }
            .map { it.toStudentProfile() }
            .singleOrNull()
    }

    private fun ResultRow.toStudentProfile() = StudentProfile(
        userId = this[StudentsTable.userId],
        regNo = this[StudentsTable.regNo],
        fullName = this[StudentsTable.fullName],
        studentEmail = this[StudentsTable.studentEmail],
        faculty = this[StudentsTable.faculty],
        department = this[StudentsTable.department],
        program = this[StudentsTable.program],
        yearOfStudy = this[StudentsTable.yearOfStudy],
        semester = this[StudentsTable.semester],
        campus = this[StudentsTable.campus],
        nationalId = this[StudentsTable.nationalId],
        mobileNumber = this[StudentsTable.mobileNumber],
        photoUrl = this[StudentsTable.photoUrl],
        isFeeCleared = this[StudentsTable.isFeeCleared],
    )

    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction(database) { block() } }
}
```

*(`Database.connect` in Exposed returns a handle you pass explicitly to `transaction(database) { ... }` — this repo takes its `Database` in the constructor rather than relying on Exposed's global "last connected" default, so tests can spin up an isolated H2 instance per test without cross-test interference.)*

- [ ] **Step 7: Run test, verify it passes**

Run: `./gradlew :server:test --tests "com.wolfeleo2.myuon.server.db.ExposedStudentRepositoryTest"`
Expected: PASS (4 tests)

- [ ] **Step 8: Commit**

```bash
git add server/src/main/kotlin/com/wolfeleo2/myuon/server/domain/StudentRepository.kt server/src/main/kotlin/com/wolfeleo2/myuon/server/db/ server/src/test/kotlin/com/wolfeleo2/myuon/server/db/
git commit -m "feat(server): add students table and Exposed repository"
```

---

### Task 4: `NeonAuthClient` interface + real HTTP implementation

**Files:**
- Create: `server/src/main/kotlin/com/wolfeleo2/myuon/server/domain/NeonAuthClient.kt`
- Create: `server/src/main/kotlin/com/wolfeleo2/myuon/server/infra/NeonAuthHttpClient.kt`
- Test: `server/src/test/kotlin/com/wolfeleo2/myuon/server/infra/NeonAuthHttpClientTest.kt`

**Interfaces:**
- Produces: `sealed class NeonAuthResult { data class Success(val userId: String, val sessionToken: String); data class Failure(val message: String) }`, `interface NeonAuthClient { suspend fun signUp(email: String, password: String): NeonAuthResult; suspend fun signIn(email: String, password: String): NeonAuthResult }`, `class NeonAuthHttpClient(httpClient: HttpClient, baseUrl: String, projectId: String, serverSecretKey: String, signUpPath: String = "/sign-up/email", signInPath: String = "/sign-in/email") : NeonAuthClient`. Task 5 consumes `NeonAuthClient`/`NeonAuthResult` (interface only, not the HTTP impl).

We don't have a live Neon project to test against yet, so this task verifies **request shape** (method, URL, headers, body) and **response parsing** against a mocked HTTP engine — not a real network call.

- [ ] **Step 1: Write the failing test**

```kotlin
// server/src/test/kotlin/com/wolfeleo2/myuon/server/infra/NeonAuthHttpClientTest.kt
package com.wolfeleo2.myuon.server.infra

import com.wolfeleo2.myuon.server.domain.NeonAuthResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NeonAuthHttpClientTest {

    @Test
    fun `signIn sends project and secret headers and parses a successful response`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("https://auth.example.neon.tech/sign-in/email", request.url.toString())
            assertEquals("proj-123", request.headers["x-stack-project-id"])
            assertEquals("secret-abc", request.headers["x-stack-secret-server-key"])
            assertTrue(request.body.toByteArray().decodeToString().contains("\"email\":\"a@b.com\""))

            respond(
                content = """{"userId":"user-1","sessionToken":"token-1"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = NeonAuthHttpClient(
            httpClient = HttpClient(engine) { install(ContentNegotiation) { json() } },
            baseUrl = "https://auth.example.neon.tech",
            projectId = "proj-123",
            serverSecretKey = "secret-abc",
        )

        val result = client.signIn("a@b.com", "password123")

        val success = assertIs<NeonAuthResult.Success>(result)
        assertEquals("user-1", success.userId)
        assertEquals("token-1", success.sessionToken)
    }

    @Test
    fun `signIn returns Failure with the response body on a non-2xx status`() = runTest {
        val engine = MockEngine {
            respond(content = """{"message":"invalid credentials"}""", status = HttpStatusCode.Unauthorized)
        }
        val client = NeonAuthHttpClient(
            httpClient = HttpClient(engine) { install(ContentNegotiation) { json() } },
            baseUrl = "https://auth.example.neon.tech",
            projectId = "proj-123",
            serverSecretKey = "secret-abc",
        )

        val result = client.signIn("a@b.com", "wrong-password")

        val failure = assertIs<NeonAuthResult.Failure>(result)
        assertTrue(failure.message.contains("invalid credentials"))
    }
}
```

- [ ] **Step 2: Run test, verify it fails**

Run: `./gradlew :server:test --tests "com.wolfeleo2.myuon.server.infra.NeonAuthHttpClientTest"`
Expected: FAIL — `NeonAuthClient`/`NeonAuthHttpClient` don't exist.

- [ ] **Step 3: Create the domain interface**

```kotlin
// server/src/main/kotlin/com/wolfeleo2/myuon/server/domain/NeonAuthClient.kt
package com.wolfeleo2.myuon.server.domain

sealed class NeonAuthResult {
    data class Success(val userId: String, val sessionToken: String) : NeonAuthResult()
    data class Failure(val message: String) : NeonAuthResult()
}

interface NeonAuthClient {
    suspend fun signUp(email: String, password: String): NeonAuthResult
    suspend fun signIn(email: String, password: String): NeonAuthResult
}
```

- [ ] **Step 4: Create the HTTP implementation**

```kotlin
// server/src/main/kotlin/com/wolfeleo2/myuon/server/infra/NeonAuthHttpClient.kt
package com.wolfeleo2.myuon.server.infra

import com.wolfeleo2.myuon.server.domain.NeonAuthClient
import com.wolfeleo2.myuon.server.domain.NeonAuthResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

/**
 * Calls Neon Auth's hosted REST API. There is no Kotlin/JVM SDK for Neon Auth
 * (verified 2026-08-30) — every call here is a plain HTTP request, matching
 * Better Auth's documented default paths (`/sign-up/email`, `/sign-in/email`).
 *
 * ponytail: [signUpPath]/[signInPath] default to Better Auth's stock paths,
 * unverified against a live Neon project (none exists yet). Once a real Neon
 * Auth project is provisioned, confirm these against that project's own
 * `/reference` page and pass overrides here if they differ — no code change
 * needed elsewhere, this class is the only caller.
 */
class NeonAuthHttpClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val projectId: String,
    private val serverSecretKey: String,
    private val signUpPath: String = "/sign-up/email",
    private val signInPath: String = "/sign-in/email",
) : NeonAuthClient {

    @Serializable
    private data class CredentialsRequest(val email: String, val password: String)

    @Serializable
    private data class AuthResponse(val userId: String, val sessionToken: String)

    override suspend fun signUp(email: String, password: String): NeonAuthResult =
        request(signUpPath, email, password)

    override suspend fun signIn(email: String, password: String): NeonAuthResult =
        request(signInPath, email, password)

    private suspend fun request(path: String, email: String, password: String): NeonAuthResult {
        val response = httpClient.post("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            header("x-stack-project-id", projectId)
            header("x-stack-secret-server-key", serverSecretKey)
            setBody(CredentialsRequest(email, password))
        }
        return if (response.status.isSuccess()) {
            val parsed = response.body<AuthResponse>()
            NeonAuthResult.Success(parsed.userId, parsed.sessionToken)
        } else {
            NeonAuthResult.Failure(response.bodyAsText())
        }
    }
}
```

- [ ] **Step 5: Run test, verify it passes**

Run: `./gradlew :server:test --tests "com.wolfeleo2.myuon.server.infra.NeonAuthHttpClientTest"`
Expected: PASS (2 tests)

- [ ] **Step 6: Commit**

```bash
git add server/src/main/kotlin/com/wolfeleo2/myuon/server/domain/NeonAuthClient.kt server/src/main/kotlin/com/wolfeleo2/myuon/server/infra/ server/src/test/kotlin/com/wolfeleo2/myuon/server/infra/
git commit -m "feat(server): add NeonAuthClient interface and HTTP implementation"
```

---

### Task 5: `AuthService` — signup and dual-mode login

**Files:**
- Create: `server/src/main/kotlin/com/wolfeleo2/myuon/server/domain/AuthService.kt`
- Create: `server/src/test/kotlin/com/wolfeleo2/myuon/server/domain/FakeNeonAuthClient.kt`
- Test: `server/src/test/kotlin/com/wolfeleo2/myuon/server/domain/AuthServiceTest.kt`

**Interfaces:**
- Consumes: `StudentRepository` (Task 3), `NeonAuthClient`/`NeonAuthResult` (Task 4), `StudentProfile` (Task 2).
- Produces: `sealed class LoginMode { data object Ad; data object Smis }`, `sealed class AuthOutcome { data class Success(val userId: String, val sessionToken: String); data class Failure(val reason: String) }`, `class AuthService(neonAuthClient: NeonAuthClient, studentRepository: StudentRepository) { suspend fun signUp(email: String, password: String, profile: StudentProfile): AuthOutcome; suspend fun login(mode: LoginMode, identifier: String, password: String): AuthOutcome }`. Task 6 consumes all of the above.

- [ ] **Step 1: Write the failing tests**

```kotlin
// server/src/test/kotlin/com/wolfeleo2/myuon/server/domain/FakeNeonAuthClient.kt
package com.wolfeleo2.myuon.server.domain

class FakeNeonAuthClient(
    private val signUpResult: (String, String) -> NeonAuthResult = { _, _ -> NeonAuthResult.Failure("not stubbed") },
    private val signInResult: (String, String) -> NeonAuthResult = { _, _ -> NeonAuthResult.Failure("not stubbed") },
) : NeonAuthClient {
    val signInCalls = mutableListOf<Pair<String, String>>()

    override suspend fun signUp(email: String, password: String): NeonAuthResult = signUpResult(email, password)

    override suspend fun signIn(email: String, password: String): NeonAuthResult {
        signInCalls += email to password
        return signInResult(email, password)
    }
}
```

```kotlin
// server/src/test/kotlin/com/wolfeleo2/myuon/server/domain/AuthServiceTest.kt
package com.wolfeleo2.myuon.server.domain

import com.wolfeleo2.myuon.server.db.DatabaseFactory
import com.wolfeleo2.myuon.server.db.ExposedStudentRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthServiceTest {

    private fun freshRepository(): StudentRepository {
        val db = DatabaseFactory.connect(
            url = "jdbc:h2:mem:auth-test-${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
        )
        return ExposedStudentRepository(db)
    }

    private val newStudent = StudentProfile(
        userId = "", // AuthService fills this in with the id Neon Auth issues
        regNo = "P15/12345/2022",
        fullName = "Jane Student",
        studentEmail = "jane.student@students.uonbi.ac.ke",
        faculty = "Science",
        department = "Computing",
        program = "BSc Computer Science",
        yearOfStudy = 3,
        semester = 1,
        campus = "Main",
        nationalId = "12345678",
        mobileNumber = "0700000000",
        photoUrl = null,
        isFeeCleared = true,
    )

    @Test
    fun `signUp links the Neon Auth user id to a new students row`() = runTest {
        val repo = freshRepository()
        val neonAuth = FakeNeonAuthClient(
            signUpResult = { _, _ -> NeonAuthResult.Success(userId = "user-1", sessionToken = "token-1") },
        )
        val service = AuthService(neonAuth, repo)

        val outcome = service.signUp("jane.student@students.uonbi.ac.ke", "password123", newStudent)

        val success = assertIs<AuthOutcome.Success>(outcome)
        assertEquals("user-1", success.userId)
        assertEquals("jane.student@students.uonbi.ac.ke", repo.findByUserId("user-1")?.studentEmail)
    }

    @Test
    fun `AD-mode login passes the identifier straight through as the email`() = runTest {
        val repo = freshRepository()
        val neonAuth = FakeNeonAuthClient(
            signInResult = { _, _ -> NeonAuthResult.Success(userId = "user-1", sessionToken = "token-1") },
        )
        val service = AuthService(neonAuth, repo)

        val outcome = service.login(LoginMode.Ad, "jane.student@students.uonbi.ac.ke", "password123")

        assertIs<AuthOutcome.Success>(outcome)
        assertEquals(listOf("jane.student@students.uonbi.ac.ke" to "password123"), neonAuth.signInCalls)
    }

    @Test
    fun `SMIS-mode login resolves regNo to email before signing in`() = runTest {
        val repo = freshRepository()
        repo.create(newStudent.copy(userId = "user-1"))
        val neonAuth = FakeNeonAuthClient(
            signInResult = { _, _ -> NeonAuthResult.Success(userId = "user-1", sessionToken = "token-1") },
        )
        val service = AuthService(neonAuth, repo)

        val outcome = service.login(LoginMode.Smis, "P15/12345/2022", "password123")

        assertIs<AuthOutcome.Success>(outcome)
        assertEquals(listOf("jane.student@students.uonbi.ac.ke" to "password123"), neonAuth.signInCalls)
    }

    @Test
    fun `SMIS-mode login fails fast for an unknown regNo without calling Neon Auth`() = runTest {
        val repo = freshRepository()
        val neonAuth = FakeNeonAuthClient()
        val service = AuthService(neonAuth, repo)

        val outcome = service.login(LoginMode.Smis, "UNKNOWN/000", "password123")

        val failure = assertIs<AuthOutcome.Failure>(outcome)
        assertTrue(failure.reason.contains("UNKNOWN/000"))
        assertTrue(neonAuth.signInCalls.isEmpty())
    }
}
```

- [ ] **Step 2: Run tests, verify they fail**

Run: `./gradlew :server:test --tests "com.wolfeleo2.myuon.server.domain.AuthServiceTest"`
Expected: FAIL — `AuthService`, `LoginMode`, `AuthOutcome` don't exist.

- [ ] **Step 3: Implement `AuthService`**

```kotlin
// server/src/main/kotlin/com/wolfeleo2/myuon/server/domain/AuthService.kt
package com.wolfeleo2.myuon.server.domain

sealed class LoginMode {
    data object Ad : LoginMode()
    data object Smis : LoginMode()
}

sealed class AuthOutcome {
    data class Success(val userId: String, val sessionToken: String) : AuthOutcome()
    data class Failure(val reason: String) : AuthOutcome()
}

class AuthService(
    private val neonAuthClient: NeonAuthClient,
    private val studentRepository: StudentRepository,
) {
    suspend fun signUp(email: String, password: String, profile: StudentProfile): AuthOutcome =
        when (val result = neonAuthClient.signUp(email, password)) {
            is NeonAuthResult.Success -> {
                studentRepository.create(profile.copy(userId = result.userId, studentEmail = email))
                AuthOutcome.Success(result.userId, result.sessionToken)
            }
            is NeonAuthResult.Failure -> AuthOutcome.Failure(result.message)
        }

    suspend fun login(mode: LoginMode, identifier: String, password: String): AuthOutcome {
        val email = when (mode) {
            LoginMode.Ad -> identifier
            LoginMode.Smis -> {
                val student = studentRepository.findByRegNo(identifier)
                    ?: return AuthOutcome.Failure("No account found for regNo $identifier")
                student.studentEmail
            }
        }
        return when (val result = neonAuthClient.signIn(email, password)) {
            is NeonAuthResult.Success -> AuthOutcome.Success(result.userId, result.sessionToken)
            is NeonAuthResult.Failure -> AuthOutcome.Failure(result.message)
        }
    }
}
```

- [ ] **Step 4: Run tests, verify they pass**

Run: `./gradlew :server:test --tests "com.wolfeleo2.myuon.server.domain.AuthServiceTest"`
Expected: PASS (4 tests)

- [ ] **Step 5: Commit**

```bash
git add server/src/main/kotlin/com/wolfeleo2/myuon/server/domain/AuthService.kt server/src/test/kotlin/com/wolfeleo2/myuon/server/domain/
git commit -m "feat(server): add AuthService for signup and dual-mode login"
```

---

### Task 6: `/auth/signup` and `/auth/login` routes

**Files:**
- Create: `server/src/main/kotlin/com/wolfeleo2/myuon/server/routes/AuthRoutes.kt`
- Test: `server/src/test/kotlin/com/wolfeleo2/myuon/server/routes/AuthRoutesTest.kt`

**Interfaces:**
- Consumes: `AuthService`, `LoginMode`, `AuthOutcome` (Task 5), `StudentProfile` (Task 2).
- Produces: `fun Route.authRoutes(authService: AuthService)`. Task 7 mounts this in `Application.module()`.

- [ ] **Step 1: Write the failing route tests**

```kotlin
// server/src/test/kotlin/com/wolfeleo2/myuon/server/routes/AuthRoutesTest.kt
package com.wolfeleo2.myuon.server.routes

import com.wolfeleo2.myuon.server.db.DatabaseFactory
import com.wolfeleo2.myuon.server.db.ExposedStudentRepository
import com.wolfeleo2.myuon.server.domain.AuthService
import com.wolfeleo2.myuon.server.domain.FakeNeonAuthClient
import com.wolfeleo2.myuon.server.domain.NeonAuthResult
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRoutesTest {

    private fun freshAuthService(neonAuth: FakeNeonAuthClient): AuthService {
        val db = DatabaseFactory.connect(
            url = "jdbc:h2:mem:routes-test-${System.nanoTime()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
        )
        return AuthService(neonAuth, ExposedStudentRepository(db))
    }

    @Test
    fun `signup creates an account and returns 201`() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            val authService = freshAuthService(
                FakeNeonAuthClient(signUpResult = { _, _ -> NeonAuthResult.Success("user-1", "token-1") }),
            )
            routing { authRoutes(authService) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.post("/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"email":"jane@students.uonbi.ac.ke","password":"password123","regNo":"P15/12345/2022",
                    |"fullName":"Jane Student","faculty":"Science","department":"Computing",
                    |"program":"BSc Computer Science","yearOfStudy":3,"semester":1,"campus":"Main",
                    |"nationalId":"12345678","mobileNumber":"0700000000"}""".trimMargin(),
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText())
        assertEquals("user-1", body.jsonObject["userId"]?.jsonPrimitive?.content)
    }

    @Test
    fun `login with an unknown mode returns 400`() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            routing { authRoutes(freshAuthService(FakeNeonAuthClient())) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"mode":"BOGUS","identifier":"a@b.com","password":"x"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `login with a wrong password returns 401`() = testApplication {
        application {
            install(ContentNegotiation) { json() }
            val authService = freshAuthService(
                FakeNeonAuthClient(signInResult = { _, _ -> NeonAuthResult.Failure("invalid credentials") }),
            )
            routing { authRoutes(authService) }
        }
        val client = createClient { install(ClientContentNegotiation) { json() } }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"mode":"AD","identifier":"jane@students.uonbi.ac.ke","password":"wrong"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
```

- [ ] **Step 2: Run tests, verify they fail**

Run: `./gradlew :server:test --tests "com.wolfeleo2.myuon.server.routes.AuthRoutesTest"`
Expected: FAIL — `authRoutes` doesn't exist.

- [ ] **Step 3: Implement the routes**

```kotlin
// server/src/main/kotlin/com/wolfeleo2/myuon/server/routes/AuthRoutes.kt
package com.wolfeleo2.myuon.server.routes

import com.wolfeleo2.myuon.server.domain.AuthOutcome
import com.wolfeleo2.myuon.server.domain.AuthService
import com.wolfeleo2.myuon.server.domain.LoginMode
import com.wolfeleo2.myuon.server.domain.StudentProfile
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val regNo: String,
    val fullName: String,
    val faculty: String,
    val department: String,
    val program: String,
    val yearOfStudy: Int,
    val semester: Int,
    val campus: String,
    val nationalId: String,
    val mobileNumber: String,
    val photoUrl: String? = null,
    val isFeeCleared: Boolean = true,
)

@Serializable
data class LoginRequest(val mode: String, val identifier: String, val password: String)

@Serializable
data class AuthResponse(val userId: String, val sessionToken: String)

@Serializable
data class ErrorResponse(val error: String)

fun Route.authRoutes(authService: AuthService) {
    post("/auth/signup") {
        val body = call.receive<SignUpRequest>()
        val profile = StudentProfile(
            userId = "",
            regNo = body.regNo,
            fullName = body.fullName,
            studentEmail = body.email,
            faculty = body.faculty,
            department = body.department,
            program = body.program,
            yearOfStudy = body.yearOfStudy,
            semester = body.semester,
            campus = body.campus,
            nationalId = body.nationalId,
            mobileNumber = body.mobileNumber,
            photoUrl = body.photoUrl,
            isFeeCleared = body.isFeeCleared,
        )
        when (val outcome = authService.signUp(body.email, body.password, profile)) {
            is AuthOutcome.Success -> call.respond(HttpStatusCode.Created, AuthResponse(outcome.userId, outcome.sessionToken))
            is AuthOutcome.Failure -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(outcome.reason))
        }
    }

    post("/auth/login") {
        val body = call.receive<LoginRequest>()
        val mode = when (body.mode.uppercase()) {
            "AD" -> LoginMode.Ad
            "SMIS" -> LoginMode.Smis
            else -> {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("mode must be AD or SMIS"))
                return@post
            }
        }
        when (val outcome = authService.login(mode, body.identifier, body.password)) {
            is AuthOutcome.Success -> call.respond(HttpStatusCode.OK, AuthResponse(outcome.userId, outcome.sessionToken))
            is AuthOutcome.Failure -> call.respond(HttpStatusCode.Unauthorized, ErrorResponse(outcome.reason))
        }
    }
}
```

- [ ] **Step 4: Run tests, verify they pass**

Run: `./gradlew :server:test --tests "com.wolfeleo2.myuon.server.routes.AuthRoutesTest"`
Expected: PASS (3 tests)

- [ ] **Step 5: Commit**

```bash
git add server/src/main/kotlin/com/wolfeleo2/myuon/server/routes/ server/src/test/kotlin/com/wolfeleo2/myuon/server/routes/
git commit -m "feat(server): add /auth/signup and /auth/login routes"
```

---

### Task 7: Wire real config, document env vars

**Files:**
- Modify: `server/src/main/kotlin/com/wolfeleo2/myuon/server/Application.kt`
- Create: `server/.env.example`
- Create: `server/README.md`
- Test: `server/src/test/kotlin/com/wolfeleo2/myuon/server/ApplicationConfigTest.kt`

**Interfaces:**
- Consumes: `DatabaseFactory`, `ExposedStudentRepository` (Task 3), `NeonAuthHttpClient` (Task 4), `AuthService` (Task 5), `authRoutes` (Task 6).

- [ ] **Step 1: Write the failing config test**

```kotlin
// server/src/test/kotlin/com/wolfeleo2/myuon/server/ApplicationConfigTest.kt
package com.wolfeleo2.myuon.server

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ApplicationConfigTest {
    @Test
    fun `requireEnv throws a message naming the missing variable`() {
        val exception = assertFailsWith<IllegalStateException> {
            requireEnv("MYUON_DEFINITELY_UNSET_VAR")
        }
        assertTrue(exception.message!!.contains("MYUON_DEFINITELY_UNSET_VAR"))
    }
}
```

- [ ] **Step 2: Run test, verify it fails**

Run: `./gradlew :server:test --tests "com.wolfeleo2.myuon.server.ApplicationConfigTest"`
Expected: FAIL — `requireEnv` is `internal`, exists since Task 1, but confirm this is genuinely exercising it (it should already pass once Task 1 landed; if it passes immediately, skip to Step 3 — nothing to implement for this step, `requireEnv` is done).

- [ ] **Step 3: Wire real dependencies into `Application.module()`**

```kotlin
// server/src/main/kotlin/com/wolfeleo2/myuon/server/Application.kt
package com.wolfeleo2.myuon.server

import com.wolfeleo2.myuon.server.db.DatabaseFactory
import com.wolfeleo2.myuon.server.db.ExposedStudentRepository
import com.wolfeleo2.myuon.server.domain.AuthService
import com.wolfeleo2.myuon.server.infra.NeonAuthHttpClient
import com.wolfeleo2.myuon.server.routes.authRoutes
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) { json() }

    val database = DatabaseFactory.connect(
        url = requireEnv("DATABASE_URL"),
        driver = "org.postgresql.Driver",
        user = requireEnv("DATABASE_USER"),
        password = requireEnv("DATABASE_PASSWORD"),
    )
    val studentRepository = ExposedStudentRepository(database)

    val httpClient = HttpClient(CIO) { install(ClientContentNegotiation) { json() } }
    val neonAuthClient = NeonAuthHttpClient(
        httpClient = httpClient,
        baseUrl = requireEnv("NEON_AUTH_BASE_URL"),
        projectId = requireEnv("NEON_AUTH_PROJECT_ID"),
        serverSecretKey = requireEnv("NEON_AUTH_SERVER_SECRET_KEY"),
    )
    val authService = AuthService(neonAuthClient, studentRepository)

    routing {
        get("/health") { call.respondText("OK") }
        authRoutes(authService)
    }
}

internal fun requireEnv(name: String): String =
    System.getenv(name) ?: error("Missing required environment variable: $name")
```

- [ ] **Step 4: Run the full test suite, verify everything still passes**

Run: `./gradlew :server:test`
Expected: PASS — `ApplicationTest` still passes because it never calls `Application.module()` (Task 1, Step 3 design choice); the new env-var requirement only affects `main()`/real startup.

- [ ] **Step 5: Document required env vars**

```
# server/.env.example
DATABASE_URL=jdbc:postgresql://<neon-host>/<db>?sslmode=require
DATABASE_USER=<neon-role>
DATABASE_PASSWORD=<neon-role-password>
NEON_AUTH_BASE_URL=<neon-auth-project-base-url>
NEON_AUTH_PROJECT_ID=<neon-auth-project-id>
NEON_AUTH_SERVER_SECRET_KEY=<neon-auth-server-secret-key>
PORT=8080
```

```markdown
<!-- server/README.md -->
# myUON Server

Ktor backend. Package layout: `domain/` (plain Kotlin, no framework
dependencies), `db/` (Exposed, Postgres/Neon), `infra/` (Neon Auth HTTP
client), `routes/` (Ktor routing). See
`docs/superpowers/specs/2026-08-30-ktor-backend-shape-and-auth-design.md`
for the design this follows.

## Running locally

1. Copy `.env.example` to `.env` and fill in real values (see "Manual setup"
   below for where they come from).
2. Export the vars into your shell (or use a tool like `direnv`) — plain
   Kotlin/`System.getenv` doesn't read `.env` files on its own.
3. `./gradlew :server:run`

## Manual setup (not automatable — do this once, outside this codebase)

1. Create a Neon project (neon.tech) and a Postgres database in it — gives
   you `DATABASE_URL`/`DATABASE_USER`/`DATABASE_PASSWORD`.
2. Enable Neon Auth on that project — gives you `NEON_AUTH_BASE_URL`,
   `NEON_AUTH_PROJECT_ID`, `NEON_AUTH_SERVER_SECRET_KEY`.
3. Once enabled, open that project's own `/reference` page (linked from the
   Neon Auth dashboard) and confirm the sign-up/sign-in endpoint paths match
   `NeonAuthHttpClient`'s defaults (`/sign-up/email`, `/sign-in/email`). If
   they differ, pass `signUpPath`/`signInPath` overrides where
   `NeonAuthHttpClient` is constructed in `Application.kt`.

## Tests

`./gradlew :server:test` — all tests run against H2 (in-memory, Postgres
compatibility mode) and a mocked HTTP engine; no live Neon project is
required to run the test suite.
```

- [ ] **Step 6: Commit**

```bash
git add server/src/main/kotlin/com/wolfeleo2/myuon/server/Application.kt server/src/test/kotlin/com/wolfeleo2/myuon/server/ApplicationConfigTest.kt server/.env.example server/README.md
git commit -m "feat(server): wire real Neon Postgres and Neon Auth config from env vars"
```

---

## Manual / follow-up work (not part of this plan's automated tasks)

- Actually creating a Neon project + enabling Neon Auth (Task 7, README "Manual setup") — needs a human with account access.
- Confirming `NeonAuthHttpClient`'s endpoint paths against a live project's `/reference` page once one exists (flagged with a `ponytail:` comment in Task 4).
- No git repo exists yet at `/Users/leo/AndroidStudioProjects/myUON` — every `git commit` step above assumes one has been initialized first; if not, `git init` before starting Task 1, or drop the commit steps and commit in one batch at the end.

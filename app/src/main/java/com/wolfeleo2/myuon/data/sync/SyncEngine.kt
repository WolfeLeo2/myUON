package com.wolfeleo2.myuon.data.sync

import com.wolfeleo2.myuon.BuildConfig
import com.wolfeleo2.myuon.data.db.*
import com.wolfeleo2.myuon.data.model.SyncDeltaPayload
import com.wolfeleo2.myuon.data.model.SyncEvent
import com.wolfeleo2.myuon.data.preferences.UserPreferencesDataStore
import com.wolfeleo2.myuon.data.remote.MyUonApiClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

enum class SyncState {
    IDLE,
    SYNCING,
    CONNECTED,
    OFFLINE,
    ERROR
}

@Singleton
class SyncEngine @Inject constructor(
    private val apiClient: MyUonApiClient,
    private val preferencesDataStore: UserPreferencesDataStore,
    private val studentDao: StudentDao,
    private val courseUnitDao: CourseUnitDao,
    private val gradeDao: GradeDao,
    private val timetableDao: TimetableDao,
    private val hostelDao: HostelDao,
    private val attendanceDao: AttendanceDao,
    private val feeDao: FeeDao,
    private val examCardDao: ExamCardDao,
    private val examTimetableDao: ExamTimetableDao,
    private val academicRequestDao: AcademicRequestDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<String?>(null)
    val lastSyncTimestamp: StateFlow<String?> = _lastSyncTimestamp.asStateFlow()

    private val wsClient = HttpClient(CIO) {
        install(WebSockets) {
            pingIntervalMillis = 15_000
        }
    }

    init {
        scope.launch {
            _lastSyncTimestamp.value = preferencesDataStore.lastSyncTimestamp.firstOrNull()
            // 1. Initial Delta Pull on startup
            pullDelta()
            // 2. Start WebSocket listening in background with auto-reconnect
            startRealtimeEventStream()
        }
    }

    suspend fun pullDelta(): Boolean {
        _syncState.value = SyncState.SYNCING
        val lastTimestamp = preferencesDataStore.lastSyncTimestamp.firstOrNull()
        val regNo = preferencesDataStore.activeStudentRegNo.firstOrNull()
        if (regNo.isNullOrBlank()) {
            _syncState.value = SyncState.IDLE
            return false
        }

        val delta = apiClient.fetchDelta(since = lastTimestamp, regNo = regNo)
        if (delta != null) {
            applyDelta(delta)
            preferencesDataStore.setLastSyncTimestamp(delta.timestamp)
            _lastSyncTimestamp.value = delta.timestamp
            _syncState.value = SyncState.IDLE
            return true
        } else {
            _syncState.value = SyncState.OFFLINE
            return false
        }
    }

    suspend fun applyDelta(delta: SyncDeltaPayload) {
        // Upsert student
        if (delta.studentProfile != null) {
            studentDao.insertOrUpdateStudent(delta.studentProfile.toEntity())
        }

        // Upsert course units
        if (delta.units.isNotEmpty()) {
            courseUnitDao.insertUnits(delta.units.map { it.toEntity() })
        }

        // Upsert grades
        if (delta.grades.isNotEmpty()) {
            gradeDao.insertGrades(delta.grades.map { it.toEntity() })
        }

        // Upsert timetable
        if (delta.timetable.isNotEmpty()) {
            timetableDao.insertSlots(delta.timetable.map { it.toEntity() })
        }

        // Upsert hostels
        if (delta.hostels.isNotEmpty()) {
            hostelDao.insertHalls(delta.hostels.map { it.toEntity() })
        }

        // Upsert hostel active booking
        if (delta.hostelBooking != null) {
            hostelDao.insertBooking(delta.hostelBooking.toEntity())
        }

        // Upsert attendance
        if (delta.attendanceOverview.isNotEmpty()) {
            attendanceDao.insertAttendance(delta.attendanceOverview.map { it.toEntity() })
        }

        // Upsert fee statement
        if (delta.feeStatement != null) {
            feeDao.insertStatement(delta.feeStatement.toEntity())
        }

        // Upsert exam card
        if (delta.examCard != null) {
            examCardDao.insertExamCard(delta.examCard.toEntity())
        }

        // Upsert exam timetable
        if (delta.examTimetable.isNotEmpty()) {
            examTimetableDao.insertExamSlots(delta.examTimetable.map { it.toEntity() })
        }

        // Upsert academic requests
        if (delta.academicRequests != null) {
            if (delta.academicRequests.specialExams.isNotEmpty()) {
                academicRequestDao.insertSpecialExams(delta.academicRequests.specialExams.map { it.toEntity() })
            }
            if (delta.academicRequests.supplementaries.isNotEmpty()) {
                academicRequestDao.insertSupplementaryRequests(delta.academicRequests.supplementaries.map { it.toEntity() })
            }
            if (delta.academicRequests.missingMarks.isNotEmpty()) {
                academicRequestDao.insertMissingMarksDisputes(delta.academicRequests.missingMarks.map { it.toEntity() })
            }
        }
    }

    private fun startRealtimeEventStream() {
        scope.launch {
            while (isActive) {
                try {
                    val regNo = preferencesDataStore.activeStudentRegNo.firstOrNull()
                    val rawUrl = BuildConfig.API_BASE_URL.replace("http://", "").replace("https://", "").removeSuffix("/")
                    val wsUrl = if (!regNo.isNullOrBlank()) "ws://$rawUrl/sync/events?regNo=$regNo" else "ws://$rawUrl/sync/events"

                    wsClient.webSocket(urlString = wsUrl) {
                        _syncState.value = SyncState.CONNECTED
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                val event = runCatching { json.decodeFromString<SyncEvent>(text) }.getOrNull()
                                if (event != null && event.eventType == "DELTA_AVAILABLE") {
                                    // Instant Delta Pull upon server push notification
                                    pullDelta()
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    _syncState.value = SyncState.OFFLINE
                }
                delay(5000)
            }
        }
    }
}

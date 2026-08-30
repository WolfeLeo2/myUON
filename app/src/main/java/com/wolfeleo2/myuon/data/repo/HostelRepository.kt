package com.wolfeleo2.myuon.data.repo

import com.wolfeleo2.myuon.data.db.HostelDao
import com.wolfeleo2.myuon.data.db.toDomain
import com.wolfeleo2.myuon.data.db.toEntity
import com.wolfeleo2.myuon.data.model.GenderTarget
import com.wolfeleo2.myuon.data.model.HostelHall
import com.wolfeleo2.myuon.data.model.HostelRoomBooking
import com.wolfeleo2.myuon.data.remote.MyUonApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostelRepository @Inject constructor(
    private val apiClient: MyUonApiClient,
    private val hostelDao: HostelDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val defaultHalls = listOf(
        HostelHall("HALL-01", "Hall 1 (Main Campus)", "Main Campus", GenderTarget.MALE, 120, 14, 6500.0, true),
        HostelHall("HALL-02", "Hall 2 (Chiromo)", "Chiromo Campus", GenderTarget.MALE, 90, 8, 7000.0, true),
        HostelHall("HALL-03", "Hall 3 (Women's Hall)", "Main Campus", GenderTarget.FEMALE, 150, 22, 6500.0, true),
        HostelHall("HALL-04", "Prefabs (Chiromo)", "Chiromo Campus", GenderTarget.MALE, 60, 4, 5500.0, true),
        HostelHall("HALL-05", "Parklands Annex", "Parklands Campus", GenderTarget.CO_ED, 80, 0, 8000.0, false)
    )

    private val _hostels = MutableStateFlow<List<HostelHall>>(defaultHalls)
    val hostels: StateFlow<List<HostelHall>> = _hostels.asStateFlow()

    private val _activeBooking = MutableStateFlow<HostelRoomBooking?>(
        HostelRoomBooking(
            bookingId = "BK-994821",
            regNo = "P15/12345/2022",
            hallName = "Hall 2 (Chiromo)",
            roomNumber = "RM-204",
            bedSpace = "A",
            academicYear = "2025/2026",
            semester = 2,
            rentAmount = 7000.0,
            isPaid = true,
            isKeyIssued = true,
            bookedDate = "15 Jan 2026"
        )
    )
    val activeBooking: StateFlow<HostelRoomBooking?> = _activeBooking.asStateFlow()

    init {
        scope.launch {
            val cached = hostelDao.getAllHalls().firstOrNull()?.map { it.toDomain() } ?: emptyList()
            if (cached.isNotEmpty()) {
                _hostels.value = cached
            } else {
                hostelDao.insertHalls(defaultHalls.map { it.toEntity() })
            }
            refreshFromRemote("P15/12345/2022")
        }
    }

    suspend fun refreshFromRemote(regNo: String) {
        val remoteHostels = apiClient.getHostels()
        if (!remoteHostels.isNullOrEmpty()) {
            _hostels.value = remoteHostels
            hostelDao.insertHalls(remoteHostels.map { it.toEntity() })
        }
        val remoteBooking = apiClient.getHostelBooking(regNo)
        if (remoteBooking != null) {
            _activeBooking.value = remoteBooking
        }
    }

    suspend fun bookRoom(
        regNo: String,
        hall: HostelHall,
        roomNumber: String,
        bedSpace: String
    ): Boolean {
        if (hall.availableRooms <= 0) return false

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val booking = HostelRoomBooking(
            bookingId = "BK-${UUID.randomUUID().toString().take(6).uppercase()}",
            regNo = regNo,
            hallName = hall.hallName,
            roomNumber = roomNumber,
            bedSpace = bedSpace,
            academicYear = "2025/2026",
            semester = 2,
            rentAmount = hall.rentPerSemester,
            isPaid = false,
            isKeyIssued = false,
            bookedDate = dateFormat.format(Date())
        )

        apiClient.bookHostel(regNo, hall.hallName, roomNumber, bedSpace, hall.rentPerSemester)

        _activeBooking.value = booking
        val updated = _hostels.value.map {
            if (it.hallId == hall.hallId) {
                it.copy(availableRooms = (it.availableRooms - 1).coerceAtLeast(0))
            } else it
        }
        _hostels.value = updated
        scope.launch { hostelDao.insertHalls(updated.map { it.toEntity() }) }
        return true
    }

    suspend fun payHostelRent(): Boolean {
        val booking = _activeBooking.value ?: return false
        if (booking.isPaid) return false

        apiClient.payHostel(booking.bookingId)

        _activeBooking.value = booking.copy(isPaid = true)
        return true
    }
}

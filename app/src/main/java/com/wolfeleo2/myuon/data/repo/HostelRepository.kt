package com.wolfeleo2.myuon.data.repo

import com.wolfeleo2.myuon.data.db.HostelDao
import com.wolfeleo2.myuon.data.db.toDomain
import com.wolfeleo2.myuon.data.db.toEntity
import com.wolfeleo2.myuon.data.model.HostelHall
import com.wolfeleo2.myuon.data.model.HostelRoomBooking
import com.wolfeleo2.myuon.data.preferences.UserPreferencesDataStore
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
    private val hostelDao: HostelDao,
    private val preferencesDataStore: UserPreferencesDataStore
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _hostels = MutableStateFlow<List<HostelHall>>(emptyList())
    val hostels: StateFlow<List<HostelHall>> = _hostels.asStateFlow()

    private val _activeBooking = MutableStateFlow<HostelRoomBooking?>(null)
    val activeBooking: StateFlow<HostelRoomBooking?> = _activeBooking.asStateFlow()

    init {
        scope.launch {
            val cached = hostelDao.getAllHalls().firstOrNull()?.map { it.toDomain() } ?: emptyList()
            if (cached.isNotEmpty()) {
                _hostels.value = cached
            }
            preferencesDataStore.activeStudentRegNo.collect { regNo ->
                if (!regNo.isNullOrBlank()) {
                    val cachedBooking = hostelDao.getActiveBooking(regNo).firstOrNull()?.toDomain()
                    if (cachedBooking != null) {
                        _activeBooking.value = cachedBooking
                    }
                    refreshFromRemote(regNo)
                }
            }
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
            hostelDao.insertBooking(remoteBooking.toEntity())
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
        scope.launch { hostelDao.insertBooking(booking.toEntity()) }
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

        val updatedBooking = booking.copy(isPaid = true)
        _activeBooking.value = updatedBooking
        scope.launch { hostelDao.insertBooking(updatedBooking.toEntity()) }
        return true
    }
}

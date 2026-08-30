package com.wolfeleo2.myuon.data.repo

import com.wolfeleo2.myuon.data.db.TimetableDao
import com.wolfeleo2.myuon.data.db.toDomain
import com.wolfeleo2.myuon.data.db.toEntity
import com.wolfeleo2.myuon.data.model.ClassType
import com.wolfeleo2.myuon.data.model.TimetableItem
import com.wolfeleo2.myuon.data.remote.MyUonApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimetableRepository @Inject constructor(
    private val apiClient: MyUonApiClient,
    private val timetableDao: TimetableDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val defaultTimetable = listOf(
        TimetableItem("TT-01", "Monday", "09:00", "11:00", "CSC 311", "Advanced Database Systems", "Prof. Peter Wagacha", "pwagacha@uonbi.ac.ke", "Chiromo Lab 02", "Chiromo", ClassType.LECTURE),
        TimetableItem("TT-02", "Monday", "14:00", "16:00", "CSC 321", "Distributed Systems & Cloud Computing", "Dr. Richard Omollo", "romollo@uonbi.ac.ke", "Chiromo Lab 01", "Chiromo", ClassType.LECTURE),
        TimetableItem("TT-03", "Tuesday", "11:00", "13:00", "CSC 315", "Operating Systems Principles", "Dr. Andrew Mwangi", "amwangi@uonbi.ac.ke", "MLT 01", "Main Campus", ClassType.LECTURE),
        TimetableItem("TT-04", "Wednesday", "08:00", "10:00", "CSC 323", "Artificial Intelligence & Machine Learning", "Prof. Peter Wagacha", "pwagacha@uonbi.ac.ke", "Chiromo Lab 03", "Chiromo", ClassType.LECTURE),
        TimetableItem("TT-05", "Thursday", "10:00", "12:00", "CSC 327", "Compiler Construction", "Prof. Christopher Chepken", "cchepken@uonbi.ac.ke", "Chiromo Rm 204", "Chiromo", ClassType.LECTURE),
        TimetableItem("TT-06", "Friday", "14:00", "16:00", "CSC 331", "Computer Graphics & Multimedia", "Dr. Elisha Opiyo", "eopiyo@uonbi.ac.ke", "Graphics Lab", "Chiromo", ClassType.LABORATORY)
    )

    private val _timetableSlots = MutableStateFlow<List<TimetableItem>>(defaultTimetable)
    val timetableSlots: StateFlow<List<TimetableItem>> = _timetableSlots.asStateFlow()

    init {
        scope.launch {
            val cached = timetableDao.getAllSlots().firstOrNull()?.map { it.toDomain() } ?: emptyList()
            if (cached.isNotEmpty()) {
                _timetableSlots.value = cached
            } else {
                timetableDao.insertSlots(defaultTimetable.map { it.toEntity() })
            }
            refreshFromRemote("P15/12345/2022")
        }
    }

    suspend fun refreshFromRemote(regNo: String) {
        val remoteSlots = apiClient.getTimetable(regNo)
        if (!remoteSlots.isNullOrEmpty()) {
            _timetableSlots.value = remoteSlots
            timetableDao.insertSlots(remoteSlots.map { it.toEntity() })
        }
    }

    fun getSlotsForDay(day: String): List<TimetableItem> {
        return _timetableSlots.value.filter { it.dayOfWeek.equals(day, ignoreCase = true) }
    }
}

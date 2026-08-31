package com.wolfeleo2.myuon.data.repo

import com.wolfeleo2.myuon.data.db.TimetableDao
import com.wolfeleo2.myuon.data.db.toDomain
import com.wolfeleo2.myuon.data.db.toEntity
import com.wolfeleo2.myuon.data.model.TimetableItem
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimetableRepository @Inject constructor(
    private val apiClient: MyUonApiClient,
    private val timetableDao: TimetableDao,
    private val preferencesDataStore: UserPreferencesDataStore
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _timetableSlots = MutableStateFlow<List<TimetableItem>>(emptyList())
    val timetableSlots: StateFlow<List<TimetableItem>> = _timetableSlots.asStateFlow()

    init {
        scope.launch {
            val cached = timetableDao.getAllSlots().firstOrNull()?.map { it.toDomain() } ?: emptyList()
            if (cached.isNotEmpty()) {
                _timetableSlots.value = cached
            }
            preferencesDataStore.activeStudentRegNo.collect { regNo ->
                if (!regNo.isNullOrBlank()) {
                    refreshFromRemote(regNo)
                }
            }
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

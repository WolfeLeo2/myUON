package com.wolfeleo2.myuon.server.db

import com.wolfeleo2.myuon.server.domain.GenderTarget
import com.wolfeleo2.myuon.server.domain.HostelHall
import com.wolfeleo2.myuon.server.domain.HostelRepository
import com.wolfeleo2.myuon.server.domain.HostelRoomBooking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class ExposedHostelRepository(private val database: Database) : HostelRepository {

    override suspend fun getHostels(): List<HostelHall> = dbQuery {
        HostelsTable.selectAll().map { it.toHostelHall() }
    }

    override suspend fun getHostelBooking(regNo: String): HostelRoomBooking? = dbQuery {
        val student = StudentsTable.selectAll().where { StudentsTable.regNo eq regNo }.singleOrNull()
            ?: return@dbQuery null
        val studentId = student[StudentsTable.userId]

        val bookingRow = HostelBookingsTable.selectAll().where { HostelBookingsTable.studentId eq studentId }
            .singleOrNull() ?: return@dbQuery null

        val hostel = HostelsTable.selectAll().where { HostelsTable.id eq bookingRow[HostelBookingsTable.hostelId] }
            .singleOrNull()

        HostelRoomBooking(
            bookingId = bookingRow[HostelBookingsTable.id].toString(),
            regNo = regNo,
            hallName = hostel?.get(HostelsTable.name) ?: "Residence Hall",
            roomNumber = bookingRow[HostelBookingsTable.roomNumber],
            bedSpace = "A",
            academicYear = bookingRow[HostelBookingsTable.academicYear],
            semester = bookingRow[HostelBookingsTable.semester],
            rentAmount = hostel?.get(HostelsTable.ratePerSemester) ?: 6500.0,
            isPaid = bookingRow[HostelBookingsTable.isPaid],
            isKeyIssued = true,
            bookedDate = bookingRow[HostelBookingsTable.bookedAt]
        )
    }

    override suspend fun createBooking(booking: HostelRoomBooking): HostelRoomBooking = dbQuery {
        val student = StudentsTable.selectAll().where { StudentsTable.regNo eq booking.regNo }.singleOrNull()
        val studentId = student?.get(StudentsTable.userId) ?: booking.regNo
        val uuid = runCatching { UUID.fromString(booking.bookingId) }.getOrElse { UUID.randomUUID() }

        HostelBookingsTable.insert {
            it[HostelBookingsTable.id] = uuid
            it[HostelBookingsTable.studentId] = studentId
            it[HostelBookingsTable.hostelId] = "hall-1"
            it[HostelBookingsTable.roomNumber] = booking.roomNumber
            it[HostelBookingsTable.academicYear] = booking.academicYear
            it[HostelBookingsTable.semester] = booking.semester
            it[HostelBookingsTable.status] = "ACTIVE"
            it[HostelBookingsTable.isPaid] = booking.isPaid
            it[HostelBookingsTable.bookedAt] = booking.bookedDate
        }
        booking.copy(bookingId = uuid.toString())
    }

    override suspend fun markBookingPaid(bookingId: String): Boolean = dbQuery {
        val uuid = runCatching { UUID.fromString(bookingId) }.getOrNull() ?: return@dbQuery false
        HostelBookingsTable.update({ HostelBookingsTable.id eq uuid }) {
            it[isPaid] = true
        } > 0
    }

    private fun ResultRow.toHostelHall() = HostelHall(
        hallId = this[HostelsTable.id],
        hallName = this[HostelsTable.name],
        campus = this[HostelsTable.campus],
        genderTarget = GenderTarget.valueOf(this[HostelsTable.gender].uppercase()),
        totalRooms = this[HostelsTable.totalRooms],
        availableRooms = this[HostelsTable.availableRooms],
        rentPerSemester = this[HostelsTable.ratePerSemester],
        amenities = listOf("Wi-Fi", "Study Area", "Water 24/7", "Catering")
    )

    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction(database) { block() } }
}

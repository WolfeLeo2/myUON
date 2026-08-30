package com.wolfeleo2.myuon.server.db

import org.jetbrains.exposed.sql.Table

object HostelsTable : Table("hostels") {
    val id = varchar("id", 50)
    val name = varchar("name", 100)
    val campus = varchar("campus", 100)
    val gender = varchar("gender", 20)
    val totalRooms = integer("total_rooms")
    val availableRooms = integer("available_rooms")
    val ratePerSemester = double("rate_per_semester")

    override val primaryKey = PrimaryKey(id)
}

object HostelBookingsTable : Table("hostel_bookings") {
    val id = uuid("id")
    val studentId = varchar("student_id", 64)
    val hostelId = varchar("hostel_id", 50)
    val roomNumber = varchar("room_number", 20)
    val academicYear = varchar("academic_year", 20)
    val semester = integer("semester")
    val status = varchar("status", 20).default("ACTIVE")
    val isPaid = bool("is_paid").default(false)
    val bookedAt = varchar("booked_at", 64)

    override val primaryKey = PrimaryKey(id)
}

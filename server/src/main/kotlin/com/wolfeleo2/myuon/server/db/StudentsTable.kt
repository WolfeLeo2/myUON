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

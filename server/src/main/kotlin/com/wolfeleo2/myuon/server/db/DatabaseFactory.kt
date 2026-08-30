package com.wolfeleo2.myuon.server.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun connect(url: String, driver: String, user: String = "", password: String = ""): Database {
        val database = Database.connect(url = url, driver = driver, user = user, password = password)
        transaction(database) {
            SchemaUtils.create(
                StudentsTable,
                UnitsTable,
                UnitRegistrationsTable,
                GradeRecordsTable,
                TimetableItemsTable,
                ExamTimetableItemsTable,
                AttendanceSessionsTable,
                StudentAttendanceTable,
                AcademicRequestsTable,
                FeeStatementsTable,
                FeeTransactionsTable,
                HostelsTable,
                HostelBookingsTable
            )
        }
        return database
    }
}

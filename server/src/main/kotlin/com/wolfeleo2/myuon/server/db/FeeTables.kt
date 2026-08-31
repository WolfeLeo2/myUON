package com.wolfeleo2.myuon.server.db

import org.jetbrains.exposed.sql.Table

object FeeStatementsTable : Table("fee_statements") {
    val id = uuid("id")
    val studentId = varchar("student_id", 64) references StudentsTable.userId
    val academicYear = varchar("academic_year", 20)
    val semester = integer("semester")
    val totalInvoiced = double("total_invoiced").default(0.0)
    val totalPaid = double("total_paid").default(0.0)
    val currentBalance = double("current_balance").default(0.0)
    val updatedAt = varchar("updated_at", 64)

    override val primaryKey = PrimaryKey(id)
}

object FeeTransactionsTable : Table("fee_transactions") {
    val id = uuid("id")
    val studentId = varchar("student_id", 64) references StudentsTable.userId
    val academicYear = varchar("academic_year", 20)
    val semester = integer("semester")
    val transactionType = varchar("transaction_type", 30)
    val description = varchar("description", 255)
    val referenceNo = varchar("reference_no", 100).nullable()
    val amount = double("amount")
    val balanceAfter = double("balance_after")
    val transactionDate = varchar("transaction_date", 30)

    override val primaryKey = PrimaryKey(id)
}

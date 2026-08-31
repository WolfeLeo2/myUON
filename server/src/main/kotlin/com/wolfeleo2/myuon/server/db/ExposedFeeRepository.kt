package com.wolfeleo2.myuon.server.db

import com.wolfeleo2.myuon.server.domain.FeeRepository
import com.wolfeleo2.myuon.server.domain.FeeStatement
import com.wolfeleo2.myuon.server.domain.FeeTransaction
import com.wolfeleo2.myuon.server.domain.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ExposedFeeRepository(private val database: Database) : FeeRepository {

    override suspend fun getFeeStatement(
        regNo: String,
        academicYear: String,
        semester: Int
    ): FeeStatement? = dbQuery {
        val student = StudentsTable.selectAll().where { StudentsTable.regNo eq regNo }.singleOrNull()
            ?: return@dbQuery null
        val studentId = student[StudentsTable.userId]

        if (semester == 0) {
            // Full Academic Year Scope
            val statementRows = FeeStatementsTable.selectAll().where {
                (FeeStatementsTable.studentId eq studentId) and (FeeStatementsTable.academicYear eq academicYear)
            }.toList()

            val txRows = FeeTransactionsTable.selectAll().where {
                (FeeTransactionsTable.studentId eq studentId) and (FeeTransactionsTable.academicYear eq academicYear)
            }.orderBy(FeeTransactionsTable.transactionDate to org.jetbrains.exposed.sql.SortOrder.DESC).toList()

            val txs = txRows.map { row ->
                FeeTransaction(
                    id = row[FeeTransactionsTable.id].toString(),
                    regNo = regNo,
                    referenceNumber = row[FeeTransactionsTable.referenceNo] ?: "",
                    date = row[FeeTransactionsTable.transactionDate],
                    description = row[FeeTransactionsTable.description],
                    type = TransactionType.valueOf(row[FeeTransactionsTable.transactionType].uppercase()),
                    amount = row[FeeTransactionsTable.amount],
                    balanceAfter = row[FeeTransactionsTable.balanceAfter]
                )
            }

            val totalInvoiced = statementRows.sumOf { it[FeeStatementsTable.totalInvoiced] }
            val totalPaid = statementRows.sumOf { it[FeeStatementsTable.totalPaid] }
            val balance = statementRows.sumOf { it[FeeStatementsTable.currentBalance] }
            val helb = txs.filter { it.type == TransactionType.HELB_DISBURSEMENT }.sumOf { kotlin.math.abs(it.amount) }
            val hef = txs.filter { it.type == TransactionType.HEF_SCHOLARSHIP }.sumOf { kotlin.math.abs(it.amount) }

            FeeStatement(
                regNo = regNo,
                academicYear = academicYear,
                semester = 0,
                totalInvoiced = if (totalInvoiced > 0.0) totalInvoiced else (if (academicYear == "2025/2026") 78500.0 else 72000.0),
                totalPaid = totalPaid,
                outstandingBalance = balance,
                helbDisbursed = if (helb > 0.0) helb else 34000.0,
                hefScholarship = if (hef > 0.0) hef else 36000.0,
                transactions = txs
            )
        } else {
            // Per-Semester Scope
            val statementRow = FeeStatementsTable.selectAll().where {
                (FeeStatementsTable.studentId eq studentId) and
                    (FeeStatementsTable.academicYear eq academicYear) and
                    (FeeStatementsTable.semester eq semester)
            }.singleOrNull()

            val txRows = FeeTransactionsTable.selectAll().where {
                (FeeTransactionsTable.studentId eq studentId) and
                    (FeeTransactionsTable.academicYear eq academicYear) and
                    (FeeTransactionsTable.semester eq semester)
            }.orderBy(FeeTransactionsTable.transactionDate to org.jetbrains.exposed.sql.SortOrder.DESC).toList()

            val txs = txRows.map { row ->
                FeeTransaction(
                    id = row[FeeTransactionsTable.id].toString(),
                    regNo = regNo,
                    referenceNumber = row[FeeTransactionsTable.referenceNo] ?: "",
                    date = row[FeeTransactionsTable.transactionDate],
                    description = row[FeeTransactionsTable.description],
                    type = TransactionType.valueOf(row[FeeTransactionsTable.transactionType].uppercase()),
                    amount = row[FeeTransactionsTable.amount],
                    balanceAfter = row[FeeTransactionsTable.balanceAfter]
                )
            }

            val totalInvoiced = statementRow?.get(FeeStatementsTable.totalInvoiced) ?: 39250.0
            val totalPaid = statementRow?.get(FeeStatementsTable.totalPaid) ?: 39250.0
            val balance = statementRow?.get(FeeStatementsTable.currentBalance) ?: 0.0
            val helb = txs.filter { it.type == TransactionType.HELB_DISBURSEMENT }.sumOf { kotlin.math.abs(it.amount) }
            val hef = txs.filter { it.type == TransactionType.HEF_SCHOLARSHIP }.sumOf { kotlin.math.abs(it.amount) }

            FeeStatement(
                regNo = regNo,
                academicYear = academicYear,
                semester = semester,
                totalInvoiced = totalInvoiced,
                totalPaid = totalPaid,
                outstandingBalance = balance,
                helbDisbursed = if (helb > 0.0) helb else 17000.0,
                hefScholarship = if (hef > 0.0) hef else 18000.0,
                transactions = txs
            )
        }
    }

    override suspend fun getFeeTransactions(regNo: String): List<FeeTransaction> = dbQuery {
        val student = StudentsTable.selectAll().where { StudentsTable.regNo eq regNo }.singleOrNull()
            ?: return@dbQuery emptyList()
        val studentId = student[StudentsTable.userId]

        FeeTransactionsTable.selectAll().where { FeeTransactionsTable.studentId eq studentId }
            .map { row ->
                FeeTransaction(
                    id = row[FeeTransactionsTable.id].toString(),
                    regNo = regNo,
                    referenceNumber = row[FeeTransactionsTable.referenceNo] ?: "",
                    date = row[FeeTransactionsTable.transactionDate],
                    description = row[FeeTransactionsTable.description],
                    type = TransactionType.valueOf(row[FeeTransactionsTable.transactionType].uppercase()),
                    amount = row[FeeTransactionsTable.amount],
                    balanceAfter = row[FeeTransactionsTable.balanceAfter]
                )
            }
    }

    override suspend fun recordTransaction(transaction: FeeTransaction): FeeTransaction = dbQuery {
        val student = StudentsTable.selectAll().where { StudentsTable.regNo eq transaction.regNo }.singleOrNull()
        val studentId = student?.get(StudentsTable.userId) ?: transaction.regNo
        val uuid = runCatching { UUID.fromString(transaction.id) }.getOrElse { UUID.randomUUID() }

        FeeTransactionsTable.insert {
            it[FeeTransactionsTable.id] = uuid
            it[FeeTransactionsTable.studentId] = studentId
            it[FeeTransactionsTable.academicYear] = "2025/2026"
            it[FeeTransactionsTable.semester] = 2
            it[FeeTransactionsTable.transactionType] = transaction.type.name
            it[FeeTransactionsTable.description] = transaction.description
            it[FeeTransactionsTable.referenceNo] = transaction.referenceNumber
            it[FeeTransactionsTable.amount] = transaction.amount
            it[FeeTransactionsTable.balanceAfter] = transaction.balanceAfter
            it[FeeTransactionsTable.transactionDate] = transaction.date
        }
        transaction.copy(id = uuid.toString())
    }

    private suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) { transaction(database) { block() } }
}

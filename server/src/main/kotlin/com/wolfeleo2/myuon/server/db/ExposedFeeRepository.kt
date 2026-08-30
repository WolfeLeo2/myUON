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

        val statementRow = FeeStatementsTable.selectAll().where {
            (FeeStatementsTable.studentId eq studentId) and
                (FeeStatementsTable.academicYear eq academicYear) and
                (FeeStatementsTable.semester eq semester)
        }.singleOrNull()

        val txs = FeeTransactionsTable.selectAll().where { FeeTransactionsTable.studentId eq studentId }
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

        FeeStatement(
            regNo = regNo,
            academicYear = academicYear,
            semester = semester,
            totalInvoiced = statementRow?.get(FeeStatementsTable.totalInvoiced) ?: 36000.0,
            totalPaid = statementRow?.get(FeeStatementsTable.totalPaid) ?: 36000.0,
            outstandingBalance = statementRow?.get(FeeStatementsTable.currentBalance) ?: 0.0,
            helbDisbursed = 18000.0,
            hefScholarship = 10000.0,
            transactions = txs
        )
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

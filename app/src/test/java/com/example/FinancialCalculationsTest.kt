package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.AppSettingEntity
import com.example.data.entity.TransactionEntity
import com.example.data.repository.TransactionRepository
import com.example.model.BudgetStatus
import com.example.model.Currency
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FinancialCalculationsTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: TransactionRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TransactionRepository(db.transactionDao(), db.appSettingDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testCurrencyFormattingUSD() {
        // 1250 minor units = $12.50
        val formatted = CurrencyFormatter.format(1250L, Currency.USD)
        assertEquals("$12.50", formatted)

        // Single digit cents: 505 minor units = $5.05
        val singleDigitCent = CurrencyFormatter.format(505L, Currency.USD)
        assertEquals("$5.05", singleDigitCent)

        // Zero cents
        val zeroCent = CurrencyFormatter.format(1000L, Currency.USD)
        assertEquals("$10.00", zeroCent)

        // Signed format for expense
        val signedExpense = CurrencyFormatter.format(1500L, Currency.USD, showSign = true, isIncome = false)
        assertEquals("- $15.00", signedExpense)

        // Signed format for income
        val signedIncome = CurrencyFormatter.format(25000L, Currency.USD, showSign = true, isIncome = true)
        assertEquals("+ $250.00", signedIncome)
    }

    @Test
    fun testCurrencyFormattingSOS() {
        // 25000 minor units = 25,000 Sh.So.
        val formatted = CurrencyFormatter.format(25000L, Currency.SOS)
        assertEquals("25,000 Sh.So.", formatted)

        val signedExpense = CurrencyFormatter.format(50000L, Currency.SOS, showSign = true, isIncome = false)
        assertEquals("- 50,000 Sh.So.", signedExpense)
    }

    @Test
    fun testCurrencyInputParsing() {
        // USD parses decimal inputs to minor cents
        assertEquals(1250L, CurrencyFormatter.parseInputToMinorUnits("12.50", Currency.USD))
        assertEquals(1250L, CurrencyFormatter.parseInputToMinorUnits("12.5", Currency.USD))
        assertEquals(1200L, CurrencyFormatter.parseInputToMinorUnits("12", Currency.USD))
        assertEquals(99L, CurrencyFormatter.parseInputToMinorUnits("0.99", Currency.USD))
        assertEquals(150000L, CurrencyFormatter.parseInputToMinorUnits("1,500.00", Currency.USD))

        // SOS parses whole units
        assertEquals(25000L, CurrencyFormatter.parseInputToMinorUnits("25000", Currency.SOS))
        assertEquals(25000L, CurrencyFormatter.parseInputToMinorUnits("25,000", Currency.SOS))

        // Invalid inputs
        assertNull(CurrencyFormatter.parseInputToMinorUnits("", Currency.USD))
        assertNull(CurrencyFormatter.parseInputToMinorUnits("abc", Currency.USD))
        assertNull(CurrencyFormatter.parseInputToMinorUnits("-50", Currency.USD))
    }

    @Test
    fun testBalanceAndNetCalculations() {
        // Income entries
        val salaryMinor = 250000L // $2,500.00
        val freelanceMinor = 45000L // $450.00
        val totalIncome = salaryMinor + freelanceMinor

        // Expense entries
        val rentMinor = 80000L // $800.00
        val foodMinor = 35000L // $350.00
        val transportMinor = 15000L // $150.00
        val totalExpense = rentMinor + foodMinor + transportMinor

        val balance = totalIncome - totalExpense
        assertEquals(295000L, totalIncome)
        assertEquals(130000L, totalExpense)
        assertEquals(165000L, balance) // $1,650.00 net savings
    }

    @Test
    fun testMinorUnitFloatingPointPrecision() {
        // Floating point 0.1 + 0.2 produces 0.30000000000000004
        // In integer minor units: 10 + 20 = 30 cents exactly
        val cent1 = CurrencyFormatter.parseInputToMinorUnits("0.10", Currency.USD)!!
        val cent2 = CurrencyFormatter.parseInputToMinorUnits("0.20", Currency.USD)!!
        val sum = cent1 + cent2
        assertEquals(30L, sum)
        assertEquals("$0.30", CurrencyFormatter.format(sum, Currency.USD))
    }

    @Test
    fun testBudgetThresholdAlerts() {
        val limitMinor = 100000L // $1,000.00 limit

        // Safe spending (70%)
        val spent70 = 70000L
        val statusSafe = when {
            spent70 >= limitMinor -> BudgetStatus.EXCEEDED
            spent70.toFloat() / limitMinor.toFloat() >= 0.8f -> BudgetStatus.WARNING_80
            else -> BudgetStatus.SAFE
        }
        assertEquals(BudgetStatus.SAFE, statusSafe)

        // Warning threshold (85%)
        val spent85 = 85000L
        val statusWarning = when {
            spent85 >= limitMinor -> BudgetStatus.EXCEEDED
            spent85.toFloat() / limitMinor.toFloat() >= 0.8f -> BudgetStatus.WARNING_80
            else -> BudgetStatus.SAFE
        }
        assertEquals(BudgetStatus.WARNING_80, statusWarning)

        // Exceeded threshold (105%)
        val spent105 = 105000L
        val statusExceeded = when {
            spent105 >= limitMinor -> BudgetStatus.EXCEEDED
            spent105.toFloat() / limitMinor.toFloat() >= 0.8f -> BudgetStatus.WARNING_80
            else -> BudgetStatus.SAFE
        }
        assertEquals(BudgetStatus.EXCEEDED, statusExceeded)
    }

    @Test
    fun testRoomDatabaseOperations() = runBlocking {
        val dao = db.transactionDao()
        val settingDao = db.appSettingDao()

        // 1. Insert transactions
        val tx1Id = dao.insertTransaction(
            TransactionEntity(
                amountMinor = 5000L,
                type = "INCOME",
                categoryKey = "SALARY",
                description = "Monthly Pay",
                dateTimestamp = 1000L,
                currencyCode = "USD"
            )
        )
        assertTrue(tx1Id > 0)

        val tx2Id = dao.insertTransaction(
            TransactionEntity(
                amountMinor = 1200L,
                type = "EXPENSE",
                categoryKey = "FOOD",
                description = "Groceries",
                dateTimestamp = 2000L,
                currencyCode = "USD"
            )
        )
        assertTrue(tx2Id > 0)

        // 2. Query transactions list (ordered by dateTimestamp DESC)
        val list = dao.getAllTransactionsList()
        assertEquals(2, list.size)
        assertEquals(tx2Id, list[0].id) // Most recent first

        // 3. Update transaction
        val toUpdate = dao.getTransactionById(tx2Id)!!
        dao.updateTransaction(toUpdate.copy(amountMinor = 1500L, description = "Supermarket"))
        val updated = dao.getTransactionById(tx2Id)!!
        assertEquals(1500L, updated.amountMinor)
        assertEquals("Supermarket", updated.description)

        // 4. Settings persistence
        settingDao.setSetting(AppSettingEntity("language", "so"))
        val lang = settingDao.getSettingValue("language")
        assertEquals("so", lang)

        // 5. Delete transaction
        dao.deleteTransactionById(tx1Id)
        val afterDelete = dao.getAllTransactionsList()
        assertEquals(1, afterDelete.size)

        // 6. Delete all
        dao.deleteAllTransactions()
        assertEquals(0, dao.getAllTransactionsList().size)
    }

    @Test
    fun testBackupAndRestoreWithDeduplication() = runBlocking {
        // Insert initial data
        repository.insertTransaction(
            TransactionEntity(
                amountMinor = 2500L,
                type = "EXPENSE",
                categoryKey = "TRANSPORT",
                description = "Bus fare",
                dateTimestamp = 1729000000000L,
                currencyCode = "USD"
            )
        )
        repository.setSetting("currency", "USD")

        // Export JSON
        val exported = repository.exportJson()
        assertNotNull(exported)
        assertTrue(exported.contains("Bus fare"))
        assertTrue(exported.contains("TRANSPORT"))

        // Clear data
        repository.deleteAllData()
        assertEquals(0, db.transactionDao().getAllTransactionsList().size)

        // Restore JSON
        val restoreResult = repository.importJson(exported)
        assertTrue(restoreResult.isSuccess)
        assertEquals(1, restoreResult.getOrNull())

        val restoredTxs = db.transactionDao().getAllTransactionsList()
        assertEquals(1, restoredTxs.size)
        assertEquals("Bus fare", restoredTxs[0].description)

        // Attempting to restore the same file again must deduplicate
        val secondRestore = repository.importJson(exported)
        assertTrue(secondRestore.isSuccess)
        assertEquals(0, secondRestore.getOrNull()) // 0 new transactions added

        val afterSecond = db.transactionDao().getAllTransactionsList()
        assertEquals(1, afterSecond.size) // No duplicates created
    }

    @Test
    fun testMalformedAndEmptyBackupHandling() = runBlocking {
        // Empty string
        val emptyResult = repository.importJson("")
        assertTrue(emptyResult.isFailure)

        // Whitespace string
        val whitespaceResult = repository.importJson("   ")
        assertTrue(whitespaceResult.isFailure)

        // Invalid JSON syntax
        val invalidSyntax = repository.importJson("{ broken json")
        assertTrue(invalidSyntax.isFailure)

        // JSON missing transactions/settings root
        val wrongSchema = repository.importJson("{\"otherKey\": 123}")
        assertTrue(wrongSchema.isFailure)
    }
}

package com.example

import com.example.model.BudgetStatus
import com.example.model.Currency
import com.example.util.CurrencyFormatter
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FinancialCalculationsTest {

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
    fun testBackupJsonSerialization() {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", 1729000000000L)

        val txArray = JSONArray()
        val tx1 = JSONObject().apply {
            put("id", 1L)
            put("amountMinor", 1250L)
            put("type", "EXPENSE")
            put("categoryKey", "FOOD")
            put("description", "Lunch")
            put("dateTimestamp", 1729000000000L)
            put("currencyCode", "USD")
        }
        txArray.put(tx1)
        root.put("transactions", txArray)

        val jsonString = root.toString()
        assertNotNull(jsonString)

        // Verify deserialization
        val parsed = JSONObject(jsonString)
        val readArray = parsed.getJSONArray("transactions")
        assertEquals(1, readArray.length())
        val readTx = readArray.getJSONObject(0)
        assertEquals(1250L, readTx.getLong("amountMinor"))
        assertEquals("FOOD", readTx.getString("categoryKey"))
        assertEquals("USD", readTx.getString("currencyCode"))
    }
}

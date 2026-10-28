package com.example.util

import com.example.model.Currency
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    /**
     * Formats an amount stored in minor units into a human-readable display string.
     * USD uses 2 decimal places (e.g., 1250 minor units -> "$12.50").
     * SOS uses whole units with thousands separators (e.g., 25000 minor units -> "25,000 Sh.So.").
     */
    fun format(
        amountMinor: Long,
        currency: Currency,
        showSign: Boolean = false,
        isIncome: Boolean? = null
    ): String {
        val isNegative = amountMinor < 0
        val absAmount = Math.abs(amountMinor)

        val formattedValue = when (currency) {
            Currency.USD -> {
                val dollars = absAmount / 100
                val cents = absAmount % 100
                val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }
                val decimalValue = BigDecimal(dollars).add(BigDecimal(cents).movePointLeft(2))
                "${currency.symbol}${numberFormat.format(decimalValue)}"
            }
            Currency.SOS -> {
                val numberFormat = NumberFormat.getNumberInstance(Locale.US)
                "${numberFormat.format(absAmount)} ${currency.symbol}"
            }
        }

        return when {
            isIncome == true && showSign -> "+ $formattedValue"
            isIncome == false && showSign -> "- $formattedValue"
            isNegative -> "- $formattedValue"
            else -> formattedValue
        }
    }

    /**
     * Parses user input text into integer minor units without floating-point inaccuracies.
     * For USD: "12.5" -> 1250L, "12.50" -> 1250L, "12" -> 1200L
     * For SOS: "25000" or "25,000" -> 25000L
     */
    fun parseInputToMinorUnits(input: String, currency: Currency): Long? {
        val sanitized = input.trim().replace(",", "").replace(" ", "")
        if (sanitized.isEmpty()) return null

        return try {
            val bd = BigDecimal(sanitized)
            if (bd < BigDecimal.ZERO) return null

            when (currency) {
                Currency.USD -> {
                    // scale to 2 decimal places, then multiply by 100
                    bd.setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal(100)).toLong()
                }
                Currency.SOS -> {
                    // whole shillings only
                    bd.setScale(0, RoundingMode.HALF_UP).toLong()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Formats minor units for raw editing input field without currency symbol.
     */
    fun formatForEditInput(amountMinor: Long, currency: Currency): String {
        if (amountMinor <= 0) return ""
        return when (currency) {
            Currency.USD -> {
                val dollars = amountMinor / 100
                val cents = amountMinor % 100
                if (cents == 0L) {
                    "$dollars"
                } else {
                    String.format(Locale.US, "%d.%02d", dollars, cents)
                }
            }
            Currency.SOS -> amountMinor.toString()
        }
    }
}

package com.example.model

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class Currency(val code: String, val symbol: String, val minorUnitDecimals: Int) {
    USD("USD", "$", 2),
    SOS("SOS", "Sh.So.", 0)
}

enum class AppLanguage(val code: String, val displayName: String) {
    SOMALI("so", "Af-Soomaali"),
    ENGLISH("en", "English")
}

enum class DateRangeFilter {
    THIS_MONTH,
    LAST_MONTH,
    ALL_TIME
}

enum class BudgetStatus {
    SAFE,
    WARNING_80,
    EXCEEDED
}

data class CategoryItem(
    val key: String,
    val type: TransactionType,
    val nameEn: String,
    val nameSo: String,
    val iconName: String,
    val colorHex: Long
)

object Categories {
    val EXPENSE_CATEGORIES = listOf(
        CategoryItem("FOOD", TransactionType.EXPENSE, "Food", "Cunto", "Restaurant", 0xFFE57373),
        CategoryItem("TRANSPORT", TransactionType.EXPENSE, "Transport", "Gaadiid", "DirectionsCar", 0xFF4FC3F7),
        CategoryItem("RENT", TransactionType.EXPENSE, "Rent", "Kiira", "Home", 0xFFFFB74D),
        CategoryItem("HEALTHCARE", TransactionType.EXPENSE, "Healthcare", "Caafimaad", "LocalHospital", 0xFF81C784),
        CategoryItem("EDUCATION", TransactionType.EXPENSE, "Education", "Waxbarasho", "School", 0xFFBA68C8),
        CategoryItem("SHOPPING", TransactionType.EXPENSE, "Shopping", "Adeeg", "ShoppingBag", 0xFFF06292),
        CategoryItem("BILLS", TransactionType.EXPENSE, "Bills", "Biilal", "Receipt", 0xFF4DB6AC),
        CategoryItem("OTHER_EXPENSE", TransactionType.EXPENSE, "Other", "Kale", "MoreHoriz", 0xFF90A4AE)
    )

    val INCOME_CATEGORIES = listOf(
        CategoryItem("SALARY", TransactionType.INCOME, "Salary", "Mushaaro", "Payments", 0xFF43A047),
        CategoryItem("BUSINESS", TransactionType.INCOME, "Business", "Ganacsi", "Storefront", 0xFF1E88E5),
        CategoryItem("INVESTMENT", TransactionType.INCOME, "Investment", "Maalgashi", "TrendingUp", 0xFF8E24AA),
        CategoryItem("GIFT", TransactionType.INCOME, "Gift", "Hadyad", "CardGiftcard", 0xFFFB8C00),
        CategoryItem("OTHER_INCOME", TransactionType.INCOME, "Other", "Kale", "AddCircleOutline", 0xFF78909C)
    )

    fun getCategory(key: String): CategoryItem {
        return (EXPENSE_CATEGORIES + INCOME_CATEGORIES).firstOrNull { it.key == key }
            ?: CategoryItem(key, TransactionType.EXPENSE, "Other", "Kale", "MoreHoriz", 0xFF90A4AE)
    }
}

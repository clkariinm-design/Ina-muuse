package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.TransactionEntity
import com.example.data.repository.TransactionRepository
import com.example.model.AppLanguage
import com.example.model.BudgetStatus
import com.example.model.Currency
import com.example.model.DateRangeFilter
import com.example.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategorySummary(
    val categoryKey: String,
    val totalAmountMinor: Long,
    val percentage: Float
)

data class DaySpending(
    val dayOfMonth: Int,
    val amountMinor: Long
)

data class MonthSpending(
    val monthLabel: String,
    val incomeMinor: Long,
    val expenseMinor: Long
)

class TransactionViewModel(
    application: Application,
    private val repository: TransactionRepository
) : AndroidViewModel(application) {

    // Preferences & Settings State
    private val _currency = MutableStateFlow(Currency.USD)
    val currency: StateFlow<Currency> = _currency.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.SOMALI)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _monthlyBudgetMinor = MutableStateFlow(50000L) // Default $500.00 USD
    val monthlyBudgetMinor: StateFlow<Long> = _monthlyBudgetMinor.asStateFlow()

    private val _themeMode = MutableStateFlow("SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Filter & Search State
    private val _filterType = MutableStateFlow<TransactionType?>(null)
    val filterType: StateFlow<TransactionType?> = _filterType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _dateRangeFilter = MutableStateFlow(DateRangeFilter.THIS_MONTH)
    val dateRangeFilter: StateFlow<DateRangeFilter> = _dateRangeFilter.asStateFlow()

    // All raw transactions from Room
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Load persisted settings
        viewModelScope.launch {
            val savedCurrency = repository.getSettingValue("currency", Currency.USD.code)
            _currency.value = if (savedCurrency == Currency.SOS.code) Currency.SOS else Currency.USD

            val savedLang = repository.getSettingValue("language", AppLanguage.SOMALI.code)
            _language.value = if (savedLang == AppLanguage.ENGLISH.code) AppLanguage.ENGLISH else AppLanguage.SOMALI

            val defaultBudget = if (_currency.value == Currency.USD) 50000L else 5000000L
            val savedBudget = repository.getSettingValue("monthly_budget_${_currency.value.code}", defaultBudget.toString())
            _monthlyBudgetMinor.value = savedBudget.toLongOrNull() ?: defaultBudget

            val savedTheme = repository.getSettingValue("theme_mode", "SYSTEM")
            _themeMode.value = savedTheme
        }
    }

    // Filtered transactions for the Transactions tab
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _currency,
        _filterType,
        _searchQuery,
        _dateRangeFilter
    ) { txList, curr, type, query, dateFilter ->
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        val (startMillis, endMillis) = when (dateFilter) {
            DateRangeFilter.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            DateRangeFilter.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            DateRangeFilter.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
        }

        txList.filter { tx ->
            val matchCurrency = tx.currencyCode == curr.code
            val matchType = type == null || tx.type == type.name
            val matchDate = tx.dateTimestamp in startMillis..endMillis
            val matchQuery = query.isBlank() || tx.description.contains(query, ignoreCase = true) ||
                    tx.categoryKey.contains(query, ignoreCase = true)
            matchCurrency && matchType && matchDate && matchQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Month Transactions for Dashboard & Reports
    private val thisMonthTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _currency
    ) { txList, curr ->
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val end = calendar.timeInMillis

        txList.filter { it.currencyCode == curr.code && it.dateTimestamp in start..end }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Total Balance across all time for selected currency
    val totalBalance: StateFlow<Long> = combine(allTransactions, _currency) { txList, curr ->
        var income = 0L
        var expense = 0L
        for (tx in txList) {
            if (tx.currencyCode == curr.code) {
                if (tx.type == TransactionType.INCOME.name) {
                    income += tx.amountMinor
                } else {
                    expense += tx.amountMinor
                }
            }
        }
        income - expense
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    // Current Month Income
    val thisMonthIncome: StateFlow<Long> = thisMonthTransactions.combine(_currency) { txList, _ ->
        txList.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amountMinor }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    // Current Month Expense
    val thisMonthExpense: StateFlow<Long> = thisMonthTransactions.combine(_currency) { txList, _ ->
        txList.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amountMinor }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    // Budget Status
    val budgetStatus: StateFlow<BudgetStatus> = combine(
        thisMonthExpense,
        _monthlyBudgetMinor
    ) { spent, limit ->
        if (limit <= 0) return@combine BudgetStatus.SAFE
        val ratio = spent.toFloat() / limit.toFloat()
        when {
            ratio >= 1.0f -> BudgetStatus.EXCEEDED
            ratio >= 0.8f -> BudgetStatus.WARNING_80
            else -> BudgetStatus.SAFE
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetStatus.SAFE
    )

    val budgetProgress: StateFlow<Float> = combine(
        thisMonthExpense,
        _monthlyBudgetMinor
    ) { spent, limit ->
        if (limit <= 0) 0f else (spent.toFloat() / limit.toFloat()).coerceAtLeast(0f)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    val budgetRemaining: StateFlow<Long> = combine(
        thisMonthExpense,
        _monthlyBudgetMinor
    ) { spent, limit ->
        limit - spent
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    // Category Breakdown for Reports
    val categorySummaries: StateFlow<List<CategorySummary>> = thisMonthTransactions.combine(_currency) { txList, _ ->
        val expenseList = txList.filter { it.type == TransactionType.EXPENSE.name }
        val totalExpense = expenseList.sumOf { it.amountMinor }
        if (totalExpense == 0L) return@combine emptyList<CategorySummary>()

        val grouped = expenseList.groupBy { it.categoryKey }
        grouped.map { (catKey, items) ->
            val catTotal = items.sumOf { it.amountMinor }
            val pct = (catTotal.toFloat() / totalExpense.toFloat()) * 100f
            CategorySummary(catKey, catTotal, pct)
        }.sortedByDescending { it.totalAmountMinor }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Daily Spending throughout current month (for Canvas Bar Chart)
    val dailySpendings: StateFlow<List<DaySpending>> = thisMonthTransactions.combine(_currency) { txList, _ ->
        val cal = Calendar.getInstance()
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dailyMap = mutableMapOf<Int, Long>()
        for (d in 1..daysInMonth) dailyMap[d] = 0L

        for (tx in txList) {
            if (tx.type == TransactionType.EXPENSE.name) {
                cal.timeInMillis = tx.dateTimestamp
                val day = cal.get(Calendar.DAY_OF_MONTH)
                dailyMap[day] = (dailyMap[day] ?: 0L) + tx.amountMinor
            }
        }
        dailyMap.toSortedMap().map { DaySpending(it.key, it.value) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Monthly Spending across last 6 months (for Reports)
    val monthlySpendings: StateFlow<List<MonthSpending>> = combine(allTransactions, _currency) { txList, curr ->
        val monthsList = mutableListOf<MonthSpending>()
        val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        for (i in 5 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)
            val monthIndex = cal.get(Calendar.MONTH)
            val label = monthNames[monthIndex]

            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis

            cal.add(Calendar.MONTH, 1)
            cal.add(Calendar.MILLISECOND, -1)
            val end = cal.timeInMillis

            val monthTxs = txList.filter { it.currencyCode == curr.code && it.dateTimestamp in start..end }
            val inc = monthTxs.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amountMinor }
            val exp = monthTxs.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amountMinor }

            monthsList.add(MonthSpending(label, inc, exp))
        }
        monthsList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Actions
    fun addTransaction(
        amountMinor: Long,
        type: TransactionType,
        categoryKey: String,
        description: String,
        dateTimestamp: Long
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    amountMinor = amountMinor,
                    type = type.name,
                    categoryKey = categoryKey,
                    description = description.trim(),
                    dateTimestamp = dateTimestamp,
                    currencyCode = _currency.value.code
                )
            )
        }
    }

    fun updateTransaction(
        id: Long,
        amountMinor: Long,
        type: TransactionType,
        categoryKey: String,
        description: String,
        dateTimestamp: Long
    ) {
        viewModelScope.launch {
            repository.updateTransaction(
                TransactionEntity(
                    id = id,
                    amountMinor = amountMinor,
                    type = type.name,
                    categoryKey = categoryKey,
                    description = description.trim(),
                    dateTimestamp = dateTimestamp,
                    currencyCode = _currency.value.code
                )
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteTransactionById(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun setCurrency(newCurrency: Currency) {
        _currency.value = newCurrency
        viewModelScope.launch {
            repository.setSetting("currency", newCurrency.code)
            val defaultBudget = if (newCurrency == Currency.USD) 50000L else 5000000L
            val savedBudget = repository.getSettingValue("monthly_budget_${newCurrency.code}", defaultBudget.toString())
            _monthlyBudgetMinor.value = savedBudget.toLongOrNull() ?: defaultBudget
        }
    }

    fun setLanguage(newLanguage: AppLanguage) {
        _language.value = newLanguage
        viewModelScope.launch {
            repository.setSetting("language", newLanguage.code)
        }
    }

    fun setMonthlyBudget(newBudgetMinor: Long) {
        _monthlyBudgetMinor.value = newBudgetMinor
        viewModelScope.launch {
            repository.setSetting("monthly_budget_${_currency.value.code}", newBudgetMinor.toString())
        }
    }

    fun setThemeMode(theme: String) {
        _themeMode.value = theme
        viewModelScope.launch {
            repository.setSetting("theme_mode", theme)
        }
    }

    fun setFilterType(type: TransactionType?) {
        _filterType.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDateRangeFilter(filter: DateRangeFilter) {
        _dateRangeFilter.value = filter
    }

    fun deleteAllData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteAllData()
            // Reset state defaults
            val defaultBudget = if (_currency.value == Currency.USD) 50000L else 5000000L
            _monthlyBudgetMinor.value = defaultBudget
            onComplete()
        }
    }

    suspend fun exportJson(): String {
        return repository.exportJson()
    }

    fun importJson(jsonContent: String, onResult: (Result<Int>) -> Unit) {
        viewModelScope.launch {
            val result = repository.importJson(jsonContent)
            if (result.isSuccess) {
                // Reload budget & settings
                val savedCurrency = repository.getSettingValue("currency", _currency.value.code)
                _currency.value = if (savedCurrency == Currency.SOS.code) Currency.SOS else Currency.USD

                val savedBudget = repository.getSettingValue("monthly_budget_${_currency.value.code}", "50000")
                _monthlyBudgetMinor.value = savedBudget.toLongOrNull() ?: 50000L
            }
            onResult(result)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(application)
                    val repo = TransactionRepository(db.transactionDao(), db.appSettingDao())
                    return TransactionViewModel(application, repo) as T
                }
            }
    }
}

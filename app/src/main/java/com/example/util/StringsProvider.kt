package com.example.util

import com.example.model.AppLanguage
import com.example.model.CategoryItem
import com.example.model.TransactionType

object AppStrings {
    // App Bar & Brand
    fun appTitle(lang: AppLanguage) = "LacagTaliye"
    fun offlineBadge(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Khad La'aan" else "Offline"

    // Navigation Tabs
    fun tabHome(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Hoyga" else "Home"
    fun tabTransactions(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Dhaqdhaqaaqa" else "Transactions"
    fun tabReports(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Warbixinta" else "Reports"
    fun tabSettings(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Hagaajinta" else "Settings"

    // Home Screen
    fun totalBalance(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Wadarta Haraaga" else "Total Balance"
    fun totalIncome(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Wadarta Dakhliga" else "Total Income"
    fun totalExpenses(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Wadarta Kharashka" else "Total Expenses"
    fun thisMonthSpending(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Kharashka Bisha" else "This Month's Spending"
    fun thisMonthIncome(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Dakhliga Bisha" else "This Month's Income"
    fun netSavings(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Kaydka Bisha" else "Month's Net Savings"
    fun recentTransactions(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Dhaqdhaqaaqii Ugu Dambeeyay" else "Recent Transactions"
    fun viewAll(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Eeg Dhammaan" else "View All"
    fun addIncome(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Ku dar Dakhli" else "Add Income"
    fun addExpense(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Ku dar Kharash" else "Add Expense"
    fun addTransaction(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Dhaqdhaqaaq Cusub" else "Add Transaction"

    // Budget
    fun monthlyBudget(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Miisaaniyadda Bisha" else "Monthly Budget"
    fun budgetLimit(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Xadka Miisaaniyadda" else "Budget Limit"
    fun spentOfBudget(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "La isticmaalay" else "Spent of"
    fun remainingBudget(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Haray" else "Remaining"
    fun budgetExceeded(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Digniin: Miisaaniyadda waa la dhaafay!" else "Alert: Budget exceeded!"
    fun budgetWarning(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Digniin: Waxaad gaartay 80%+ miisaaniyadda!" else "Warning: You've used 80%+ of your budget!"
    fun setBudgetPrompt(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Qeex xadka miisaaniyadda bisha" else "Set monthly spending limit"
    fun budgetNotSet(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Miisaaniyad lama dejin" else "No budget set"

    // Transaction Details & Form
    fun editTransaction(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Wax ka beddel" else "Edit Transaction"
    fun newIncome(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Dakhli Cusub" else "New Income"
    fun newExpense(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Kharash Cusub" else "New Expense"
    fun amountLabel(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Cadadka Lacagta" else "Amount"
    fun categoryLabel(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Qaybta" else "Category"
    fun descriptionLabel(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Faahfaahin (ikhtiyaari)" else "Description (optional)"
    fun dateLabel(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Taariikhda" else "Date"
    fun saveButton(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Keydi" else "Save"
    fun cancelButton(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Ka noqo" else "Cancel"
    fun deleteButton(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Tirtir" else "Delete"
    fun confirmDeleteTitle(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Ma hubtaa inaad tirtirto?" else "Confirm Deletion"
    fun confirmDeleteMsg(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Dhaqdhaqaaqan lama soo celin karo marka la tirtiro." else "This transaction record cannot be recovered once deleted."

    // Filters & Search
    fun searchPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Raadi faahfaahinta..." else "Search descriptions..."
    fun filterAll(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Dhammaan" else "All"
    fun filterIncome(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Dakhli" else "Income"
    fun filterExpense(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Kharash" else "Expense"
    fun filterThisMonth(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Bishan" else "This Month"
    fun filterLastMonth(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Bishii Hore" else "Last Month"
    fun filterAllTime(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Waqti Kasta" else "All Time"

    // Reports Screen
    fun reportsTitle(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Falanqaynta Maaliyadda" else "Financial Analytics"
    fun dailySpending(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Kharashka Maalmaha" else "Daily Spending"
    fun monthlySpending(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Kharashka Bilaha" else "Monthly Spending"
    fun categoryBreakdown(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Kharashyada Qayb ahaan" else "Expenses by Category"
    fun averageDailyExpense(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Celceliska Maalintii" else "Daily Average"
    fun noDataReport(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Xog laguma helin xilligan." else "No data available for this period."

    // Empty States
    fun noTransactions(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Weli wax dhaqdhaqaaq ah ma jiraan." else "No transactions recorded yet."
    fun noTransactionsSub(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Guji badhanka hoose si aad u diiwaangeliso lacag bixintaada." else "Tap the button below to record your first income or expense."

    // Settings
    fun settingsTitle(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Hagaajinta & Xogta" else "Settings & Data"
    fun languageSetting(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Luqadda" else "Language"
    fun currencySetting(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Nooca Lacagta" else "Currency"
    fun themeSetting(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Muuqaalka (Theme)" else "Appearance Theme"
    fun themeSystem(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Nidaamka Telefoonka" else "System Default"
    fun themeLight(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Iftiin (Light)" else "Light Mode"
    fun themeDark(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Madow (Dark)" else "Dark Mode"
    fun backupRestoreSection(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Keydinta & Soo Celinta Xogta" else "Backup & Restore"
    fun exportData(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Soo saar Xogta (Backup JSON)" else "Export Backup (JSON)"
    fun importData(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Soo celi Xogta (Restore JSON)" else "Restore Data (JSON)"
    fun deleteAllData(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Tirtir Dhammaan Xogta" else "Delete All Data"
    fun confirmDeleteAllTitle(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Digniin: Tirtir Dhammaan Xogta" else "Danger: Delete All Data"
    fun confirmDeleteAllMsg(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Tani waxay tirtiri doontaa dhammaan diiwaanka dakhliga, kharashka iyo miisaaniyadda. Talaabadan dib looma celin karo!" else "This will permanently delete all transaction history and budget configurations. This action cannot be undone!"

    // Validation & Messages
    fun invalidAmount(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Fadlan geli lacag sax ah oo ka weyn 0" else "Please enter a valid amount greater than 0"
    fun backupSuccess(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Xogta si guul leh ayaa loo keydiyay!" else "Backup saved successfully!"
    fun restoreSuccess(lang: AppLanguage, count: Int) = if (lang == AppLanguage.SOMALI) "Waxaa la soo celiyay $count diiwaan!" else "Successfully restored $count records!"
    fun restoreError(lang: AppLanguage) = if (lang == AppLanguage.SOMALI) "Khalad ayaa dhacay markii la soo celinayay xogta." else "Failed to restore backup file. Invalid format."

    // Category name helper
    fun categoryName(item: CategoryItem, lang: AppLanguage): String {
        return if (lang == AppLanguage.SOMALI) item.nameSo else item.nameEn
    }
}

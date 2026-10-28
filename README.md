# LacagTaliye (Personal Income & Expense Tracker)

**LacagTaliye** is a native offline Android application designed to track personal income, expenses, and monthly budgets. Built with modern Android architecture using Jetpack Compose, Material Design 3, Kotlin Coroutines & Flow, and Room Database.

## Features

- **100% Offline by Design**: Works without internet connection, accounts, or servers. No `android.permission.INTERNET` requested.
- **Precision Financial Arithmetic**: All financial calculations use 64-bit integer minor units (cents for USD, whole units for SOS) to prevent floating-point rounding errors.
- **Dual Currency Support**: Instant toggle between US Dollar (USD, `$`) and Somali Shilling (SOS, `Sh.So.`).
- **Bilingual Interface**: Native support for Somali (*Af-Soomaali*) and English, switchable instantly without restarting.
- **Interactive Dashboard**: Real-time total balance, monthly income, monthly expenses, net cash flow, recent transactions, and quick action buttons.
- **Transaction Ledger**: Filter by Income/Expense, date ranges (This Month, Last Month, All Time), and real-time description search.
- **Budget Tracking & Alert System**: Set monthly spending limits with progress indicators, 80% warning alerts, and 100%+ budget exceeded banners.
- **Native Visualizations & Reports**: Pure Jetpack Compose Canvas bar and trend charts showing daily spending, category breakdowns, and monthly trends without external network libraries.
- **Local JSON Backup & Restore**: Export and restore your complete transaction and budget history using Android's Storage Access Framework (SAF) document picker.
- **Appearance Themes**: System Default, Light Mode, and Dark Mode with the custom *Emerald Vault* color palette.

## Architecture

- **Pattern**: Model-View-ViewModel (MVVM) + Repository Pattern
- **UI Framework**: Jetpack Compose with Material Design 3
- **Local Persistence**: Room SQLite Database (`TransactionEntity`, `AppSettingEntity`, `TransactionDao`, `AppSettingDao`)
- **Reactive Streams**: Kotlin Coroutines and `StateFlow`
- **Testing**: Local JVM & Robolectric unit tests for financial calculations and minor unit formatting

## Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                  # Activity entry point, Scaffold, and bottom navigation
├── data/
│   ├── AppDatabase.kt               # Room database definition
│   ├── dao/
│   │   ├── AppSettingDao.kt         # DAO for key-value settings
│   │   └── TransactionDao.kt        # DAO for transactions with reactive Flow queries
│   ├── entity/
│   │   ├── AppSettingEntity.kt      # Settings entity
│   │   └── TransactionEntity.kt     # Transaction entity (amount in minor units)
│   └── repository/
│       └── TransactionRepository.kt # Repository abstraction and JSON backup/restore
├── model/
│   └── Models.kt                    # Domain models, Categories, Currency, and Language enums
├── ui/
│   ├── components/
│   │   ├── AddEditTransactionSheet.kt # ModalBottomSheet for recording transactions
│   │   ├── CanvasCharts.kt          # Native Canvas daily and monthly charts
│   │   ├── CategoryIcon.kt          # Category icons and colored badges
│   │   └── FinancialCards.kt        # Total balance hero card and budget progress bar
│   ├── screens/
│   │   ├── HomeScreen.kt            # Financial dashboard
│   │   ├── ReportsScreen.kt         # Analytics and visual charts
│   │   ├── SettingsScreen.kt        # Language, currency, budget, theme & backup
│   │   └── TransactionsScreen.kt    # Searchable and filterable transaction history
│   └── theme/
│       ├── Color.kt                 # Emerald Vault theme colors
│       ├── Theme.kt                 # Material 3 theme configurations
│       └── Type.kt                  # Typography scale
└── util/
    ├── CurrencyFormatter.kt         # Exact integer minor unit formatting and parsing
    └── StringsProvider.kt           # Complete Somali & English bilingual resource strings
```

## Running Unit Tests

Run local Robolectric and unit tests:
```bash
gradle :app:testDebugUnitTest
```

## Building the Debug APK

Generate the debug APK:
```bash
gradle assembleDebug
```
The resulting APK is located at:
`app/build/outputs/apk/debug/app-debug.apk`

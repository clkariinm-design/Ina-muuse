# LacagTaliye - Native Android Personal Income & Expense Tracker

A complete, production-grade native Android application for tracking personal income, expenses, and monthly budgets completely offline with zero cloud or AI dependencies. Built using Jetpack Compose, Material Design 3, Kotlin Coroutines/Flow, and Room Database with high-precision minor-unit currency calculations, bilingual support (Somali & English), native data visualizations, and local JSON backup/restore.

## User Review & Critical Decisions

> [!IMPORTANT]
> The clarifying questions were skipped by the user, so the following recommended production defaults have been adopted:

- **Confirmed Decision 1 (Currency System)**: Default currency is US Dollar (USD, `$`) with instantaneous one-tap toggle to Somali Shilling (SOS, `Sh.So.`) in Settings and on the Dashboard. Financial amounts are strictly stored as 64-bit integer minor units (cents for USD, whole integer units for SOS) to eliminate floating-point rounding errors entirely.
- **Confirmed Decision 2 (Bilingual Localization)**: Default interface language is Somali (*Af-Soomaali* - "LacagTaliye", "Dakhli", "Kharash", "Haraaga", "Miisaaniyadda") with a full English toggle and dynamic in-app locale switching that remembers user preference without requiring an app restart.
- **Confirmed Decision 3 (Budget Tracking & Warnings)**: Monthly spending limit tracks all monthly expenses, displaying a visual progress indicator. A warning status appears when spending crosses 80%, and an alert card/banner triggers when spending exceeds 100% of the limit.
- **Confirmed Decision 4 (100% Offline Integrity)**: Zero network permissions (`android.permission.INTERNET` omitted from `AndroidManifest.xml`). Data stays private and resilient in an on-device encrypted Room SQLite database with full Storage Access Framework (SAF) JSON backup and restore.

---

## 1. Overview & Core Concept

- **What It Does**: LacagTaliye empowers users to track their everyday financial health: logging earnings and spending across standard and customizable categories, monitoring current month cash flow, analyzing daily and monthly trends via native Compose charts, tracking monthly budget boundaries with active alerts, and managing their financial data with export/import tools.
- **Target Audience / Persona**: Individuals, freelancers, and household managers in Somalia and the global diaspora who require a trustworthy, private, lightning-fast financial ledger that works seamlessly in airplane mode or low/no-connectivity environments.
- **Key Value**: Immediate offline responsiveness, zero cloud surveillance or account requirements, crystal-clear Somali & English terminology, and precision arithmetic preventing any lost cent or shilling.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **First Launch & Overview (Home Dashboard)**:
   - High-contrast balance card showing Total Balance, Monthly Income, Monthly Expense, and Net Cash Flow.
   - Quick Budget Health Bar with % consumed and remaining budget.
   - Quick Action buttons: "+ Dakhli" (Income) and "- Kharash" (Expense).
   - Recent Transactions list with category badges, amounts color-coded (emerald green for income, coral red for expense), and relative dates.
2. **Transaction Entry & Editing (Income / Expense BottomSheet / Screen)**:
   - Numerical keypad entry with live currency prefix/suffix.
   - Category picker chips with intuitive iconography (Food/Cunto, Transport/Gaadiid, Rent/Kiira, Healthcare/Caafimaad, Education/Waxbarasho, Shopping/Adeeg, Bills/Biilal, Salary/Mushaaro, Business/Ganacsi, Other/Kale).
   - Date picker defaulting to today, with custom calendar selection.
   - Description note field with instant validation (prevents 0 or empty amounts).
3. **Transaction History & Filter Hub (Transactions Tab)**:
   - Search bar filtering descriptions and notes in real time.
   - Filter chips: All, Income Only, Expense Only.
   - Date range selector (This Month, Last Month, Custom Date Range).
   - Swipe or tap to Edit / Delete with undo or confirmation dialog.
4. **Visual Reports & Analytics (Reports Tab)**:
   - Monthly and daily expense trends rendered using pure Jetpack Compose Canvas (no external network-dependent chart libraries).
   - Category breakdown doughnut/bar chart showing percentage distribution.
   - Financial summaries: Total Income, Total Expenses, Net Savings Rate, and Daily Average Spending.
5. **Budget Management (Integrated in Home & Dedicated Modal/Settings)**:
   - Set monthly spending threshold in chosen currency.
   - Dynamic alert levels (Normal < 80%, Warning 80–99%, Over Budget ≥ 100%).
6. **Settings & Data Sovereignty (Settings Tab)**:
   - Language selector: Somali (*Af-Soomaali*) / English.
   - Currency selector: USD (`$`) / SOS (`Sh.So.`).
   - Theme toggle: System Default, Light Mode, Dark Mode.
   - Backup & Restore: Export all transactions & budget settings to a timestamped JSON file via Android's document picker; import & restore with schema integrity check.
   - "Delete All Data" action guarded with an explicit confirmation dialog.

### Visual Identity & Theme
- **Theme Direction**: "Emerald Vault" — A prestigious, calming financial aesthetic combining deep forest emerald tones, warm charcoal surfaces, and vibrant semantic accents.
  - *Primary*: Deep Emerald (`#0A6847` / `#38E54D` in dark mode).
  - *Secondary / Accent*: Warm Mint (`#7ABA78` / `#22C55E`).
  - *Income Accent*: Emerald Green (`#16A34A`).
  - *Expense Accent*: Rose Crimson (`#E11D48`).
  - *Backgrounds*: Pristine Off-White (`#F8FAFC`) in Light Mode; Slate Obsidian (`#0F172A`) in Dark Mode.
  - *Surfaces & Cards*: Clean Elevated Cards with subtle M3 tonal borders (`#E2E8F0` / `#1E293B`).
- **Typography & Layout**:
  - Crisp M3 type scales with tabular numerals (`FontFeatureSettings = "tnum"`) for financial figures to align numbers cleanly.
  - Generous touch targets (minimum 48dp on all interactive chips, buttons, and list rows).
  - Edge-to-edge safe area handling (`safeDrawingPadding`, `navigationBarsPadding`).

---

## 3. Key Product Decisions & Trade-Offs

- **Integer Minor Units (Long) vs Floating-Point (Double)**:
  - *Chosen*: Financial calculations store amounts as `Long` in minor units (e.g. cents: `$10.50` = `1050L`; SOS `25,000` = `25000L`).
  - *Why*: Eliminates IEEE 754 floating point inaccuracies where `0.1 + 0.2 != 0.3`. Formatter utility converts to display string based on currency rules.
- **Local JSON Backup via Storage Access Framework (SAF)**:
  - *Chosen*: Use Android's native `ActivityResultContracts.CreateDocument` and `OpenDocument` to export/import JSON files.
  - *Why*: Zero runtime storage permissions needed (`READ_EXTERNAL_STORAGE` is not needed on Android 10+ with SAF). Users have 100% control over where backup files live (Downloads, Google Drive, SD card).
- **In-App Dynamic Localization (Somali & English)**:
  - *Chosen*: A localized string repository/resource provider backed by DataStore/Room preferences, with full string coverage in both Somali and English.
  - *Why*: Allows instantaneous language switching inside the app settings without changing the entire Android OS language.
- **Pure Jetpack Compose Canvas Visualizations**:
  - *Chosen*: Native Compose `Canvas` drawing for category distribution bar charts, circular progress rings, and trend lines.
  - *Why*: Guarantees 100% offline capability, zero third-party webview or heavy chart library overhead, smooth 60fps animations, and perfect theme adaptability.

---

## 4. Technical Architecture & Data Strategy

### System Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                          LacagTaliye Android App                       │
│                                                                        │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    Jetpack Compose UI Layer                      │  │
│  │  ┌───────────────┐ ┌───────────────┐ ┌────────────┐ ┌─────────┐  │  │
│  │  │  HomeScreen   │ │ Transactions  │ │  Reports   │ │Settings │  │  │
│  │  │  (Dashboard,  │ │  (List, Date  │ │ (Analytics,│ │ (Lang,  │  │  │
│  │  │   Budget Bar) │ │  Filter, Sort)│ │   Charts)  │ │ Backup) │  │  │
│  │  └───────┬───────┘ └───────┬───────┘ └─────┬──────┘ └────┬────┘  │  │
│  │          │                 │               │             │       │  │
│  │  ┌───────▼─────────────────▼───────────────▼─────────────▼────┐  │  │
│  │  │               TransactionBottomSheet / Dialogs             │  │  │
│  │  └─────────────────────────┬──────────────────────────────────┘  │  │
│  └────────────────────────────┼─────────────────────────────────────┘  │
│                               │ StateFlow / Events                     │
│  ┌────────────────────────────▼─────────────────────────────────────┐  │
│  │            TransactionViewModel & SettingsViewModel              │  │
│  │  - Currency & Language state                                     │  │
│  │  - Budget progress & alert calculation                           │  │
│  │  - Filter queries & search state                                 │  │
│  │  - Backup / Restore orchestration                                │  │
│  └────────────────────────────┬─────────────────────────────────────┘  │
│                               │ Kotlin Coroutines & Flow               │
│  ┌────────────────────────────▼─────────────────────────────────────┐  │
│  │                     TransactionRepository                        │  │
│  │  - TransactionRepositoryImpl                                     │  │
│  │  - PreferencesRepository (Currency, Language, Monthly Budget)    │  │
│  └────────────────────────────┬─────────────────────────────────────┘  │
│                               │                                        │
│  ┌────────────────────────────▼─────────────────────────────────────┐  │
│  │                  Room Database (Offline SQLite)                  │  │
│  │  ┌────────────────────────┐  ┌────────────────────────────────┐  │  │
│  │  │  TransactionEntity     │  │  BudgetEntity / Preferences    │  │  │
│  │  ├────────────────────────┤  ├────────────────────────────────┤  │  │
│  │  │ id: Long (PK autoGen)  │  │ key: String (PK)               │  │  │
│  │  │ amountMinor: Long      │  │ value: String                  │  │  │
│  │  │ type: TransactionType  │  └────────────────────────────────┘  │  │
│  │  │ category: String       │  ┌────────────────────────────────┐  │  │
│  │  │ description: String    │  │  TransactionDao                │  │  │
│  │  │ timestamp: Long        │  │  Flow<List<TransactionEntity>> │  │  │
│  │  │ currency: String       │  └────────────────────────────────┘  │  │
│  │  └────────────────────────┘                                      │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Model & Entities
- `TransactionEntity`:
  - `id: Long = 0` (Primary Key, autogenerate)
  - `amountMinor: Long` (e.g. 1500 for $15.00)
  - `type: String` ("INCOME" or "EXPENSE")
  - `category: String` (e.g. "FOOD", "TRANSPORT", "RENT", "SALARY", etc.)
  - `description: String` (user note)
  - `dateTimestamp: Long` (epoch millis of transaction)
  - `currencyCode: String` ("USD" or "SOS")
- `AppSettingEntity` (Key-Value persistence for budget limit, language code, currency code, theme mode).
- `TransactionDao`:
  - Reactive queries returning `Flow<List<TransactionEntity>>`.
  - Filter queries by type, date range, search query.
  - Insert, update, delete, clear all.
- `AppDatabase`: Room database with schema versioning.

### Testing Strategy
- Unit tests (`ExampleRobolectricTest.kt` & JVM tests):
  - Currency conversion and minor unit formatting test (USD cents vs SOS integer formatting).
  - Balance, income, expense, and savings rate calculations.
  - Monthly budget limit and 80%/100% threshold alert calculations.
  - JSON backup serialization and deserialization integrity.

---

## 5. Execution Steps Once Approved
1. Update `metadata.json` and `strings.xml` to set official app name `LacagTaliye`.
2. Generate custom adaptive launcher icon for LacagTaliye (emerald theme with currency/wallet emblem).
3. Enable `androidx.navigation.compose` in `app/build.gradle.kts`.
4. Create data layer: `TransactionEntity`, `AppSettingEntity`, `TransactionDao`, `AppDatabase`, `TransactionRepository`.
5. Create localization engine: Somali & English bilingual resource manager and dictionary.
6. Create ViewModel: `TransactionViewModel` handling currency, budget, filtering, reports, and SAF backup/restore.
7. Build Jetpack Compose UI components:
   - Navigation scaffolding & M3 BottomBar.
   - Dashboard (Home) with Balance Cards, Budget Progress Ring, Recent Transactions.
   - Add/Edit Transaction BottomSheet with category chips and numeric validation.
   - Transactions Screen with search, filters (Income/Expense/All), and date selector.
   - Reports Screen with native Canvas charts (Monthly/Daily spending, Category pie/bar).
   - Settings Screen with Language, Currency, Theme, Backup/Restore, and Reset dialog.
8. Add Robolectric/JVM unit tests covering all financial calculations.
9. Verify compilation via `compile_applet` and ensure debug APK is created.

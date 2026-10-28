package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.TransactionEntity
import com.example.model.TransactionType
import com.example.ui.components.AddEditTransactionSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.LacagTaliyeTheme
import com.example.util.AppStrings
import com.example.viewmodel.TransactionViewModel

sealed class AppTab(val route: String) {
    data object Home : AppTab("home")
    data object Transactions : AppTab("transactions")
    data object Reports : AppTab("reports")
    data object Settings : AppTab("settings")
}

data class NavItem(
    val tab: AppTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val titleRes: (com.example.model.AppLanguage) -> String,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: TransactionViewModel = viewModel(
                factory = TransactionViewModel.provideFactory(application)
            )
            LacagTaliyeApp(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LacagTaliyeApp(viewModel: TransactionViewModel) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf<AppTab>(AppTab.Home) }
    var showAddEditSheet by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var initialTransactionType by remember { mutableStateOf(TransactionType.EXPENSE) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    val navItems = listOf(
        NavItem(
            tab = AppTab.Home,
            selectedIcon = Icons.Default.Home,
            unselectedIcon = Icons.Outlined.Home,
            titleRes = { AppStrings.tabHome(it) },
            testTag = "nav_home"
        ),
        NavItem(
            tab = AppTab.Transactions,
            selectedIcon = Icons.AutoMirrored.Filled.List,
            unselectedIcon = Icons.AutoMirrored.Outlined.List,
            titleRes = { AppStrings.tabTransactions(it) },
            testTag = "nav_transactions"
        ),
        NavItem(
            tab = AppTab.Reports,
            selectedIcon = Icons.Default.BarChart,
            unselectedIcon = Icons.Outlined.BarChart,
            titleRes = { AppStrings.tabReports(it) },
            testTag = "nav_reports"
        ),
        NavItem(
            tab = AppTab.Settings,
            selectedIcon = Icons.Default.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            titleRes = { AppStrings.tabSettings(it) },
            testTag = "nav_settings"
        )
    )

    LacagTaliyeTheme(themeMode = themeMode) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentTab == item.tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = item.tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.titleRes(language),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.titleRes(language),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            },
            floatingActionButton = {
                if (currentTab == AppTab.Home || currentTab == AppTab.Transactions) {
                    FloatingActionButton(
                        onClick = {
                            transactionToEdit = null
                            initialTransactionType = TransactionType.EXPENSE
                            showAddEditSheet = true
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("add_transaction_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = AppStrings.addTransaction(language),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.Home -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToTransactions = { currentTab = AppTab.Transactions },
                        onAddTransactionClick = { type ->
                            transactionToEdit = null
                            initialTransactionType = type
                            showAddEditSheet = true
                        },
                        onEditTransactionClick = { tx ->
                            transactionToEdit = tx
                            showAddEditSheet = true
                        }
                    )
                    AppTab.Transactions -> TransactionsScreen(
                        viewModel = viewModel,
                        onEditTransactionClick = { tx ->
                            transactionToEdit = tx
                            showAddEditSheet = true
                        }
                    )
                    AppTab.Reports -> ReportsScreen(
                        viewModel = viewModel
                    )
                    AppTab.Settings -> SettingsScreen(
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState
                    )
                }
            }

            // Add/Edit Transaction Bottom Sheet
            if (showAddEditSheet) {
                AddEditTransactionSheet(
                    sheetState = sheetState,
                    transactionToEdit = transactionToEdit,
                    initialType = initialTransactionType,
                    currency = currency,
                    language = language,
                    onDismiss = {
                        showAddEditSheet = false
                        transactionToEdit = null
                    },
                    onSave = { id, amountMinor, type, categoryKey, description, dateTimestamp ->
                        if (id != null) {
                            viewModel.updateTransaction(
                                id = id,
                                amountMinor = amountMinor,
                                type = type,
                                categoryKey = categoryKey,
                                description = description,
                                dateTimestamp = dateTimestamp
                            )
                        } else {
                            viewModel.addTransaction(
                                amountMinor = amountMinor,
                                type = type,
                                categoryKey = categoryKey,
                                description = description,
                                dateTimestamp = dateTimestamp
                            )
                        }
                    },
                    onDelete = { tx ->
                        viewModel.deleteTransaction(tx)
                    }
                )
            }
        }
    }
}

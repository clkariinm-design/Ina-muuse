package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppLanguage
import com.example.model.Currency
import com.example.ui.theme.ExpenseRed
import com.example.util.AppStrings
import com.example.util.CurrencyFormatter
import com.example.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun SettingsScreen(
    viewModel: TransactionViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudgetMinor.collectAsStateWithLifecycle()

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInputText by remember { mutableStateOf("") }
    var budgetErrorText by remember { mutableStateOf<String?>(null) }

    // Backup SAF CreateDocument launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val jsonString = viewModel.exportJson()
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                    }
                    snackbarHostState.showSnackbar(AppStrings.backupSuccess(language))
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Backup error: ${e.message}")
                }
            }
        }
    }

    // Restore SAF OpenDocument launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val stringBuilder = StringBuilder()
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                            var line: String? = reader.readLine()
                            while (line != null) {
                                stringBuilder.append(line)
                                line = reader.readLine()
                            }
                        }
                    }
                    val jsonContent = stringBuilder.toString()
                    viewModel.importJson(jsonContent) { result ->
                        scope.launch {
                            if (result.isSuccess) {
                                val count = result.getOrNull() ?: 0
                                snackbarHostState.showSnackbar(AppStrings.restoreSuccess(language, count))
                            } else {
                                snackbarHostState.showSnackbar(AppStrings.restoreError(language))
                            }
                        }
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(AppStrings.restoreError(language))
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = AppStrings.settingsTitle(language),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Language Setting Card
        item {
            SettingsCard(title = AppStrings.languageSetting(language), icon = Icons.Default.Language) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setLanguage(AppLanguage.SOMALI) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == AppLanguage.SOMALI,
                            onClick = { viewModel.setLanguage(AppLanguage.SOMALI) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Af-Soomaali (Somali)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (language == AppLanguage.SOMALI) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setLanguage(AppLanguage.ENGLISH) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == AppLanguage.ENGLISH,
                            onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "English",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (language == AppLanguage.ENGLISH) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Currency Setting Card
        item {
            SettingsCard(title = AppStrings.currencySetting(language), icon = Icons.Default.AttachMoney) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setCurrency(Currency.USD) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currency == Currency.USD,
                            onClick = { viewModel.setCurrency(Currency.USD) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "US Dollar (USD - $)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (currency == Currency.USD) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setCurrency(Currency.SOS) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currency == Currency.SOS,
                            onClick = { viewModel.setCurrency(Currency.SOS) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Somali Shilling (SOS - Sh.So.)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (currency == Currency.SOS) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Monthly Budget Setting Card
        item {
            SettingsCard(title = AppStrings.monthlyBudget(language), icon = Icons.Default.Payments) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            budgetInputText = CurrencyFormatter.formatForEditInput(monthlyBudget, currency)
                            budgetErrorText = null
                            showBudgetDialog = true
                        }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.budgetLimit(language),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = CurrencyFormatter.format(monthlyBudget, currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Theme Setting Card
        item {
            SettingsCard(title = AppStrings.themeSetting(language), icon = Icons.Default.Brightness4) {
                val themes = listOf(
                    Pair("SYSTEM", AppStrings.themeSystem(language)),
                    Pair("LIGHT", AppStrings.themeLight(language)),
                    Pair("DARK", AppStrings.themeDark(language))
                )
                Column {
                    themes.forEachIndexed { index, (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setThemeMode(key) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == key,
                                onClick = { viewModel.setThemeMode(key) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (themeMode == key) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        if (index < themes.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Backup & Restore Section
        item {
            SettingsCard(title = AppStrings.backupRestoreSection(language), icon = Icons.Default.Download) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Export JSON Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                exportLauncher.launch("lacagtaliye_backup_${System.currentTimeMillis()}.json")
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = AppStrings.exportData(language),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Import JSON Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                importLauncher.launch(arrayOf("application/json"))
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = AppStrings.importData(language),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Danger Zone: Delete All Data
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeleteAllDialog = true }
                    .testTag("delete_all_data_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = ExpenseRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = AppStrings.deleteAllData(language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                        Text(
                            text = AppStrings.confirmDeleteAllTitle(language),
                            fontSize = 12.sp,
                            color = ExpenseRed.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Budget Edit Dialog
    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text(AppStrings.monthlyBudget(language)) },
            text = {
                Column {
                    Text(
                        text = AppStrings.setBudgetPrompt(language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = budgetInputText,
                        onValueChange = {
                            budgetInputText = it
                            budgetErrorText = null
                        },
                        label = { Text(AppStrings.budgetLimit(language)) },
                        prefix = { Text("${currency.symbol} ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = budgetErrorText != null,
                        supportingText = budgetErrorText?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = CurrencyFormatter.parseInputToMinorUnits(budgetInputText, currency)
                        if (parsed == null || parsed <= 0L) {
                            budgetErrorText = AppStrings.invalidAmount(language)
                            return@Button
                        }
                        viewModel.setMonthlyBudget(parsed)
                        showBudgetDialog = false
                    }
                ) {
                    Text(AppStrings.saveButton(language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text(AppStrings.cancelButton(language))
                }
            }
        )
    }

    // Delete All Confirmation Dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text(
                    text = AppStrings.confirmDeleteAllTitle(language),
                    color = ExpenseRed,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = AppStrings.confirmDeleteAllMsg(language),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllData {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (language == AppLanguage.SOMALI) "Xogta oo dhan waa la tirtiray." else "All data successfully deleted."
                                )
                            }
                        }
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text(AppStrings.deleteButton(language), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text(AppStrings.cancelButton(language))
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

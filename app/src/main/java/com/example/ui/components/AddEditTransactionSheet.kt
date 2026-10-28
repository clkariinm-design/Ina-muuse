package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TransactionEntity
import com.example.model.AppLanguage
import com.example.model.Categories
import com.example.model.CategoryItem
import com.example.model.Currency
import com.example.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.AppStrings
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
    sheetState: SheetState,
    transactionToEdit: TransactionEntity?,
    initialType: TransactionType = TransactionType.EXPENSE,
    currency: Currency,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (
        id: Long?,
        amountMinor: Long,
        type: TransactionType,
        categoryKey: String,
        description: String,
        dateTimestamp: Long
    ) -> Unit,
    onDelete: ((TransactionEntity) -> Unit)? = null
) {
    val isEdit = transactionToEdit != null
    var selectedType by remember {
        mutableStateOf(
            if (isEdit) {
                if (transactionToEdit?.type == TransactionType.INCOME.name) TransactionType.INCOME else TransactionType.EXPENSE
            } else initialType
        )
    }

    var amountText by remember {
        mutableStateOf(
            if (isEdit && transactionToEdit != null) {
                CurrencyFormatter.formatForEditInput(transactionToEdit.amountMinor, currency)
            } else ""
        )
    }

    var selectedCategoryKey by remember {
        mutableStateOf(
            if (isEdit && transactionToEdit != null) {
                transactionToEdit.categoryKey
            } else {
                if (selectedType == TransactionType.INCOME) "SALARY" else "FOOD"
            }
        )
    }

    var descriptionText by remember {
        mutableStateOf(transactionToEdit?.description ?: "")
    }

    var selectedDateMillis by remember {
        mutableLongStateOf(transactionToEdit?.dateTimestamp ?: System.currentTimeMillis())
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance().apply { timeInMillis = selectedDateMillis } }
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US) }

    val categoriesList = if (selectedType == TransactionType.INCOME) {
        Categories.INCOME_CATEGORIES
    } else {
        Categories.EXPENSE_CATEGORIES
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEdit) AppStrings.editTransaction(language)
                    else if (selectedType == TransactionType.INCOME) AppStrings.newIncome(language)
                    else AppStrings.newExpense(language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Income / Expense Toggle Segment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                // Income Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedType == TransactionType.INCOME) IncomeGreen else Color.Transparent)
                        .clickable {
                            selectedType = TransactionType.INCOME
                            if (Categories.INCOME_CATEGORIES.none { it.key == selectedCategoryKey }) {
                                selectedCategoryKey = "SALARY"
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.filterIncome(language),
                        fontWeight = FontWeight.Bold,
                        color = if (selectedType == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Expense Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedType == TransactionType.EXPENSE) ExpenseRed else Color.Transparent)
                        .clickable {
                            selectedType = TransactionType.EXPENSE
                            if (Categories.EXPENSE_CATEGORIES.none { it.key == selectedCategoryKey }) {
                                selectedCategoryKey = "FOOD"
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.filterExpense(language),
                        fontWeight = FontWeight.Bold,
                        color = if (selectedType == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Input Field
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    errorMessage = null
                },
                label = { Text(AppStrings.amountLabel(language)) },
                prefix = {
                    Text(
                        text = "${currency.symbol} ",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = errorMessage != null,
                supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Picker
            Text(
                text = AppStrings.categoryLabel(language),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoriesList.forEach { category ->
                    val isSelected = category.key == selectedCategoryKey
                    val catColor = Color(category.colorHex)
                    val catName = AppStrings.categoryName(category, language)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) catColor.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { selectedCategoryKey = category.key }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryIcon(categoryKey = category.key, size = 26.dp, iconSize = 16.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = catName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description / Note Input
            OutlinedTextField(
                value = descriptionText,
                onValueChange = { descriptionText = it },
                label = { Text(AppStrings.descriptionLabel(language)) },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable {
                        calendar.timeInMillis = selectedDateMillis
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                selectedDateMillis = calendar.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = AppStrings.dateLabel(language),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFormatter.format(Date(selectedDateMillis)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isEdit && transactionToEdit != null && onDelete != null) {
                    OutlinedButton(
                        onClick = {
                            onDelete(transactionToEdit)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("delete_transaction_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.deleteButton(language))
                    }
                }

                Button(
                    onClick = {
                        val parsedMinor = CurrencyFormatter.parseInputToMinorUnits(amountText, currency)
                        if (parsedMinor == null || parsedMinor <= 0L) {
                            errorMessage = AppStrings.invalidAmount(language)
                            return@Button
                        }
                        onSave(
                            transactionToEdit?.id,
                            parsedMinor,
                            selectedType,
                            selectedCategoryKey,
                            descriptionText,
                            selectedDateMillis
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == TransactionType.INCOME) IncomeGreen else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(2f)
                        .testTag("save_transaction_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.saveButton(language),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

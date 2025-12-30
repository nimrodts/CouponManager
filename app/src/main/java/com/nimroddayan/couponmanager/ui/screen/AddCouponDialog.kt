package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nimroddayan.couponmanager.data.model.Category
import com.nimroddayan.couponmanager.ui.viewmodel.CategoryViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.CouponViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCouponDialog(
    categoryViewModel: CategoryViewModel,
    couponViewModel: CouponViewModel,
    onAddCoupon: (String, Double, Long, Long) -> Unit,
    onDismiss: () -> Unit,
    onAddCategory: () -> Unit
) {
    val categories by categoryViewModel.allCategories.collectAsState()
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var expiration by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    val parsedCoupon by couponViewModel.parsedCoupon.collectAsState()
    val isLoading by couponViewModel.isLoading.collectAsState()
    val error by couponViewModel.error.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(parsedCoupon) {
        parsedCoupon?.let { coupon ->
            name = coupon.storeName ?: name
            value = coupon.initialValue?.toString() ?: value
            coupon.expirationDate?.let {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                expiration = sdf.parse(it)?.time
            }
            couponViewModel.clearParsedCoupon()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Add Coupon", style = MaterialTheme.typography.titleLarge)

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
                }

                error?.let { errorMessage ->
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { couponViewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }

                Button(
                    onClick = {
                        clipboardManager.getText()?.text?.let { text ->
                            coroutineScope.launch {
                                couponViewModel.autofillFromClipboard(text)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("✨ Auto-fill from Clipboard")
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Coupon Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Initial Value") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box {
                    OutlinedTextField(
                        value = expiration?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) } ?: "",
                        onValueChange = {},
                        label = { Text("Expiration Date") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = { 
                                datePickerState.selectedDateMillis?.let { date ->
                                    val timeZone = TimeZone.getDefault()
                                    val offset = timeZone.getOffset(date)
                                    expiration = date + offset
                                }
                                showDatePicker = false 
                            }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState,
                            title = { Text("Select date", modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)) },
                            headline = { 
                                datePickerState.selectedDateMillis?.let {
                                    DatePickerDefaults.DatePickerHeadline(
                                        selectedDateMillis = it,
                                        displayMode = datePickerState.displayMode,
                                        dateFormatter = DatePickerDefaults.dateFormatter(),
                                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp)
                                    )
                                }
                            },
                            showModeToggle = true
                        )
                    }
                }

                if (categories.isEmpty()) {
                    TextButton(onClick = onAddCategory) {
                        Text("No categories found. Add a new one?")
                    }
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            readOnly = true,
                            value = selectedCategory?.name ?: "",
                            onValueChange = {},
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        expiration?.let { exp ->
                            selectedCategory?.let { category ->
                                onAddCoupon(name, value.toDouble(), exp, category.id)
                            }
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedCategory != null && expiration != null
                ) {
                    Text("Save")
                }
            }
        }
    }
}

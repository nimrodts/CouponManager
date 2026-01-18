package com.nimroddayan.clipit.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nimroddayan.clipit.data.model.Currency

@Composable
fun CurrencySelectionDialog(
        currentCurrencyCode: String,
        onCurrencySelected: (Currency) -> Unit,
        onDismiss: () -> Unit
) {
    val selectedCurrency = remember { mutableStateOf(Currency.fromCode(currentCurrencyCode)) }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Currency") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Currency.entries.forEach { currency ->
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(4.dp)
                        ) {
                            RadioButton(
                                    selected = currency == selectedCurrency.value,
                                    onClick = { selectedCurrency.value = currency }
                            )
                            Text(
                                    text = "${currency.displayName} (${currency.symbol})",
                                    modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                        onClick = {
                            onCurrencySelected(selectedCurrency.value)
                            onDismiss()
                        }
                ) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

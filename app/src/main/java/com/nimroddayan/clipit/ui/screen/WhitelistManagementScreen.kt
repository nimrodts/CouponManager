package com.nimroddayan.clipit.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.nimroddayan.clipit.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitelistManagementScreen(viewModel: SettingsViewModel, onNavigateUp: () -> Unit) {
    val whitelist by viewModel.smsSenderWhitelist.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("SMS Sender Whitelist") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateUp) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                )
                            }
                        }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Sender")
                }
            }
    ) { innerPadding ->
        if (whitelist.isEmpty()) {
            Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = "No allowed senders yet.\nTap + to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(whitelist.toList().sorted()) { sender ->
                    ListItem(
                            headlineContent = { Text(sender) },
                            trailingContent = {
                                IconButton(
                                        onClick = { viewModel.removeSenderFromWhitelist(sender) }
                                ) {
                                    Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAddDialog) {
        var newSender by remember { mutableStateOf("") }
        AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Sender ID") },
                text = {
                    OutlinedTextField(
                            value = newSender,
                            onValueChange = { newSender = it },
                            label = { Text("Sender Name/Number") },
                            singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                            onClick = {
                                if (newSender.isNotBlank()) {
                                    viewModel.addSenderToWhitelist(newSender.trim())
                                    showAddDialog = false
                                }
                            }
                    ) { Text("Add") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
        )
    }
}

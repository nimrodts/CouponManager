package com.nimroddayan.clipit.ui.screen

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.nimroddayan.clipit.data.DatabaseManager
import com.nimroddayan.clipit.data.GoogleDriveManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseSettingsScreen(
        onNavigateUp: () -> Unit,
        onResetDatabase: () -> Unit,
) {
        var showResetConfirmationDialog by remember { mutableStateOf(false) }
        var showRestoreConfirmationDialog by remember { mutableStateOf(false) } // New confirmation
        var showImportSuccessDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val databaseManager = remember { DatabaseManager(context) }

        val scope = rememberCoroutineScope()
        val googleDriveManager = remember { GoogleDriveManager(context) }
        var signedInAccount by remember { mutableStateOf<GoogleSignInAccount?>(null) }
        var lastBackupTime by remember { mutableStateOf<Long?>(null) } // Store last backup time
        var isBackupLoading by remember { mutableStateOf(false) }
        var isRestoreLoading by remember { mutableStateOf(false) }
        var showSignOutConfirmationDialog by remember { mutableStateOf(false) }
        var statusMessage by remember { mutableStateOf<String?>(null) }
        var showErrorDialog by remember { mutableStateOf(false) }
        var errorDialogMessage by remember { mutableStateOf("") }

        // Formatting date
        val dateFormat = remember { java.text.SimpleDateFormat.getDateTimeInstance() }

        // Fetch metadata when account key changes
        LaunchedEffect(signedInAccount) {
                if (signedInAccount != null) {
                        try {
                                lastBackupTime =
                                        googleDriveManager.getBackupMetadata(signedInAccount!!)
                        } catch (e: Exception) {
                                e.printStackTrace()
                        }
                }
        }

        val signInLauncher =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                                signedInAccount = task.getResult(ApiException::class.java)
                        } catch (e: ApiException) {
                                e.printStackTrace()
                                val errorDescription =
                                        when (e.statusCode) {
                                                7 ->
                                                        "Network error - check your internet connection"
                                                8 -> "Internal error - please try again"
                                                10 ->
                                                        "Developer error - SHA-1 fingerprint may not be configured in Google Cloud Console"
                                                12 -> "Sign-in required - please sign in again"
                                                12500 ->
                                                        "Sign-in failed - Google Play Services error"
                                                12501 -> "Sign-in cancelled by user"
                                                12502 -> "Sign-in already in progress"
                                                else -> "Unknown error"
                                        }
                                statusMessage =
                                        "Sign-in failed (${e.statusCode}): $errorDescription"
                                errorDialogMessage =
                                        "Sign-in failed (${e.statusCode})\n\n$errorDescription"
                                showErrorDialog = true
                        }
                }

        LaunchedEffect(Unit) { signedInAccount = googleDriveManager.getLastSignedInAccount() }

        val exportLauncher =
                rememberLauncherForActivityResult(
                        contract =
                                ActivityResultContracts.CreateDocument("application/octet-stream"),
                        onResult = { uri -> uri?.let { databaseManager.exportDatabase(it) } }
                )

        val importLauncher =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent(),
                        onResult = { uri ->
                                if (uri != null) {
                                        if (databaseManager.importDatabase(uri)) {
                                                showImportSuccessDialog = true
                                        }
                                }
                        }
                )

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text("Database Settings") },
                                navigationIcon = {
                                        IconButton(onClick = onNavigateUp) {
                                                Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                )
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.background,
                                                titleContentColor =
                                                        MaterialTheme.colorScheme.primary,
                                                navigationIconContentColor =
                                                        MaterialTheme.colorScheme.primary,
                                                actionIconContentColor =
                                                        MaterialTheme.colorScheme.primary
                                        )
                        )
                }
        ) {
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(it)
                                        .background(MaterialTheme.colorScheme.background)
                ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                                // Google Drive Section
                                Text(
                                        text = "Full Cloud Backup (Google Drive)",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        color = MaterialTheme.colorScheme.primary
                                )
                                ElevatedCard(
                                        colors =
                                                CardDefaults.elevatedCardColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.surface
                                                ),
                                        elevation = CardDefaults.elevatedCardElevation(2.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                                ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                                if (signedInAccount == null) {
                                                        SettingsItem(
                                                                icon = Icons.Filled.Cloud,
                                                                title = "Connect Google Drive",
                                                                onClick = {
                                                                        signInLauncher.launch(
                                                                                googleDriveManager
                                                                                        .getSignInClient()
                                                                                        .signInIntent
                                                                        )
                                                                }
                                                        )
                                                } else {
                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Default
                                                                                        .AccountCircle,
                                                                        contentDescription = null,
                                                                        tint =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary,
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        end = 8.dp
                                                                                )
                                                                )
                                                                Column(
                                                                        modifier =
                                                                                Modifier.weight(1f)
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        signedInAccount
                                                                                                ?.email
                                                                                                ?: "Unknown Account",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .bodyLarge,
                                                                                fontWeight =
                                                                                        androidx.compose
                                                                                                .ui
                                                                                                .text
                                                                                                .font
                                                                                                .FontWeight
                                                                                                .Medium
                                                                        )
                                                                        Text(
                                                                                text =
                                                                                        "Google Drive Connected",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .bodySmall,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .onSurfaceVariant
                                                                        )
                                                                }
                                                                IconButton(
                                                                        onClick = {
                                                                                showSignOutConfirmationDialog =
                                                                                        true
                                                                        }
                                                                ) {
                                                                        Icon(
                                                                                imageVector =
                                                                                        Icons.AutoMirrored
                                                                                                .Filled
                                                                                                .ExitToApp,
                                                                                contentDescription =
                                                                                        "Sign Out",
                                                                                tint =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .error
                                                                        )
                                                                }
                                                        }

                                                        HorizontalDivider(
                                                                modifier =
                                                                        Modifier.padding(
                                                                                vertical = 12.dp
                                                                        )
                                                        )

                                                        // Status Display
                                                        if (lastBackupTime != null) {
                                                                Text(
                                                                        text =
                                                                                "Last Backup: ${dateFormat.format(java.util.Date(lastBackupTime!!))}",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .bodyMedium,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary,
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        bottom =
                                                                                                12.dp
                                                                                )
                                                                )
                                                        } else {
                                                                Text(
                                                                        text =
                                                                                "No backup found in Drive.",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .bodyMedium,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .secondary,
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        bottom =
                                                                                                12.dp
                                                                                )
                                                                )
                                                        }

                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically,
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(8.dp),
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Button(
                                                                        onClick = {
                                                                                scope.launch {
                                                                                        isBackupLoading =
                                                                                                true
                                                                                        try {
                                                                                                googleDriveManager
                                                                                                        .uploadBackup(
                                                                                                                signedInAccount!!
                                                                                                        )
                                                                                                statusMessage =
                                                                                                        "Backup successful"
                                                                                                // Refresh metadata
                                                                                                lastBackupTime =
                                                                                                        googleDriveManager
                                                                                                                .getBackupMetadata(
                                                                                                                        signedInAccount!!
                                                                                                                )
                                                                                        } catch (
                                                                                                e:
                                                                                                        Exception) {
                                                                                                e.printStackTrace()
                                                                                                statusMessage =
                                                                                                        "Backup failed: ${e.message}"
                                                                                        } finally {
                                                                                                isBackupLoading =
                                                                                                        false
                                                                                        }
                                                                                }
                                                                        },
                                                                        enabled = !isBackupLoading,
                                                                        modifier =
                                                                                Modifier.weight(1f)
                                                                ) {
                                                                        if (isBackupLoading) {
                                                                                CircularProgressIndicator(
                                                                                        modifier =
                                                                                                Modifier.padding(
                                                                                                                end =
                                                                                                                        8.dp
                                                                                                        )
                                                                                                        .size(
                                                                                                                16.dp
                                                                                                        ),
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onPrimary,
                                                                                        strokeWidth =
                                                                                                2.dp
                                                                                )
                                                                        } else {
                                                                                Icon(
                                                                                        Icons.Filled
                                                                                                .CloudUpload,
                                                                                        contentDescription =
                                                                                                null,
                                                                                        modifier =
                                                                                                Modifier.padding(
                                                                                                        end =
                                                                                                                8.dp
                                                                                                )
                                                                                )
                                                                        }
                                                                        Text("Backup")
                                                                }

                                                                Button(
                                                                        onClick = {
                                                                                showRestoreConfirmationDialog =
                                                                                        true
                                                                        },
                                                                        enabled = !isRestoreLoading,
                                                                        colors =
                                                                                ButtonDefaults
                                                                                        .buttonColors(
                                                                                                containerColor =
                                                                                                        MaterialTheme
                                                                                                                .colorScheme
                                                                                                                .secondary
                                                                                        ),
                                                                        modifier =
                                                                                Modifier.weight(1f)
                                                                ) {
                                                                        if (isRestoreLoading) {
                                                                                CircularProgressIndicator(
                                                                                        modifier =
                                                                                                Modifier.padding(
                                                                                                                end =
                                                                                                                        8.dp
                                                                                                        )
                                                                                                        .size(
                                                                                                                16.dp
                                                                                                        ),
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSecondary,
                                                                                        strokeWidth =
                                                                                                2.dp
                                                                                )
                                                                        } else {
                                                                                Icon(
                                                                                        Icons.Filled
                                                                                                .CloudDownload,
                                                                                        contentDescription =
                                                                                                null,
                                                                                        modifier =
                                                                                                Modifier.padding(
                                                                                                        end =
                                                                                                                8.dp
                                                                                                )
                                                                                )
                                                                        }
                                                                        Text("Restore")
                                                                }
                                                        }
                                                }

                                                if (statusMessage != null) {
                                                        Text(
                                                                text = statusMessage!!,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .secondary,
                                                                modifier =
                                                                        Modifier.padding(top = 8.dp)
                                                        )
                                                }
                                        }
                                }

                                Text(
                                        text = "Manual Backup (File)",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        color = MaterialTheme.colorScheme.primary
                                )
                                ElevatedCard(
                                        colors =
                                                CardDefaults.elevatedCardColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.surface
                                                ),
                                        elevation = CardDefaults.elevatedCardElevation(2.dp)
                                ) {
                                        Column {
                                                SettingsItem(
                                                        icon = Icons.Filled.ArrowUpward,
                                                        title = "Import Database",
                                                        onClick = { importLauncher.launch("*/*") }
                                                )
                                                HorizontalDivider(
                                                        modifier = Modifier.padding(start = 56.dp),
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .outlineVariant.copy(
                                                                        alpha = 0.5f
                                                                )
                                                )
                                                SettingsItem(
                                                        icon = Icons.Filled.ArrowDownward,
                                                        title = "Export Database",
                                                        onClick = {
                                                                exportLauncher.launch(
                                                                        "coupon_database.db"
                                                                )
                                                        }
                                                )
                                                HorizontalDivider(
                                                        modifier = Modifier.padding(start = 56.dp),
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .outlineVariant.copy(
                                                                        alpha = 0.5f
                                                                )
                                                )
                                                SettingsItem(
                                                        icon = Icons.Filled.Delete,
                                                        title = "Reset Database",
                                                        onClick = {
                                                                showResetConfirmationDialog = true
                                                        }
                                                )
                                        }
                                }
                        }
                }
        }

        if (showResetConfirmationDialog) {
                ConfirmationDialog(
                        onConfirm = onResetDatabase,
                        onDismiss = { showResetConfirmationDialog = false },
                        title = "Reset Database",
                        message =
                                "Are you sure you want to reset the database? This will permanently delete all your coupons and categories."
                )
        }

        if (showRestoreConfirmationDialog) {
                ConfirmationDialog(
                        onConfirm = {
                                showRestoreConfirmationDialog = false
                                scope.launch {
                                        isRestoreLoading = true
                                        try {
                                                if (googleDriveManager.restoreBackup(
                                                                signedInAccount!!
                                                        )
                                                ) {
                                                        val tempFile =
                                                                googleDriveManager
                                                                        .getTempRestoreFile()
                                                        if (databaseManager.replaceDatabase(
                                                                        tempFile
                                                                )
                                                        ) {
                                                                showImportSuccessDialog = true
                                                        } else {
                                                                statusMessage =
                                                                        "Database replacement failed"
                                                        }
                                                } else {
                                                        statusMessage = "No backup found in Drive"
                                                }
                                        } catch (e: Exception) {
                                                e.printStackTrace()
                                                statusMessage = "Restore failed: ${e.message}"
                                        } finally {
                                                isRestoreLoading = false
                                        }
                                }
                        },
                        onDismiss = { showRestoreConfirmationDialog = false },
                        title = "Restore Backup?",
                        message =
                                "This will overwrite your current data with the version from Google Drive. This action cannot be undone."
                )
        }

        if (showSignOutConfirmationDialog) {
                ConfirmationDialog(
                        onConfirm = {
                                showSignOutConfirmationDialog = false
                                googleDriveManager
                                        .getSignInClient()
                                        .signOut()
                                        .addOnCompleteListener { signedInAccount = null }
                        },
                        onDismiss = { showSignOutConfirmationDialog = false },
                        title = "Sign Out",
                        message = "Are you sure you want to disconnect your Google Drive account?"
                )
        }

        if (showImportSuccessDialog) {
                ConfirmationDialog(
                        onConfirm = { restartApp(context) },
                        onDismiss = { restartApp(context) },
                        title = "Success",
                        message =
                                "The database has been successfully updated. The app will now reload."
                )
        }

        if (showErrorDialog) {
                Dialog(onDismissRequest = { showErrorDialog = false }) {
                        ElevatedCard(
                                shape = MaterialTheme.shapes.extraLarge,
                                colors =
                                        CardDefaults.elevatedCardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                elevation = CardDefaults.elevatedCardElevation(8.dp)
                        ) {
                                Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                        Icon(
                                                imageVector = Icons.Filled.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(56.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                                text = "Sign-In Error",
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                                text = errorDialogMessage,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(
                                                onClick = { showErrorDialog = false },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .error
                                                        )
                                        ) { Text("Dismiss") }
                                }
                        }
                }
        }
}

private fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.putExtra("destination", "database_settings")
                context.startActivity(intent)
                // Kill the process to ensure a clean reload of the database/Room
                Runtime.getRuntime().exit(0)
        }
}

@Composable
private fun SettingsItem(
        icon: ImageVector,
        title: String,
        onClick: () -> Unit,
) {
        Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                        text = title,
                        modifier = Modifier.padding(start = 16.dp).weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
        }
}

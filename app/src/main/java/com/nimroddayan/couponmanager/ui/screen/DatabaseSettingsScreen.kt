package com.nimroddayan.couponmanager.ui.screen

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.nimroddayan.couponmanager.data.DatabaseManager
import com.nimroddayan.couponmanager.data.GoogleDriveManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseSettingsScreen(
        onNavigateUp: () -> Unit,
        onResetDatabase: () -> Unit,
) {
        var showResetConfirmationDialog by remember { mutableStateOf(false) }
        var showImportSuccessDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val databaseManager = remember { DatabaseManager(context) }

        val scope = rememberCoroutineScope()
        val googleDriveManager = remember { GoogleDriveManager(context) }
        var signedInAccount by remember { mutableStateOf<GoogleSignInAccount?>(null) }
        var isBackupLoading by remember { mutableStateOf(false) }
        var isRestoreLoading by remember { mutableStateOf(false) }
        var statusMessage by remember { mutableStateOf<String?>(null) }

        val signInLauncher =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                                signedInAccount = task.getResult(ApiException::class.java)
                        } catch (e: ApiException) {
                                e.printStackTrace()
                                statusMessage = "Sign-in failed: ${e.statusCode}"
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
                                                        Text(
                                                                text =
                                                                        "Connected as: ${signedInAccount?.email}",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium,
                                                                modifier =
                                                                        Modifier.padding(
                                                                                bottom = 16.dp
                                                                        )
                                                        )

                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Button(
                                                                        onClick = {
                                                                                scope.launch {
                                                                                        isBackupLoading =
                                                                                                true
                                                                                        try {
                                                                                                databaseManager
                                                                                                        .checkpoint()
                                                                                                googleDriveManager
                                                                                                        .uploadBackup(
                                                                                                                signedInAccount!!
                                                                                                        )
                                                                                                statusMessage =
                                                                                                        "Backup successful"
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
                                                                                Modifier.padding(
                                                                                        end = 8.dp
                                                                                )
                                                                ) {
                                                                        if (isBackupLoading) {
                                                                                CircularProgressIndicator(
                                                                                        modifier =
                                                                                                Modifier.padding(
                                                                                                        end =
                                                                                                                8.dp
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
                                                                                                null
                                                                                )
                                                                        }
                                                                        Text("Backup")
                                                                }

                                                                Button(
                                                                        onClick = {
                                                                                scope.launch {
                                                                                        isRestoreLoading =
                                                                                                true
                                                                                        try {
                                                                                                if (googleDriveManager
                                                                                                                .restoreBackup(
                                                                                                                        signedInAccount!!
                                                                                                                )
                                                                                                ) {
                                                                                                        val tempFile =
                                                                                                                googleDriveManager
                                                                                                                        .getTempRestoreFile()
                                                                                                        if (databaseManager
                                                                                                                        .replaceDatabase(
                                                                                                                                tempFile
                                                                                                                        )
                                                                                                        ) {
                                                                                                                showImportSuccessDialog =
                                                                                                                        true
                                                                                                        } else {
                                                                                                                statusMessage =
                                                                                                                        "Database replacement failed"
                                                                                                        }
                                                                                                } else {
                                                                                                        statusMessage =
                                                                                                                "No backup found in Drive"
                                                                                                }
                                                                                        } catch (
                                                                                                e:
                                                                                                        Exception) {
                                                                                                e.printStackTrace()
                                                                                                statusMessage =
                                                                                                        "Restore failed: ${e.message}"
                                                                                        } finally {
                                                                                                isRestoreLoading =
                                                                                                        false
                                                                                        }
                                                                                }
                                                                        },
                                                                        enabled = !isRestoreLoading
                                                                ) {
                                                                        if (isRestoreLoading) {
                                                                                CircularProgressIndicator(
                                                                                        modifier =
                                                                                                Modifier.padding(
                                                                                                        end =
                                                                                                                8.dp
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
                                                                                                .CloudDownload,
                                                                                        contentDescription =
                                                                                                null
                                                                                )
                                                                        }
                                                                        Text("Restore")
                                                                }
                                                        }

                                                        Text(
                                                                text = "Sign Out",
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .error,
                                                                modifier =
                                                                        Modifier.clickable {
                                                                                        googleDriveManager
                                                                                                .getSignInClient()
                                                                                                .signOut()
                                                                                                .addOnCompleteListener {
                                                                                                        signedInAccount =
                                                                                                                null
                                                                                                }
                                                                                }
                                                                                .padding(
                                                                                        top = 16.dp
                                                                                )
                                                        )
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

        if (showImportSuccessDialog) {
                ConfirmationDialog(
                        onConfirm = { restartApp(context) },
                        onDismiss = { restartApp(context) },
                        title = "Import Successful",
                        message =
                                "The database has been imported successfully. The app will now restart."
                )
        }
}

private fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
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

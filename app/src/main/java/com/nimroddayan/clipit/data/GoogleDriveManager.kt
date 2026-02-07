package com.nimroddayan.clipit.data

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleDriveManager(private val context: Context) {
    private val driveScope = Scope(DriveScopes.DRIVE_FILE)
    private val appDataScope = Scope(DriveScopes.DRIVE_APPDATA)

    fun getSignInClient(): GoogleSignInClient {
        val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(driveScope, appDataScope)
                        .build()
        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    suspend fun uploadBackup(account: GoogleSignInAccount) {
        withContext(Dispatchers.IO) {
            // 1. Initialize Drive Service
            val credential =
                    GoogleAccountCredential.usingOAuth2(
                            context,
                            Collections.singleton(DriveScopes.DRIVE_FILE)
                    )
            credential.selectedAccount = account.account
            val googleDriveService =
                    Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    GsonFactory.getDefaultInstance(),
                                    credential
                            )
                            .setApplicationName("ClipIt")
                            .build()

            // 2. Prepare Database Backup
            val tempBackupFile = File(context.cacheDir, "drive_backup_temp.db")
            val databaseManager = DatabaseManager(context)
            if (!databaseManager.createBackup(tempBackupFile)) {
                throw Exception("Failed to create local backup file")
            }

            // 3. Metadata with timestamped filename
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileMetadata = com.google.api.services.drive.model.File()
            fileMetadata.name = "coupon_manager_backup_$timestamp.db"
            fileMetadata.mimeType = "application/x-sqlite3"

            val mediaContent = FileContent("application/x-sqlite3", tempBackupFile)

            try {
                // Always create a new file (preserves backup history)
                googleDriveService.files().create(fileMetadata, mediaContent).execute()
            } finally {
                tempBackupFile.delete()
            }
        }
    }

    suspend fun restoreBackup(account: GoogleSignInAccount): Boolean {
        return withContext(Dispatchers.IO) {
            // 1. Initialize Drive Service
            val credential =
                    GoogleAccountCredential.usingOAuth2(
                            context,
                            Collections.singleton(DriveScopes.DRIVE_FILE)
                    )
            credential.selectedAccount = account.account
            val googleDriveService =
                    Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    GsonFactory.getDefaultInstance(),
                                    credential
                            )
                            .setApplicationName("ClipIt")
                            .build()

            // 2. Find Latest Backup (search for prefix, order by modifiedTime desc)
            val fileList =
                    googleDriveService
                            .files()
                            .list()
                            .setQ("name contains 'coupon_manager_backup' and trashed = false")
                            .setSpaces("drive")
                            .setOrderBy("modifiedTime desc")
                            .setPageSize(1)
                            .execute()

            if (fileList.files.isEmpty()) return@withContext false

            val fileId = fileList.files[0].id

            // 3. Download
            // We download to a temp file first
            val tempFile = File(context.cacheDir, "restore_temp.db")
            val outputStream = FileOutputStream(tempFile)
            googleDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            outputStream.flush()
            outputStream.close()

            // 4. Move to Database Location (Caller must handle DB closing and WAL cleanup)
            // Just return true if download succeeded.
            // The caller (ViewModel) will coordinate the DB replacement.
            return@withContext true
        }
    }

    fun getTempRestoreFile(): File {
        return File(context.cacheDir, "restore_temp.db")
    }

    suspend fun getBackupMetadata(account: GoogleSignInAccount): Long? {
        return withContext(Dispatchers.IO) {
            val credential =
                    GoogleAccountCredential.usingOAuth2(
                            context,
                            Collections.singleton(DriveScopes.DRIVE_FILE)
                    )
            credential.selectedAccount = account.account
            val googleDriveService =
                    Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    GsonFactory.getDefaultInstance(),
                                    credential
                            )
                            .setApplicationName("ClipIt")
                            .build()

            // Find latest backup by modifiedTime
            val fileList =
                    googleDriveService
                            .files()
                            .list()
                            .setQ("name contains 'coupon_manager_backup' and trashed = false")
                            .setSpaces("drive")
                            .setOrderBy("modifiedTime desc")
                            .setPageSize(1)
                            .setFields("files(modifiedTime)")
                            .execute()

            if (fileList.files.isNotEmpty()) {
                fileList.files[0].modifiedTime.value
            } else {
                null
            }
        }
    }
}

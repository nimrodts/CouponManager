package com.nimroddayan.clipit.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.nimroddayan.clipit.worker.CouponExtractionWorker

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val messagesBySender = messages?.groupBy { it.originatingAddress }

            messagesBySender?.forEach { (sender, smsList) ->
                if (!sender.isNullOrBlank()) {
                    val fullBody = StringBuilder()
                    smsList.forEach { fullBody.append(it.messageBody) }

                    val workData =
                            Data.Builder()
                                    .putString("sender", sender)
                                    .putString("body", fullBody.toString())
                                    .build()

                    val workRequest =
                            OneTimeWorkRequestBuilder<CouponExtractionWorker>()
                                    .setInputData(workData)
                                    .build()

                    WorkManager.getInstance(context).enqueue(workRequest)
                }
            }
        }
    }
}

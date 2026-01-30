package com.nimroddayan.clipit.data.gemini

import android.content.Context
import android.util.Log

/**
 * A Local Classifier using Heuristic Rules (Regex + Keywords). This acts as a "Local Model" to
 * detect coupons without Cloud API or heavy downloads.
 */
class LocalCouponClassifier(private val context: Context) {
    private val TAG = "LocalCouponClassifier"

    fun isCoupon(text: String): Boolean {
        Log.d(TAG, "Running Local Heuristic classification...")

        // 1. Keyword Check (Broad)
        val keywords =
                listOf(
                        "coupon",
                        "code",
                        "voucher",
                        "gift card",
                        "promo",
                        "קופון",
                        "קוד",
                        "שובר",
                        "מתנה",
                        "הטבה",
                        "מימוש",
                        "אסמכתא",
                        "כרטיס",
                        "Gift",
                        "Discount",
                        "Save",
                        "Off"
                )
        if (keywords.any { text.contains(it, ignoreCase = true) }) {
            Log.d(TAG, "Matched keyword.")
            return true
        }

        // 2. Pattern Check (Specific)
        // Check for "Currency + Number" or "Number + Currency"
        // Example: ₪100, 100 NIS, $50
        val valueRegex = Regex("""(₪|\$|€|NIS|nis|ש"ח)\s*\d+|\d+\s*(₪|\$|€|NIS|nis|ש"ח)""")
        if (valueRegex.containsMatchIn(text)) {
            Log.d(TAG, "Matched value pattern.")
            return true
        }

        // 3. Code Pattern Check
        // Example: 1234-5678, ABCD-1234
        val codeRegex = Regex("""(?<!\d)\d{4,}-\d{4,}(?!\d)|[A-Z0-9]{4,}-[A-Z0-9]{4,}""")
        if (codeRegex.containsMatchIn(text)) {
            Log.d(TAG, "Matched code pattern.")
            return true
        }

        Log.d(TAG, "No local match found.")
        return false
    }
}

package com.nimroddayan.couponmanager.data.gemini

data class ParsedCoupon(
    val storeName: String?,
    val code: String?,
    val initialValue: Double?,
    val expirationDate: String?,
    val description: String?
)

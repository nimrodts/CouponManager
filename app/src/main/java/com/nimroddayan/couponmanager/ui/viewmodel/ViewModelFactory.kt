package com.nimroddayan.couponmanager.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nimroddayan.couponmanager.data.CouponRepository
import com.nimroddayan.couponmanager.data.gemini.GeminiApiKeyRepository
import com.nimroddayan.couponmanager.data.gemini.GeminiCouponExtractor

class ViewModelFactory(
    private val context: Context,
    private val couponRepository: CouponRepository,
    private val geminiApiKeyRepository: GeminiApiKeyRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CouponViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CouponViewModel(
                couponRepository,
                GeminiCouponExtractor(geminiApiKeyRepository)
            ) as T
        }
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(geminiApiKeyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.nimroddayan.clipit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nimroddayan.clipit.data.CouponRepository
import com.nimroddayan.clipit.data.db.CouponHistoryDao

class HistoryViewModelFactory(
        private val couponHistoryDao: CouponHistoryDao,
        private val couponRepository: CouponRepository,
        private val userPreferencesRepository: com.nimroddayan.clipit.data.UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(
                    couponHistoryDao,
                    couponRepository,
                    userPreferencesRepository
            ) as
                    T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

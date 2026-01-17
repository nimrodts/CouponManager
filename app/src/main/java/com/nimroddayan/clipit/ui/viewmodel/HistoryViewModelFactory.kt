package com.nimroddayan.clipit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nimroddayan.clipit.data.CouponRepository
import com.nimroddayan.clipit.data.db.CouponHistoryDao

class HistoryViewModelFactory(
    private val couponHistoryDao: CouponHistoryDao,
    private val couponRepository: CouponRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(couponHistoryDao, couponRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



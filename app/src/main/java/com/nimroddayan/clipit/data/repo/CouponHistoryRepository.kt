package com.nimroddayan.clipit.data.repo

import com.nimroddayan.clipit.data.db.CouponHistoryDao
import com.nimroddayan.clipit.data.model.CouponHistory
import kotlinx.coroutines.flow.Flow

class CouponHistoryRepository(private val couponHistoryDao: CouponHistoryDao) {
    fun getHistoryForCoupon(couponId: Long): Flow<List<CouponHistory>> {
        return couponHistoryDao.getHistoryForCoupon(couponId)
    }

    suspend fun addHistoryEntry(couponHistory: CouponHistory) {
        couponHistoryDao.insert(couponHistory)
    }
}



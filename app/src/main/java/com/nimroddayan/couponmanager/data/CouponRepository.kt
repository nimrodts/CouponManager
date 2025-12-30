package com.nimroddayan.couponmanager.data

import com.nimroddayan.couponmanager.data.db.CouponDao
import com.nimroddayan.couponmanager.data.model.Coupon
import kotlinx.coroutines.flow.Flow

class CouponRepository(private val couponDao: CouponDao) {
    val allCoupons: Flow<List<Coupon>> = couponDao.getAll()
    val archivedCoupons: Flow<List<Coupon>> = couponDao.getArchived()

    suspend fun insert(coupon: Coupon) {
        couponDao.insertAll(coupon)
    }

    suspend fun update(coupon: Coupon) {
        couponDao.update(coupon)
    }

    suspend fun clearAll() {
        couponDao.clearAll()
    }
}

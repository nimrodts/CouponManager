package com.nimroddayan.couponmanager.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.nimroddayan.couponmanager.data.model.CouponHistory
import com.nimroddayan.couponmanager.data.model.MonthlySpending
import kotlinx.coroutines.flow.Flow

@Dao
interface CouponHistoryDao {
    @Insert
    suspend fun insert(couponHistory: CouponHistory)

    @Query("SELECT * FROM coupon_history WHERE couponId = :couponId ORDER BY timestamp DESC")
    fun getHistoryForCoupon(couponId: Long): Flow<List<CouponHistory>>

    @Query("SELECT * FROM coupon_history")
    fun getAll(): Flow<List<CouponHistory>>

    @Query("SELECT strftime('%Y-%m', timestamp / 1000, 'unixepoch') as month, SUM(CAST(changeSummary AS REAL)) as totalSpent FROM coupon_history WHERE action = 'Coupon Used' GROUP BY month ORDER BY month DESC")
    fun getSpendingByMonth(): Flow<List<MonthlySpending>>

    @Delete
    suspend fun delete(couponHistory: CouponHistory)
}

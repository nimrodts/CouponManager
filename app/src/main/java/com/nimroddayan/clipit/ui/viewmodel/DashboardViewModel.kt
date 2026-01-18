package com.nimroddayan.clipit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nimroddayan.clipit.data.UserPreferencesRepository
import com.nimroddayan.clipit.data.db.AppDatabase
import com.nimroddayan.clipit.data.model.CategorySpending
import com.nimroddayan.clipit.data.model.MonthlySpending
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(db: AppDatabase, userPreferencesRepository: UserPreferencesRepository) :
        ViewModel() {
        private val couponDao = db.couponDao()
        private val couponHistoryDao = db.couponHistoryDao()

        val currencySymbol: StateFlow<String> =
                userPreferencesRepository
                        .selectedCurrency
                        .map { code ->
                                com.nimroddayan.clipit.data.model.Currency.fromCode(code).symbol
                        }
                        .stateIn(
                                scope = viewModelScope,
                                started = SharingStarted.WhileSubscribed(5000),
                                initialValue = "₪"
                        )

        val totalBalance: StateFlow<Double> =
                couponDao
                        .getTotalBalance()
                        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

        val totalSpent: StateFlow<Double> =
                couponDao
                        .getTotalSpent()
                        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

        val spendingByCategory: StateFlow<List<CategorySpending>> =
                couponDao
                        .getSpendingByCategory()
                        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

        val spendingByMonth: StateFlow<List<MonthlySpending>> =
                couponHistoryDao
                        .getSpendingByMonth()
                        .map { spendingData ->
                                val calendar = Calendar.getInstance()
                                val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                                val last6Months =
                                        (0..5)
                                                .map { i ->
                                                        val cal = Calendar.getInstance()
                                                        cal.add(Calendar.MONTH, -i)
                                                        monthFormat.format(cal.time)
                                                }
                                                .reversed()

                                last6Months.map { month ->
                                        spendingData.find { it.month == month }
                                                ?: MonthlySpending(month, 0.0)
                                }
                        }
                        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
}

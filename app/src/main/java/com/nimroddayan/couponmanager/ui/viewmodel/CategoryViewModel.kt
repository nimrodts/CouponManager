package com.nimroddayan.couponmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.nimroddayan.couponmanager.data.db.AppDatabase
import com.nimroddayan.couponmanager.data.model.Category
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(private val db: AppDatabase) : ViewModel() {
    private val categoryDao = db.categoryDao()
    private val couponDao = db.couponDao()

    val allCategories: StateFlow<List<Category>> = categoryDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insert(category: Category) = viewModelScope.launch {
        categoryDao.insert(category)
    }

    fun update(category: Category) = viewModelScope.launch {
        categoryDao.update(category)
    }

    fun delete(category: Category) = viewModelScope.launch {
        db.withTransaction {
            couponDao.updateCategoryForCoupons(category.id, null)
            categoryDao.delete(category)
        }
    }

    fun clearAll() = viewModelScope.launch {
        categoryDao.clearAll()
    }
}

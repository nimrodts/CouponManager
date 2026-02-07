package com.nimroddayan.clipit.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nimroddayan.clipit.data.model.Category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [CategoryDao]. These tests run on an Android device/emulator with an
 * in-memory database.
 */
@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setUp() {
        database =
                Room.inMemoryDatabaseBuilder(
                                ApplicationProvider.getApplicationContext(),
                                AppDatabase::class.java
                        )
                        .allowMainThreadQueries()
                        .build()
        categoryDao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createTestCategory(
            name: String = "Test Category",
            colorHex: String = "#FF5733",
            iconName: String = "Grocery"
    ) = Category(name = name, colorHex = colorHex, iconName = iconName)

    @Test
    fun insertAndRetrieveCategory() = runTest {
        val category = createTestCategory(name = "Food")
        categoryDao.insert(category)

        val categories = categoryDao.getAll().first()

        assertEquals(1, categories.size)
        assertEquals("Food", categories[0].name)
    }

    @Test
    fun updateCategory() = runTest {
        val category = createTestCategory(name = "Original")
        val insertedId = categoryDao.insert(category)

        val insertedCategory =
                Category(
                        id = insertedId,
                        name = "Updated",
                        colorHex = "#00FF00",
                        iconName = "Restaurant"
                )
        categoryDao.update(insertedCategory)

        val categories = categoryDao.getAll().first()
        assertEquals(1, categories.size)
        assertEquals("Updated", categories[0].name)
        assertEquals("#00FF00", categories[0].colorHex)
    }

    @Test
    fun deleteCategory() = runTest {
        val category = createTestCategory()
        val insertedId = categoryDao.insert(category)

        val categoryToDelete =
                Category(
                        id = insertedId,
                        name = category.name,
                        colorHex = category.colorHex,
                        iconName = category.iconName
                )
        categoryDao.delete(categoryToDelete)

        val categories = categoryDao.getAll().first()
        assertTrue(categories.isEmpty())
    }

    @Test
    fun getAllReturnsCategoriesSortedByName() = runTest {
        categoryDao.insert(createTestCategory(name = "Zebra"))
        categoryDao.insert(createTestCategory(name = "Apple"))
        categoryDao.insert(createTestCategory(name = "Mango"))

        val categories = categoryDao.getAll().first()

        assertEquals(3, categories.size)
        assertEquals("Apple", categories[0].name)
        assertEquals("Mango", categories[1].name)
        assertEquals("Zebra", categories[2].name)
    }

    @Test
    fun clearAllRemovesAllCategories() = runTest {
        categoryDao.insert(createTestCategory(name = "Cat 1"))
        categoryDao.insert(createTestCategory(name = "Cat 2"))
        categoryDao.insert(createTestCategory(name = "Cat 3"))

        categoryDao.clearAll()

        val categories = categoryDao.getAll().first()
        assertTrue(categories.isEmpty())
    }
}

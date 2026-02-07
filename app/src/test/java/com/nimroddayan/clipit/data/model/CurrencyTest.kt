package com.nimroddayan.clipit.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for the [Currency] enum class. */
class CurrencyTest {

    @Test
    fun `fromCode returns NIS for ILS code`() {
        val result = Currency.fromCode("ILS")
        assertEquals(Currency.NIS, result)
    }

    @Test
    fun `fromCode returns USD for USD code`() {
        val result = Currency.fromCode("USD")
        assertEquals(Currency.USD, result)
    }

    @Test
    fun `fromCode returns EUR for EUR code`() {
        val result = Currency.fromCode("EUR")
        assertEquals(Currency.EUR, result)
    }

    @Test
    fun `fromCode returns GBP for GBP code`() {
        val result = Currency.fromCode("GBP")
        assertEquals(Currency.GBP, result)
    }

    @Test
    fun `fromCode returns JPY for JPY code`() {
        val result = Currency.fromCode("JPY")
        assertEquals(Currency.JPY, result)
    }

    @Test
    fun `fromCode returns NIS as default for unknown code`() {
        val result = Currency.fromCode("UNKNOWN")
        assertEquals(Currency.NIS, result)
    }

    @Test
    fun `fromCode returns NIS for empty string`() {
        val result = Currency.fromCode("")
        assertEquals(Currency.NIS, result)
    }

    @Test
    fun `NIS has correct symbol`() {
        assertEquals("₪", Currency.NIS.symbol)
    }

    @Test
    fun `USD has correct symbol`() {
        assertEquals("$", Currency.USD.symbol)
    }

    @Test
    fun `EUR has correct symbol`() {
        assertEquals("€", Currency.EUR.symbol)
    }

    @Test
    fun `GBP has correct symbol`() {
        assertEquals("£", Currency.GBP.symbol)
    }

    @Test
    fun `JPY has correct symbol`() {
        assertEquals("¥", Currency.JPY.symbol)
    }

    @Test
    fun `NIS has correct display name`() {
        assertEquals("New Israeli Shekel (NIS)", Currency.NIS.displayName)
    }

    @Test
    fun `NIS has correct code property`() {
        assertEquals("ILS", Currency.NIS.code)
    }
}

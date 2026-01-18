package com.nimroddayan.clipit.data.model

enum class Currency(val code: String, val symbol: String, val displayName: String) {
    NIS("ILS", "₪", "New Israeli Shekel (NIS)"),
    USD("USD", "$", "US Dollar (USD)"),
    EUR("EUR", "€", "Euro (EUR)"),
    GBP("GBP", "£", "British Pound (GBP)"),
    JPY("JPY", "¥", "Japanese Yen (JPY)");

    companion object {
        fun fromCode(code: String): Currency {
            return entries.find { it.code == code } ?: NIS
        }
    }
}

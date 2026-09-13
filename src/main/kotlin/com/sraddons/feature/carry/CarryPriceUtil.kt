package com.sraddons.feature.carry

object CarryPriceUtil {
    private const val MILLION = 1_000_000L
    private const val THOUSAND = 1_000L

    fun parsePrice(input: String): Long? {
        val trimmed = input.trim().uppercase()
        if (trimmed.isEmpty()) return null

        val multiplier =
            when {
                trimmed.endsWith("M") -> MILLION

                trimmed.endsWith("K") -> THOUSAND

                // No suffix: a plain integer overview of the value.
                else -> return trimmed.toLongOrNull()?.takeIf { it >= 0 }
            }

        val number = trimmed.dropLast(1).toDoubleOrNull() ?: return null
        if (number < 0) return null

        val value = number * multiplier
        return if (value > Long.MAX_VALUE.toDouble()) null else value.toLong()
    }

    fun formatPrice(coins: Long): String {
        tryFormat(coins, MILLION, "M")?.let { return it }
        tryFormat(coins, THOUSAND, "K")?.let { return it }
        return coins.toString()
    }

    private fun tryFormat(
        coins: Long,
        divisor: Long,
        suffix: String,
    ): String? {
        if (coins < divisor) return null
        val whole = coins / divisor
        val remainder = coins % divisor
        if (remainder == 0L) return "${whole}$suffix"
        val formatted = String.format("%.1f", coins.toDouble() / divisor)
        return "$formatted$suffix"
    }

    fun effectivePrice(
        type: CarryType,
        client: CarryClient,
    ): Long {
        if (!client.useBulk) return type.price
        val bulk = type.bulkPrice
        if (bulk != null && client.amount >= type.bulkThreshold) return bulk
        return type.price
    }
}

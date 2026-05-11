package com.sraddons.feature.carry

object CarryPriceUtil {

    private const val MILLION = 1_000_000L
    private const val THOUSAND = 1_000L

    fun parsePrice(input: String): Long? {
        val trimmed = input.trim().uppercase()
        if (trimmed.isEmpty()) return null

        return when {
            trimmed.endsWith("M") -> {
                val num = trimmed.dropLast(1).toDoubleOrNull() ?: return null
                if (num < 0) null else (num * MILLION).toLong()
            }
            trimmed.endsWith("K") -> {
                val num = trimmed.dropLast(1).toDoubleOrNull() ?: return null
                if (num < 0) null else (num * THOUSAND).toLong()
            }
            else -> {
                val num = trimmed.toLongOrNull() ?: return null
                if (num < 0) null else num
            }
        }
    }

    fun formatPrice(coins: Long): String {
        return when {
            coins >= MILLION -> {
                val millions = coins.toDouble() / MILLION.toDouble()
                if (millions == millions.toLong().toDouble()) {
                    "${millions.toLong()}M"
                } else {
                    "${String.format("%.1f", millions)}M"
                }
            }
            coins >= THOUSAND -> {
                val thousands = coins.toDouble() / THOUSAND.toDouble()
                if (thousands == thousands.toLong().toDouble()) {
                    "${thousands.toLong()}K"
                } else {
                    "${String.format("%.1f", thousands)}K"
                }
            }
            else -> coins.toString()
        }
    }

    fun effectivePrice(type: CarryType, amount: Int): Long {
        val bulk = type.bulkPrice
        if (bulk != null && amount >= type.bulkThreshold) return bulk
        return type.price
    }
}

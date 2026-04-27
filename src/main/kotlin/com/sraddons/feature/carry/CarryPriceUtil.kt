package com.sraddons.feature.carry

object CarryPriceUtil {

    fun parsePrice(input: String): Long? {
        val trimmed = input.trim().uppercase()
        if (trimmed.isEmpty()) return null

        return when {
            trimmed.endsWith("M") -> {
                val num = trimmed.dropLast(1).toDoubleOrNull() ?: return null
                if (num < 0) null else (num * 1_000_000).toLong()
            }
            trimmed.endsWith("K") -> {
                val num = trimmed.dropLast(1).toDoubleOrNull() ?: return null
                if (num < 0) null else (num * 1_000).toLong()
            }
            else -> {
                val num = trimmed.toLongOrNull() ?: return null
                if (num < 0) null else num
            }
        }
    }

    fun formatPrice(coins: Long): String {
        return when {
            coins >= 1_000_000 -> {
                val millions = coins.toDouble() / 1_000_000.0
                if (millions == millions.toLong().toDouble()) {
                    "${millions.toLong()}M"
                } else {
                    "${String.format("%.1f", millions)}M"
                }
            }
            coins >= 1_000 -> {
                val thousands = coins.toDouble() / 1_000.0
                if (thousands == thousands.toLong().toDouble()) {
                    "${thousands.toLong()}K"
                } else {
                    "${String.format("%.1f", thousands)}K"
                }
            }
            else -> coins.toString()
        }
    }

    fun effectivePrice(type: CarryType, amount: Int, useBulk: Boolean = false): Long {
        if (useBulk) {
            val bulk = type.bulkPrice
            if (bulk != null && amount >= type.bulkThreshold) return bulk
        }
        return type.price
    }
}

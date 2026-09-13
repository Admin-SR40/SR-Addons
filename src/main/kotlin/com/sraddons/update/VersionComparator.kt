package com.sraddons.update

/**
 * Compares dotted versions numerically instead of comparing the raw strings, so
 * `1.7.10` is correctly newer than `1.7.9`.
 *
 * A release counts as newer than its pre-release: `1.7.5` > `1.7.5-beta1`.
 */
internal object VersionComparator {
    fun compare(
        left: String,
        right: String,
    ): Int {
        val a = parts(left)
        val b = parts(right)
        for (index in 0 until maxOf(a.size, b.size)) {
            val rawA = a.getOrNull(index)
            val rawB = b.getOrNull(index)
            val numberA = rawA?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
            val numberB = rawB?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
            if (numberA != numberB) return numberA.compareTo(numberB)

            val suffixA = rawA?.dropWhile { it.isDigit() } ?: ""
            val suffixB = rawB?.dropWhile { it.isDigit() } ?: ""
            if (suffixA != suffixB) {
                if (suffixA.isEmpty()) return 1
                if (suffixB.isEmpty()) return -1
                return suffixA.compareTo(suffixB)
            }
        }
        return 0
    }

    private fun parts(version: String): List<String> =
        version
            .removePrefix("v")
            .split('.', '-', '+', '_')
            .filter { it.isNotEmpty() }
}

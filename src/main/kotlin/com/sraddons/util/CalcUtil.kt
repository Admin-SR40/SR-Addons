package com.sraddons.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.pow

object CalcUtil {
    private val suffixRegex = Regex("""(\d+(?:\.\d+)?)([KkMmBb])""")
    private val tokenRegex = Regex("""\d+(\.\d+)?|[+\-*/x^%()]""")
    private val priority = mapOf(
        "+" to 1, "-" to 1,
        "*" to 2, "x" to 2, "/" to 2, "%" to 2,
        "^" to 3
    )

    fun evaluate(expression: String): Double {
        val expanded = expandSuffixes(expression)
        val tokens = tokenize(expanded)
        val out = mutableListOf<String>()
        val ops = mutableListOf<String>()

        for (s in tokens) {
            when {
                s.toDoubleOrNull() != null -> out += s
                s in priority -> {
                    while (ops.isNotEmpty() && ops.last() != "(") {
                        val t = priority[ops.last()] ?: break
                        val c = priority[s] ?: break
                        if (t > c || (t == c && s != "^")) out += ops.removeAt(ops.lastIndex)
                        else break
                    }
                    ops += s
                }
                s == "(" -> ops += s
                s == ")" -> {
                    while (ops.isNotEmpty() && ops.last() != "(") out += ops.removeAt(ops.lastIndex)
                    if (ops.isNotEmpty() && ops.last() == "(") ops.removeAt(ops.lastIndex)
                }
            }
        }

        while (ops.isNotEmpty()) out += ops.removeAt(ops.lastIndex)

        val stack = mutableListOf<Double>()
        for (o in out) {
            o.toDoubleOrNull()?.let { stack += it; continue }
            if (o in priority) {
                val b = stack.removeAt(stack.lastIndex)
                val a = stack.removeAt(stack.lastIndex)
                stack += when (o) {
                    "+" -> a + b
                    "-" -> a - b
                    "*", "x" -> a * b
                    "/" -> a / b
                    "%" -> a % b
                    "^" -> a.pow(b)
                    else -> 0.0
                }
            }
        }

        return stack.first()
    }

    private val intFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
    private val decFormat = DecimalFormat("#,###.############", DecimalFormatSymbols(Locale.US))

    fun format(result: Double): String {
        return if (result == result.toLong().toDouble())
            intFormat.format(result.toLong())
        else
            decFormat.format(result)
    }

    private fun expandSuffixes(expression: String): String {
        return suffixRegex.replace(expression.replace(" ", "")) { match ->
            val value = match.groupValues[1].toDouble()
            val suffix = match.groupValues[2].uppercase()
            val multiplier = when (suffix) {
                "K" -> 1_000.0
                "M" -> 1_000_000.0
                "B" -> 1_000_000_000.0
                else -> 1.0
            }
            val expanded = value * multiplier
            if (expanded == expanded.toLong().toDouble()) expanded.toLong().toString()
            else expanded.toString()
        }
    }

    private fun tokenize(expression: String): List<String> {
        val str = tokenRegex.findAll(expression).map { it.value }.toMutableList()
        val tokens = mutableListOf<String>()

        for (i in str.indices) {
            val token = str[i]
            if (token == "") continue

            val unary = i == 0 || str[i - 1] in priority.keys || str[i - 1] == "("

            if (token == "+" && unary) continue

            if (token == "-" && unary) {
                val next = str.getOrNull(i + 1)
                if (next == "(") {
                    tokens += "0"
                    tokens += "-"
                    continue
                }
                tokens += "-${next ?: "0"}"
                str[i + 1] = ""
                continue
            }

            tokens += token
        }

        return tokens
    }
}

package com.sraddons.feature.helper

import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.COLOR_CODE_REGEX
import com.sraddons.util.GradientText
import com.sraddons.util.TitleUtil
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.FormattedCharSequence
import net.minecraft.network.chat.Style

object TextReplacer {

    private val gradientPrefix = ":g:"

    val defaults = linkedMapOf(
        "[MVP++] Admin_SR40" to "&b[&cDEV&b] :g:cyanToLightBlue:Admin_SR40",
        "[MVP+] Admin_SR40" to "&b[&cDEV&b] :g:cyanToLightBlue:Admin_SR40",
        "Admin_SR40" to ":g:cyanToLightBlue:Admin_SR40"
    )

    private val customs: LinkedHashMap<String, String> = linkedMapOf()

    fun getCustoms(): Map<String, String> = customs.toMap()

    val activePatterns: List<Pair<String, String>>
        get() = patternSet.patterns.toList()

    private data class PatternSet(
        val patterns: List<Pair<String, String>>,
        val compiled: List<Pair<Regex, String>>
    )

    @Volatile
    private var patternSet = PatternSet(emptyList(), emptyList())

    val gradientNames = listOf(
        "cyanToLightBlue", "goldToYellow", "aquaToGreen", "redToOrange", "purpleToPink"
    )

    fun init() {
        customs.clear()
        customs.putAll(ReplaceTextsData.load())
        rebuild()
    }

    fun rebuild() {
        val newPatterns = mutableListOf<Pair<String, String>>()

        if (SRConfig.settings.general.highlightDevName) {
            defaults.forEach { (k, v) -> newPatterns.add(k to v) }
        }

        customs.entries.forEach { (k, v) -> newPatterns.add(k to v) }

        newPatterns.sortByDescending { it.first.length }

        val newCompiled = newPatterns.map { (k, v) -> Regex(Regex.escape(k)) to v }

        patternSet = PatternSet(newPatterns, newCompiled)
    }

    fun add(key: String, value: String) {
        customs[key] = value
        save()
    }

    fun remove(key: String): Boolean {
        val existed = customs.remove(key) != null
        if (existed) save()
        return existed
    }

    private fun save() {
        ReplaceTextsData.save(customs)
        rebuild()
    }

    fun replace(text: String): String {
        if (!SRConfig.settings.general.replaceTextsEnabled || patternSet.patterns.isEmpty()) return text
        if (isModMessage(text)) return text

        val stripped = stripColorCodes(text)
        var result = stripped

        for ((regex, value) in patternSet.compiled) {
            val plainReplacement = stripGradientAndColors(value)
            result = result.replace(regex, TitleUtil.parseColorCodes(plainReplacement) + "§r")
        }

        return result
    }

    fun replaceFormattedSeq(seq: FormattedCharSequence): FormattedCharSequence {
        if (!SRConfig.settings.general.replaceTextsEnabled || patternSet.patterns.isEmpty()) return seq

        val chars = mutableListOf<Int>()
        val styles = mutableListOf<Style>()
        seq.accept { _, style, cp -> chars.add(cp); styles.add(style); true }

        if (chars.isEmpty()) return seq

        val clean = buildString { chars.forEach { appendCodePoint(it) } }

        if (isModMessage(clean)) return seq

        val matches = findMatches(clean)
        if (matches.isEmpty()) return seq

        val segments = mutableListOf<FormattedCharSequence>()
        var pos = 0
        for ((start, end, replacement) in matches) {
            if (pos < start) {
                segments.add(subSequence(chars, styles, pos, start))
            }
            segments.add(buildReplacementComponent(replacement).visualOrderText)
            pos = end
        }
        if (pos < chars.size) {
            segments.add(subSequence(chars, styles, pos, chars.size))
        }

        return FormattedCharSequence.composite(segments)
    }

    private fun subSequence(
        chars: List<Int>, styles: List<Style>,
        from: Int, to: Int
    ): FormattedCharSequence = FormattedCharSequence { sink ->
        for (i in from until to) {
            sink.accept(i - from, styles[i], chars[i])
        }
        true
    }

    private fun buildReplacementComponent(value: String): Component {
        val gradientIdx = value.indexOf(gradientPrefix)
        if (gradientIdx < 0) {
            return Component.literal(TitleUtil.parseColorCodes(value + "&r"))
        }

        val plainBefore = value.substring(0, gradientIdx)
        val afterPrefix = value.substring(gradientIdx + gradientPrefix.length)
        val colonIdx = afterPrefix.indexOf(':')
        if (colonIdx <= 0) {
            return Component.literal(TitleUtil.parseColorCodes(value + "&r"))
        }

        val gradientName = afterPrefix.substring(0, colonIdx)
        val text = afterPrefix.substring(colonIdx + 1)

        var result = Component.empty()
        if (plainBefore.isNotEmpty()) {
            result = result.append(Component.literal(TitleUtil.parseColorCodes(plainBefore)))
        }
        result = result.append(
            when (gradientName) {
                "cyanToLightBlue" -> GradientText.cyanToLightBlue(text)
                "goldToYellow" -> GradientText.goldToYellow(text)
                "aquaToGreen" -> GradientText.aquaToGreen(text)
                "redToOrange" -> GradientText.redToOrange(text)
                "purpleToPink" -> GradientText.purpleToPink(text)
                else -> Component.literal(text)
            }
        )
        return result.append(Component.literal("§r"))
    }

    private fun findMatches(clean: String): List<Triple<Int, Int, String>> {
        val matches = mutableListOf<Triple<Int, Int, String>>()
        val occupied = BooleanArray(clean.length)

        for ((key, value) in patternSet.patterns) {
            var searchFrom = 0
            while (true) {
                val idx = clean.indexOf(key, searchFrom)
                if (idx < 0) break

                val end = idx + key.length
                val alreadyOccupied = (idx until end).any { occupied[it] }
                if (!alreadyOccupied) {
                    matches.add(Triple(idx, end, value))
                    for (i in idx until end) occupied[i] = true
                }
                searchFrom = idx + 1
            }
        }

        matches.sortBy { it.first }
        return matches
    }

    private fun stripGradientAndColors(value: String): String {
        val gIdx = value.indexOf(gradientPrefix)
        val flat = if (gIdx >= 0) {
            val before = value.substring(0, gIdx)
            val after = value.substring(gIdx + gradientPrefix.length)
            val ci = after.indexOf(':')
            val text = if (ci > 0) after.substring(ci + 1) else after
            before + text
        } else {
            value
        }
        return TitleUtil.parseColorCodes(flat).replace(COLOR_CODE_REGEX, "")
    }

    private fun isModMessage(text: String): Boolean =
        text.contains("[SR-Addons]") || text.contains("[ReplaceTexts]")

    private fun stripColorCodes(text: String): String =
        text.replace(COLOR_CODE_REGEX, "")
}

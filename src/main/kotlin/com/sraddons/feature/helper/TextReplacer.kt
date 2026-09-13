package com.sraddons.feature.helper

import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.utils.COLOR_CODE_REGEX
import com.sraddons.util.GradientText
import com.sraddons.util.TitleUtil
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import java.util.BitSet

object TextReplacer {
    private val gradientPrefix = ":g:"

    private val defaults =
        linkedMapOf(
            "Admin_SR40" to ":g:cyanToLightBlue:Admin_SR40",
        )

    private val replacing = ThreadLocal.withInitial { false }

    private val customs: LinkedHashMap<String, String> = linkedMapOf()

    fun getCustoms(): Map<String, String> = synchronized(this) { customs.toMap() }

    private data class PatternSet(
        val patterns: List<Pair<String, String>>,
        val compiled: List<Pair<Regex, String>>,
        val minLen: Int,
    )

    @Volatile
    private var patternSet = PatternSet(emptyList(), emptyList(), Int.MAX_VALUE)

    fun init() {
        synchronized(this) {
            customs.clear()
            customs.putAll(ReplaceTextsData.load())
            rebuild()
        }
    }

    fun rebuild() {
        synchronized(this) {
            val newPatterns =
                buildList {
                    if (SRConfig.settings.general.highlightDevName) {
                        defaults.forEach { (key, value) -> add(key to value) }
                    }
                    customs.forEach { (key, value) -> add(key to value) }
                }.sortedByDescending { it.first.length }

            val newCompiled = newPatterns.map { (k, v) -> Regex(Regex.escape(k)) to v }
            val newMinLen = if (newPatterns.isEmpty()) Int.MAX_VALUE else newPatterns.minOf { it.first.length }

            patternSet = PatternSet(newPatterns, newCompiled, newMinLen)
        }
    }

    fun add(
        key: String,
        value: String,
    ) {
        synchronized(this) {
            customs[key] = value
            save()
        }
    }

    fun remove(key: String): Boolean {
        synchronized(this) {
            val existed = customs.remove(key) != null
            if (existed) save()
            return existed
        }
    }

    private fun save() {
        ReplaceTextsData.save(customs)
        rebuild()
    }

    fun replaceFormattedSeq(seq: FormattedCharSequence): FormattedCharSequence {
        if (!SRConfig.settings.general.replaceTextsEnabled || patternSet.patterns.isEmpty()) return seq
        if (replacing.get()) return seq

        replacing.set(true)
        try {
            // The plain text is built first: the vast majority of drawn sequences contain nothing
            // to replace, and this way they never allocate a style list or boxed code points.
            val clean =
                buildString {
                    seq.accept { _, _, codePoint ->
                        appendCodePoint(codePoint)
                        true
                    }
                }
            if (clean.length < patternSet.minLen) return seq
            if (isModMessage(clean)) return seq

            val matches = findMatches(clean)
            if (matches.isEmpty()) return seq

            // Something will change — collect code points and styles for the sequence now.
            val chars = ArrayList<Int>(clean.length)
            val styles = ArrayList<Style>(clean.length)
            seq.accept { _, style, codePoint ->
                chars.add(codePoint)
                styles.add(style)
                true
            }

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
        } finally {
            replacing.set(false)
        }
    }

    private fun subSequence(
        chars: List<Int>,
        styles: List<Style>,
        from: Int,
        to: Int,
    ): FormattedCharSequence =
        FormattedCharSequence { sink ->
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
        result =
            result.append(
                when (gradientName) {
                    "cyanToLightBlue" -> GradientText.cyanToLightBlue(text)
                    "goldToYellow" -> GradientText.goldToYellow(text)
                    "aquaToGreen" -> GradientText.aquaToGreen(text)
                    "redToOrange" -> GradientText.redToOrange(text)
                    "purpleToPink" -> GradientText.purpleToPink(text)
                    else -> Component.literal(text)
                },
            )
        return result.append(Component.literal("§r"))
    }

    private fun findMatches(clean: String): List<Triple<Int, Int, String>> {
        val matches = mutableListOf<Triple<Int, Int, String>>()
        val occupied = BitSet(clean.length)

        for ((key, value) in patternSet.patterns) {
            var searchFrom = 0
            while (true) {
                val idx = clean.indexOf(key, searchFrom)
                if (idx < 0) break

                val end = idx + key.length
                val alreadyOccupied = occupied.nextSetBit(idx) in idx until end
                if (!alreadyOccupied) {
                    matches.add(Triple(idx, end, value))
                    occupied.set(idx, end)
                }
                searchFrom = idx + 1
            }
        }

        matches.sortBy { it.first }
        return matches
    }

    private fun stripGradientAndColors(value: String): String {
        val gIdx = value.indexOf(gradientPrefix)
        val flat =
            if (gIdx >= 0) {
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

    private fun isModMessage(text: String): Boolean = text.contains("[SR-Addons]") || text.contains("[ReplaceTexts]")
}

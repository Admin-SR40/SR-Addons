package com.sraddons.feature.partycommands.utils

/**
 * Matches legacy color/format codes in both the section-sign (§) and ampersand (&) form,
 * including uppercase variants and both hex notations:
 *
 * - `§a`, `&a`, `§A`, `&L`, …
 * - `§x§1§2§3§4§5§6` (per-character hex used by vanilla)
 * - `&#112233` (ampersand hex used by many plugins)
 *
 * Hypixel keeps the letter behind a color code when a raw code reaches party chat,
 * which is why `§aTest` used to show up as `aTest`.
 */
val COLOR_CODE_REGEX =
    Regex(
        "(?i)[\u00a7&](?:#[0-9a-f]{6}|x(?:[\u00a7&][0-9a-f]){6}|[0-9a-fk-or])",
    )

private val WHITESPACE_REGEX = Regex("\\s+")

/** Removes legacy color codes from [this]. */
val String.noControlCodes: String
    get() = this.replace(COLOR_CODE_REGEX, "")

/**
 * Prepares [this] for a chat command: color codes are removed and line breaks are
 * collapsed so the text stays a single, valid command argument.
 */
fun String.toPlainChatMessage(): String = noControlCodes.replace(WHITESPACE_REGEX, " ").trim()

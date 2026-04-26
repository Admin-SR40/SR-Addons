package com.sraddons.feature.partycommands.utils

fun getPingColor(ping: Int): String = when {
    ping >= 300 -> "\u00a7c"
    ping >= 250 -> "\u00a76"
    ping >= 200 -> "\u00a7e"
    ping >= 150 -> "\u00a7a"
    else -> "\u00a7a"
}

fun getTpsColor(tps: Double): String = when {
    tps >= 19.5 -> "\u00a7a"
    tps >= 18.0 -> "\u00a7e"
    tps >= 15.0 -> "\u00a76"
    else -> "\u00a7c"
}

fun getFpsColor(fps: Int): String = when {
    fps >= 120 -> "\u00a7a"
    fps >= 60 -> "\u00a7a"
    fps >= 30 -> "\u00a7e"
    fps >= 15 -> "\u00a76"
    else -> "\u00a7c"
}

const val RESPONSE_PREFIX = "\u00a7b"
const val RESPONSE_LABEL = "\u00a7e"
const val RESPONSE_VALUE = "\u00a7f"
const val RESPONSE_SEPARATOR = "\u00a77"

fun formatResponse(label: String, value: String, valueColor: String = RESPONSE_VALUE): String {
    return "${RESPONSE_PREFIX}$label${RESPONSE_SEPARATOR}: $valueColor$value"
}

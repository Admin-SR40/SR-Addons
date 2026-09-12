package com.sraddons.feature.partycommands.utils

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

fun getPingColor(ping: Int): Int = when {
    ping >= 300 -> 0xFF5555
    ping >= 250 -> 0xFFAA00
    ping >= 200 -> 0xFFFF55
    else -> 0x55FF55
}

fun getTpsColor(tps: Double): Int = when {
    tps > 19.8 -> 0x00AA00
    tps > 19.0 -> 0x55FF55
    tps > 17.5 -> 0xFFAA00
    tps > 12.0 -> 0xFF5555
    else -> 0xAA0000
}

fun getFpsColor(fps: Int): Int = when {
    fps >= 60 -> 0x55FF55
    fps >= 30 -> 0xFFFF55
    fps >= 15 -> 0xFFAA00
    else -> 0xFF5555
}

fun formatResponse(label: Component, value: Component): MutableComponent {
    return Component.literal("§b").append(label)
        .append(Component.literal("§7: "))
        .append(value)
}

package com.sraddons.feature.partycommands.utils

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

/** `Label: value` in the mod's standard chat colours. */
fun formatResponse(
    label: Component,
    value: Component,
): MutableComponent =
    Component
        .literal("§b")
        .append(label)
        .append(Component.literal("§7: "))
        .append(value)

/** Red error value, e.g. `formatResponse(label("error"), errorComponent("not_leader"))`. */
fun errorComponent(
    key: String,
    vararg args: Any,
): Component = Component.translatable("sraddons.pc.error.$key", *args).withColor(0xFF5555)

/** Sends `Error: <message>` through the configured response channels. */
fun respondError(
    key: String,
    vararg args: Any,
) = respond(formatResponse(label("error"), errorComponent(key, *args)))

/** Sends a `Usage: !command …` hint through the configured response channels. */
fun respondUsage(syntax: String) = respond(formatResponse(label("usage"), Component.literal("§c$syntax")))

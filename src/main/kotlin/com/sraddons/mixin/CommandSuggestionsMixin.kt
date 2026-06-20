package com.sraddons.mixin

import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestions
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.commands.Commands
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.CommandSuggestions
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.multiplayer.ClientSuggestionProvider
import net.minecraft.util.FormattedCharSequence
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.concurrent.CompletableFuture

@Mixin(CommandSuggestions::class)
abstract class CommandSuggestionsMixin {

    @Shadow
    @JvmField
    var currentParse: ParseResults<ClientSuggestionProvider>? = null

    @Shadow
    @Final
    @JvmField
    var input: EditBox? = null

    @Shadow
    @JvmField
    var keepSuggestions: Boolean = false

    @Shadow
    @JvmField
    var pendingSuggestions: CompletableFuture<Suggestions>? = null

    @Shadow
    @JvmField
    var suggestions: CommandSuggestions.SuggestionsList? = null

    @Shadow
    @JvmField
    var allowSuggestions: Boolean = false

    @Shadow
    @JvmField
    var commandUsage: List<FormattedCharSequence>? = null

    @Shadow
    protected abstract fun showSuggestions(bl: Boolean)

    @Shadow
    private fun updateUsageInfo(parse: ParseResults<ClientSuggestionProvider>, suggestions: Suggestions?) {}

    @Inject(method = ["showSuggestions"], at = [At("HEAD")])
    private fun onShowSuggestions(bl: Boolean, ci: CallbackInfo) {
        val editBox = input ?: return
        val pending = pendingSuggestions ?: return
        val value = editBox.value
        val prefix = SRConfig.settings.partyCommands.prefix

        if (value.startsWith(prefix) && pending.isDone) {
            try {
                val brigadierSuggestions = pending.getNow(null)
                if (brigadierSuggestions != null) {
                    val filtered = brigadierSuggestions.list.filter { !it.text.startsWith("!") }

                    if (filtered.size != brigadierSuggestions.list.size) {
                        pendingSuggestions = CompletableFuture.completedFuture(
                            Suggestions(brigadierSuggestions.range, filtered)
                        )
                    }
                }
            } catch (e: Exception) {
                org.apache.logging.log4j.LogManager.getLogger("SR-Addons-Mixin")
                    .warn("Failed to filter ! suggestions", e)
            }
        }
    }

    @Inject(method = ["updateCommandInfo"], at = [At("HEAD")], cancellable = true, require = 1)
    private fun onUpdateCommandInfo(ci: CallbackInfo) {
        val editBox = input ?: return
        val value = editBox.value
        val prefix = SRConfig.settings.partyCommands.prefix
        val length = prefix.length

        if (!value.startsWith(prefix)) return

        if (!keepSuggestions) {
            editBox.setSuggestion(null)
            suggestions = null
        }

        if (currentParse?.reader?.string != value) {
            currentParse = null
        }

        val reader = StringReader(value)
        reader.cursor = length

        if (currentParse == null) {
            val player = Minecraft.getInstance().player
            if (player != null) {
                val raw = Commands.DISPATCHER.parse(reader, player.connection.suggestionsProvider)
                currentParse = @Suppress("UNCHECKED_CAST") (raw as? ParseResults<ClientSuggestionProvider>) ?: return
            }
        }

        val cursor = editBox.cursorPosition
        if (cursor >= length && (suggestions == null || !keepSuggestions)) {
            val parse = currentParse
            if (parse != null && parse.exceptions.isNotEmpty()) {
                pendingSuggestions = CompletableFuture.completedFuture(
                    Suggestions(StringRange.at(cursor), emptyList())
                )
            } else {
                @Suppress("UNCHECKED_CAST")
                pendingSuggestions = Commands.DISPATCHER.getCompletionSuggestions(
                    parse as? ParseResults<net.minecraft.commands.SharedSuggestionProvider>, cursor)
            }

            pendingSuggestions = pendingSuggestions?.thenApply { s ->
                val filtered = s.list.filter { !it.text.startsWith("!") }
                Suggestions(s.range, filtered)
            }

            if (pendingSuggestions?.isDone == true && allowSuggestions) {
                if (currentParse?.reader?.string == value) {
                    showSuggestions(false)
                }
            }
        }

        val parse = currentParse
        if (parse?.exceptions?.isNotEmpty() == true) {
            updateUsageInfo(parse, pendingSuggestions?.getNow(null))
        } else {
            (commandUsage as? MutableList<*>)?.clear()
        }

        ci.cancel()
    }
}

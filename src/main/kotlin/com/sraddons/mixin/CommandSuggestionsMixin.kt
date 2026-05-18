package com.sraddons.mixin

import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.suggestion.Suggestion
import com.mojang.brigadier.suggestion.Suggestions
import com.sraddons.config.SRConfig
import com.sraddons.feature.partycommands.commands.Commands
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.CommandSuggestions
import net.minecraft.client.gui.components.EditBox
import net.minecraft.commands.SharedSuggestionProvider
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
    var currentParse: ParseResults<SharedSuggestionProvider>? = null

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
    var commandUsage: MutableList<FormattedCharSequence>? = null

    @Shadow
    protected abstract fun showSuggestions(bl: Boolean)

    @Shadow
    private fun updateUsageInfo() {}

    @Inject(method = ["showSuggestions"], at = [At("HEAD")])
    private fun onShowSuggestions(bl: Boolean, ci: CallbackInfo) {
        val value = input!!.value
        val prefix = SRConfig.settings.partyCommands.prefix

        if (value.startsWith(prefix) && pendingSuggestions != null && pendingSuggestions!!.isDone) {
            try {
                val brigadierSuggestions = pendingSuggestions!!.getNow(null)
                if (brigadierSuggestions != null) {
                    val filtered = brigadierSuggestions.list.filter { !it.text.startsWith("!") }

                    if (filtered.size != brigadierSuggestions.list.size) {
                        pendingSuggestions = CompletableFuture.completedFuture(
                            Suggestions(brigadierSuggestions.range, filtered)
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    @Inject(method = ["updateCommandInfo"], at = [At("HEAD")], cancellable = true)
    private fun onUpdateCommandInfo(ci: CallbackInfo) {
        val value = input!!.value
        val prefix = SRConfig.settings.partyCommands.prefix
        val length = prefix.length

        if (value.startsWith(prefix)) {
            if (!keepSuggestions) {
                input!!.setSuggestion(null)
                suggestions = null
            }

            if (currentParse != null && currentParse!!.reader.string != value) {
                currentParse = null
            }

            val reader = StringReader(value)
            reader.cursor = length

            if (currentParse == null) {
                val player = Minecraft.getInstance().player
                if (player != null) {
                    currentParse = Commands.DISPATCHER.parse(reader, player.connection.suggestionsProvider)
                }
            }

            val cursor = input!!.cursorPosition
            if (cursor >= length && (suggestions == null || !keepSuggestions)) {
                if (currentParse != null && currentParse!!.exceptions.isNotEmpty()) {
                    pendingSuggestions = CompletableFuture.completedFuture(
                        Suggestions(StringRange.at(cursor), emptyList())
                    )
                } else {
                    pendingSuggestions = Commands.DISPATCHER.getCompletionSuggestions(currentParse, cursor)
                }

                pendingSuggestions = pendingSuggestions!!.thenApply { s ->
                    val filtered = s.list.filter { !it.text.startsWith("!") }
                    Suggestions(s.range, filtered)
                }

                if (pendingSuggestions!!.isDone && allowSuggestions) {
                    if (currentParse != null && currentParse!!.reader.string == value) {
                        showSuggestions(false)
                    }
                }
            }

            if (currentParse != null && currentParse!!.exceptions.isNotEmpty()) {
                updateUsageInfo()
            } else {
                commandUsage!!.clear()
            }

            ci.cancel()
        }
    }
}

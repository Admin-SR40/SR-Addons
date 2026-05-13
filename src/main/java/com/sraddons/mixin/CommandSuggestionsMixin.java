package com.sraddons.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.Suggestion;
import com.sraddons.config.SRConfig;
import com.sraddons.feature.partycommands.commands.Commands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @Shadow
    private ParseResults<SharedSuggestionProvider> currentParse;

    @Shadow
    @Final
    private EditBox input;

    @Shadow
    private boolean keepSuggestions;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    private CommandSuggestions.SuggestionsList suggestions;

    @Shadow
    private boolean allowSuggestions;

    @Shadow
    private List<net.minecraft.util.FormattedCharSequence> commandUsage;

    @Shadow
    protected abstract void showSuggestions(boolean bl);

    @Shadow
    private void updateUsageInfo() {}

    @Inject(method = "showSuggestions", at = @At("HEAD"))
    private void onShowSuggestions(boolean bl, CallbackInfo ci) {
        String value = this.input.getValue();
        String prefix = SRConfig.INSTANCE.getSettings().getPartyCommands().getPrefix();

        if (value.startsWith(prefix) && this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
            try {
                Suggestions suggestions = this.pendingSuggestions.getNow(null);
                if (suggestions != null) {
                    List<Suggestion> filtered = suggestions.getList().stream()
                        .filter(s -> !s.getText().startsWith("!"))
                        .collect(Collectors.toList());

                    if (filtered.size() != suggestions.getList().size()) {
                        this.pendingSuggestions = CompletableFuture.completedFuture(
                            new Suggestions(suggestions.getRange(), filtered)
                        );
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    @Inject(method = "updateCommandInfo", at = @At("HEAD"), cancellable = true)
    private void onUpdateCommandInfo(CallbackInfo ci) {
        String value = this.input.getValue();
        String prefix = SRConfig.INSTANCE.getSettings().getPartyCommands().getPrefix();
        int length = prefix.length();

        if (value.startsWith(prefix)) {
            if (!this.keepSuggestions) {
                this.input.setSuggestion(null);
                this.suggestions = null;
            }

            if (this.currentParse != null && !this.currentParse.getReader().getString().equals(value)) {
                this.currentParse = null;
            }

            StringReader reader = new StringReader(value);
            reader.setCursor(length);

            if (this.currentParse == null) {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    this.currentParse = Commands.DISPATCHER.parse(reader, player.connection.getSuggestionsProvider());
                }
            }

            int cursor = this.input.getCursorPosition();
            if (cursor >= length && (this.suggestions == null || !this.keepSuggestions)) {
                if (this.currentParse != null && !this.currentParse.getExceptions().isEmpty()) {
                    this.pendingSuggestions = CompletableFuture.completedFuture(
                        new Suggestions(StringRange.at(cursor), Collections.emptyList())
                    );
                } else {
                    this.pendingSuggestions = Commands.DISPATCHER.getCompletionSuggestions(this.currentParse, cursor);
                }

                this.pendingSuggestions = this.pendingSuggestions.thenApply(suggestions -> {
                    List<Suggestion> filtered = suggestions.getList().stream()
                        .filter(s -> !s.getText().startsWith("!"))
                        .collect(Collectors.toList());
                    return new Suggestions(suggestions.getRange(), filtered);
                });

                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone() && this.allowSuggestions) {
                        if (this.currentParse != null && this.currentParse.getReader().getString().equals(value)) {
                            this.showSuggestions(false);
                        }
                    }
                });
            }

            if (this.currentParse != null && !this.currentParse.getExceptions().isEmpty()) {
                this.updateUsageInfo();
            } else {
                this.commandUsage.clear();
            }

            ci.cancel();
        }
    }
}

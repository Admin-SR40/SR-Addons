package com.sraddons.mixin

import dev.isxander.yacl3.gui.AbstractWidget
import dev.isxander.yacl3.gui.controllers.ListEntryWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.gen.Accessor
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(ListEntryWidget::class)
abstract class YACLListEntryFocusMixin {

    @Accessor("entryWidget")
    abstract fun getEntryWidget(): AbstractWidget

    @Shadow
    abstract fun getFocused(): GuiEventListener?

    @Shadow
    abstract fun setFocused(listener: GuiEventListener?)

    @Inject(method = ["mouseClicked"], at = [At("HEAD")])
    private fun beforeMouseClicked(
        mouseButton: net.minecraft.client.input.MouseButtonEvent,
        bl: Boolean,
        cir: CallbackInfoReturnable<Boolean>
    ) {
        // mouseClicked is only called when the widget is clicked (hovered).
        // Ensure the inner entry widget receives focus so text input works.
        val entry = getEntryWidget()
        if (getFocused() !== entry) {
            setFocused(entry as GuiEventListener)
        }
    }
}
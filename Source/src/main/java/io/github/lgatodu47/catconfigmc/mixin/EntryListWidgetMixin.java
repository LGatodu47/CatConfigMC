package io.github.lgatodu47.catconfigmc.mixin;

import io.github.lgatodu47.catconfigmc.screen.EntryListWidgetExtension;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntryListWidget.class)
public class EntryListWidgetMixin {
    @Inject(method = "getEntryAtPosition", at = @At("HEAD"), cancellable = true)
    private void catconfigmc$inject_getEntryAtPosition(double x, double y, CallbackInfoReturnable<Object> cir) {
        if(this instanceof EntryListWidgetExtension extension) {
            cir.setReturnValue(extension.catconfigmc$getEntryAtPosition(x, y));
        }
    }
}

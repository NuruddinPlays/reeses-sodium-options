package me.flashyreese.mods.reeses_sodium_options.mixin.sodium;

import me.flashyreese.mods.reeses_sodium_options.client.gui.FlatButtonWidgetExtended;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FlatButtonWidget.class)
public abstract class MixinFlatButtonWidget extends AbstractWidget implements FlatButtonWidgetExtended {

    @Shadow
    @Final
    private Dim2i dim;

    @Unique
    private boolean leftAligned;

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/gui/widgets/FlatButtonWidget;drawString(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/network/chat/Component;III)V"))
    public void redirectDrawString(Args args) {
        if (this.leftAligned) { // Aligns the text to the left by 10 pixels
            //this.drawString(guiGraphics, text, this.dim.x() + 10, y, color);
            args.set(2, this.dim.x() + 10);
        }
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/gui/widgets/FlatButtonWidget;drawRect(Lnet/minecraft/client/gui/GuiGraphics;IIIII)V", ordinal = 1))
    public void redirectDrawRect(Args args) {
        if (this.leftAligned) {
            //this.drawRect(guiGraphics, x1, this.dim.y(), x1 + 1, y2, color);
            args.set(2, this.dim.y());
            args.set(3, (int) args.get(1) + 1);
        }
    }

    @Override
    public Dim2i getDimensions() {
        return this.dim;
    }

    @Override
    public boolean isLeftAligned() {
        return this.leftAligned;
    }

    @Override
    public void setLeftAligned(boolean leftAligned) {
        this.leftAligned = leftAligned;
    }
}

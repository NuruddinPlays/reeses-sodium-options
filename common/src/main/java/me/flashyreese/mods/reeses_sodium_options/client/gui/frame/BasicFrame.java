package me.flashyreese.mods.reeses_sodium_options.client.gui.frame;

import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BasicFrame extends AbstractFrame {

    protected List<Function<Dim2i, AbstractWidget>> functions;

    public BasicFrame(Dim2i dim, boolean renderOutline, List<Function<Dim2i, AbstractWidget>> functions) {
        super(dim, renderOutline);
        this.functions = functions;
        this.buildFrame();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void buildFrame() {
        this.children.clear();
        this.controlElements.clear();

        this.functions.forEach(function -> this.children.add(function.apply(dim)));

        super.buildFrame();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    public static class Builder {
        private final List<Function<Dim2i, AbstractWidget>> functions = new ArrayList<>();
        private Dim2i dim;
        private boolean renderOutline;

        public Builder withDimension(Dim2i dim) {
            this.dim = dim;
            return this;
        }

        public Builder withRenderOutline(boolean renderOutline) {
            this.renderOutline = renderOutline;
            return this;
        }

        public Builder addChild(Function<Dim2i, AbstractWidget> function) {
            this.functions.add(function);
            return this;
        }

        public BasicFrame build() {
            Validate.notNull(this.dim, "Dimension must be specified");

            return new BasicFrame(this.dim, this.renderOutline, this.functions);
        }
    }
}
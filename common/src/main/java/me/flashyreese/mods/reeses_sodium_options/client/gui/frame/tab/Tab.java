package me.flashyreese.mods.reeses_sodium_options.client.gui.frame.tab;

import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.AbstractFrame;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.OptionPageFrame;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.ScrollableFrame;
import net.caffeinemc.mods.sodium.client.gui.options.OptionPage;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.network.chat.Component;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public record Tab<T extends AbstractFrame>(Component title, Function<Dim2i, T> frameFunction) {

    public static Tab.Builder<?> builder() {
        return new Tab.Builder<>();
    }

    public Component getTitle() {
        return title;
    }

    public Function<Dim2i, T> getFrameFunction() {
        return this.frameFunction;
    }

    public static class Builder<T extends AbstractFrame> {
        private Component title;
        private Function<Dim2i, T> frameFunction;

        public Builder<T> withTitle(Component title) {
            this.title = title;
            return this;
        }

        public Builder<T> withFrameFunction(Function<Dim2i, T> frameFunction) {
            this.frameFunction = frameFunction;
            return this;
        }

        public Tab<T> build() {
            return new Tab<T>(this.title, this.frameFunction);
        }

        public Tab<ScrollableFrame> from(OptionPage page, AtomicReference<Integer> verticalScrollBarOffset) {
            return new Tab<>(page.getName(), dim2i -> ScrollableFrame
                    .builder()
                    .withDimension(dim2i)
                    .withFrame(OptionPageFrame
                            .builder()
                            .withDimension(new Dim2i(dim2i.x(), dim2i.y(), dim2i.width(), dim2i.height()))
                            .withOptionPage(page)
                            .build())
                    .withVerticalScrollBarOffset(verticalScrollBarOffset)
                    .build());
        }
    }
}
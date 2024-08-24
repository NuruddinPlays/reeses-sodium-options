package me.flashyreese.mods.reeses_sodium_options.client.gui.frame.components;

import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class ScrollBarComponent extends AbstractWidget {

    protected static final int SCROLL_OFFSET = 6;

    protected final Dim2i dim;
    private final Mode mode;
    private final int frameLength;
    private final int viewPortLength;
    private final int maxScrollBarOffset;
    private final Consumer<Integer> onSetOffset;
    private int offset = 0;
    private boolean isDragging;

    private Dim2i scrollThumb = null;
    private int scrollThumbClickOffset;

    private final Dim2i extendedScrollArea;

    public ScrollBarComponent(Dim2i trackArea, Mode mode, int frameLength, int viewPortLength, Consumer<Integer> onSetOffset) {
        this(trackArea, mode, frameLength, viewPortLength, onSetOffset, null);
    }

    public ScrollBarComponent(Dim2i scrollBarArea, Mode mode, int frameLength, int viewPortLength, Consumer<Integer> onSetOffset, Dim2i extendedTrackArea) {
        this.dim = scrollBarArea;
        this.mode = mode;
        this.frameLength = frameLength;
        this.viewPortLength = viewPortLength;
        this.onSetOffset = onSetOffset;
        this.maxScrollBarOffset = this.frameLength - this.viewPortLength;
        this.extendedScrollArea = extendedTrackArea;
        this.updateThumbPosition();
    }

    public void updateThumbPosition() {
        int trackSize = (this.mode == Mode.VERTICAL ? this.dim.height() : this.dim.width() - 6);
        int scrollThumbLength = (this.viewPortLength * trackSize) / this.frameLength;
        int maximumScrollThumbOffset = this.viewPortLength - scrollThumbLength;
        int scrollThumbOffset = (this.offset * maximumScrollThumbOffset) / this.maxScrollBarOffset;
        this.scrollThumb = new Dim2i(
                this.dim.x() + 2 + (this.mode == Mode.HORIZONTAL ? scrollThumbOffset : 0),
                this.dim.y() + 2 + (this.mode == Mode.VERTICAL ? scrollThumbOffset : 0),
                (this.mode == Mode.VERTICAL ? this.dim.width() : scrollThumbLength) - 4,
                (this.mode == Mode.VERTICAL ? scrollThumbLength : this.dim.height()) - 4
        );
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.drawBorder(guiGraphics, this.dim.x(), this.dim.y(), this.dim.getLimitX(), this.dim.getLimitY(), 0xFFAAAAAA);
        this.drawRect(guiGraphics, this.scrollThumb.x(), this.scrollThumb.y(), this.scrollThumb.getLimitX(), this.scrollThumb.getLimitY(), 0xFFAAAAAA);
        if (this.isFocused()) {
            this.drawBorder(guiGraphics, this.dim.x(), this.dim.y(), this.dim.getLimitX(), this.dim.getLimitY(), -1);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.dim.containsCursor(mouseX, mouseY)) {
            if (this.scrollThumb.containsCursor(mouseX, mouseY)) {
                this.scrollThumbClickOffset = (int) (this.mode == Mode.VERTICAL ? mouseY - this.scrollThumb.getCenterY() : mouseX - this.scrollThumb.getCenterX());
                this.isDragging = true;
            } else {
                int thumbLength = this.mode == Mode.VERTICAL ? this.scrollThumb.height() : this.scrollThumb.width();
                int trackLength = this.mode == Mode.VERTICAL ? this.dim.height() : this.dim.width();
                int value = (int) (((this.mode == Mode.VERTICAL ? mouseY - this.dim.y() : mouseX - this.dim.x()) - thumbLength / 2.0) * this.maxScrollBarOffset / (trackLength - thumbLength));
                this.setOffset(value);
                this.isDragging = false;
            }
            return true;
        }
        this.isDragging = false;
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isDragging = false;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.isDragging) {
            int thumbLength = this.mode == Mode.VERTICAL ? this.scrollThumb.height() : this.scrollThumb.width();
            int trackLength = this.mode == Mode.VERTICAL ? this.dim.height() : this.dim.width();
            int value = (int) (((this.mode == Mode.VERTICAL ? mouseY : mouseX) - this.scrollThumbClickOffset - (this.mode == Mode.VERTICAL ? this.dim.y() : this.dim.x()) - thumbLength / 2.0) * this.maxScrollBarOffset / (trackLength - thumbLength));
            this.setOffset(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.dim.containsCursor(mouseX, mouseY) || this.extendedScrollArea != null && this.extendedScrollArea.containsCursor(mouseX, mouseY)) {
            this.setOffset(this.offset - (int) verticalAmount * SCROLL_OFFSET);
            return true;
        }
        return false;
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int value) {
        this.offset = Mth.clamp(value, 0, this.maxScrollBarOffset);
        this.updateThumbPosition();
        this.onSetOffset.accept(this.offset);
    }

    @Override
    public @NotNull ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.dim.x(), this.dim.y(), this.dim.width(), this.dim.height());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isFocused()) {
            return false;
        }

        int newOffset = switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> this.getOffset() - SCROLL_OFFSET;
            case GLFW.GLFW_KEY_DOWN -> this.getOffset() + SCROLL_OFFSET;
            case GLFW.GLFW_KEY_LEFT -> this.mode == Mode.HORIZONTAL ? this.getOffset() - SCROLL_OFFSET : this.getOffset();
            case GLFW.GLFW_KEY_RIGHT -> this.mode == Mode.HORIZONTAL ? this.getOffset() + SCROLL_OFFSET : this.getOffset();
            default -> this.getOffset();
        };

        if (newOffset != this.getOffset()) {
            this.setOffset(newOffset);
            return true;
        }

        return false;
    }

    public enum Mode {
        HORIZONTAL,
        VERTICAL
    }
}
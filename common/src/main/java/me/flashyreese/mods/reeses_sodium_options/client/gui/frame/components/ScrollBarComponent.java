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

    protected static final int SCROLL_STEP = 6;

    protected final Dim2i scrollBarArea;
    private final ScrollDirection mode;
    private final int contentLength;
    private final int visibleAreaLength;
    private final int maxContentOffset;
    private final Consumer<Integer> offsetChangeListener;
    private int offset = 0;
    private boolean isDragging;

    private Dim2i scrollThumb = null;
    private int scrollThumbClickOffset;

    private final Dim2i extraScrollArea;

    public ScrollBarComponent(Dim2i trackArea, ScrollDirection scrollDirection, int contentLength, int visibleAreaLength, Consumer<Integer> offsetChangeListener) {
        this(trackArea, scrollDirection, contentLength, visibleAreaLength, offsetChangeListener, null);
    }

    public ScrollBarComponent(Dim2i scrollBarArea, ScrollDirection scrollDirection, int contentLength, int visibleAreaLength, Consumer<Integer> offsetChangeListener, Dim2i extraScrollArea) {
        this.scrollBarArea = scrollBarArea;
        this.mode = scrollDirection;
        this.contentLength = contentLength;
        this.visibleAreaLength = visibleAreaLength;
        this.offsetChangeListener = offsetChangeListener;
        this.maxContentOffset = this.contentLength - this.visibleAreaLength;
        this.extraScrollArea = extraScrollArea;
        this.updateThumbLocation();
    }

    public void updateThumbLocation() {
        int trackSize = (this.mode == ScrollDirection.VERTICAL ? this.scrollBarArea.height() : this.scrollBarArea.width() - 6);
        int scrollThumbLength = (this.visibleAreaLength * trackSize) / this.contentLength;
        int maximumScrollThumbOffset = this.visibleAreaLength - scrollThumbLength;
        int scrollThumbOffset = (this.offset * maximumScrollThumbOffset) / this.maxContentOffset;
        this.scrollThumb = new Dim2i(
                this.scrollBarArea.x() + 2 + (this.mode == ScrollDirection.HORIZONTAL ? scrollThumbOffset : 0),
                this.scrollBarArea.y() + 2 + (this.mode == ScrollDirection.VERTICAL ? scrollThumbOffset : 0),
                (this.mode == ScrollDirection.VERTICAL ? this.scrollBarArea.width() : scrollThumbLength) - 4,
                (this.mode == ScrollDirection.VERTICAL ? scrollThumbLength : this.scrollBarArea.height()) - 4
        );
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.drawBorder(guiGraphics, this.scrollBarArea.x(), this.scrollBarArea.y(), this.scrollBarArea.getLimitX(), this.scrollBarArea.getLimitY(), 0xFFAAAAAA);
        this.drawRect(guiGraphics, this.scrollThumb.x(), this.scrollThumb.y(), this.scrollThumb.getLimitX(), this.scrollThumb.getLimitY(), 0xFFAAAAAA);
        if (this.isFocused()) {
            this.drawBorder(guiGraphics, this.scrollBarArea.x(), this.scrollBarArea.y(), this.scrollBarArea.getLimitX(), this.scrollBarArea.getLimitY(), -1);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.scrollBarArea.containsCursor(mouseX, mouseY)) {
            if (this.scrollThumb.containsCursor(mouseX, mouseY)) {
                this.scrollThumbClickOffset = (int) (this.mode == ScrollDirection.VERTICAL ? mouseY - this.scrollThumb.getCenterY() : mouseX - this.scrollThumb.getCenterX());
                this.isDragging = true;
            } else {
                int thumbLength = this.mode == ScrollDirection.VERTICAL ? this.scrollThumb.height() : this.scrollThumb.width();
                int trackLength = this.mode == ScrollDirection.VERTICAL ? this.scrollBarArea.height() : this.scrollBarArea.width();
                int value = (int) (((this.mode == ScrollDirection.VERTICAL ? mouseY - this.scrollBarArea.y() : mouseX - this.scrollBarArea.x()) - thumbLength / 2.0) * this.maxContentOffset / (trackLength - thumbLength));
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
            int thumbLength = this.mode == ScrollDirection.VERTICAL ? this.scrollThumb.height() : this.scrollThumb.width();
            int trackLength = this.mode == ScrollDirection.VERTICAL ? this.scrollBarArea.height() : this.scrollBarArea.width();
            int value = (int) (((this.mode == ScrollDirection.VERTICAL ? mouseY : mouseX) - this.scrollThumbClickOffset - (this.mode == ScrollDirection.VERTICAL ? this.scrollBarArea.y() : this.scrollBarArea.x()) - thumbLength / 2.0) * this.maxContentOffset / (trackLength - thumbLength));
            this.setOffset(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.scrollBarArea.containsCursor(mouseX, mouseY) || this.extraScrollArea != null && this.extraScrollArea.containsCursor(mouseX, mouseY)) {
            this.setOffset(this.offset - (int) verticalAmount * SCROLL_STEP);
            return true;
        }
        return false;
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int value) {
        this.offset = Mth.clamp(value, 0, this.maxContentOffset);
        this.updateThumbLocation();
        this.offsetChangeListener.accept(this.offset);
    }

    @Override
    public @NotNull ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.scrollBarArea.x(), this.scrollBarArea.y(), this.scrollBarArea.width(), this.scrollBarArea.height());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isFocused()) {
            return false;
        }

        int newOffset = switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> this.getOffset() - SCROLL_STEP;
            case GLFW.GLFW_KEY_DOWN -> this.getOffset() + SCROLL_STEP;
            case GLFW.GLFW_KEY_LEFT -> this.mode == ScrollDirection.HORIZONTAL ? this.getOffset() - SCROLL_STEP : this.getOffset();
            case GLFW.GLFW_KEY_RIGHT -> this.mode == ScrollDirection.HORIZONTAL ? this.getOffset() + SCROLL_STEP : this.getOffset();
            default -> this.getOffset();
        };

        if (newOffset != this.getOffset()) {
            this.setOffset(newOffset);
            return true;
        }

        return false;
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        return this.scrollBarArea.containsCursor(x, y);
    }

    public enum ScrollDirection {
        HORIZONTAL,
        VERTICAL
    }
}
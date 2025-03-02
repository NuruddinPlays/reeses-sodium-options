package me.flashyreese.mods.reeses_sodium_options.client.gui.frame.tab;

import me.flashyreese.mods.reeses_sodium_options.client.gui.Dim2iExtended;
import me.flashyreese.mods.reeses_sodium_options.client.gui.FlatButtonWidgetExtended;
import me.flashyreese.mods.reeses_sodium_options.client.gui.Point2i;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.AbstractFrame;
import me.flashyreese.mods.reeses_sodium_options.client.gui.frame.components.ScrollBarComponent;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class TabFrame extends AbstractFrame {

    private final boolean tabSectionCanScroll;
    private final Dim2i tabSection;
    private final Dim2i frameSection;
    private final List<Tab<?>> tabs = new ArrayList<>();
    private final Runnable onSetTab;
    private final AtomicReference<Component> tabSectionSelectedTab;
    private ScrollBarComponent tabSectionScrollBar = null;
    private Optional<Tab<?>> selectedTab = Optional.empty();
    private AbstractFrame selectedFrame;

    public TabFrame(Dim2i dim, boolean renderOutline, List<Tab<?>> tabs, Runnable onSetTab, AtomicReference<Component> tabSectionSelectedTab, AtomicReference<Integer> tabSectionScrollBarOffset) {
        super(dim, renderOutline);
        this.tabs.addAll(tabs);
        int tabSectionY = this.tabs.size() * 18;
        this.tabSectionCanScroll = tabSectionY > this.dim.height();

        Optional<Integer> result = tabs.stream().map(tab -> this.getStringWidth(tab.title())).max(Integer::compareTo);

        this.tabSection = new Dim2i(this.dim.x(), this.dim.y(), result.map(integer -> integer + (this.tabSectionCanScroll ? 32 : 24)).orElseGet(() -> (int) (this.dim.width() * 0.35D)), this.dim.height());
        this.frameSection = new Dim2i(this.tabSection.getLimitX(), this.dim.y(), this.dim.width() - this.tabSection.width(), this.dim.height());

        this.onSetTab = onSetTab;
        if (this.tabSectionCanScroll) {
            this.tabSectionScrollBar = new ScrollBarComponent(new Dim2i(this.tabSection.getLimitX() - 11, this.tabSection.y(), 10, this.tabSection.height()), ScrollBarComponent.ScrollDirection.VERTICAL, tabSectionY, this.dim.height(), offset -> {
                //this.buildFrame();
                tabSectionScrollBarOffset.set(offset);
                ((Dim2iExtended) ((Object) this.tabSection)).setY(this.dim.y() - this.tabSectionScrollBar.getOffset());
            }, this.dim);
            this.tabSectionScrollBar.setOffset(tabSectionScrollBarOffset.get());
        }
        this.tabSectionSelectedTab = tabSectionSelectedTab;

        if (this.tabSectionSelectedTab.get() != null) {
            this.selectedTab = this.tabs.stream().filter(tab -> tab.getTitle().getString().equals(this.tabSectionSelectedTab.get().getString())).findAny();
        }

        this.buildFrame();

        // Let's build each frame, future note for anyone: do not move this line.
        this.tabs.stream().filter(tab -> this.selectedTab.filter(value -> value != tab).isPresent()).forEach(tab -> tab.getFrameFunction().apply(this.frameSection));
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public void setTab(Optional<Tab<?>> tab) {
        this.selectedTab = tab;
        this.selectedTab.ifPresent(value -> this.tabSectionSelectedTab.set(value.getTitle()));
        if (this.onSetTab != null) {
            this.onSetTab.run();
        }
        this.buildFrame();
    }

    @Override
    public void buildFrame() {
        this.children.clear();
        this.controlElements.clear();

        if (this.selectedTab.isEmpty()) {
            if (!this.tabs.isEmpty()) {
                // Just use the first tab for now
                this.selectedTab = Optional.ofNullable(this.tabs.getFirst());
            }
        }

        this.rebuildTabs();
        this.rebuildTabFrame();

        if (this.tabSectionCanScroll) {
            this.tabSectionScrollBar.updateThumbLocation();
            this.children.add(this.tabSectionScrollBar);
        }

        super.buildFrame();
        this.registerFocusListener(element -> {
            if (element instanceof FlatButtonWidgetExtended flatButtonWidget && this.tabSectionCanScroll) {
                Dim2i dim = flatButtonWidget.getDimensions();
                int inputOffset = this.tabSectionScrollBar.getOffset();
                if (dim.y() <= this.dim.y()) {
                    inputOffset += dim.y() - this.dim.y();
                } else if (dim.getLimitY() >= this.dim.getLimitY()) {
                    inputOffset += dim.getLimitY() - this.dim.getLimitY();
                }
                this.tabSectionScrollBar.setOffset(inputOffset);
            }
        });
    }

    private void rebuildTabs() {
        int offsetY = 0;
        for (Tab<?> tab : this.tabs) {
            int width = this.tabSection.width() - (this.tabSectionCanScroll ? 12 : 4);
            int height = 18;
            Dim2i tabDim = new Dim2i(0, offsetY, width, height);
            ((Dim2iExtended)(Object) tabDim).setPoint2i(((Point2i)(Object) this.tabSection));

            FlatButtonWidget button = new FlatButtonWidget(tabDim, tab.getTitle(), () -> this.setTab(Optional.of(tab)));
            button.setSelected(this.selectedTab.isPresent() && this.selectedTab.get() == tab);
            ((FlatButtonWidgetExtended) button).setLeftAligned(true);
            this.children.add(button);

            offsetY += 18;
        }
    }

    private void rebuildTabFrame() {
        if (this.selectedTab.isEmpty()) return;
        AbstractFrame frame = this.selectedTab.get().getFrameFunction().apply(this.frameSection);
        if (frame != null) {
            this.selectedFrame = frame;
            frame.buildFrame();
            this.children.add(frame);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.applyScissor(guiGraphics, this.dim.x(), this.dim.y(), this.dim.width(), this.dim.height(), () -> {
            for (AbstractWidget widget : this.children) {
                if (widget != this.selectedFrame) {
                    widget.render(guiGraphics, mouseX, mouseY, delta);
                }
            }
        });
        this.selectedFrame.render(guiGraphics, mouseX, mouseY, delta);
        if (this.tabSectionCanScroll) {
            this.tabSectionScrollBar.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return (this.dim.containsCursor(mouseX, mouseY) && super.mouseClicked(mouseX, mouseY, button)) || (this.tabSectionCanScroll && this.tabSectionScrollBar.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) || (this.tabSectionCanScroll && this.tabSectionScrollBar.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button) || (this.tabSectionCanScroll && this.tabSectionScrollBar.mouseReleased(mouseX, mouseY, button));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount) || (this.tabSectionCanScroll && this.tabSectionScrollBar.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount));
    }

    public static class Builder {
        private final List<Tab<?>> functions = new ArrayList<>();
        private Dim2i dim;
        private boolean renderOutline;
        private Runnable onSetTab;
        private AtomicReference<Component> tabSectionSelectedTab = new AtomicReference<>(null);
        private AtomicReference<Integer> tabSectionScrollBarOffset = new AtomicReference<>(0);

        public Builder setDimension(Dim2i dim) {
            this.dim = dim;
            return this;
        }

        public Builder shouldRenderOutline(boolean renderOutline) {
            this.renderOutline = renderOutline;
            return this;
        }

        public Builder addTabs(Consumer<List<Tab<?>>> tabs) {
            tabs.accept(this.functions);
            return this;
        }

        public Builder onSetTab(Runnable onSetTab) {
            this.onSetTab = onSetTab;
            return this;
        }

        public Builder setTabSectionSelectedTab(AtomicReference<Component> tabSectionSelectedTab) {
            this.tabSectionSelectedTab = tabSectionSelectedTab;
            return this;
        }

        public Builder setTabSectionScrollBarOffset(AtomicReference<Integer> tabSectionScrollBarOffset) {
            this.tabSectionScrollBarOffset = tabSectionScrollBarOffset;
            return this;
        }

        public TabFrame build() {
            Validate.notNull(this.dim, "Dimension must be specified");

            return new TabFrame(this.dim, this.renderOutline, this.functions, this.onSetTab, this.tabSectionSelectedTab, this.tabSectionScrollBarOffset);
        }
    }

    public List<Tab<?>> getTabs() {
        return tabs;
    }

    public AbstractFrame getSelectedFrame() {
        return selectedFrame;
    }

    public Optional<Tab<?>> getSelectedTab() {
        return selectedTab;
    }
}
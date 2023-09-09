package de.ambertation.wunderlib.ui.layout.components;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.values.Value;
import de.ambertation.wunderlib.ui.vanilla.LayoutScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DropDown extends Button {
    public interface OnChange {
        void now(DropDown dropDown, Object value);
    }

    Panel panel;
    private final VerticalStack items;
    private Component valueComponent;
    private Object currentValue;

    private List<ValueItem> values = new ArrayList<>(4);

    private OnChange onChange;

    public DropDown(
            Value width,
            Value height,
            Component component
    ) {
        super(width, height, component);
        this.onPress = this::onPress;

        this.items = new VerticalStack(Value.fit(), Value.fit());
        for (int i = 0; i < 20; i++) {
            addItem(Component.literal("Item " + i), i);
        }
    }

    public void close() {
        if (panel != null) {
            panel = null;
            parentPanel.parentScreen.setOverlayProvider(null);
            didClose();
        }
    }

    private void didClose() {
        onBoundsChanged();
    }

    private void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private record ValueItem(
            Component title,
            Object value,
            LayoutComponent<?, ?> component,
            Consumer<ValueItem> callback) {

    }

    public DropDown onChange(OnChange onChange) {
        this.onChange = onChange;
        return this;
    }

    public DropDown addItem(Component title, Object value) {
        return addItem(title, value, null);
    }

    public DropDown addItem(Component title, Object value, @Nullable Consumer<ValueItem> callback) {
        final var component = new Text(Value.fit(), Value.fixed(20), title) {
            @Override
            public boolean mouseClicked(double d, double e, int i) {
                if (this.relativeBounds.contains(d, e)) {
                    playDownSound(Minecraft.getInstance().getSoundManager());
                    select(value, true);
                    close();
                    return true;
                }
                return false;
            }
        };
        
        ValueItem item = new ValueItem(title, value, component, callback);
        values.add(item);
        items.add(item.component);

        if (valueComponent == null) select(value, false);
        return this;
    }

    public boolean select(Object value) {
        return select(value, true);
    }

    public boolean select(Object value, boolean fireEvent) {
        for (var item : values) {
            if (item.value.equals(value)) {
                valueComponent = item.title;
                currentValue = value;
                if (fireEvent && item.callback != null) item.callback.accept(item);
                if (fireEvent && onChange != null) onChange.now(this, value);
                return true;
            }
        }

        valueComponent = null;
        currentValue = null;
        if (fireEvent && onChange != null) onChange.now(this, null);
        return false;
    }

    public Object getValue() {
        return currentValue;
    }

    @Override
    protected Component contentComponent() {
        if (valueComponent == null)
            return Component.literal("---");
        return valueComponent;
    }

    private void onPress(Button button) {
        if (panel == null) {
            final int PADDING = 1;
            panel = new Panel(parentPanel.parentScreen) {
                @Override
                public boolean mouseClicked(double d, double e, int i) {
                    if (!super.mouseClicked(d, e, i)) {
                        close();
                    }
                    return true;
                }
            };
            Container backdrop = new Container(Value.fill(), Value.fill());
            backdrop.setBackgroundColor(ColorHelper.CONTAINER_BACKGROUND);
            panel.setChild(backdrop).setZIndex(parentPanel.getZIndex() + 50);

            //height calculation
            items.calculateLayoutInParent(parentPanel);
            int height = items.getScreenBounds().height;
            if (screenBounds.bottom() + height > parentPanel.parentScreen.height) {
                height = Math.max(30, parentPanel.parentScreen.height - screenBounds.top);
            }

            Container contentContainer = Container.create(
                    Value.fit(),
                    Value.fixed(height + 2 * PADDING),
                    VerticalScroll.create(items)
            );
            contentContainer.setOutlineColor(ColorHelper.OVERLAY_BORDER);
            contentContainer.setBackgroundColor(ColorHelper.OVERLAY_BACKGROUND);
            contentContainer.setPadding(PADDING + 4, PADDING, PADDING, PADDING);

            backdrop.addChild(screenBounds.left + 10, screenBounds.bottom(), contentContainer);


            panel.calculateLayout();
            parentPanel.parentScreen.setOverlayProvider(new LayoutScreen.OverlayProvider() {
                @Override
                public @NotNull Panel getOverlay() {
                    return panel;
                }

                @Override
                public void willRemoveOverlay() {
                    parentPanel.setInputEnabled(true);
                    panel = null;
                    didClose();
                }
            });

            parentPanel.setInputEnabled(false);
        }
    }
}

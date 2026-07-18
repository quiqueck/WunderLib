package de.ambertation.wunderlib.ui.layout.components;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import de.ambertation.wunderlib.ui.layout.components.render.EditBoxRenderer;
import de.ambertation.wunderlib.ui.layout.values.Value;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class Input extends AbstractVanillaComponent<EditBox, Input> {
    private Consumer<String> responder;
    private BiFunction<String, Integer, FormattedCharSequence> formatter;
    private Predicate<String> filter;
    private String initialValue = "";

    // EditBox#setFilter was removed in 26.1. We re-implement the same revert-on-reject
    // behaviour on top of EditBox#setResponder: lastValidValue is the last value the filter
    // accepted, and `reverting` guards against the re-entrant responder call that setValue triggers.
    private String lastValidValue = "";
    private boolean reverting = false;

    public Input(
            Value width,
            Value height,
            Component component,
            String initialValue
    ) {
        super(width, height, new EditBoxRenderer(), component);
        this.initialValue = initialValue;
    }

    @Override
    protected EditBox createVanillaComponent() {
        EditBox eb = new EditBox(renderer.getFont(),
                0, 0,
                relativeBounds.width, relativeBounds.height,
                null,
                component
        );
        lastValidValue = initialValue;
        // Always install our wrapper so the filter is enforced even without a user responder.
        eb.setResponder(this::onEditBoxChanged);
        if (formatter != null) eb.addFormatter(formatter::apply);
        eb.setValue(initialValue);
        eb.setBordered(true);
        eb.setEditable(true);

        return eb;
    }

    // Enforces `filter` on top of the vanilla EditBox and forwards accepted values to `responder`.
    private void onEditBoxChanged(String newValue) {
        if (reverting) return;
        if (filter == null || filter.test(newValue)) {
            lastValidValue = newValue;
            if (responder != null) responder.accept(newValue);
        } else if (vanillaComponent != null) {
            // Reject: restore the last accepted value, keeping the caret where the edit happened.
            int cursor = vanillaComponent.getCursorPosition();
            int delta = newValue.length() - lastValidValue.length();
            reverting = true;
            vanillaComponent.setValue(lastValidValue);
            int newCursor = Math.max(0, Math.min(cursor - delta, lastValidValue.length()));
            vanillaComponent.setCursorPosition(newCursor);
            vanillaComponent.setHighlightPos(newCursor);
            reverting = false;
        }
    }

    public Input setResponder(Consumer<String> consumer) {
        // Stored only; the wrapper installed in createVanillaComponent reads this field each change.
        this.responder = consumer;
        return this;
    }

    public Input setFormatter(BiFunction<String, Integer, FormattedCharSequence> formatter) {
        this.formatter = formatter;
        if (vanillaComponent != null) vanillaComponent.addFormatter(formatter::apply);
        return this;
    }

    public Input setFilter(Predicate<String> filter) {
        // Enforced via onEditBoxChanged (revert-on-reject), since EditBox#setFilter is gone in 26.1.
        this.filter = filter;
        return this;
    }

    public String getValue() {
        if (vanillaComponent != null) return vanillaComponent.getValue();
        return "";
    }

    public Input setValue(String value) {
        if (vanillaComponent != null) {
            // Programmatic set is trusted: mark it valid up front so the filter wrapper won't revert it.
            lastValidValue = value;
            vanillaComponent.setValue(value);
        } else {
            initialValue = value;
        }

        return this;
    }

    @Override
    protected Component contentComponent() {
        return Component.literal(initialValue + "..");
    }

    @Override
    public void setFocused(boolean bl) {
        super.setFocused(bl);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isFocused() {
        return super.isFocused();
    }


}

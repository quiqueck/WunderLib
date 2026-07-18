package de.ambertation.wunderlib.ui.layout.components.input;

import de.ambertation.wunderlib.ui.layout.values.Rectangle;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public interface RelativeContainerEventHandler extends ContainerEventHandler {
    Rectangle getInputBounds();

    /**
     * Creates a copy of the given mouse event with its position translated by (-dx, -dy).
     */
    static MouseButtonEvent relativize(MouseButtonEvent event, double dx, double dy) {
        return new MouseButtonEvent(event.x() - dx, event.y() - dy, event.buttonInfo());
    }

    default Optional<GuiEventListener> getChildAt(double d, double e) {
        Rectangle r = getInputBounds();
        return ContainerEventHandler.super.getChildAt(d, e);
    }

    default boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (getFocused() != null) {
            //getFocused().mouseClicked(event, doubleClick);
        }
        Rectangle r = getInputBounds();
        return ContainerEventHandler.super.mouseClicked(relativize(event, r.left, r.top), doubleClick);
    }

    default boolean mouseReleased(MouseButtonEvent event) {
        Rectangle r = getInputBounds();
        return ContainerEventHandler.super.mouseReleased(relativize(event, r.left, r.top));
    }

    default boolean mouseDragged(MouseButtonEvent event, double f, double g) {
        Rectangle r = getInputBounds();
        return ContainerEventHandler.super.mouseDragged(relativize(event, r.left, r.top), f - r.left, g - r.top);
    }

    default boolean mouseScrolled(double d, double e, double f, double g) {
        Rectangle r = getInputBounds();
        return ContainerEventHandler.super.mouseScrolled(d - r.left, e - r.top, f, g);
    }

    default boolean isMouseOver(double x, double y) {
        Rectangle r = getInputBounds();
        boolean res = false;
        for (GuiEventListener c : children()) {
            res |= c.isMouseOver(x - r.left, y - r.top);
        }

        return res || r.contains(x, y);
    }
}


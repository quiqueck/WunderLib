package de.ambertation.wunderlib.ui.vanilla;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.components.*;
import de.ambertation.wunderlib.ui.layout.values.Value;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class LayoutScreen extends Screen {
    protected final int topPadding;
    protected final int bottomPadding;
    protected final int sidePadding;
    protected final int titleSpacing;

    public LayoutScreen(Component component) {
        this(null, component, 20, 10, 20, 15);
    }

    public LayoutScreen(@Nullable Screen parent, Component component) {
        this(parent, component, 20, 10, 20, 15);
    }

    public LayoutScreen(
            @Nullable Screen parent,
            Component component,
            int topPadding,
            int bottomPadding,
            int sidePadding
    ) {
        this(parent, component, topPadding, bottomPadding, sidePadding, 15);
    }

    public LayoutScreen(
            @Nullable Screen parent,
            Component component,
            int topPadding,
            int bottomPadding,
            int sidePadding,
            int titleSpacing
    ) {
        super(component);
        this.parent = parent;
        this.topPadding = topPadding;
        this.bottomPadding = topPadding;
        this.sidePadding = sidePadding;
        this.titleSpacing = titleSpacing;
    }

    @Nullable
    protected Panel main;

    @Nullable
    public final Screen parent;

    protected abstract LayoutComponent<?, ?> initContent();

    protected void openLink(String uri) {
        ConfirmLinkScreen cls = new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(uri);
            }
            this.minecraft.setScreen(this);
        }, uri, true);

        Minecraft.getInstance().setScreen(cls);
    }

    @Override
    protected final void init() {
        super.init();
        main = new Panel(this.width, this.height);
        main.setChild(createScreen(initContent()));

        main.calculateLayout();
        addRenderableWidget(main);
    }

    protected LayoutComponent<?, ?> createTitle() {
        var text = new Text(fit(), fit(), title).centerHorizontal()
                                                .setColor(ColorHelper.WHITE)
                                                .setDebugName("title");
        return text;
    }

    protected LayoutComponent<?, ?> createScreen(LayoutComponent<?, ?> content) {
        VerticalStack rows = new VerticalStack(fill(), fill()).setDebugName("title stack");

        if (topPadding > 0) rows.addSpacer(topPadding);
        rows.add(createTitle());
        rows.addSpacer(titleSpacing);
        rows.add(content);
        if (bottomPadding > 0) rows.addSpacer(bottomPadding);

        if (sidePadding <= 0) return rows;

        HorizontalStack cols = new HorizontalStack(fill(), fill()).setDebugName("padded side");
        cols.addSpacer(sidePadding);
        cols.add(rows);
        cols.addSpacer(sidePadding);

        return cols;
    }

    protected void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        renderDirtBackground(guiGraphics);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        renderBackground(guiGraphics, i, j, f);
        super.render(guiGraphics, i, j, f);
    }

    final protected void closeScreen() {
        onClose();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    public static Value fit() {
        return Value.fit();
    }

    public static Value fitOrFill() {
        return Value.fitOrFill();
    }

    public static Value fill() {
        return Value.fill();
    }

    public static Value fixed(int size) {
        return Value.fixed(size);
    }

    public static Value relative(double percentage) {
        return Value.relative(percentage);
    }

    public static MutableComponent translatable(String key) {
        return Component.translatable(key);
    }

    public static MutableComponent literal(String content) {
        return Component.literal(content);
    }
}

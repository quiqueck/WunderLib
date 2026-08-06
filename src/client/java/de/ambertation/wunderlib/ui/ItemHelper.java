package de.ambertation.wunderlib.ui;

import de.ambertation.wunderlib.WunderLib;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.io.File;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public class ItemHelper {
    /** Fully transparent clear color (matches vanilla's {@code GuiRenderer.CLEAR_COLOR}). */
    private static final Vector4fc CLEAR_COLOR = new Vector4f(0);

    private ItemHelper() {
    }

    public static void renderAll(
            @NotNull Stream<Item> items,
            @NotNull File folder
    ) {
        renderAll(items, 8.f, folder);
    }

    public static void renderAll(
            @NotNull Stream<Item> items,
            float scale,
            @NotNull File folder
    ) {
        folder.mkdirs();
        items.forEach(item -> {
            var id = BuiltInRegistries.ITEM.getKey(item);
            File subFolder = new File(folder, id.getNamespace());
            subFolder.mkdirs();
            ItemStack stack = new ItemStack(item);
            var file = new File(subFolder, id.getPath() + ".png");
            renderToFile(stack, scale, file);
        });
    }

    public static void renderToFile(
            @NotNull ItemLike item,
            @NotNull File file
    ) {
        renderToFile(new ItemStack(item), null, 8.f, file);
    }

    public static void renderToFile(
            @NotNull ItemStack stack,
            @NotNull File file
    ) {
        renderToFile(stack, null, 8.f, file);
    }

    public static void renderToFile(
            @NotNull ItemStack stack,
            float scale,
            @NotNull File file
    ) {
        renderToFile(stack, null, scale, file);
    }

    public static void renderToFile(
            @NotNull ItemStack stack,
            @Nullable String overlayText,
            float scale,
            @NotNull File file
    ) {
        executeRender(stack, overlayText, scale, file);
    }

    private static void executeRender(ItemStack stack, String overlayText, float scale, File file) {
        // Calculate size based on scale - standard item is 16x16
        int size = (int) (16 * scale);

        // Create a render target for our item (useDepth=true; 26.2's TextureTarget takes the color
        // format explicitly and derives the depth format itself)
        RenderTarget framebuffer = new TextureTarget("item_render", size, size, true, GpuFormat.RGBA8_UNORM);

        try {
            renderItemToFramebuffer(stack, overlayText, scale, framebuffer);
            writeFramebufferToFile(framebuffer, file);
        } finally {
            // Clean up the framebuffer
            framebuffer.destroyBuffers();
        }
    }

    /**
     * Renders an item directly into an offscreen GPU texture (the given {@link RenderTarget}'s own
     * color/depth texture views), bypassing {@link GuiGraphicsExtractor}/{@code GuiRenderState} entirely.
     * <p>
     * {@code GuiRenderer.draw()} (the only class that actually draws a {@code GuiRenderState}) is
     * hard-coded to the main window's render target and offers no redirect - so building up a
     * {@code GuiRenderState} via {@link GuiGraphicsExtractor} and hoping it lands in our framebuffer
     * (the previous, broken approach) can never work. Instead this follows vanilla's own pattern for
     * "render 3D content into a private offscreen texture" (see
     * {@code net.minecraft.client.gui.render.pip.PictureInPictureRenderer}/{@code OversizedItemRenderer},
     * and - the closest match for a single standard-size item icon -
     * {@code net.minecraft.client.gui.render.GuiItemAtlas#drawToSlot}): redirect
     * {@link RenderSystem#outputColorTextureOverride}/{@link RenderSystem#outputDepthTextureOverride} to
     * this framebuffer's own texture views, set up a matching orthographic projection, resolve the item
     * via {@link net.minecraft.client.renderer.item.ItemModelResolver}, submit it (and the overlay text)
     * into a private {@link SubmitNodeStorage}, then run {@link FeatureRenderDispatcher#renderAllFeatures}
     * and restore the overrides.
     * <p>
     * The override redirect is still how vanilla 26.2 does this - {@code GuiItemAtlas} sets both
     * overrides around its own {@code renderAllFeatures} call. (26.3 replaced that with an explicit
     * {@code RenderPass} built against the target's views, and moved {@code renderAllFeatures} to a
     * static taking that pass; neither exists here.)
     */
    private static void renderItemToFramebuffer(ItemStack stack, String text, float scale, RenderTarget framebuffer) {
        Minecraft minecraft = Minecraft.getInstance();
        int size = framebuffer.width;

        GpuDevice device = RenderSystem.getDevice();
        // Clear to fully transparent (alpha channel 0) and a fresh depth buffer (reversed-Z: far == 0.0)
        device.createCommandEncoder().clearColorAndDepthTextures(
                framebuffer.getColorTexture(), CLEAR_COLOR, framebuffer.getDepthTexture(), 0.0
        );

        Projection projection = new Projection();
        // invertY=true -> (0,0) is top-left and Y grows downward, matching normal GUI pixel space
        projection.setupOrtho(-1000.0f, 1000.0f, size, size, true);
        ProjectionMatrixBuffer projectionMatrixBuffer = new ProjectionMatrixBuffer("wunderlib_item_render");

        try {
            RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);

            // Redirect all rendering below to our own offscreen texture instead of the main window
            RenderSystem.outputColorTextureOverride = framebuffer.getColorTextureView();
            RenderSystem.outputDepthTextureOverride = framebuffer.getDepthTextureView();

            // Resolve the item's render state the same way GuiGraphicsExtractor#fakeItem does
            // (owner=null, level may be null when no world is loaded - the item model resolver tolerates that)
            TrackingItemStackRenderState itemStackRenderState = new TrackingItemStackRenderState();
            minecraft.getItemModelResolver()
                    .updateForTopItem(itemStackRenderState, stack, ItemDisplayContext.GUI, minecraft.level, null, 0);

            Lighting.Entry lighting = itemStackRenderState.usesBlockLight() ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT;
            minecraft.gameRenderer.lighting().setupFor(lighting);

            // Same transform GuiItemAtlas#drawToSlot uses to render one standard item icon into a
            // size x size square: center + flip Y (model space is Y-up, our projection is Y-down)
            PoseStack itemPose = new PoseStack();
            itemPose.translate(size / 2.0f, size / 2.0f, 0.0f);
            itemPose.scale(size, -size, size);

            FeatureRenderDispatcher featureRenderDispatcher = minecraft.gameRenderer.featureRenderDispatcher();
            SubmitNodeStorage submitNodeStorage = new SubmitNodeStorage();
            itemStackRenderState.submit(itemPose, submitNodeStorage, 15728880 /* full brightness */, OverlayTexture.NO_OVERLAY, 0);

            // Decoration overlay: only the stack-count/custom text is reimplemented here (submitted as a
            // text node into the same SubmitNodeStorage, so it is drawn by the same pass below). The
            // durability bar and cooldown overlay from GuiGraphicsExtractor#itemDecorations are NOT
            // reimplemented: they only ever get built into a GuiRenderState, which nothing but the
            // (bypassed) GuiRenderer.draw() ever consumes, and re-deriving them here as raw colored quads
            // outside that pipeline would need a hand-rolled render pipeline for little practical benefit
            // for an icon-rendering utility (stacks rendered via renderAll() always have count 1, so the
            // bar is never populated to begin with; see the report for details).
            String amount = text;
            if (stack.getCount() > 1 && amount == null) amount = String.valueOf(stack.getCount());
            if (amount != null) {
                PoseStack textPose = new PoseStack();
                textPose.scale(scale, scale, 1.0f);
                float textX = 19 - 2 - minecraft.font.width(amount);
                float textY = 6 + 3;
                submitNodeStorage.submitText(
                        textPose, textX, textY,
                        Component.literal(amount).getVisualOrderText(),
                        true, Font.DisplayMode.NORMAL,
                        15728880, -1, 0, 0
                );
            }

            // Execute everything that was submitted; the overrides above send it to our own texture
            featureRenderDispatcher.renderAllFeatures(submitNodeStorage);
        } finally {
            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;
            projectionMatrixBuffer.close();
        }
    }

    /**
     * Write the framebuffer's color texture to a PNG file, preserving its real alpha channel.
     * <p>
     * This deliberately does NOT use {@link Screenshot#takeScreenshot}: that method's pixel
     * conversion unconditionally does {@code argb | 0xFF000000} (see its decompiled source),
     * forcing every pixel fully opaque - correct for a window screenshot (which has no meaningful
     * alpha) but it silently discards the transparency we specifically render for here. This is a
     * hand-rolled copy of the same GPU texture-to-buffer readback {@code Screenshot.takeScreenshot}
     * uses internally, minus that alpha-forcing step.
     */
    private static void writeFramebufferToFile(RenderTarget framebuffer, File file) {
        try {
            int width = framebuffer.width;
            int height = framebuffer.height;
            GpuTexture sourceTexture = framebuffer.getColorTexture();
            if (sourceTexture == null) {
                throw new IllegalStateException("Tried to capture item render of an incomplete framebuffer");
            }

            GpuDevice device = RenderSystem.getDevice();
            GpuBuffer buffer = device.createBuffer(
                    () -> "WunderLib item render readback", 9, (long) width * height * sourceTexture.getFormat().blockSize()
            );
            CommandEncoder commandEncoder = device.createCommandEncoder();
            commandEncoder.copyTextureToBuffer(sourceTexture, buffer, 0L, () -> {
                try {
                    NativeImage image;
                    try (GpuBufferSlice.MappedView read = buffer.map(true, false)) {
                        image = new NativeImage(width, height, false);
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                int argb = read.data().getInt((x + y * width) * sourceTexture.getFormat().blockSize());
                                // No "| 0xFF000000" here - keep the real (possibly 0) alpha value.
                                image.setPixelABGR(x, height - y - 1, argb);
                            }
                        }
                    }

                    Util.ioPool().execute(() -> {
                        try {
                            file.getParentFile().mkdirs();
                            image.writeToFile(file);
                            WunderLib.LOGGER.info("Successfully saved item render to: " + file.getAbsolutePath());
                        } catch (Exception exception) {
                            WunderLib.LOGGER.warn("Couldn't save item render", exception);
                        } finally {
                            image.close();
                        }
                    });
                } finally {
                    buffer.close();
                }
            }, 0);
        } catch (Exception e) {
            WunderLib.LOGGER.error("Failed to capture item render", e);
        }
    }

    /**
     * Render an item within an existing GUI context (most reliable method)
     * Based on the renderSlot method from Gui class
     */
    public static void renderToExistingContext(
            GuiGraphicsExtractor guiGraphics,
            ItemStack stack,
            @Nullable String overlayText,
            float scale,
            int x, int y
    ) {
        if (stack.isEmpty()) {
            return;
        }

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(scale, scale);

        // Render the item using the same method as the hotbar
        guiGraphics.fakeItem(stack, 0, 0);

        // Render decorations (count, durability bar, cooldown overlay)
        String text = overlayText;
        if (stack.getCount() > 1 && text == null) text = String.valueOf(stack.getCount());
        if (text != null) {
            guiGraphics.itemDecorations(Minecraft.getInstance().font, stack, 0, 0, text);
        }

        guiGraphics.pose().popMatrix();
    }

    /**
     * Alternative method that renders to a specific area within an existing framebuffer
     * Useful for creating item grids or inventories
     */
    public static void renderItemGrid(
            GuiGraphicsExtractor guiGraphics,
            ItemStack[] items,
            int startX, int startY,
            int itemSize, int spacing,
            int columns
    ) {
        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                int col = i % columns;
                int row = i / columns;
                int x = startX + col * (itemSize + spacing);
                int y = startY + row * (itemSize + spacing);

                float scale = itemSize / 16.0f; // 16 is the standard item size
                renderToExistingContext(guiGraphics, items[i], null, scale, x, y);
            }
        }
    }

    /**
     * Utility method to render a single item at standard size (16x16)
     */
    public static void renderStandardItem(GuiGraphicsExtractor guiGraphics, ItemStack stack, int x, int y) {
        renderToExistingContext(guiGraphics, stack, null, 1.0f, x, y);
    }
}

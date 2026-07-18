package de.ambertation.wunderlib.ui;

import de.ambertation.wunderlib.WunderLib;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Registers the {@code /wunderlib renderItems <namespace>} client command, which renders every
 * registered {@link Item} of a given namespace (this naturally includes {@code BlockItem}s, since
 * they are a subtype of {@link Item}) to
 * {@code <.minecraft>/screenshots/<namespace>/<path>.png} with a transparent background, using
 * {@link ItemHelper#renderAll}.
 */
public class ItemRenderCommand {
    private ItemRenderCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, buildContext) -> dispatcher.register(
                        ClientCommands.literal("wunderlib")
                                .then(
                                        ClientCommands.literal("renderItems")
                                                .then(
                                                        ClientCommands.argument("namespace", StringArgumentType.word())
                                                                .executes(ItemRenderCommand::execute)
                                                )
                                )
                )
        );
    }

    private static int execute(CommandContext<FabricClientCommandSource> context) {
        String namespace = StringArgumentType.getString(context, "namespace");
        FabricClientCommandSource source = context.getSource();

        if (source.getClient().level == null) {
            source.sendError(Component.literal("/wunderlib renderItems requires a loaded world (item rendering needs a client level)."));
            return 0;
        }

        List<Item> items = BuiltInRegistries.ITEM.stream()
                .filter(item -> {
                    Identifier id = BuiltInRegistries.ITEM.getKey(item);
                    return id.getNamespace().equals(namespace);
                })
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            source.sendError(Component.literal("No items found for namespace '" + namespace + "'."));
            return 0;
        }

        File screenshotsFolder = new File(source.getClient().gameDirectory, Screenshot.SCREENSHOT_DIR);
        File targetFolder = new File(screenshotsFolder, namespace);

        source.sendFeedback(
                Component.literal(
                        "Rendering " + items.size() + " item(s) from namespace '" + namespace
                                + "' to " + targetFolder.getAbsolutePath() + " ..."
                )
        );

        try {
            // ItemHelper.renderAll creates a "<namespace>" sub-folder inside the folder it is given,
            // so we hand it the screenshots root, not targetFolder itself.
            ItemHelper.renderAll(items.stream(), screenshotsFolder);
            source.sendFeedback(
                    Component.literal("Finished rendering " + items.size() + " item(s) for namespace '" + namespace + "'.")
            );
        } catch (Exception e) {
            WunderLib.LOGGER.error("Failed to render items for namespace '" + namespace + "'", e);
            source.sendError(Component.literal("Rendering failed, see the log for details: " + e));
            return 0;
        }

        return items.size();
    }
}

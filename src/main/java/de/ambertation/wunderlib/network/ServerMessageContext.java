package de.ambertation.wunderlib.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

/**
 * Context handed to a {@link ServerBoundMessage}'s {@code handle} callback and available while preparing a
 * {@link ClientBoundMessage}. Contains only common-safe types.
 */
public record ServerMessageContext(ServerPlayer player, MinecraftServer server, PacketSender responseSender) {
}

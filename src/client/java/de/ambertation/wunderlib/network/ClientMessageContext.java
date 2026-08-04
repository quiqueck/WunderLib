package de.ambertation.wunderlib.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

import org.jetbrains.annotations.Nullable;

/**
 * Context available to the client-only half of a message: preparing a {@link ServerBoundMessage} before it's
 * sent, or handling a received {@link ClientBoundMessage}.
 *
 * @param responseSender {@code null} while preparing a message to send; present while handling a received one
 */
public record ClientMessageContext(LocalPlayer player, Minecraft client, @Nullable PacketSender responseSender) {
}

package de.ambertation.wunderlib.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class SendToServerImpl implements SendToServerAdapter{
    public void sendToServer(ResourceLocation channelName, FriendlyByteBuf buf) {
        ClientPlayNetworking.send(channelName, buf);
    }

    public static void registerAdapter(){
        final SendToServerImpl adapter = new SendToServerImpl();
        ServerBoundPacketHandler.packetHandlers.forEach(packetHandler -> {
            packetHandler.sendToServerAdapter = adapter;
        });
    }
}

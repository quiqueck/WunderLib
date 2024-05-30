package de.ambertation.wunderlib.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SendToServerImpl implements SendToServerAdapter {
    public void sendToServer(ResourceLocation channelName, FriendlyByteBuf buf) {
        //ClientPlayNetworking.send(channelName, buf);
        System.err.println("SendToServerImpl.sendToServer: " + channelName + " not implemented");
        //TODO: 1.21 Disabled network stack
    }

    public static void registerAdapter() {
        final SendToServerImpl adapter = new SendToServerImpl();
        ServerBoundPacketHandler.packetHandlers.forEach(packetHandler -> {
            packetHandler.sendToServerAdapter = adapter;
        });
    }
}

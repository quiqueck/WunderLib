package de.ambertation.wunderlib.network;

import net.minecraft.client.Minecraft;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import org.jetbrains.annotations.ApiStatus;

public class SendToServerImpl implements SendToServerAdapter {
    @Override
    public <P> void sendToServer(ServerBoundMessage<P> key) {
        Minecraft client = Minecraft.getInstance();
        ClientMessageContext ctx = new ClientMessageContext(client.player, client, null);
        P payload = ClientNetworkRegistry.prepare(key, ctx);
        sendToServer(key, payload);
    }

    @Override
    public <P> void sendToServer(ServerBoundMessage<P> key, P payload) {
        ClientPlayNetworking.send(new Envelope<>(payload, key.type));
    }

    @ApiStatus.Internal
    public static void registerAdapter() {
        NetworkRegistry.registerSendToServerAdapter(new SendToServerImpl());
    }
}

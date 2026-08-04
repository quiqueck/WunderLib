package de.ambertation.wunderlib.network;

import de.ambertation.wunderlib.utils.EnvHelper;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import org.jetbrains.annotations.ApiStatus;

public class SendToClientImpl implements SendToClientAdapter {
    @ApiStatus.Internal
    public static void registerAdapter() {
        NetworkRegistry.registerSendToClientAdapter(new SendToClientImpl());
    }

    @Override
    public <P> void registerReceiver(ClientBoundMessage<P> key) {
        ClientPlayConnectionEvents.INIT.register((handler, client) ->
                ClientPlayNetworking.registerReceiver(key.type, (envelope, context) -> receiveOnClient(key, envelope, context))
        );
    }

    <P> void receiveOnClient(ClientBoundMessage<P> key, Envelope<P> envelope, ClientPlayNetworking.Context context) {
        if (!EnvHelper.isClient()) return;

        ClientMessageContext ctx = new ClientMessageContext(context.client().player, context.client(), context.responseSender());
        ClientNetworkRegistry.dispatch(key, envelope.payload, ctx, context.client());
    }
}

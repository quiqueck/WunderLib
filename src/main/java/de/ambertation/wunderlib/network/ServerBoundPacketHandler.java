package de.ambertation.wunderlib.network;

import de.ambertation.wunderlib.utils.EnvHelper;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.LinkedList;
import java.util.List;

public abstract class ServerBoundPacketHandler<D> {
    protected CustomPacketPayload.Type CHANNEL;
    SendToServerAdapter sendToServerAdapter;
    static List<ServerBoundPacketHandler<?>> packetHandlers = new LinkedList<>();

    public static <D, T extends ServerBoundPacketHandler<D>> T register(ResourceLocation channel, T packetHandler) {
        packetHandler.CHANNEL = new CustomPacketPayload.Type(channel);
        packetHandlers.add(packetHandler);
        packetHandler.onRegister();

        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayNetworking.registerReceiver(
                    handler,
                    packetHandler.CHANNEL,
                    packetHandler::receiveOnServer
            );
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayNetworking.unregisterReceiver(handler, packetHandler.CHANNEL.id());
        });

        return packetHandler;
    }

    public void sendToServer(D content) {
        if (sendToServerAdapter != null && EnvHelper.isClient()) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            serializeOnClient(buf, content);
            sendToServerAdapter.sendToServer(CHANNEL.id(), buf);
        } else {
            //
        }
    }

    private void receiveOnServer(
            CustomPacketPayload payload,
            ServerPlayNetworking.Context context
    ) {
//        receiveOnServer(
//                context.player().getServer(),
//                context.player(),
//                context.responseSender().
//                context.responseSender()
//
//        );
        System.err.println("ServerBoundPacketHandler.receiveOnServer not implemented");
        //TODO: 1.21 Network stack rework
    }

    void receiveOnServer(
            MinecraftServer server,
            ServerPlayer player,
            ServerGamePacketListenerImpl handler,
            FriendlyByteBuf buf,
            PacketSender responseSender
    ) {
        D content = deserializeOnServer(buf, player, responseSender);
        server.execute(() -> processOnGameThread(server, player, content));
    }

    protected abstract void serializeOnClient(FriendlyByteBuf buf, D content);

    protected abstract D deserializeOnServer(FriendlyByteBuf buf, ServerPlayer player, PacketSender responseSender);

    protected abstract void processOnGameThread(MinecraftServer server, ServerPlayer player, D content);

    protected void onRegister() {
    }
}

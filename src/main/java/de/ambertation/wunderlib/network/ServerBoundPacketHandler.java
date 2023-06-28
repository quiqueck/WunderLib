package de.ambertation.wunderlib.network;

import de.ambertation.wunderlib.utils.EnvHelper;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

import java.util.LinkedList;
import java.util.List;

public abstract class ServerBoundPacketHandler<D> {
    protected ResourceLocation CHANNEL;
    SendToServerAdapter sendToServerAdapter;
    static List<ServerBoundPacketHandler<?>> packetHandlers = new LinkedList<>();

    public static <D, T extends ServerBoundPacketHandler<D>> T register(ResourceLocation channel, T packetHandler) {
        packetHandler.CHANNEL = channel;
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
            ServerPlayNetworking.unregisterReceiver(handler, packetHandler.CHANNEL);
        });

        return packetHandler;
    }

    public void sendToServer(D content) {
        if (sendToServerAdapter!=null && EnvHelper.isClient()) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            serializeOnClient(buf, content);
            sendToServerAdapter.sendToServer(CHANNEL, buf);
        } else {
            //
        }
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

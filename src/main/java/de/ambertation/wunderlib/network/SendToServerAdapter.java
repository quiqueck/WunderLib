package de.ambertation.wunderlib.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface SendToServerAdapter {
    void sendToServer(ResourceLocation channelName, FriendlyByteBuf buf);
}

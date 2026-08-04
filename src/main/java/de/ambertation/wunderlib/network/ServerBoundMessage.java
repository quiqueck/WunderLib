package de.ambertation.wunderlib.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * A client-to-server message. Handlers are attached separately, any number of them, via
 * {@link NetworkRegistry#addServerHandler} - each tagged with an {@link ExecutionPhase} - since they only ever
 * run on the server. The client-side counterpart - building the payload before it's sent - is registered
 * separately, from client-only code, via {@code ClientNetworkRegistry.setClientPrepare}: nothing in this class
 * or {@link NetworkRegistry} ever needs to reference a client-only type.
 *
 * @param <P> the message's payload type
 */
public final class ServerBoundMessage<P> implements MessageKey<P> {
    final ResourceLocation id;
    final StreamCodec<RegistryFriendlyByteBuf, P> codec;
    final CustomPacketPayload.Type<Envelope<P>> type;

    ServerBoundMessage(
            ResourceLocation id,
            StreamCodec<RegistryFriendlyByteBuf, P> codec,
            CustomPacketPayload.Type<Envelope<P>> type
    ) {
        this.id = id;
        this.codec = codec;
        this.type = type;
    }

    @Override
    public ResourceLocation id() {
        return id;
    }
}

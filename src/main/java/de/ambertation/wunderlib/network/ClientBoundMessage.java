package de.ambertation.wunderlib.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Function;

/**
 * A server-to-client message. {@code prepare} is optional, supplied at registration time (common code, since it
 * only ever runs on the server, building the payload for a specific player): it exists for the case where the
 * server should derive the payload from the player itself. Most senders already have the payload data at the
 * call site and use {@code NetworkRegistry.sendToClient(player, key, payload)} directly instead. The
 * client-side counterpart - reacting to the message once received - is registered separately, from client-only
 * code, via {@code ClientNetworkRegistry.addClientHandler}: nothing in this class or {@link NetworkRegistry}
 * ever needs to reference a client-only type.
 *
 * @param <P> the message's payload type
 */
public final class ClientBoundMessage<P> implements MessageKey<P> {
    final Identifier id;
    final StreamCodec<RegistryFriendlyByteBuf, P> codec;
    final Function<ServerPlayer, P> prepare;
    final CustomPacketPayload.Type<Envelope<P>> type;

    ClientBoundMessage(
            Identifier id,
            StreamCodec<RegistryFriendlyByteBuf, P> codec,
            Function<ServerPlayer, P> prepare,
            CustomPacketPayload.Type<Envelope<P>> type
    ) {
        this.id = id;
        this.codec = codec;
        this.prepare = prepare;
        this.type = type;
    }

    @Override
    public Identifier id() {
        return id;
    }
}

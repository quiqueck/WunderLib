package de.ambertation.wunderlib.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Internal wire wrapper around a message's payload. One {@code Envelope} class is reused for every registered
 * {@link MessageKey}; erasure makes that safe since each key carries its own distinct {@link Type} instance.
 */
final class Envelope<P> implements CustomPacketPayload {
    final P payload;
    final Type<Envelope<P>> type;

    Envelope(P payload, Type<Envelope<P>> type) {
        this.payload = payload;
        this.type = type;
    }

    @Override
    public Type<Envelope<P>> type() {
        return type;
    }
}

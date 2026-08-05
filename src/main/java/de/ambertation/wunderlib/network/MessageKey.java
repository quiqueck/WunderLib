package de.ambertation.wunderlib.network;

import net.minecraft.resources.Identifier;

/**
 * Identifies a registered network message and its direction. Obtained from
 * {@link NetworkRegistry#registerServerBound} / {@link NetworkRegistry#registerClientBound}; never implemented
 * directly.
 *
 * @param <P> the message's payload type
 */
public sealed interface MessageKey<P> permits ServerBoundMessage, ClientBoundMessage {
    Identifier id();
}

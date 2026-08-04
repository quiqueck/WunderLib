package de.ambertation.wunderlib.network;

/**
 * Bridges a {@link ClientBoundMessage}'s reception to the actual client-only receiver registration.
 * Implemented once, by client-only code, and registered via {@code ClientNetworkRegistry} at client startup.
 */
public interface SendToClientAdapter {
    <P> void registerReceiver(ClientBoundMessage<P> key);
}

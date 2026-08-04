package de.ambertation.wunderlib.network;

/**
 * Bridges {@link NetworkRegistry#sendToServer} to the actual client-only send call. Implemented once, by
 * client-only code, and registered via {@code ClientNetworkRegistry} at client startup.
 */
public interface SendToServerAdapter {
    /**
     * Builds the payload via the registered {@code ClientNetworkRegistry.setClientPrepare} callback, then sends
     * it.
     */
    <P> void sendToServer(ServerBoundMessage<P> key);

    /**
     * Sends an already-built payload directly, skipping the prepare callback - for the common case where the
     * caller already has the data to send.
     */
    <P> void sendToServer(ServerBoundMessage<P> key, P payload);
}

package de.ambertation.wunderlib.network;

/**
 * When a registered message handler runs, relative to the packet's arrival.
 */
public enum ExecutionPhase {
    /** Runs immediately, on the network thread, as the packet arrives. */
    NETWORK_THREAD,
    /** Scheduled onto the receiving side's game thread - safe to touch game state. */
    GAME_THREAD
}

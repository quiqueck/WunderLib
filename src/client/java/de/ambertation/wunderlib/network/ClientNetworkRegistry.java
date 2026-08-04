package de.ambertation.wunderlib.network;

import de.ambertation.wunderlib.WunderLib;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Client-side registry mapping a {@link ServerBoundMessage}/{@link ClientBoundMessage} to the client-only
 * callback that prepares or handles it. Populated from a mod's {@code ClientModInitializer}; this is the only
 * place allowed to touch client-only types such as {@code Minecraft} for these messages, which is why it's a
 * separate class from {@link NetworkRegistry}. Mirrors the {@code wover.client.traits} registries.
 */
public final class ClientNetworkRegistry {
    private record PhasedHandler<P>(ExecutionPhase phase, BiConsumer<P, ClientMessageContext> handler) {
    }

    private static final Map<ServerBoundMessage<?>, Function<ClientMessageContext, ?>> PREPARE = new HashMap<>();
    private static final Map<ClientBoundMessage<?>, List<PhasedHandler<?>>> CLIENT_HANDLERS = new HashMap<>();

    private ClientNetworkRegistry() {
    }

    /**
     * Registers the callback that builds a {@link ServerBoundMessage}'s payload just before it's sent.
     */
    public static <P> void setClientPrepare(ServerBoundMessage<P> key, Function<ClientMessageContext, P> prepare) {
        PREPARE.put(key, prepare);
    }

    /**
     * Attaches a client-side handler to a {@link ClientBoundMessage}. Any number of handlers may be attached, in
     * any combination of {@link ExecutionPhase}s; all of them run for every received message.
     */
    public static <P> void addClientHandler(
            ClientBoundMessage<P> key,
            ExecutionPhase phase,
            BiConsumer<P, ClientMessageContext> handler
    ) {
        CLIENT_HANDLERS.computeIfAbsent(key, k -> new ArrayList<>()).add(new PhasedHandler<>(phase, handler));
    }

    @SuppressWarnings("unchecked")
    static <P> P prepare(ServerBoundMessage<P> key, ClientMessageContext ctx) {
        Function<ClientMessageContext, P> fn = (Function<ClientMessageContext, P>) PREPARE.get(key);
        if (fn == null) {
            throw new IllegalStateException("No client prepare registered for " + key.id());
        }
        return fn.apply(ctx);
    }

    @SuppressWarnings("unchecked")
    static <P> void dispatch(ClientBoundMessage<P> key, P payload, ClientMessageContext ctx, Minecraft client) {
        List<PhasedHandler<?>> handlers = CLIENT_HANDLERS.get(key);
        if (handlers == null || handlers.isEmpty()) {
            WunderLib.LOGGER.warn("Received {} but no client handler is registered for it.", key.id());
            return;
        }

        for (PhasedHandler<?> phasedHandler : handlers) {
            BiConsumer<P, ClientMessageContext> handler = (BiConsumer<P, ClientMessageContext>) phasedHandler.handler();
            if (phasedHandler.phase() == ExecutionPhase.NETWORK_THREAD) {
                handler.accept(payload, ctx);
            } else {
                client.execute(() -> handler.accept(payload, ctx));
            }
        }
    }
}

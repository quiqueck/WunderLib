package de.ambertation.wunderlib.network;

import de.ambertation.wunderlib.WunderLib;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus;

/**
 * Registers client&lt;-&gt;server messages and sends them. Replaces the old {@code NetworkPayload} class
 * hierarchy: a message is a plain, codec-serializable payload type {@code P} plus callbacks supplied at
 * registration, not a class extending an abstract base.
 * <p>
 * A {@link ServerBoundMessage} can have any number of server-side handlers attached via
 * {@link #addServerHandler}, each tagged with an {@link ExecutionPhase} - mirrors how {@code
 * ClientNetworkRegistry.addClientHandler} works for the client-side reaction to a {@link ClientBoundMessage}.
 * Common code only ever registers the callback that's safe to run where it's declared - server-side handlers
 * here, a {@link ClientBoundMessage}'s {@code prepare} here too. The client-only half of each direction is
 * wired separately, from client-only code, via {@code ClientNetworkRegistry}. See {@link ServerBoundMessage} and
 * {@link ClientBoundMessage} for why.
 */
public final class NetworkRegistry {
    private NetworkRegistry() {
    }

    private record PhasedHandler<P>(ExecutionPhase phase, BiConsumer<P, ServerMessageContext> handler) {
    }

    private static SendToServerAdapter sendToServerAdapter;
    private static SendToClientAdapter sendToClientAdapter;
    private static final List<ClientBoundMessage<?>> pendingClientBoundKeys = new ArrayList<>();
    private static final Map<ServerBoundMessage<?>, List<PhasedHandler<?>>> SERVER_HANDLERS = new HashMap<>();

    /**
     * {@link CustomPacketPayload#createType(String)} treats its whole argument as a path under the default
     * ("minecraft") namespace rather than parsing "namespace:path" - it does not accept a colon. Folding the
     * namespace into the path this way keeps every registered id's own namespace out of the resulting type's
     * path, avoiding an invalid-character crash at registration.
     */
    private static <P> CustomPacketPayload.Type<Envelope<P>> typeFor(ResourceLocation id) {
        return CustomPacketPayload.createType(id.getNamespace() + "_" + id.getPath());
    }

    private static <P> StreamCodec<RegistryFriendlyByteBuf, Envelope<P>> envelopeCodec(
            StreamCodec<RegistryFriendlyByteBuf, P> codec,
            CustomPacketPayload.Type<Envelope<P>> type
    ) {
        return CustomPacketPayload.codec(
                (envelope, buf) -> codec.encode(buf, envelope.payload),
                buf -> new Envelope<>(codec.decode(buf), type)
        );
    }

    /**
     * Registers a client-to-server message with no handlers attached yet - use {@link #addServerHandler} to add
     * any number of them.
     */
    public static <P> ServerBoundMessage<P> registerServerBound(
            ResourceLocation id,
            StreamCodec<RegistryFriendlyByteBuf, P> codec
    ) {
        CustomPacketPayload.Type<Envelope<P>> type = typeFor(id);
        ServerBoundMessage<P> key = new ServerBoundMessage<>(id, codec, type);

        PayloadTypeRegistry.playC2S().register(type, envelopeCodec(codec, type));

        ServerPlayConnectionEvents.INIT.register((handler, initServer) ->
                ServerPlayNetworking.registerReceiver(handler, type, (envelope, context) -> {
                    ServerMessageContext ctx = new ServerMessageContext(
                            context.player(),
                            context.player().getServer(),
                            context.responseSender()
                    );
                    dispatchServerHandlers(key, envelope.payload, ctx);
                })
        );

        return key;
    }

    /**
     * Registers a client-to-server message with a single, {@link ExecutionPhase#GAME_THREAD} handler - the
     * common case. Equivalent to {@code registerServerBound(id, codec)} followed by {@code addServerHandler(key,
     * ExecutionPhase.GAME_THREAD, handle)}.
     */
    public static <P> ServerBoundMessage<P> registerServerBound(
            ResourceLocation id,
            StreamCodec<RegistryFriendlyByteBuf, P> codec,
            BiConsumer<P, ServerMessageContext> handle
    ) {
        ServerBoundMessage<P> key = registerServerBound(id, codec);
        addServerHandler(key, ExecutionPhase.GAME_THREAD, handle);
        return key;
    }

    /**
     * Attaches another server-side handler to a {@link ServerBoundMessage}. Any number of handlers may be
     * attached, in any combination of phases; all of them run for every received message.
     */
    public static <P> void addServerHandler(
            ServerBoundMessage<P> key,
            ExecutionPhase phase,
            BiConsumer<P, ServerMessageContext> handler
    ) {
        SERVER_HANDLERS.computeIfAbsent(key, k -> new ArrayList<>()).add(new PhasedHandler<>(phase, handler));
    }

    @SuppressWarnings("unchecked")
    private static <P> void dispatchServerHandlers(ServerBoundMessage<P> key, P payload, ServerMessageContext ctx) {
        List<PhasedHandler<?>> handlers = SERVER_HANDLERS.get(key);
        if (handlers == null || handlers.isEmpty()) {
            WunderLib.LOGGER.warn("Received {} but no server handler is registered for it.", key.id());
            return;
        }

        for (PhasedHandler<?> phasedHandler : handlers) {
            BiConsumer<P, ServerMessageContext> handler = (BiConsumer<P, ServerMessageContext>) phasedHandler.handler();
            if (phasedHandler.phase() == ExecutionPhase.NETWORK_THREAD) {
                handler.accept(payload, ctx);
            } else {
                final var server = ctx.server();
                if (server != null) server.execute(() -> handler.accept(payload, ctx));
            }
        }
    }

    /**
     * Registers a server-to-client message with no automatic payload derivation - every send must go through
     * {@link #sendToClient(ServerPlayer, ClientBoundMessage, Object)} with an explicit payload.
     */
    public static <P> ClientBoundMessage<P> registerClientBound(
            ResourceLocation id,
            StreamCodec<RegistryFriendlyByteBuf, P> codec
    ) {
        return registerClientBound(id, codec, null);
    }

    /**
     * Registers a server-to-client message whose payload can also be derived automatically from the player via
     * {@code prepare}, for {@link #sendToClient(ServerPlayer, ClientBoundMessage)} - useful when the server
     * should compute what to send rather than the caller supplying it directly.
     */
    public static <P> ClientBoundMessage<P> registerClientBound(
            ResourceLocation id,
            StreamCodec<RegistryFriendlyByteBuf, P> codec,
            Function<ServerPlayer, P> prepare
    ) {
        CustomPacketPayload.Type<Envelope<P>> type = typeFor(id);
        ClientBoundMessage<P> key = new ClientBoundMessage<>(id, codec, prepare, type);

        PayloadTypeRegistry.playS2C().register(type, envelopeCodec(codec, type));

        if (sendToClientAdapter != null) {
            sendToClientAdapter.registerReceiver(key);
        } else {
            pendingClientBoundKeys.add(key);
        }

        return key;
    }

    /**
     * Sends a message built from the registered {@code ClientNetworkRegistry.setClientPrepare} callback.
     */
    public static <P> void sendToServer(ServerBoundMessage<P> key) {
        if (sendToServerAdapter == null) {
            WunderLib.LOGGER.warn("sendToServer({}) called before the client is ready - dropping.", key.id());
            return;
        }
        sendToServerAdapter.sendToServer(key);
    }

    /**
     * Sends {@code payload} directly - the common case, for messages whose caller already has the data to send.
     */
    public static <P> void sendToServer(ServerBoundMessage<P> key, P payload) {
        if (sendToServerAdapter == null) {
            WunderLib.LOGGER.warn("sendToServer({}) called before the client is ready - dropping.", key.id());
            return;
        }
        sendToServerAdapter.sendToServer(key, payload);
    }

    /**
     * Sends a payload built from the {@code prepare} callback given at registration.
     */
    public static <P> void sendToClient(ServerPlayer player, ClientBoundMessage<P> key) {
        if (key.prepare == null) {
            WunderLib.LOGGER.warn("sendToClient({}) called with no prepare callback registered.", key.id());
            return;
        }
        sendToClient(player, key, key.prepare.apply(player));
    }

    /**
     * Sends {@code payload} directly - the common case, for messages whose caller already has the data to send.
     */
    public static <P> void sendToClient(ServerPlayer player, ClientBoundMessage<P> key, P payload) {
        ServerPlayNetworking.send(player, new Envelope<>(payload, key.type));
    }

    public static <P> void sendToClient(Collection<ServerPlayer> players, ClientBoundMessage<P> key) {
        players.forEach(player -> sendToClient(player, key));
    }

    public static <P> void sendToClient(ServerLevel level, ClientBoundMessage<P> key) {
        sendToClient(level.players(), key);
    }

    /**
     * Sends {@code payload} directly to every player in {@code players} - the common case.
     */
    public static <P> void sendToClient(Collection<ServerPlayer> players, ClientBoundMessage<P> key, P payload) {
        players.forEach(player -> sendToClient(player, key, payload));
    }

    /**
     * Sends {@code payload} directly to every player in {@code level} - the common case.
     */
    public static <P> void sendToClient(ServerLevel level, ClientBoundMessage<P> key, P payload) {
        sendToClient(level.players(), key, payload);
    }

    @ApiStatus.Internal
    public static void registerSendToServerAdapter(SendToServerAdapter adapter) {
        sendToServerAdapter = adapter;
    }

    @ApiStatus.Internal
    public static void registerSendToClientAdapter(SendToClientAdapter adapter) {
        sendToClientAdapter = adapter;
        for (ClientBoundMessage<?> key : pendingClientBoundKeys) {
            adapter.registerReceiver(key);
        }
        pendingClientBoundKeys.clear();
    }
}

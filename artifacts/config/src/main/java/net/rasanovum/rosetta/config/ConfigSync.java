package net.rasanovum.rosetta.config;

import com.google.gson.*;
import eu.midnightdust.lib.config.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.rasanovum.rosetta.event.*;
import net.rasanovum.rosetta.loaders.Platform;
import net.rasanovum.rosetta.network.*;
import net.rasanovum.rosetta.util.EntityCompat;
import net.rasanovum.rosetta.config.mixin.ConfigEntryAccessor;
//? if >=26.1 {
/*import net.minecraft.server.players.NameAndId;
*///?}
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ConfigSync {
    public static final int MAX_LENGTH = 30000;
    private static final Gson JSON = new Gson();
    private static final Map<String, Consumer<MinecraftServer>> SERVER_CHANGES = new ConcurrentHashMap<>();
    private static final ClassValue<List<Field>> SERVER_FIELDS = new ClassValue<>() {
        @Override protected List<Field> computeValue(Class<?> type) {
            return Arrays.stream(type.getFields()).filter(ConfigSync::isServerSetting).toList();
        }
    };
    private static final ClassValue<EditAccess> EDIT_ACCESS = new ClassValue<>() {
        @Override protected EditAccess computeValue(Class<?> type) {
            int minimumLevel = Integer.MAX_VALUE;
            boolean allowOwner = false;
            for (Field field : fields(type)) {
                ServerSetting setting = field.getAnnotation(ServerSetting.class);
                minimumLevel = Math.min(minimumLevel, permissionLevel(setting));
                allowOwner |= !setting.requireCheats();
            }
            return new EditAccess(minimumLevel, allowOwner);
        }
    };
    private static final double DEFAULT_MIN = defaultMinimum();
    private static volatile MinecraftServer server;

    private record EditAccess(int minimumLevel, boolean allowOwner) {
        boolean allows(ServerPlayer player) {
            return (minimumLevel != Integer.MAX_VALUE && EntityCompat.hasPermission(player, minimumLevel))
                    || (allowOwner && isSingleplayerOwner(player));
        }
    }

    private ConfigSync() {}

    static void initialize() {
        RosettaNetwork.channel("rosetta_config")
                .serverbound("edit", Edit.class, Edit::new, Edit::handle)
                .clientbound("sync", Sync.class, Sync::new, Sync::handle);
        ServerHooks.register(new ServerHooks.Callbacks() {
            @Override public void onServerStarting(MinecraftServer active) { server = active; }
            @Override public void onServerStopping(MinecraftServer active) { server = null; }
            @Override public void onPlayerJoin(ServerPlayer player) {
                MidnightConfig.configInstances.forEach((id, config) -> sync(id, player));
            }
        });
        if (Platform.INSTANCE.isClientSide()) {
            ClientHooks.register(new ClientHooks.Callbacks() {
                @Override public void onDisconnect() { ClientSync.disconnect(); }
            });
        }
    }

    public static void onServerChange(String id, Consumer<MinecraftServer> callback) {
        SERVER_CHANGES.put(id, Objects.requireNonNull(callback));
    }

    public static boolean onServerThread() {
        MinecraftServer active = server;
        return active != null && active.isSameThread();
    }

    public static void changed(String id) {
        if (!onServerThread()) return;
        try {
            Consumer<MinecraftServer> callback = SERVER_CHANGES.get(id);
            if (callback != null) callback.accept(server);
        } catch (RuntimeException error) {
            System.getLogger(ConfigSync.class.getName()).log(System.Logger.Level.ERROR, id + " config refresh failed", error);
        } finally {
            broadcast(id);
        }
    }

    public static boolean isServerSetting(Field field) {
        return field != null && field.isAnnotationPresent(ServerSetting.class)
                && field.isAnnotationPresent(MidnightConfig.Entry.class)
                && Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers());
    }

    public static List<Field> fields(Class<?> type) {
        return SERVER_FIELDS.get(type);
    }

    public static Object copy(Object value) { return value instanceof List<?> list ? new ArrayList<>(list) : value; }

    public static Object read(Field field) {
        try {
            return copy(field.get(null));
        } catch (IllegalAccessException error) {
            throw new IllegalStateException(error);
        }
    }

    private static void set(Field field, Object value) {
        try {
            field.set(null, copy(value));
        } catch (IllegalAccessException error) {
            throw new IllegalStateException(error);
        }
    }

    private static boolean canEdit(ServerPlayer player, Field field) {
        ServerSetting setting = field.getAnnotation(ServerSetting.class);
        return EntityCompat.hasPermission(player, permissionLevel(setting))
                || (!setting.requireCheats() && isSingleplayerOwner(player));
    }

    private static int permissionLevel(ServerSetting setting) {
        return setting.requireCheats() ? Math.max(2, setting.permissionLevel()) : setting.permissionLevel();
    }

    private static boolean isSingleplayerOwner(ServerPlayer player) {
        MinecraftServer active = EntityCompat.getPlayerServer(player);
        if (active == null) return false;
        //? if >=26.1 {
        /*return active.isSingleplayerOwner(new NameAndId(player.getGameProfile()));
        *///?} else {
        return active.isSingleplayerOwner(player.getGameProfile());
        //?}
    }

    private static JsonObject snapshot(MidnightConfig config) {
        JsonObject values = new JsonObject();
        fields(config.configClass).forEach(field -> values.add(field.getName(), JSON.toJsonTree(read(field))));
        return values;
    }

    private static String syncValues(String id, MidnightConfig config) {
        EDIT_ACCESS.get(config.configClass);
        String values = snapshot(config).toString();
        if (values.equals("{}")) return null;
        if (values.length() > MAX_LENGTH) {
            System.getLogger(ConfigSync.class.getName()).log(System.Logger.Level.ERROR, id + " config exceeds sync packet limit");
            return null;
        }
        return values;
    }

    private static void broadcast(String id) {
        MidnightConfig config = MidnightConfig.configInstances.get(id);
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (config == null || players.isEmpty()) return;
        String values = syncValues(id, config);
        if (values == null) return;
        for (ServerPlayer player : players) sendSync(id, config, values, player);
    }

    public static void sync(String id, ServerPlayer player) {
        MidnightConfig config = MidnightConfig.configInstances.get(id);
        if (config == null) return;
        String values = syncValues(id, config);
        if (values != null) sendSync(id, config, values, player);
    }

    private static void sendSync(String id, MidnightConfig config, String values, ServerPlayer player) {
        Set<String> editable = new HashSet<>();
        for (Field field : fields(config.configClass)) {
            if (canEdit(player, field)) editable.add(field.getName());
        }
        RosettaNetwork.sendToPlayer(new Sync(id, values, editable), player);
    }

    private static Map<Field, Object> decode(MidnightConfig config, String json) {
        if (json.length() > MAX_LENGTH) throw new IllegalArgumentException("Config update is too large");
        Map<Field, Object> values = new LinkedHashMap<>();
        for (var entry : JsonParser.parseString(json).getAsJsonObject().entrySet()) {
            try {
                Field field = config.configClass.getField(entry.getKey());
                if (!isServerSetting(field)) throw new IllegalArgumentException("Not a server setting: " + entry.getKey());
                Object value = JSON.fromJson(entry.getValue(), field.getGenericType());
                if (value == null) throw new IllegalArgumentException("Missing value: " + entry.getKey());
                values.put(field, value);
            } catch (NoSuchFieldException error) {
                throw new IllegalArgumentException("Unknown setting: " + entry.getKey(), error);
            }
        }
        return values;
    }

    private static double defaultMinimum() {
        try {
            return (double) MidnightConfig.Entry.class.getMethod("min").getDefaultValue();
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static void validate(Field field, Object value) {
        MidnightConfig.Entry bounds = field.getAnnotation(MidnightConfig.Entry.class);
        if (value instanceof List<?> list) {
            for (Object element : list) {
                if (element == null) throw new IllegalArgumentException("Null list element: " + field.getName());
                validateNumber(field, bounds, element);
            }
        } else {
            validateNumber(field, bounds, value);
        }
    }

    private static void validateNumber(Field field, MidnightConfig.Entry bounds, Object value) {
        if (!(value instanceof Number number)) return;
        double numeric = number.doubleValue();
        if (!Double.isFinite(numeric)) {
            throw new IllegalArgumentException("Non-finite numeric value: " + field.getName());
        }
        if (bounds != null && ((bounds.min() != DEFAULT_MIN && numeric < bounds.min()) || numeric > bounds.max())) {
            throw new IllegalArgumentException("Value outside permitted bounds: " + field.getName());
        }
    }

    private static Set<String> readEditable(FriendlyByteBuf buffer) {
        String[] names = JSON.fromJson(buffer.readUtf(MAX_LENGTH), String[].class);
        if (names == null) return Set.of();
        Set<String> editable = new LinkedHashSet<>();
        for (String name : names) {
            if (name != null) editable.add(name);
        }
        return Collections.unmodifiableSet(editable);
    }

    public record Edit(String modId, String values) implements RosettaPacket {
        public Edit(FriendlyByteBuf buf) { this(buf.readUtf(64), buf.readUtf(MAX_LENGTH)); }
        @Override
        public void write(FriendlyByteBuf buf) { buf.writeUtf(modId, 64); buf.writeUtf(values, MAX_LENGTH); }
        public static void handle(Edit packet, ServerPlayer sender) {
            String modId = packet.modId();
            String values = packet.values();
            MidnightConfig config = MidnightConfig.configInstances.get(modId);
            if (config == null || !EDIT_ACCESS.get(config.configClass).allows(sender)) return;
            if (values.isEmpty()) {
                sync(modId, sender);
                return;
            }
            try {
                if (!onServerThread()) throw new IllegalStateException("Config edits require the server thread");
                Map<Field, Object> decoded = decode(config, values);
                if (decoded.keySet().stream().anyMatch(field -> !canEdit(sender, field))) {
                    sync(modId, sender);
                    return;
                }
                if (decoded.isEmpty()) return;
                decoded.forEach(ConfigSync::validate);
                JsonObject combined = snapshot(config);
                decoded.forEach((field, value) -> combined.add(field.getName(), JSON.toJsonTree(value)));
                if (combined.toString().length() > MAX_LENGTH) throw new IllegalArgumentException("Combined config exceeds sync limit");
                decoded.forEach(ConfigSync::set);
                MidnightConfig.write(modId);
            } catch (RuntimeException error) {
                EntityCompat.displayClientMessage(sender, Component.literal(modId + " config update rejected: " + error.getMessage()), false);
                sync(modId, sender);
            }
        }
    }

    public record Sync(String modId, String values, Set<String> editable) implements RosettaPacket {
        public Sync(FriendlyByteBuf buf) { this(buf.readUtf(64), buf.readUtf(MAX_LENGTH), readEditable(buf)); }
        @Override
        public void write(FriendlyByteBuf buf) { buf.writeUtf(modId, 64); buf.writeUtf(values, MAX_LENGTH); buf.writeUtf(JSON.toJson(editable), MAX_LENGTH); }
        public static void handle(Sync packet, Player player) { ClientSync.receive(packet); }
    }

    public static final class ClientSync extends MidnightConfig {
        private static final Map<Field, Object> LOCAL_VALUES = new HashMap<>();
        private static final Map<String, Set<String>> PERMISSIONS = new HashMap<>();
        private static final Map<String, Runnable> CHANGES = new ConcurrentHashMap<>();

        public static boolean playing() { return Minecraft.getInstance().getConnection() != null; }
        public static boolean canEdit(String id, Field field) { return !playing() || PERMISSIONS.getOrDefault(id, Set.of()).contains(field.getName()); }
        public static void onChange(String id, Runnable callback) { CHANGES.put(id, Objects.requireNonNull(callback)); }
        public static void changed(String id) {
            Runnable callback = CHANGES.get(id);
            if (callback != null) callback.run();
        }

        public static void receive(Sync packet) {
            MidnightConfig config = configInstances.get(packet.modId);
            if (config == null) return;
            Map<Field, Object> values = decode(config, packet.values);
            PERMISSIONS.put(packet.modId, packet.editable);
            if (!Minecraft.getInstance().hasSingleplayerServer()) {
                values.forEach((field, value) -> LOCAL_VALUES.putIfAbsent(field, read(field)));
                values.forEach(ConfigSync::set);
            }
            entries.values().stream().filter(info -> packet.modId.equals(info.modid) && isServerSetting(info.field))
                    .forEach(ClientSync::resetEntry);
            //? if >=26.2 {
            /*var currentScreen = Minecraft.getInstance().gui.screen();
            *///?} else {
            var currentScreen = Minecraft.getInstance().screen;
            //?}
            if (currentScreen instanceof MidnightConfigScreen screen
                    && packet.modId.equals(screen.modid)) {
                screen.updateList();
            }
            changed(packet.modId);
        }

        public static void disconnect() {
            LOCAL_VALUES.forEach(ConfigSync::set);
            LOCAL_VALUES.clear();
            Set<String> ids = new HashSet<>(PERMISSIONS.keySet());
            PERMISSIONS.clear();
            ids.forEach(ClientSync::changed);
        }

        public static void preserveLocalValues(JsonObject values, Class<?> type, Gson gson) {
            fields(type).forEach(field -> {
                if (LOCAL_VALUES.containsKey(field)) values.add(field.getName(), gson.toJsonTree(LOCAL_VALUES.get(field)));
            });
        }

        private static void resetEntry(EntryInfo info) {
            ConfigEntryAccessor access = (ConfigEntryAccessor) info;
            access.rosettaConfig$value(read(info.field));
            access.rosettaConfig$tempValue(info.toTemporaryValue());
        }

        public static void refreshEntries(String id, Collection<?> source, Consumer<Object> refresh) {
            for (Object item : source) {
                EntryInfo info = (EntryInfo) item;
                if (!id.equals(info.modid)) continue;
                refresh.accept(info);
                if (isServerSetting(info.field)) resetEntry(info);
            }
        }

        public static void prepare(MidnightConfigScreen screen) {
            for (EntryInfo info : entries.values()) {
                if (!screen.modid.equals(info.modid) || !isServerSetting(info.field) || !playing()) continue;
                ConfigEntryAccessor access = (ConfigEntryAccessor) info;
                if (!canEdit(screen.modid, info.field)) resetEntry(info);
                else if (access.rosettaConfig$value() == access.rosettaConfig$defaultValue()) {
                    access.rosettaConfig$value(copy(access.rosettaConfig$value()));
                }
            }
        }

        public static void permissions(MidnightConfigScreen screen) {
            if (screen.list == null) return;
            for (var row : screen.list.children()) {
                if (row.info != null && isServerSetting(row.info.field) && !canEdit(screen.modid, row.info.field) && row.buttons != null) {
                    row.buttons.forEach(button -> {
                        button.active = false;
                        if (button instanceof EditBox textField) {
                            textField.setEditable(false);
                            textField.setTextColorUneditable(0xFF707070);
                        }
                    });
                }
            }
        }

        public static void save(String id) {
            JsonObject changes = new JsonObject();
            if (playing()) {
                for (EntryInfo info : entries.values()) {
                    if (!id.equals(info.modid) || !isServerSetting(info.field) || !canEdit(id, info.field)) continue;
                    Object value = ((ConfigEntryAccessor) info).rosettaConfig$value();
                    if (!Objects.equals(value, read(info.field))) changes.add(info.fieldName, JSON.toJsonTree(value));
                }
            }
            Minecraft client = Minecraft.getInstance();
            MinecraftServer singleplayer = client.getSingleplayerServer();
            MidnightConfig config = configInstances.get(id);
            if (singleplayer != null && config != null && !fields(config.configClass).isEmpty()) {
                singleplayer.execute(() -> {
                    MidnightConfig.write(id);
                    client.execute(() -> changed(id));
                });
            } else {
                MidnightConfig.write(id);
                changed(id);
            }
            if (changes.size() > 0) RosettaNetwork.sendToServer(new Edit(id, changes.toString()));
        }
    }
}

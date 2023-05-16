package de.ambertation.wunderlib.configs;


import de.ambertation.wunderlib.WunderLib;
import de.ambertation.wunderlib.utils.Version;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.*;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigFile {
    public static final String MODIFY_VERSION = "modify_version";
    public static final String CREATE_VERSION = "create_version";
    private static final Gson JSON_BUILDER = new GsonBuilder().setPrettyPrinting()
                                                              .create();
    public final String category;
    private final File path;
    private final List<Value<?, ?>> knownValues = new LinkedList<>();
    private JsonObject root;
    private boolean modified;
    private final Version.ModVersionProvider versionProvider;

    public ConfigFile(Version.ModVersionProvider versionProvider, String category) {
        this(versionProvider, versionProvider.getModID(), category);
    }

    public ConfigFile(Version.ModVersionProvider versionProvider, String basePath, String category) {
        final Path dir = FabricLoader.getInstance().getConfigDir().resolve(basePath);
        path = dir.resolve(category + ".json").toFile();
        this.category = basePath + "." + category;
        this.versionProvider = versionProvider;

        if (!dir.toFile().exists()) dir.toFile().mkdirs();
        loadFromDisc();
    }

    private void setModified() {
        modified = true;
    }

    public int getMaxOrder() {
        int highestOrder = 0;
        for (Value<?, ?> v : knownValues) {
            if (v.order > highestOrder) {
                highestOrder = v.order;
            }
        }
        return highestOrder;
    }

    private void registerValue(Value<?, ?> v) {
        knownValues.remove(v);
        knownValues.add(v);
        v.order = getMaxOrder() + 1;
        v.parentFile = this;
    }

    private JsonElement getValue(ConfigToken t, boolean addIfMissing) {
        JsonObject obj = getPathElement(t.path, addIfMissing);
        if (!obj.has(t.key)) return null;

        return obj.get(t.key);
    }

    private void setValue(ConfigToken t, JsonElement value) {
        if (!value.equals(getValue(t, true))) {
            setModified();
        }

        JsonObject obj = getPathElement(t.path, true);
        obj.add(t.key, value);
    }

    private void removeValue(ConfigToken t) {
        JsonObject o = getPathElement(t.path, false);
        if (o.has(t.key)) {
            WunderLib.LOGGER.info("Removing Config " + t.path + "." + t.key);
            o.remove(t.key);
            setModified();
        }
    }

    private JsonObject getPathElement(String path, boolean addIfMissing) {
        if (path == null || path.trim().equals("")) {
            return root;
        }

        String[] names = path.split("\\.");
        JsonObject obj = root;

        for (int i = 0; i < names.length; i++) {
            final String p = names[i];
            if (obj.has(p)) {
                obj = obj.get(p).getAsJsonObject();
            } else {
                JsonObject newObject = new JsonObject();
                if (addIfMissing) {
                    obj.add(p, newObject);
                }
                obj = newObject;
            }
        }
        return obj;
    }

    public void loadFromDisc() {
        modified = false;
        if (path.exists()) {
            try (Reader reader = new FileReader(path)) {
                this.root = JSON_BUILDER.fromJson(reader, JsonElement.class).getAsJsonObject();
            } catch (Exception ex) {
                WunderLib.LOGGER.error("Unable to open Config File at '{}'.", path.toString(), ex);
            }
        } else {
            this.root = new JsonObject();
            this.root.add(CREATE_VERSION, new JsonPrimitive(versionProvider.getModVersion().toString()));
        }
    }

    public void save() {
        save(false);
    }

    public void save(boolean force) {
        if (!modified && !force) return;

        try (FileWriter jsonWriter = new FileWriter(path)) {
            this.root.add(MODIFY_VERSION, new JsonPrimitive(versionProvider.getModVersion().toString()));
            String string = JSON_BUILDER.toJson(root);
            jsonWriter.write(string);
            jsonWriter.flush();
            modified = false;
        } catch (IOException ex) {
            WunderLib.LOGGER.error("Unable to store Config File at '{}'.", path.toString(), ex);
        }
    }

    private String getVersionString(String name) {
        JsonObject mod = getPathElement("", true);
        if (mod == null) return "1.0.0";
        JsonPrimitive p = mod.getAsJsonPrimitive(name);
        if (p == null) return versionProvider.getModVersion().toString();
        if (!p.isString()) return "1.0.0";

        return p.getAsString();
    }

    public Version lastModifiedVersion() {
        return new Version(getVersionString(MODIFY_VERSION));
    }

    public Version createdVersion() {
        return new Version(getVersionString(CREATE_VERSION));
    }

    //---- Client Code

    /**
     * return all Values stored in the config file
     *
     * @return all stored values
     */
    @Environment(EnvType.CLIENT)
    public List<Value<?, ?>> getAllValues() {
        return knownValues;
    }

    /**
     * Returns all Values that are visible in the UI
     *
     * @return All visible Values
     */
    @Environment(EnvType.CLIENT)
    public List<Value<?, ?>> getAllVisibleValues() {
        List<Value<?, ?>> values = new ArrayList<>();
        for (Value<?, ?> v : knownValues) {
            if (!v.hiddenInUI) {
                values.add(v);
            }
        }
        return values;
    }

    /**
     * Returns all Values that are visible in the UI and are in the given group
     *
     * @param group The group to filter for
     * @return All visible Values in the given group
     */
    @Environment(EnvType.CLIENT)
    public List<Value<?, ?>> getAllVisibleValues(Group group) {
        List<Value<?, ?>> values = new ArrayList<>();
        for (Value<?, ?> v : knownValues) {
            if (!v.hiddenInUI && v.group == group) {
                values.add(v);
            }
        }
        values.sort(Comparator.comparingInt(o -> o.order));
        return values;
    }

    /**
     * A static method that returns all visible Values that are in the given group
     *
     * @param group       The group to filter for
     * @param configFiles an array of config files
     * @return All visible Values in the given group
     */
    @Environment(EnvType.CLIENT)
    public static List<Value<?, ?>> getAllVisibleValues(Group group, List<ConfigFile> configFiles) {
        final List<Value<?, ?>> values = new ArrayList<>();
        for (ConfigFile c : configFiles) {
            for (Value<?, ?> v : c.knownValues) {
                if (!v.hiddenInUI && v.group == group) {
                    values.add(v);
                }
            }
        }
        values.sort(Comparator.comparingInt(o -> o.order));
        return values;
    }


    /**
     * Returns all groups that are used in this config file. The groups are sorted by their order.
     *
     * @return All stored groups
     */
    @Environment(EnvType.CLIENT)
    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        for (Value<?, ?> v : knownValues) {
            if (v.group != null && !groups.contains(v.group)) {
                groups.add(v.group);
            }
        }
        groups.sort(Comparator.comparingInt(o -> o.order));
        return groups;
    }

    /**
     * A static method that retuns all groups that are used in the given config files. The groups are sorted by their order.
     *
     * @param configFiles an array of config files
     * @return All stored groups
     */
    @Environment(EnvType.CLIENT)
    public static List<Group> getAllGroups(List<ConfigFile> configFiles) {
        List<Group> groups = new ArrayList<>();
        for (ConfigFile c : configFiles) {
            for (Value<?, ?> v : c.knownValues) {
                if (v.group != null && !groups.contains(v.group)) {
                    groups.add(v.group);
                }
            }
        }
        groups.sort(Comparator.comparingInt(o -> o.order));
        return groups;
    }

    @Environment(EnvType.CLIENT)
    public static String getAllCategories(List<ConfigFile> configFiles) {
        StringBuilder sb = new StringBuilder();
        for (ConfigFile c : configFiles) {
            if (!sb.isEmpty()) sb.append(",");
            sb.append(c.category);
        }
        return sb.toString();
    }

    //---- Client Code

    public record ConfigToken<T>(String path, String key, T defaultValue) {
        @Override
        public String toString() {
            return "ConfigToken{" +
                    "path='" + path + '\'' +
                    ", key='" + key + '\'' +
                    ", defaultValue=" + defaultValue +
                    '}';
        }
    }

    public record Group(@NotNull String modID, @NotNull String title, int order) {
        @Override
        public String toString() {
            return "Group{" +
                    "title='" + title + '\'' +
                    ", order=" + order +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Group group = (Group) o;
            return title.equals(group.title);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title);
        }
    }

    public abstract class Value<T, R extends Value<T, R>> {
        @NotNull
        public final ConfigToken<T> token;
        @Nullable
        protected Supplier<Boolean> isValidSupplier;
        private boolean hiddenInUI = false;
        @Nullable
        private BooleanValue enabledInUI = null;
        private boolean deprecated = false;
        @Nullable
        private Group group;
        private int order;
        @Nullable
        private ConfigFile parentFile;


        public Value(String path, String key, T defaultValue) {
            this(new ConfigToken(path, key, defaultValue), false);
        }

        public Value(String path, String key, T defaultValue, boolean isDeprecated) {
            this(new ConfigToken(path, key, defaultValue), isDeprecated);
        }

        public Value(ConfigToken token) {
            this(token, false);
        }

        public Value(ConfigToken token, boolean isDeprecated) {
            this.deprecated = isDeprecated; //make sure this is set before get, otherwise deprecated values will get added to the config!

            this.token = token;
            this.group = null;
            get(); //has the side effect of initializing the default value
            registerValue(this);
        }

        public R hideInUI() {
            hiddenInUI = true;
            return (R) this;
        }

        public R setGroup(Group group) {
            this.group = group;
            return (R) this;
        }

        public R setOrder(int order) {
            this.order = order;
            return (R) this;
        }

        public R setDependency(BooleanValue value) {
            this.enabledInUI = value;
            return (R) this;
        }

        public boolean isHiddenInUI() {
            return hiddenInUI;
        }

        public boolean isDeprecated() {
            return deprecated;
        }

        @Nullable
        public Group getGroup() {
            return group;
        }

        public int getOrder() {
            return order;
        }

        @Nullable
        public ConfigFile getParentFile() {
            return parentFile;
        }

        public boolean hasDependency() {
            return enabledInUI != null;
        }

        @Nullable
        public BooleanValue getDependency() {
            return enabledInUI;
        }

        @Nullable
        public Supplier<Boolean> getIsValidSupplier() {
            return isValidSupplier;
        }

        public final T getRaw() {
            JsonElement el = getValue(token, !deprecated);
            if (el == null) {
                if (!deprecated) set(token.defaultValue);
                return token.defaultValue;
            }
            return convert(el);
        }

        public T get() {
            return getRaw();
        }

        public void remove() {
            removeValue(token);
        }

        public void migrate(Value<T, R> newConfig) {
            newConfig.set(get());
            remove();
        }

        protected abstract T convert(@NotNull JsonElement el);

        @NotNull
        protected abstract JsonElement convert(T value);

        public void set(T value) {
            if (deprecated) throw new IllegalStateException("'" + token.path() + "." +
                    token.key + "' is deprecated and can no-longer be used");
            setValue(token, convert(value));
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" +
                    "token=" + token +
                    ", value='" + get() + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Value)) return false;
            Value<?, ?> value = (Value<?, ?>) o;
            return token.equals(value.token);
        }

        @Override
        public int hashCode() {
            return Objects.hash(token);
        }
    }

    public class IntValue extends Value<Integer, IntValue> {
        public IntValue(String path, String key, int defaultValue) {
            super(path, key, defaultValue);
        }

        protected IntValue(ConfigToken t) {
            super(t);
        }

        public IntValue(String path, String key, int defaultValue, boolean isDeprecated) {
            super(path, key, defaultValue, isDeprecated);
        }

        protected IntValue(ConfigToken t, boolean isDeprecated) {
            super(t, isDeprecated);
        }

        @Override
        protected Integer convert(@NotNull JsonElement el) {
            return el.getAsInt();
        }

        @Override
        protected @NotNull JsonElement convert(Integer value) {
            return new JsonPrimitive(value);
        }

        public IntValue hideInUI() {
            return (IntValue) super.hideInUI();
        }
    }

    public class FloatValue extends Value<Float, FloatValue> {
        public FloatValue(String path, String key, float defaultValue) {
            super(path, key, defaultValue);
        }

        protected FloatValue(ConfigToken t) {
            super(t);
        }

        public FloatValue(String path, String key, float defaultValue, boolean isDeprecated) {
            super(path, key, defaultValue, isDeprecated);
        }

        protected FloatValue(ConfigToken t, boolean isDeprecated) {
            super(t, isDeprecated);
        }

        @Override
        protected Float convert(@NotNull JsonElement el) {
            return el.getAsFloat();
        }

        @Override
        protected @NotNull JsonElement convert(Float value) {
            return new JsonPrimitive(value);
        }

        public FloatValue hideInUI() {
            return (FloatValue) super.hideInUI();
        }
    }

    public class BooleanValue extends Value<Boolean, BooleanValue> {
        public BooleanValue(String path, String key, boolean defaultValue) {
            super(path, key, defaultValue);
        }

        protected BooleanValue(ConfigToken t) {
            super(t);
        }

        public BooleanValue(String path, String key, boolean defaultValue, boolean isDeprecated) {
            super(path, key, defaultValue, isDeprecated);
        }

        protected BooleanValue(ConfigToken t, boolean isDeprecated) {
            super(t, isDeprecated);
        }

        @Override
        protected Boolean convert(@NotNull JsonElement el) {
            return el.getAsBoolean();
        }

        @Override
        protected @NotNull JsonElement convert(Boolean value) {
            return new JsonPrimitive(value);
        }

        /**
         * This value will only be enabled if the given condition is true. If exactly one condition is supplied
         * then the condition will be used as the dependency. If multiple conditions are supplied then the
         * dependency will be the AND of all the conditions.
         *
         * @param condition the conditions to enable this value
         * @return a new BooleanValue that is only enabled if the conditions are true
         */
        public BooleanValue and(BooleanValue... condition) {
            final BooleanValue res = and(() -> Arrays.stream(condition)
                                                     .map(c -> c.get())
                                                     .reduce(true, (p, c) -> p && c));
            if (condition.length == 1) res.setDependency(condition[0]);
            return res;
        }

        public BooleanValue and(Supplier<Boolean> condition) {
            BooleanValue self = this;
            BooleanValue res = new BooleanValue(token, isDeprecated()) {
                @Override
                public Boolean get() {
                    return condition.get() && self.get();
                }

                @Override
                public void set(Boolean value) {
                    self.set(value);
                }
            };
            res.isValidSupplier = condition;
            return res;
        }

        public BooleanValue or(BooleanValue... condition) {
            return or(() -> Arrays.stream(condition).map(c -> c.get()).reduce(true, (p, c) -> p || c));
        }

        public BooleanValue or(Supplier<Boolean> condition) {
            BooleanValue self = this;
            BooleanValue res = new BooleanValue(token, isDeprecated()) {
                @Override
                public Boolean get() {
                    return condition.get() || self.get();
                }

                @Override
                public void set(Boolean value) {
                    self.set(value);
                }
            };
            res.isValidSupplier = condition;
            return res;
        }

        public BooleanValue hideInUI() {
            return (BooleanValue) super.hideInUI();
        }
    }
}

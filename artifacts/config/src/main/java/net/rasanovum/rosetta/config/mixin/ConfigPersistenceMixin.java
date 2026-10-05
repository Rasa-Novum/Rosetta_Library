package net.rasanovum.rosetta.config.mixin;

import com.google.gson.*;
import eu.midnightdust.lib.config.MidnightConfig;
import java.io.Reader;
import java.util.Collection;
import java.util.function.Consumer;
import net.rasanovum.rosetta.loaders.Platform;
import net.rasanovum.rosetta.config.ConfigSync;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MidnightConfig.class, remap = false)
public abstract class ConfigPersistenceMixin {
    @Shadow protected String modid;
    @Shadow public Class<? extends MidnightConfig> configClass;

    @Redirect(method = "loadValuesFromJson", at = @At(value = "INVOKE",
            target = "Lcom/google/gson/Gson;fromJson(Ljava/io/Reader;Ljava/lang/Class;)Ljava/lang/Object;"))
    private Object rosettaConfig$loadLocalValues(Gson gson, Reader reader, Class<?> type) {
        boolean serverLoad = ConfigSync.onServerThread();
        boolean clientLoad = !serverLoad && Platform.INSTANCE.isClientSide() && ConfigSync.ClientSync.playing();
        if (ConfigSync.fields(type).isEmpty() || !serverLoad && !clientLoad) return gson.fromJson(reader, type);
        JsonObject values = JsonParser.parseReader(reader).getAsJsonObject();
        var serverFields = ConfigSync.fields(type).stream().map(java.lang.reflect.Field::getName).toList();
        values.entrySet().removeIf(entry -> serverFields.contains(entry.getKey()) != serverLoad);
        return gson.fromJson(values, type);
    }

    @Redirect(method = "loadValuesFromJson", at = @At(value = "INVOKE",
            target = "Ljava/util/Collection;forEach(Ljava/util/function/Consumer;)V"))
    private void rosettaConfig$refreshOwnEntries(Collection<?> entries, Consumer<Object> refresh) {
        if (ConfigSync.fields(configClass).isEmpty()) {
            entries.forEach(refresh);
            return;
        }
        if (ConfigSync.onServerThread()) return;
        if (Platform.INSTANCE.isClientSide()) {
            ConfigSync.ClientSync.refreshEntries(modid, entries, refresh);
        } else {
            entries.forEach(refresh);
        }
    }

    @Redirect(method = "writeChanges()V", at = @At(value = "INVOKE",
            target = "Lcom/google/gson/Gson;toJson(Ljava/lang/Object;)Ljava/lang/String;"))
    private String rosettaConfig$localValues(Gson gson, Object config) {
        if (!Platform.INSTANCE.isClientSide() || ConfigSync.onServerThread() || ConfigSync.fields(configClass).isEmpty()) {
            return gson.toJson(config);
        }
        JsonObject values = gson.toJsonTree(config).getAsJsonObject();
        ConfigSync.ClientSync.preserveLocalValues(values, configClass, gson);
        return gson.toJson(values);
    }

    @Inject(method = {"loadValuesFromJson", "writeChanges()V"}, at = @At("RETURN"))
    private void rosettaConfig$changed(CallbackInfo ci) {
        if (!ConfigSync.fields(configClass).isEmpty()) ConfigSync.changed(modid);
    }
}

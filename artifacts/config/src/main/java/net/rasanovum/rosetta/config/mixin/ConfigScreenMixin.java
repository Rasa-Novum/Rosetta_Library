package net.rasanovum.rosetta.config.mixin;

import eu.midnightdust.lib.config.MidnightConfig;
import eu.midnightdust.lib.config.MidnightConfigScreen;
import net.rasanovum.rosetta.config.ConfigSync;
import net.rasanovum.rosetta.network.RosettaNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MidnightConfigScreen.class, remap = false)
public abstract class ConfigScreenMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void rosettaConfig$request(CallbackInfo ci) {
        String id = ((MidnightConfigScreen) (Object) this).modid;
        MidnightConfig config = MidnightConfig.configInstances.get(id);
        if (config != null && !ConfigSync.fields(config.configClass).isEmpty() && ConfigSync.ClientSync.playing()) {
            RosettaNetwork.sendToServer(new ConfigSync.Edit(id, ""));
        }
    }

    @Inject(method = "updateList", at = @At("HEAD"))
    private void rosettaConfig$prepare(CallbackInfo ci) {
        ConfigSync.ClientSync.prepare((MidnightConfigScreen) (Object) this);
    }

    @Inject(method = "updateButtons", at = @At("TAIL"))
    private void rosettaConfig$permissions(CallbackInfo ci) {
        ConfigSync.ClientSync.permissions((MidnightConfigScreen) (Object) this);
    }

    //? if >=26.1 {
    /*@Redirect(method = "lambda$init$1", at = @At(value = "INVOKE",
    *///?} else {
    @Redirect(method = "lambda$init$6", at = @At(value = "INVOKE",
    //?}
            target = "Leu/midnightdust/lib/config/MidnightConfig;write(Ljava/lang/String;)V"))
    private void rosettaConfig$save(String id) {
        ConfigSync.ClientSync.save(id);
    }
}

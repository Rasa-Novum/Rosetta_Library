package net.rasanovum.rosetta.config.mixin;

import eu.midnightdust.lib.config.EntryInfo;
import net.rasanovum.rosetta.config.ConfigSync;
import net.rasanovum.rosetta.config.ServerSetting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntryInfo.class, remap = false)
public abstract class ConfigEntryMixin {
    @ModifyArg(method = "getTooltip", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/Tooltip;create(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/client/gui/components/Tooltip;",
            remap = true), index = 0)
    private Component rosettaConfig$disabledDescription(Component description) {
        EntryInfo info = (EntryInfo) (Object) this;
        if (!ConfigSync.isServerSetting(info.field) || ConfigSync.ClientSync.canEdit(info.modid, info.field)) {
            return description;
        }
        String suffix = info.field.getAnnotation(ServerSetting.class).disabledDescription();
        return suffix.isEmpty() ? description : description.copy().append(suffix);
    }

    @Inject(method = "updateFieldValue", at = @At("HEAD"), cancellable = true)
    private void rosettaConfig$stageServerSetting(CallbackInfo ci) {
        if (ConfigSync.isServerSetting(((EntryInfo) (Object) this).field) && ConfigSync.ClientSync.playing()) {
            ci.cancel();
        }
    }
}

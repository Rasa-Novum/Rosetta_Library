package net.rasanovum.rosetta.mixin.client;

//? if forge && <1.19 {
/*import net.minecraft.client.Minecraft;
import net.rasanovum.rosetta.event.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class LegacyClientShutdownMixin {
    @Inject(method = "close", at = @At("HEAD"))
    private void rosetta$clientStopping(CallbackInfo callback) {
        ClientHooks.clientStopping();
    }
}
*///?}

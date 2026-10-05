package net.rasanovum.rosetta.mixin.client;

//? if <1.19.3 {
/*import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.rasanovum.rosetta.registry.LegacyCreativeTabHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeTab.class)
public abstract class LegacyCreativeTabMixin {
    @Inject(method = "fillItemList", at = @At("TAIL"))
    private void rosetta$appendEntries(NonNullList<ItemStack> stacks, CallbackInfo ci) {
        LegacyCreativeTabHooks.append((CreativeModeTab) (Object) this, stacks);
    }
}
*///?}

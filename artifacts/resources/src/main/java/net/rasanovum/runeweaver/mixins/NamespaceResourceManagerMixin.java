package net.rasanovum.runeweaver.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.rasanovum.runeweaver.Runeweaver;
import net.rasanovum.runeweaver.hooks.NamespaceHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(FallbackResourceManager.class)
public class NamespaceResourceManagerMixin {


    //? if <1.19 {
    /*@ModifyReturnValue(method = "getResources", at = @At("RETURN"))
    *///?} else {
    @ModifyReturnValue(method = "getResourceStack", at = @At("RETURN"))
    //?}
    private List<Resource> runRuneweaverEvents(List<Resource> original, @Local(argsOnly = true) Identifier id) {
        return Runeweaver.processHook(new NamespaceHook(original, id));
    }

    //? if <1.19 {
    /*@ModifyReturnValue(method = "getResource", at = @At("RETURN"))
    private Resource runLegacyRuneweaverEvents(Resource original, Identifier id) throws java.io.IOException {
        List<Resource> result = Runeweaver.processHook(new NamespaceHook(new ArrayList<>(List.of(original)), id));
        if (result.isEmpty()) {
            original.close();
            throw new java.io.FileNotFoundException(id.toString());
        }
        return result.get(0);
    }
    *///?} else {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ModifyReturnValue(method = "getResource", at = @At("RETURN"))
    private Optional<Resource> runRuneweaverEvents(Optional<Resource> original, Identifier id) {
        if(original.isEmpty()) return Optional.empty();
        List<Resource> result = Runeweaver.processHook(new NamespaceHook(new ArrayList<>(List.of(original.get())), id));
        return Optional.ofNullable(result.get(0));
    }
    //?}
}

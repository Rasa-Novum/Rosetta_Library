package net.rasanovum.runeweaver.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >=1.19.3
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.rasanovum.runeweaver.Runeweaver;
import net.rasanovum.runeweaver.hooks.StandardHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

//? if >=26.1 {
@Mixin(targets = "net.minecraft.resources.ResourceManagerRegistryLoadTask")
//?} else if <1.19.3 {
/*@Mixin(targets = "net.minecraft.resources.RegistryResourceAccess$1")
*///?} else {
/*@Mixin(targets = "net.minecraft.resources.RegistryDataLoader")
*///?}
public class ResourceManagerRegistryLoadTaskMixin {

    //? if <1.19.3 {
    /*@WrapOperation(method = "listResources", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/ResourceManager;listResources(Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Map;"))
    private Map<Identifier, Resource> runLegacyRuneweaverEvents(ResourceManager manager, String path,
            java.util.function.Predicate<Identifier> filter, Operation<Map<Identifier, Resource>> original) {
        return Runeweaver.processHook(new StandardHook(original.call(manager, path, filter)));
    }
    *///?} else {
    @WrapOperation(
            //? if >=26.1 {
            method = "lambda$load$0",
            //?} else if >1.20.1 {
            /*method = "loadContentsFromManager",
            *///?} else {
            /*method = "loadRegistryContents",
            *///?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/FileToIdConverter;listMatchingResources(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;")
    )
    private static Map<Identifier, Resource> runRuneweaverEvents(FileToIdConverter instance, ResourceManager resourceManager, Operation<Map<Identifier, Resource>> original) {
        return Runeweaver.processHook(new StandardHook(original.call(instance, resourceManager)));
    }
    //?}
}

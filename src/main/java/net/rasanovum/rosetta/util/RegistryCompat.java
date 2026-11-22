package net.rasanovum.rosetta.util;

import net.minecraft.resources.ResourceLocation;

/** Identifier and registry helpers. */
public final class RegistryCompat {
    private RegistryCompat() {}

    public static ResourceLocation getLocation(String location) {
        //? if <1.21 {
        /*return new ResourceLocation(location);
        *///?} else {
        return ResourceLocation.parse(location);
        //?}
    }

    public static ResourceLocation getLocation(String base, String path) {
        //? if <1.21 {
        /*return new ResourceLocation(base, path);
        *///?} else {
        return ResourceLocation.fromNamespaceAndPath(base, path);
        //?}
    }

    public static net.minecraft.world.level.block.state.BlockBehaviour.Properties blockProperties(
            ResourceLocation id,
            net.minecraft.world.level.block.state.BlockBehaviour.Properties properties
    ) {
        //? if >=26.1 {
        /*return properties.setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, id));
        *///?} else {
        return properties;
        //?}
    }

    public static net.minecraft.world.item.Item.Properties itemProperties(
            ResourceLocation id,
            net.minecraft.world.item.Item.Properties properties
    ) {
        //? if >=26.1 {
        /*return properties.setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, id));
        *///?} else {
        return properties;
        //?}
    }

    public static <T> T registryGet(net.minecraft.core.Registry<T> registry, ResourceLocation id) {
        //? if >=26.1 {
        /*return registry.getValue(id);
        *///?} else {
        return registry.get(id);
        //?}
    }

    public static ResourceLocation keyLocation(net.minecraft.resources.ResourceKey<?> key) {
        //? if >=26.1 {
        /*return key.identifier();
        *///?} else {
        return key.location();
        //?}
    }
}

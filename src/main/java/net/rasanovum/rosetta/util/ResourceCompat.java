package net.rasanovum.rosetta.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Predicate;

/** Resource enumeration and streams whose close also releases legacy metadata streams. */
public final class ResourceCompat {
    private ResourceCompat() {}

    public static List<ResourceLocation> list(ResourceManager manager, String folder, Predicate<ResourceLocation> filter) {
        //? if <1.19 {
        /*return manager.listResources(folder, name -> true).stream().filter(filter).toList();
        *///?} else {
        return manager.listResources(folder, filter::test).keySet().stream().toList();
        //?}
    }

    public static InputStream open(ResourceManager manager, ResourceLocation location) throws IOException {
        //? if <1.19 {
        /*var resource = manager.getResource(location);
        return new FilterInputStream(resource.getInputStream()) {
            @Override
            public void close() throws IOException {
                resource.close();
            }
        };
        *///?} else {
        return manager.open(location);
        //?}
    }
    public static String blockTagDirectory() {
        //? if <1.21 {
        /*return "tags/blocks";
        *///?} else {
        return "tags/block";
        //?}
    }
}

package net.rasanovum.rosetta.nbt;

import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionFile;
//? if >1.20.1 {
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
//?}

/** Region-file construction across the storage metadata API change. */
public final class RegionFileCompat {
    private RegionFileCompat() {}

    public static RegionFile open(String worldName, ResourceKey<Level> dimension, Path path, Path folder) throws IOException {
        //? if >1.20.1 {
        return new RegionFile(new RegionStorageInfo(worldName, dimension, "chunk"), path, folder, true);
        //?} else {
        /*return new RegionFile(path, folder, true);
        *///?}
    }
}

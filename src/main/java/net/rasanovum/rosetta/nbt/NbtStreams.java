package net.rasanovum.rosetta.nbt;

import java.io.DataInput;
import java.io.IOException;
import java.nio.file.Path;
//? if >=1.20.2 {
import net.minecraft.nbt.NbtAccounter;
//?}
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StreamTagVisitor;

/** Streaming NBT readers with modern parser accounting where Minecraft exposes it. */
public final class NbtStreams {
    private NbtStreams() {}

    /**
     * Reads compressed NBT into a visitor. Older Minecraft parsers do not expose an accounting overload;
     * callers must still bound their source before calling this method when that matters.
     */
    public static void parseCompressed(Path path, StreamTagVisitor visitor, long maxBytes) throws IOException {
        //? if >=1.20.2 {
        NbtIo.parseCompressed(path, visitor, NbtAccounter.create(maxBytes));
        //?} else {
        /*NbtIo.parseCompressed(path.toFile(), visitor);
        *///?}
    }

    /**
     * Reads uncompressed NBT into a visitor. Older Minecraft parsers do not expose an accounting overload.
     */
    public static void parse(DataInput input, StreamTagVisitor visitor, long maxBytes) throws IOException {
        //? if >1.20.1 {
        NbtIo.parse(input, visitor, NbtAccounter.create(maxBytes));
        //?} else {
        /*NbtIo.parse(input, visitor);
        *///?}
    }
}

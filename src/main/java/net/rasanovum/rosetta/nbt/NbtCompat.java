package net.rasanovum.rosetta.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

/** NBT accessors. */
public final class NbtCompat {
    private NbtCompat() {}

    public static byte getByte(CompoundTag tag, String key, byte def) {
        //? if >=26.1 {
        /*return tag.getByteOr(key, def);
        *///?} else {
        return tag.contains(key, 99) ? tag.getByte(key) : def;
        //?}
    }

    public static short getShort(CompoundTag tag, String key, short def) {
        //? if >=26.1 {
        /*return tag.getShortOr(key, def);
        *///?} else {
        return tag.contains(key, 99) ? tag.getShort(key) : def;
        //?}
    }

    public static int getInt(CompoundTag tag, String key, int def) {
        //? if >=26.1 {
        /*return tag.getIntOr(key, def);
        *///?} else {
        return tag.contains(key, 99) ? tag.getInt(key) : def;
        //?}
    }

    public static long getLong(CompoundTag tag, String key, long def) {
        //? if >=26.1 {
        /*return tag.getLongOr(key, def);
        *///?} else {
        return tag.contains(key, 99) ? tag.getLong(key) : def;
        //?}
    }

    public static float getFloat(CompoundTag tag, String key, float def) {
        //? if >=26.1 {
        /*return tag.getFloatOr(key, def);
        *///?} else {
        return tag.contains(key, 99) ? tag.getFloat(key) : def;
        //?}
    }

    public static double getDouble(CompoundTag tag, String key, double def) {
        //? if >=26.1 {
        /*return tag.getDoubleOr(key, def);
        *///?} else {
        return tag.contains(key, 99) ? tag.getDouble(key) : def;
        //?}
    }

    public static String getString(CompoundTag tag, String key, String def) {
        //? if >=26.1 {
        /*return tag.getStringOr(key, def);
        *///?} else {
        return tag.contains(key, 8) ? tag.getString(key) : def;
        //?}
    }

    public static boolean getBoolean(CompoundTag tag, String key, boolean def) {
        //? if >=26.1 {
        /*return tag.getBooleanOr(key, def);
        *///?} else {
        return tag.contains(key) ? tag.getBoolean(key) : def;
        //?}
    }

    public static CompoundTag getCompound(CompoundTag tag, String key) {
        //? if >=26.1 {
        /*return tag.getCompoundOrEmpty(key);
        *///?} else {
        return tag.contains(key, 10) ? tag.getCompound(key) : new CompoundTag();
        //?}
    }

    public static ListTag getList(CompoundTag tag, String key, int elementType) {
        //? if >=26.1 {
        /*return tag.getListOrEmpty(key);
        *///?} else {
        return tag.contains(key, 9) ? tag.getList(key, elementType) : new ListTag();
        //?}
    }

    public static int[] getIntArray(CompoundTag tag, String key) {
        //? if >=26.1 {
        /*return tag.getIntArray(key).orElse(new int[0]);
        *///?} else {
        return tag.contains(key, 11) ? tag.getIntArray(key) : new int[0];
        //?}
    }

    public static long[] getLongArray(CompoundTag tag, String key) {
        //? if >=26.1 {
        /*return tag.getLongArray(key).orElse(new long[0]);
        *///?} else {
        return tag.contains(key, 12) ? tag.getLongArray(key) : new long[0];
        //?}
    }

    public static byte[] getByteArray(CompoundTag tag, String key) {
        //? if >=26.1 {
        /*return tag.getByteArray(key).orElse(new byte[0]);
        *///?} else {
        return tag.contains(key, 7) ? tag.getByteArray(key) : new byte[0];
        //?}
    }

    public static boolean contains(CompoundTag tag, String key) {
        return tag.contains(key);
    }

    public static java.util.Set<String> getAllKeys(CompoundTag tag) {
        //? if >=26.1 {
        /*return tag.keySet();
        *///?} else {
        return tag.getAllKeys();
        //?}
    }

    public static boolean hasUUID(CompoundTag tag, String key) {
        //? if >=26.1 {
        /*return tag.getIntArray(key).map(arr -> arr.length == 4).orElse(false);
        *///?} else {
        return tag.hasUUID(key);
        //?}
    }

    public static java.util.UUID getUUID(CompoundTag tag, String key) {
        //? if >=26.1 {
        /*return tag.getIntArray(key)
                .filter(arr -> arr.length == 4)
                .map(net.minecraft.core.UUIDUtil::uuidFromIntArray)
                .orElse(null);
        *///?} else {
        return tag.hasUUID(key) ? tag.getUUID(key) : null;
        //?}
    }

    public static void putUUID(CompoundTag tag, String key, java.util.UUID uuid) {
        //? if >=26.1 {
        /*tag.putIntArray(key, net.minecraft.core.UUIDUtil.uuidToIntArray(uuid));
        *///?} else {
        tag.putUUID(key, uuid);
        //?}
    }

    public static CompoundTag getCompound(ListTag list, int index) {
        //? if >=26.1 {
        /*return list.getCompoundOrEmpty(index);
        *///?} else {
        return list.getCompound(index);
        //?}
    }

    public static String getString(ListTag list, int index) {
        //? if >=26.1 {
        /*return list.getStringOr(index, "");
        *///?} else {
        return list.getString(index);
        //?}
    }

    public static int getInt(ListTag list, int index, int def) {
        //? if >=26.1 {
        /*return list.getIntOr(index, def);
        *///?} else {
        return index >= 0 && index < list.size() && list.getElementType() == CompoundTag.TAG_INT
                ? list.getInt(index) : def;
        //?}
    }
}

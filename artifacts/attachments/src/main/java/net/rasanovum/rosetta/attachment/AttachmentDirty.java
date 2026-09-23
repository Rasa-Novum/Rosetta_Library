package net.rasanovum.rosetta.attachment;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;

final class AttachmentDirty {
    private AttachmentDirty() {}

    static void mark(Object owner) {
        if (owner instanceof ChunkAccess chunk) {
            //? if >=26.1 {
            /*chunk.markUnsaved();
            *///?} else {
            chunk.setUnsaved(true);
            //?}
        } else if (owner instanceof BlockEntity blockEntity) {
            blockEntity.setChanged();
        }
    }
}

package net.rasanovum.rosetta.attachment.mixin;

//? if forge {
/*import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.rasanovum.rosetta.attachment.ForgeAttachmentBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
abstract class ForgeLevelChunkMixin {
    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V",
            at = @At("RETURN"))
    private void rosetta$promoteAttachments(ServerLevel level, ProtoChunk source, LevelChunk.PostLoadProcessor postLoad,
                                           CallbackInfo info) {
        ForgeAttachmentBackend.promoteChunk(source, (LevelChunk) (Object) this);
    }
}
*///?}

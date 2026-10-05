package net.rasanovum.rosetta.mixin;

//? if <1.19 {
/*import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NoiseBasedChunkGenerator.class)
public interface LegacyNoiseGeneratorAccessor {
    @Accessor("settings")
    Holder<NoiseGeneratorSettings> rosetta$settings();
}
*///?}

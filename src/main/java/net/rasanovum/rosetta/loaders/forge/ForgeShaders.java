package net.rasanovum.rosetta.loaders.forge;

//? if forge {
/*import java.io.IOException;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.renderer.ShaderInstance;
import net.rasanovum.rosetta.event.ClientShaderHooks;

@Mod.EventBusSubscriber(modid = "rosetta_library", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ForgeShaders {
    @SubscribeEvent
    public static void register(RegisterShadersEvent event) throws IOException {
        //? if <1.19.3 {
        ClientShaderHooks.registerShaders((id, format, loaded) -> event.registerShader(new ShaderInstance(event.getResourceManager(), id, format), loaded));
        //?} else {
        ClientShaderHooks.registerShaders((id, format, loaded) -> event.registerShader(new ShaderInstance(event.getResourceProvider(), id, format), loaded));
        //?}
    }
}
*///?}

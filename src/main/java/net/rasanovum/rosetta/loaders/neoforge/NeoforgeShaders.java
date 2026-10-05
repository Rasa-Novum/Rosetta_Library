package net.rasanovum.rosetta.loaders.neoforge;

//? if neoforge && <26.1 {
/*import java.io.IOException;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.renderer.ShaderInstance;
import net.rasanovum.rosetta.event.ClientShaderHooks;

@EventBusSubscriber(modid = "rosetta_library", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NeoforgeShaders {
    @SubscribeEvent
    public static void register(RegisterShadersEvent event) throws IOException {
        ClientShaderHooks.registerShaders((id, format, loaded) -> event.registerShader(new ShaderInstance(event.getResourceProvider(), id, format), loaded));
    }
}
*///?}

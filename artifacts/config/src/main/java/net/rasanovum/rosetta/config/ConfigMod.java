package net.rasanovum.rosetta.config;
//? if forge {
/*import net.minecraftforge.fml.common.Mod;
@Mod("rosetta_config")
*///?} elif neoforge {
/*import net.neoforged.fml.common.Mod;
@Mod("rosetta_config")
*///?}
public final class ConfigMod
        //? if fabric {
        implements net.fabricmc.api.ModInitializer
        //?}
{
    //? if fabric {
    @Override public void onInitialize() { ConfigSync.initialize(); }
    //?} else {
    /*public ConfigMod() { ConfigSync.initialize(); }
    *///?}
}

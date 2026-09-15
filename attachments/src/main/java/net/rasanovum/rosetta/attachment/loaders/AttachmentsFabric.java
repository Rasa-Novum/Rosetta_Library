package net.rasanovum.rosetta.attachment.loaders;
//? if fabric {
import net.rasanovum.rosetta.attachment.AttachmentBootstrap;
import net.fabricmc.api.ModInitializer;
public final class AttachmentsFabric implements ModInitializer {
    public void onInitialize() { AttachmentBootstrap.initialize(null); }
}
//?}

package net.rasanovum.rosetta.attachment;

/** Loader bootstrap for Rosetta's attachment storage. */
public final class AttachmentBootstrap {
    private AttachmentBootstrap() {}

    public static void initialize(Object modEventBus) {
        //? if neoforge
        /*NeoForgeAttachmentBackend.register(modEventBus);*/
        //? if forge
        /*ForgeAttachmentBackend.register(modEventBus);*/
    }
}

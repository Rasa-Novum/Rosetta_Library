package net.rasanovum.rosetta.network;

import net.minecraft.server.level.ServerPlayer;

public interface NetworkBackend {
    //? if fabric
    NetworkBackend INSTANCE = new net.rasanovum.rosetta.loaders.fabric.FabricNetworkBackend();
    //? if forge
    /*NetworkBackend INSTANCE = new net.rasanovum.rosetta.loaders.forge.ForgeNetworkBackend();*/
    //? if neoforge
    /*NetworkBackend INSTANCE = new net.rasanovum.rosetta.loaders.neoforge.NeoForgeNetworkBackend();*/

    <T extends RosettaPacket> void registerServerbound(PacketDefinition<T> definition);

    <T extends RosettaPacket> void registerClientbound(PacketDefinition<T> definition);

    void sendToServer(RosettaPacket packet);

    void sendToPlayer(RosettaPacket packet, ServerPlayer player);
}

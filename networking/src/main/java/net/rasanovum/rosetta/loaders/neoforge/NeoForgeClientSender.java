package net.rasanovum.rosetta.loaders.neoforge;

//? if neoforge && <26.1 {
/*import net.neoforged.neoforge.network.PacketDistributor;
import net.rasanovum.rosetta.network.RosettaPacket;

final class NeoForgeClientSender {
    private NeoForgeClientSender() {}
    static void send(RosettaPacket packet) { PacketDistributor.sendToServer(packet); }
}
*///?} else if neoforge {
/*import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.rasanovum.rosetta.network.RosettaPacket;

final class NeoForgeClientSender {
    private NeoForgeClientSender() {}
    static void send(RosettaPacket packet) { ClientPacketDistributor.sendToServer(packet); }
}
*///?}

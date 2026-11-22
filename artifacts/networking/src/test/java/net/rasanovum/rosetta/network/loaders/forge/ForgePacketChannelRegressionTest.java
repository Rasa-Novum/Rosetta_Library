package net.rasanovum.rosetta.network.loaders.forge;

//? if forge {
/*import net.rasanovum.rosetta.util.RegistryCompat;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ForgePacketChannelRegressionTest {
    @Test
    void packetChannelsAreIndependentOfRegistrationOrderAndDoNotHaveAByteSizedNamespaceLimit() {
        var first = RegistryCompat.getLocation("test", "one");
        var channel = ForgeNetworkBackend.packetChannelId(first);
        var channels = new HashSet<>();
        for (int index = 599; index >= 0; index--) {
            assertTrue(channels.add(ForgeNetworkBackend.packetChannelId(RegistryCompat.getLocation("test", "packet/" + index))));
        }
        assertEquals(channel, ForgeNetworkBackend.packetChannelId(first));
        assertNotEquals(channel, ForgeNetworkBackend.packetChannelId(RegistryCompat.getLocation("other", "one")));
        assertNotEquals(RegistryCompat.getLocation("test", "rosetta"),
                ForgeNetworkBackend.packetChannelId(RegistryCompat.getLocation("test", "rosetta")));
    }
}
*///?}

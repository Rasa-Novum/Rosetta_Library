package net.rasanovum.rosetta.config;

import eu.midnightdust.lib.config.MidnightConfig;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigSyncRegressionTest {
    @MidnightConfig.Entry(min = -2, max = 3)
    public static double bounded;

    @MidnightConfig.Entry
    public static double unbounded;

    @MidnightConfig.Entry
    public static List<String> strings;

    @Test
    void validatesNumericBoundsAndListContents() throws ReflectiveOperationException {
        var boundedField = getClass().getField("bounded");
        ConfigSync.validate(boundedField, -2.0);
        ConfigSync.validate(boundedField, 3.0f);
        for (Number value : List.of(-3, 4, Float.NaN, Double.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) {
            assertThrows(IllegalArgumentException.class, () -> ConfigSync.validate(boundedField, value));
        }
        ConfigSync.validate(getClass().getField("unbounded"), -8.0);
        var listField = getClass().getField("strings");
        ConfigSync.validate(listField, List.of("valid"));
        assertThrows(IllegalArgumentException.class, () -> ConfigSync.validate(listField, Arrays.asList("valid", null)));
    }

    @Test
    void readsNullAndDuplicateEditableNames() {
        assertEquals(Set.of(), readPermissions("null"));
        assertEquals(Set.of("enabled", "limit"), readPermissions("[\"enabled\",null,\"enabled\",\"limit\"]"));
    }

    private static Set<String> readPermissions(String encoded) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeUtf("test");
            buffer.writeUtf("{}");
            buffer.writeUtf(encoded);
            return new ConfigSync.Sync(buffer).editable();
        } finally {
            buffer.release();
        }
    }
}

package net.rasanovum.rosetta.util;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeCompatRegressionTest {
    @Test
    void appliesAllOperationsAndEvaluatesSkipOncePerModifier() {
        //? if >=1.21 {
        var addition = modifier("add", 5.0, AttributeModifier.Operation.ADD_VALUE);
        var baseMultiplier = modifier("base", 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        var totalMultiplier = modifier("total", 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        //?} else {
        /*var addition = modifier("add", 5.0, AttributeModifier.Operation.ADDITION);
        var baseMultiplier = modifier("base", 0.25, AttributeModifier.Operation.MULTIPLY_BASE);
        var totalMultiplier = modifier("total", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
        *///?}
        var skipped = AttributeCompat.movementSpeedMultiplier("test:skipped", 4.0);
        var modifiers = List.of(totalMultiplier, skipped, baseMultiplier, addition);
        int[] checks = {0};

        double result = AttributeCompat.calculateValue(15.0, modifiers, modifier -> {
            checks[0]++;
            return modifier == skipped;
        });

        assertEquals(37.5, result);
        assertEquals(modifiers.size(), checks[0]);
    }

    private static AttributeModifier modifier(String path, double amount, AttributeModifier.Operation operation) {
        //? if >=1.21 {
        return new AttributeModifier(RegistryCompat.getLocation("test", path), amount, operation);
        //?} else {
        /*return new AttributeModifier(UUID.nameUUIDFromBytes(path.getBytes(StandardCharsets.UTF_8)), path, amount, operation);
        *///?}
    }
}

package net.rasanovum.rosetta.util;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Predicate;

/** Attribute modifier helpers. */
public final class AttributeCompat {
    private AttributeCompat() {}

    //? if >=1.21 {
    public static AttributeModifier movementSpeedMultiplier(String modifierKey, double amount) {
        return new AttributeModifier(RegistryCompat.getLocation(modifierKey), amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
    //?} else {
    /*public static AttributeModifier movementSpeedMultiplier(String modifierKey, double amount) {
        return new AttributeModifier(attributeModifierUuid(modifierKey), modifierKey, amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    *///?}

    public static boolean hasModifier(AttributeInstance attribute, String modifierKey) {
        //? if >=1.21 {
        return attribute.getModifier(RegistryCompat.getLocation(modifierKey)) != null;
        //?} else {
        /*return attribute.getModifier(attributeModifierUuid(modifierKey)) != null;
        *///?}
    }

    public static void removeModifier(AttributeInstance attribute, String modifierKey) {
        //? if >=1.21 {
        attribute.removeModifier(RegistryCompat.getLocation(modifierKey));
        //?} else {
        /*attribute.removeModifier(attributeModifierUuid(modifierKey));
        *///?}
    }

    public static String canonicalModifierKey(String modifierKey) {
        //? if >=1.21 {
        return RegistryCompat.getLocation(modifierKey).toString();
        //?} else {
        /*return attributeModifierUuid(modifierKey).toString();
        *///?}
    }

    public static String modifierKey(AttributeModifier modifier) {
        //? if >=1.21 {
        return modifier.id().toString();
        //?} else {
        /*return modifier.getId().toString();
        *///?}
    }

    public static double calculateAttributeValueSkipping(AttributeInstance attribute, Predicate<AttributeModifier> skipModifier) {
        double baseValue = attribute.getBaseValue();
        AttributeModifier[] modifiers = attribute.getModifiers().toArray(AttributeModifier[]::new);
        for (int i = 0; i < modifiers.length; i++) {
            if (skipModifier.test(modifiers[i])) modifiers[i] = null;
        }

        //? if >=1.21 {
        for (AttributeModifier modifier : modifiers) {
            if (modifier != null && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) baseValue += modifier.amount();
        }
        double value = baseValue;
        for (AttributeModifier modifier : modifiers) {
            if (modifier != null && modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) value += baseValue * modifier.amount();
        }
        for (AttributeModifier modifier : modifiers) {
            if (modifier != null && modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) value *= 1.0D + modifier.amount();
        }
        return attribute.getAttribute().value().sanitizeValue(value);
        //?} else {
        /*for (AttributeModifier modifier : modifiers) {
            if (modifier != null && modifier.getOperation() == AttributeModifier.Operation.ADDITION) baseValue += modifier.getAmount();
        }
        double value = baseValue;
        for (AttributeModifier modifier : modifiers) {
            if (modifier != null && modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) value += baseValue * modifier.getAmount();
        }
        for (AttributeModifier modifier : modifiers) {
            if (modifier != null && modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) value *= 1.0D + modifier.getAmount();
        }
        return attribute.getAttribute().sanitizeValue(value);
        *///?}
    }

    //? if <1.21 {
    /*private static UUID attributeModifierUuid(String modifierKey) {
        return UUID.nameUUIDFromBytes(modifierKey.getBytes(StandardCharsets.UTF_8));
    }
    *///?}
}

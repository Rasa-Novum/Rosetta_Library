package net.rasanovum.rosetta.util;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.rasanovum.rosetta.mixin.common.AttributeInstanceAccessor;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/** Attribute modifier helpers. */
public final class AttributeCompat {
    //? if >=1.21 {
    private static final Map<String, ResourceLocation> MODIFIER_IDS = new ConcurrentHashMap<>();
    //?} else {
    /*private static final Map<String, UUID> MODIFIER_IDS = new ConcurrentHashMap<>();
    *///?}

    private AttributeCompat() {}

    //? if >=1.21 {
    public static AttributeModifier movementSpeedMultiplier(String modifierKey, double amount) {
        return new AttributeModifier(attributeModifierId(modifierKey), amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
    //?} else {
    /*public static AttributeModifier movementSpeedMultiplier(String modifierKey, double amount) {
        return new AttributeModifier(attributeModifierUuid(modifierKey), modifierKey, amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    *///?}

    public static boolean hasModifier(AttributeInstance attribute, String modifierKey) {
        //? if >=1.21 {
        return attribute.getModifier(attributeModifierId(modifierKey)) != null;
        //?} else {
        /*return attribute.getModifier(attributeModifierUuid(modifierKey)) != null;
        *///?}
    }

    public static void removeModifier(AttributeInstance attribute, String modifierKey) {
        //? if >=1.21 {
        attribute.removeModifier(attributeModifierId(modifierKey));
        //?} else {
        /*attribute.removeModifier(attributeModifierUuid(modifierKey));
        *///?}
    }

    public static String canonicalModifierKey(String modifierKey) {
        //? if >=1.21 {
        return attributeModifierId(modifierKey).toString();
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
        double value = calculateValue(attribute.getBaseValue(),
                ((AttributeInstanceAccessor) attribute).rosetta$modifiers().values(), skipModifier);
        //? if >=1.21 {
        return attribute.getAttribute().value().sanitizeValue(value);
        //?} else {
        /*return attribute.getAttribute().sanitizeValue(value);
        *///?}
    }

    static double calculateValue(double base, Iterable<AttributeModifier> modifiers, Predicate<AttributeModifier> skipModifier) {
        double baseMultiplier = 0.0;
        double totalMultiplier = 1.0;
        for (AttributeModifier modifier : modifiers) {
            if (skipModifier.test(modifier)) continue;
            double amount = amount(modifier);
            //? if >=1.21 {
            switch (modifier.operation()) {
                case ADD_VALUE -> base += amount;
                case ADD_MULTIPLIED_BASE -> baseMultiplier += amount;
                case ADD_MULTIPLIED_TOTAL -> totalMultiplier *= 1.0 + amount;
            }
            //?} else {
            /*switch (modifier.getOperation()) {
                case ADDITION -> base += amount;
                case MULTIPLY_BASE -> baseMultiplier += amount;
                case MULTIPLY_TOTAL -> totalMultiplier *= 1.0 + amount;
            }
            *///?}
        }
        return (base + base * baseMultiplier) * totalMultiplier;
    }

    //? if >=1.21 {
    private static ResourceLocation attributeModifierId(String modifierKey) {
        return MODIFIER_IDS.computeIfAbsent(modifierKey, RegistryCompat::getLocation);
    }
    //?} else {
    /*private static UUID attributeModifierUuid(String modifierKey) {
        return MODIFIER_IDS.computeIfAbsent(modifierKey, key -> UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)));
    }
    *///?}

    public static double amount(AttributeModifier modifier) {
        //? if >=1.21 {
        return modifier.amount();
        //?} else {
        /*return modifier.getAmount();
        *///?}
    }
}

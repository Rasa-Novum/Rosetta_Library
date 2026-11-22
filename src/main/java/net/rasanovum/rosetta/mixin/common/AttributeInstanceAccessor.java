package net.rasanovum.rosetta.mixin.common;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AttributeInstance.class)
public interface AttributeInstanceAccessor {
    @Accessor("modifierById")
    Map<?, AttributeModifier> rosetta$modifiers();
}

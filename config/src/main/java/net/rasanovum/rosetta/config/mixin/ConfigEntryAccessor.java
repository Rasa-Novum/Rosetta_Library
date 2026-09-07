package net.rasanovum.rosetta.config.mixin;

import eu.midnightdust.lib.config.EntryInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntryInfo.class, remap = false)
public interface ConfigEntryAccessor {
    @Accessor("value") Object rosettaConfig$value();
    @Accessor("value") void rosettaConfig$value(Object value);
    @Accessor("defaultValue") Object rosettaConfig$defaultValue();
    @Accessor("tempValue") void rosettaConfig$tempValue(String value);
}

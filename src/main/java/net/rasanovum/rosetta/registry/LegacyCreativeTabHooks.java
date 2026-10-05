package net.rasanovum.rosetta.registry;

//? if <1.19.3 {
/*import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class LegacyCreativeTabHooks {
    private static final Map<CreativeModeTab, List<Consumer<ModRegistrar.CreativeTabOutput>>> CALLBACKS = new IdentityHashMap<>();

    private LegacyCreativeTabHooks() {}

    public static void register(CreativeModeTab tab, Consumer<ModRegistrar.CreativeTabOutput> callback) {
        CALLBACKS.computeIfAbsent(tab, ignored -> new ArrayList<>()).add(callback);
    }

    public static void append(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
        ModRegistrar.CreativeTabOutput output = stack -> {
            if (stacks.stream().noneMatch(existing -> ItemStack.matches(existing, stack))) {
                stacks.add(stack);
            }
        };
        if (tab == CreativeModeTab.TAB_SEARCH) {
            CALLBACKS.values().forEach(callbacks -> callbacks.forEach(callback -> callback.accept(output)));
        } else {
            CALLBACKS.getOrDefault(tab, List.of()).forEach(callback -> callback.accept(output));
        }
    }
}
*///?}

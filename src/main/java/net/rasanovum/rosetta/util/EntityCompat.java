package net.rasanovum.rosetta.util;
//? if >=26.1 {
/*import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
*///?}

import net.minecraft.network.chat.Component;
//? if >1.20.1
import net.minecraft.advancements.AdvancementHolder;
//? if <1.21
/*import net.minecraft.advancements.Advancement;*/
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.rasanovum.rosetta.Rosetta;

/** Player and entity helpers. */
public final class EntityCompat {
    private EntityCompat() {}

    public static MinecraftServer getPlayerServer(ServerPlayer player) {
        //? if >=26.1 {
        /*return player.level().getServer();
        *///?} else {
        return player.server;
        //?}
    }

    public static ServerLevel getPlayerServerLevel(ServerPlayer player) {
        //? if >=26.1 {
        /*return player.level();
        *///?} else {
        //? if <1.19.3 {
        /*return player.getLevel();
        *///?} else {
        return player.serverLevel();
        //?}
        //?}
    }

    public static void awardAdvancement(ServerPlayer player, String advancementId) {
        try {
            //? if <1.21 {
            /*Advancement advancement = player.server.getAdvancements().getAdvancement(new net.minecraft.resources.ResourceLocation(advancementId));
            if (advancement != null) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if (!progress.isDone()) for (String criterion : progress.getRemainingCriteria()) player.getAdvancements().award(advancement, criterion);
            }
            *///?} else {
            AdvancementHolder advancement = getPlayerServer(player).getAdvancements().get(RegistryCompat.getLocation(advancementId));
            if (advancement != null) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if (!progress.isDone()) {
                    boolean grantedAny = false;
                    for (String criterion : progress.getRemainingCriteria()) {
                        if (player.getAdvancements().award(advancement, criterion)) grantedAny = true;
                    }
                    if (grantedAny) {
                        //? if >=26.1 {
                        /*player.getAdvancements().flushDirty(player, false);
                        *///?} else {
                        player.getAdvancements().flushDirty(player);
                        //?}
                    }
                }
            }
            //?}
        } catch (Exception e) {
            Rosetta.LOGGER.warn("Failed to award advancement {} to player {}: {}", advancementId, player.getName().getString(), e.getMessage());
        }
    }

    public static boolean hasPermission(Player player, int requiredLevel) {
        //? if >=26.1 {
        /*return player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(requiredLevel)));
        *///?} else {
        return player.hasPermissions(requiredLevel);
        //?}
    }

    public static boolean hasPermission(net.minecraft.commands.CommandSourceStack source, int requiredLevel) {
        //? if >=26.1 {
        /*return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(requiredLevel)));
        *///?} else {
        return source.hasPermission(requiredLevel);
        //?}
    }

    public static void displayClientMessage(Player player, Component message, boolean actionBar) {
        //? if >=26.1 {
        /*if (actionBar) player.sendOverlayMessage(message);
        else player.sendSystemMessage(message);
        *///?} else {
        player.displayClientMessage(message, actionBar);
        //?}
    }

    public static boolean startRiding(Entity rider, Entity vehicle, boolean force) {
        //? if >=26.1 {
        /*return rider.startRiding(vehicle, force, false);
        *///?} else {
        return rider.startRiding(vehicle, force);
        //?}
    }

    public static boolean isItemOnCooldown(Player player, net.minecraft.world.item.Item item, net.minecraft.world.item.ItemStack stack) {
        //? if >=26.1 {
        /*return player.getCooldowns().isOnCooldown(stack);
        *///?} else {
        return player.getCooldowns().isOnCooldown(item);
        //?}
    }

    public static void teleportTo(Entity entity, net.minecraft.server.level.ServerLevel level, double x, double y, double z, float yRot, float xRot) {
        //? if >=26.1 {
        /*entity.teleportTo(level, x, y, z, java.util.Set.of(), yRot, xRot, false);
        *///?}
        //? if <1.19.3 {
        /*if (entity instanceof ServerPlayer player) {
            player.teleportTo(level, x, y, z, yRot, xRot);
        } else if (entity.level == level) {
            entity.moveTo(x, y, z, yRot, xRot);
        } else {
            Entity replacement = entity.getType().create(level);
            if (replacement != null) {
                replacement.restoreFrom(entity);
                replacement.moveTo(x, y, z, yRot, xRot);
                entity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                level.addDuringTeleport(replacement);
            }
        }
        *///?}
        //? if >=1.19.3 && <26.1
        entity.teleportTo(level, x, y, z, java.util.Set.of(), yRot, xRot);
    }
}

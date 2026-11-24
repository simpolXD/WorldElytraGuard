package com.abyssaldominion.elytraguard.listener;

import com.abyssaldominion.elytraguard.AbyssaldominionWorldElytraGuard;
import com.abyssaldominion.elytraguard.config.ElytraGuardConfig;
import com.abyssaldominion.elytraguard.util.ReflectionUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldTransitionListener implements Listener {

    private static final Set<UUID> PROCESSING = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final AbyssaldominionWorldElytraGuard plugin;

    public WorldTransitionListener(AbyssaldominionWorldElytraGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!player.isOnline()) {
            return;
        }

        if (plugin.getConfigManager().isRestrictedWorld(player.getWorld())) {
            handleEnterRestrictedWorld(plugin, player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOnline()) {
            return;
        }

        if (plugin.getConfigManager().isRestrictedWorld(player.getWorld())) {
            handleEnterRestrictedWorld(plugin, player);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!player.isOnline()) {
            return;
        }

        // Defer 1 tick because respawn location may not be fully applied yet
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && plugin.getConfigManager().isRestrictedWorld(player.getWorld())) {
                handleEnterRestrictedWorld(plugin, player);
            }
        }, 1L);
    }

    public static void handleEnterRestrictedWorld(AbyssaldominionWorldElytraGuard plugin, Player player) {
        UUID uuid = player.getUniqueId();
        if (!PROCESSING.add(uuid)) {
            return;
        }

        try {
            // Stop any active gliding immediately
            ReflectionUtil.stopGliding(player);

            ElytraGuardConfig config = plugin.getConfigManager();
            if (config.isForceUnequipOnEntry()) {
                safeUnequipElytra(player, config);
            }
        } finally {
            PROCESSING.remove(uuid);
        }
    }

    private static void safeUnequipElytra(Player player, ElytraGuardConfig config) {
        ItemStack chest = player.getInventory().getChestplate();
        if (chest == null || chest.getType() != Material.ELYTRA) {
            return;
        }

        ItemStack elytra = chest.clone();
        player.getInventory().setChestplate(new ItemStack(Material.AIR));

        // Try to add the Elytra to the inventory
        java.util.HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(elytra);
        if (!leftover.isEmpty()) {
            // Failed to add — restore the original Elytra to the chest slot
            player.getInventory().setChestplate(chest);
            player.sendMessage(net.kyori.adventure.text.Component.text(config.getMessage("inventory-full")).color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        }
    }
}

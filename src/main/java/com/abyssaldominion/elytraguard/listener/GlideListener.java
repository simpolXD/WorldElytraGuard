package com.abyssaldominion.elytraguard.listener;

import com.abyssaldominion.elytraguard.AbyssaldominionWorldElytraGuard;
import com.abyssaldominion.elytraguard.config.ElytraGuardConfig;
import com.abyssaldominion.elytraguard.util.ReflectionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;

public class GlideListener implements Listener {

    private final AbyssaldominionWorldElytraGuard plugin;

    public GlideListener(AbyssaldominionWorldElytraGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ElytraGuardConfig config = plugin.getConfigManager();
        if (!config.isEnabled() || !config.isPreventGliding()) {
            return;
        }

        if (!config.isRestrictedWorld(player.getWorld())) {
            return;
        }

        // Only block attempts to START gliding (isGliding() == true)
        // Allow landing (isGliding() == false) so players can stop naturally
        if (event.isGliding()) {
            event.setCancelled(true);
            player.sendMessage(Component.text(config.getMessage("glide-blocked")).color(NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onPlayerElytraBoost(PlayerElytraBoostEvent event) {
        ElytraGuardConfig config = plugin.getConfigManager();
        if (!config.isEnabled() || !config.isPreventGliding()) {
            return;
        }

        Player player = event.getPlayer();
        if (!config.isRestrictedWorld(player.getWorld())) {
            return;
        }

        event.setCancelled(true);
    }
}

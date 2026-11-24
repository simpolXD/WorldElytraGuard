package com.abyssaldominion.elytraguard;

import com.abyssaldominion.elytraguard.command.ElytraGuardCommand;
import com.abyssaldominion.elytraguard.config.ElytraGuardConfig;
import com.abyssaldominion.elytraguard.listener.ElytraEquipListener;
import com.abyssaldominion.elytraguard.listener.GlideListener;
import com.abyssaldominion.elytraguard.listener.WorldTransitionListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class AbyssaldominionWorldElytraGuard extends JavaPlugin {

    private static AbyssaldominionWorldElytraGuard instance;
    private ElytraGuardConfig configManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        configManager = new ElytraGuardConfig(this);
        configManager.load();

        getServer().getPluginManager().registerEvents(new ElytraEquipListener(this), this);
        getServer().getPluginManager().registerEvents(new GlideListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldTransitionListener(this), this);

        getCommand("elytraguard").setExecutor(new ElytraGuardCommand(this));

        reconcileOnlinePlayers();

        getLogger().info("AbyssaldominionWorldElytraGuard v" + getPluginMeta().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AbyssaldominionWorldElytraGuard v" + getPluginMeta().getVersion() + " disabled!");
    }

    private void reconcileOnlinePlayers() {
        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            if (configManager.isRestrictedWorld(player.getWorld())) {
                WorldTransitionListener.handleEnterRestrictedWorld(this, player);
            }
        }
    }

    public static AbyssaldominionWorldElytraGuard getInstance() {
        return instance;
    }

    public ElytraGuardConfig getConfigManager() {
        return configManager;
    }
}

package com.abyssaldominion.elytraguard.config;

import com.abyssaldominion.elytraguard.AbyssaldominionWorldElytraGuard;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ElytraGuardConfig {

    private final AbyssaldominionWorldElytraGuard plugin;
    private FileConfiguration config;
    private File configFile;

    private boolean enabled = true;
    private boolean preventEquipping = true;
    private boolean preventGliding = true;
    private boolean forceUnequipOnEntry = true;
    private final List<String> blockedWorlds = new ArrayList<>();
    private final Map<String, String> messages = new HashMap<>();

    public ElytraGuardConfig(AbyssaldominionWorldElytraGuard plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings == null) {
            settings = config.createSection("settings");
            settings.set("enabled", true);
            settings.set("blocked-worlds", new ArrayList<>());
            settings.set("prevent-equipping", true);
            settings.set("prevent-gliding", true);
            settings.set("force-unequip-on-entry", true);
            saveConfig();
        }

        this.enabled = settings.getBoolean("enabled", true);
        this.preventEquipping = settings.getBoolean("prevent-equipping", true);
        this.preventGliding = settings.getBoolean("prevent-gliding", true);
        this.forceUnequipOnEntry = settings.getBoolean("force-unequip-on-entry", true);

        List<String> worlds = settings.getStringList("blocked-worlds");
        if (worlds == null) {
            worlds = new ArrayList<>();
        }
        blockedWorlds.clear();
        for (String world : worlds) {
            if (world != null) {
                String trimmed = world.trim();
                if (!trimmed.isEmpty()) {
                    blockedWorlds.add(trimmed);
                }
            }
        }

        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection == null) {
            messagesSection = config.createSection("messages");
            messagesSection.set("equip-blocked", "<red>You cannot equip an Elytra in this world.");
            messagesSection.set("glide-blocked", "<red>Elytra flight is disabled in this world.");
            messagesSection.set("reload-success", "<green>AbyssaldominionWorldElytraGuard configuration reloaded.");
            messagesSection.set("inventory-full", "<yellow>Make space in your inventory to safely unequip your Elytra.");
            saveConfig();
        }

        messages.clear();
        messages.put("equip-blocked", messagesSection.getString("equip-blocked", "<red>You cannot equip an Elytra in this world."));
        messages.put("glide-blocked", messagesSection.getString("glide-blocked", "<red>Elytra flight is disabled in this world."));
        messages.put("reload-success", messagesSection.getString("reload-success", "<green>AbyssaldominionWorldElytraGuard configuration reloaded."));
        messages.put("inventory-full", messagesSection.getString("inventory-full", "<yellow>Make space in your inventory to safely unequip your Elytra."));
    }

    public void reload() {
        load();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml: " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPreventEquipping() {
        return preventEquipping;
    }

    public boolean isPreventGliding() {
        return preventGliding;
    }

    public boolean isForceUnequipOnEntry() {
        return forceUnequipOnEntry;
    }

    public boolean isRestrictedWorld(org.bukkit.World world) {
        if (world == null) return false;
        return blockedWorlds.contains(world.getName());
    }

    public boolean isRestrictedWorld(String worldName) {
        if (worldName == null) return false;
        return blockedWorlds.contains(worldName);
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "");
    }
}

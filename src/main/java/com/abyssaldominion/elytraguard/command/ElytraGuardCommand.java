package com.abyssaldominion.elytraguard.command;

import com.abyssaldominion.elytraguard.AbyssaldominionWorldElytraGuard;
import com.abyssaldominion.elytraguard.config.ElytraGuardConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ElytraGuardCommand implements CommandExecutor {

    private final AbyssaldominionWorldElytraGuard plugin;

    public ElytraGuardCommand(AbyssaldominionWorldElytraGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /" + label + " reload | version", NamedTextColor.RED));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("version")) {
            sender.sendMessage(Component.text("AbyssaldominionWorldElytraGuard v" + plugin.getPluginMeta().getVersion(), NamedTextColor.GOLD));
            sender.sendMessage(Component.text("Author: simp0ol", NamedTextColor.YELLOW));
            return true;
        }

        if (!sub.equals("reload")) {
            sender.sendMessage(Component.text("Unknown subcommand. Usage: /" + label + " reload | version", NamedTextColor.RED));
            return true;
        }

        if (!sender.hasPermission("worldelytraguard.reload")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        try {
            ElytraGuardConfig config = plugin.getConfigManager();
            config.reload();
            sender.sendMessage(Component.text(config.getMessage("reload-success")).color(NamedTextColor.GREEN));
            plugin.getLogger().info("Configuration reloaded by " + (sender instanceof Player ? ((Player) sender).getName() : "console"));
        } catch (Exception e) {
            sender.sendMessage(Component.text("Failed to reload configuration: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Config reload failed: " + e.getMessage());
        }

        return true;
    }
}

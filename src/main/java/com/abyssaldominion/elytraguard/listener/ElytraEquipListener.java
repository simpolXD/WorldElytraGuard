package com.abyssaldominion.elytraguard.listener;

import com.abyssaldominion.elytraguard.AbyssaldominionWorldElytraGuard;
import com.abyssaldominion.elytraguard.config.ElytraGuardConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import io.papermc.paper.event.player.PlayerSwapWithEquipmentSlotEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ElytraEquipListener implements Listener {

    private final AbyssaldominionWorldElytraGuard plugin;

    public ElytraEquipListener(AbyssaldominionWorldElytraGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ElytraGuardConfig config = plugin.getConfigManager();
        if (!config.isEnabled() || !config.isPreventEquipping()) {
            return;
        }

        if (!config.isRestrictedWorld(player.getWorld())) {
            return;
        }

        if (wouldEquipElytra(event)) {
            event.setCancelled(true);
            player.sendMessage(Component.text(config.getMessage("equip-blocked")).color(NamedTextColor.RED));
            player.updateInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ElytraGuardConfig config = plugin.getConfigManager();
        if (!config.isEnabled() || !config.isPreventEquipping()) {
            return;
        }

        if (!config.isRestrictedWorld(player.getWorld())) {
            return;
        }

        ItemStack cursor = event.getOldCursor();
        if (cursor == null || cursor.getType() != Material.ELYTRA) {
            return;
        }

        for (int slot : event.getRawSlots()) {
            if (event.getView().getInventory(slot) == player.getInventory()
                    && event.getView().getSlotType(slot) == InventoryType.SlotType.ARMOR
                    && event.getView().convertSlot(slot) == 38) {
                event.setCancelled(true);
                player.sendMessage(Component.text(config.getMessage("equip-blocked")).color(NamedTextColor.RED));
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerSwapWithEquipmentSlot(PlayerSwapWithEquipmentSlotEvent event) {
        ElytraGuardConfig config = plugin.getConfigManager();
        if (!config.isEnabled() || !config.isPreventEquipping()) {
            return;
        }

        Player player = event.getPlayer();
        if (!config.isRestrictedWorld(player.getWorld())) {
            return;
        }

        if (event.getSlot() == EquipmentSlot.CHEST
                && event.getItemInHand() != null
                && event.getItemInHand().getType() == Material.ELYTRA) {
            event.setCancelled(true);
            player.sendMessage(Component.text(config.getMessage("equip-blocked")).color(NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.ELYTRA) {
            return;
        }

        ElytraGuardConfig config = plugin.getConfigManager();
        if (!config.isEnabled() || !config.isPreventEquipping()) {
            return;
        }

        Player player = event.getPlayer();
        if (!config.isRestrictedWorld(player.getWorld())) {
            return;
        }

        event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        player.sendMessage(Component.text(config.getMessage("equip-blocked")).color(NamedTextColor.RED));
    }

    private boolean wouldEquipElytra(InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Normal click in chest slot with Elytra on cursor
        if (event.getSlotType() == InventoryType.SlotType.ARMOR
                && event.getSlot() == 38
                && cursor != null
                && cursor.getType() == Material.ELYTRA) {
            return true;
        }

        // Number-key swap into chest slot with Elytra
        if (event.getClick() == ClickType.NUMBER_KEY
                && event.getSlotType() == InventoryType.SlotType.ARMOR
                && event.getSlot() == 38
                && event.getHotbarButton() >= 0 && event.getHotbarButton() < 9
                && event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) != null
                && event.getWhoClicked().getInventory().getItem(event.getHotbarButton()).getType() == Material.ELYTRA) {
            return true;
        }

        if (event.getClick() == ClickType.SWAP_OFFHAND
                && event.getSlotType() == InventoryType.SlotType.ARMOR
                && event.getSlot() == 38
                && event.getWhoClicked().getInventory().getItemInOffHand() != null
                && event.getWhoClicked().getInventory().getItemInOffHand().getType() == Material.ELYTRA) {
            return true;
        }

        // Shift-click: moving Elytra TO chest slot (not FROM it)
        if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)
                && event.getAction() == org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY
                && event.getView().getType() == InventoryType.CRAFTING
                && event.getClickedInventory() == event.getWhoClicked().getInventory()
                && (event.getWhoClicked().getInventory().getChestplate() == null
                    || event.getWhoClicked().getInventory().getChestplate().getType().isAir())
                && current != null
                && current.getType() == Material.ELYTRA) {
            // If the click is not already in the chest slot, it will move there
            if (!(event.getSlotType() == InventoryType.SlotType.ARMOR && event.getSlot() == 38)) {
                return true;
            }
        }

        return false;
    }
}

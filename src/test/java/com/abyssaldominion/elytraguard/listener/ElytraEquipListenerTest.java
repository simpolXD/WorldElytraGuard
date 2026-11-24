package com.abyssaldominion.elytraguard.listener;

import com.abyssaldominion.elytraguard.AbyssaldominionWorldElytraGuard;
import com.abyssaldominion.elytraguard.config.ElytraGuardConfig;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ElytraEquipListenerTest {
    private AbyssaldominionWorldElytraGuard plugin;
    private ElytraGuardConfig config;
    private Player player;
    private PlayerInventory inventory;
    private InventoryView view;
    private ElytraEquipListener listener;

    @BeforeEach void setUp() {
        plugin = mock(AbyssaldominionWorldElytraGuard.class);
        config = mock(ElytraGuardConfig.class);
        player = mock(Player.class);
        inventory = mock(PlayerInventory.class);
        view = mock(InventoryView.class);
        when(plugin.getConfigManager()).thenReturn(config);
        when(config.isEnabled()).thenReturn(true);
        when(config.isPreventEquipping()).thenReturn(true);
        when(config.isRestrictedWorld(any(World.class))).thenReturn(true);
        when(config.getMessage(anyString())).thenReturn("Equip denied");
        when(player.getWorld()).thenReturn(mock(World.class));
        when(player.getInventory()).thenReturn(inventory);
        when(view.getPlayer()).thenReturn(player);
        when(view.getBottomInventory()).thenReturn(inventory);
        when(view.getInventory(6)).thenReturn(inventory);
        when(view.convertSlot(6)).thenReturn(38);
        listener = new ElytraEquipListener(plugin);
    }

    private ItemStack item(Material material) {
        ItemStack item = mock(ItemStack.class);
        when(item.getType()).thenReturn(material);
        when(item.getAmount()).thenReturn(1);
        when(item.clone()).thenReturn(item);
        return item;
    }

    @Test void rightClickBlockDeniesOnlyItemUseNotBlockInteraction() {
        ItemStack wings = item(Material.ELYTRA);
        org.bukkit.event.player.PlayerInteractEvent event = new org.bukkit.event.player.PlayerInteractEvent(
                player, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, wings, null,
                org.bukkit.block.BlockFace.UP, EquipmentSlot.HAND);
        org.bukkit.event.Event.Result blockBefore = event.useInteractedBlock();
        listener.onPlayerInteract(event);
        assertEquals(org.bukkit.event.Event.Result.DENY, event.useItemInHand(), "Right-click-block can also equip wings");
        assertEquals(blockBefore, event.useInteractedBlock(), "Do not cancel unrelated chest/door use");
    }

    @Test void quickMoveBetweenChestContainerAndStorageDoesNotEquip() {
        Inventory container = mock(Inventory.class);
        when(view.getType()).thenReturn(InventoryType.CHEST);
        when(view.getInventory(0)).thenReturn(container);
        ItemStack wings = item(Material.ELYTRA);
        when(view.getItem(0)).thenReturn(wings);
        InventoryClickEvent event = new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER,
                0, ClickType.SHIFT_LEFT, InventoryAction.MOVE_TO_OTHER_INVENTORY);
        listener.onInventoryClick(event);
        assertFalse(event.isCancelled(), "Moving wings between storage and a container must remain possible");
    }

    private ItemStack itemForQuickMove() { return item(Material.ELYTRA); }

    @Test void dragUsesViewRawChestMappingInsteadOfPlayerInventoryIndex() {
        ItemStack wings = item(Material.ELYTRA);
        when(view.getSlotType(6)).thenReturn(InventoryType.SlotType.ARMOR);
        InventoryDragEvent event = new InventoryDragEvent(view, null, wings, false, java.util.Map.of(6, wings));
        listener.onInventoryDrag(event);
        assertTrue(event.isCancelled(), "Player crafting-view chest raw slot is 6, not inventory index 38");
        verify(inventory, never()).setChestplate(any());
    }

    @Test void offhandSwapIntoChestChecksOffhandItem() {
        ItemStack wings = item(Material.ELYTRA);
        when(inventory.getItemInOffHand()).thenReturn(wings);
        InventoryClickEvent event = new InventoryClickEvent(view, InventoryType.SlotType.ARMOR,
                6, ClickType.SWAP_OFFHAND, InventoryAction.HOTBAR_SWAP);
        listener.onInventoryClick(event);
        assertTrue(event.isCancelled(), "Offhand Elytra must not enter the chest equipment slot");
    }

    @Test void numberKeyIntoChestChecksHotbarInsteadOfCursor() {
        ItemStack wings = item(Material.ELYTRA);
        when(inventory.getItem(2)).thenReturn(wings);
        InventoryClickEvent event = new InventoryClickEvent(view, InventoryType.SlotType.ARMOR,
                6, ClickType.NUMBER_KEY, InventoryAction.HOTBAR_SWAP, 2);
        listener.onInventoryClick(event);
        assertTrue(event.isCancelled(), "Number-key hotbar Elytra must not equip while cursor is empty");
        verify(inventory, never()).setChestplate(any());
        verify(view, never()).setCursor(any());
    }
}

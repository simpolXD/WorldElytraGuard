package com.abyssaldominion.elytraguard.support;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.stream.Stream;

/** Test-only server boundary for InventoryType's registry-backed menu constants. */
public final class TestRegistryAccess implements RegistryAccess {
    @Override public <T extends Keyed> Registry<T> getRegistry(Class<T> type) { return registry(); }
    @Override public <T extends Keyed> Registry<T> getRegistry(RegistryKey<T> key) { return registry(); }

    @SuppressWarnings("unchecked")
    private <T extends Keyed> Registry<T> registry() {
        return (Registry<T>) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Registry.class}, (proxy, method, args) -> switch (method.getName()) {
            case "get" -> menu((NamespacedKey) args[0]);
            case "stream", "keyStream" -> Stream.empty();
            case "iterator" -> List.of().iterator();
            case "size", "hashCode" -> 0;
            case "equals" -> proxy == args[0];
            case "toString" -> "TestRegistry";
            default -> null;
        });
    }

    private MenuType menu(NamespacedKey key) {
        return (MenuType) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{MenuType.Typed.class}, (proxy, method, args) -> switch (method.getName()) {
            case "typed" -> proxy;
            case "getInventoryViewClass" -> InventoryView.class;
            case "getKey" -> key;
            case "equals" -> proxy == args[0];
            case "hashCode" -> key.hashCode();
            case "toString" -> key.toString();
            default -> null;
        });
    }
}

package com.abyssaldominion.elytraguard.util;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Method;

public final class ReflectionUtil {

    private static final Method SET_GLIDING_METHOD;
    private static final Method STOP_FALL_FLYING_METHOD;
    private static final boolean USE_SET_GLIDING;

    static {
        Method setGliding = null;
        Method stopFallFlying = null;
        boolean useSetGliding = false;

        try {
            setGliding = LivingEntity.class.getMethod("setGliding", boolean.class);
            useSetGliding = true;
        } catch (NoSuchMethodException e) {
            // Fallback to NMS reflection
        }

        if (!useSetGliding) {
            try {
                stopFallFlying = Class.forName("net.minecraft.world.entity.LivingEntity")
                        .getMethod("stopFallFlying");
            } catch (Exception e) {
                // Logging handled by caller
            }
        }

        SET_GLIDING_METHOD = setGliding;
        STOP_FALL_FLYING_METHOD = stopFallFlying;
        USE_SET_GLIDING = useSetGliding;
    }

    private ReflectionUtil() {
    }

    public static void stopGliding(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        try {
            if (USE_SET_GLIDING && SET_GLIDING_METHOD != null) {
                SET_GLIDING_METHOD.invoke(player, false);
            } else if (STOP_FALL_FLYING_METHOD != null) {
                Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
                STOP_FALL_FLYING_METHOD.invoke(nmsPlayer);
            }
        } catch (Exception e) {
            // Reflection failed — log but do not crash
        }
    }
}

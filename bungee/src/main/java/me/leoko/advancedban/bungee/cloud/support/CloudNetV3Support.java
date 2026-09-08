package me.leoko.advancedban.bungee.cloud.support;

import me.leoko.advancedban.bungee.cloud.CloudSupport;

import java.lang.reflect.Method;
import java.util.UUID;

public class CloudNetV3Support implements CloudSupport {

    @Override
    public void kick(UUID uniqueID, String reason) {
        try {
            Class<?> driverClass = Class.forName("de.dytanic.cloudnet.driver.CloudNetDriver");
            Object driver = driverClass.getMethod("getInstance").invoke(null);
            Object registry = driverClass.getMethod("getServicesRegistry").invoke(driver);
            Class<?> playerMgrClass = Class.forName("de.dytanic.cloudnet.ext.bridge.player.IPlayerManager");
            Method getFirstService = registry.getClass().getMethod("getFirstService", Class.class);
            Object playerMgr = getFirstService.invoke(registry, playerMgrClass);
            Method getPlayerExecutor = playerMgrClass.getMethod("getPlayerExecutor", UUID.class);
            Object playerExecutor = getPlayerExecutor.invoke(playerMgr, uniqueID);
            Method kick = playerExecutor.getClass().getMethod("kick", String.class);
            kick.invoke(playerExecutor, reason);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

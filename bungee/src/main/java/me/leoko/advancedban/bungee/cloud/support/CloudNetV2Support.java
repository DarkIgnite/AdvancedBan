
package me.leoko.advancedban.bungee.cloud.support;

import me.leoko.advancedban.bungee.cloud.CloudSupport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public class CloudNetV2Support implements CloudSupport {

    @Override
    public void kick(UUID uniqueID, String reason) {
        try {
            Class<?> bridgeClass = Class.forName("de.dytanic.cloudnet.api.player.PlayerExecutorBridge");
            Field instanceField = bridgeClass.getField("INSTANCE");
            Object instance = instanceField.get(null);

            Class<?> cloudServerClass = Class.forName("de.dytanic.cloudnet.bridge.CloudServer");
            Object cloudServer = cloudServerClass.getMethod("getInstance").invoke(null);
            Map<?, ?> cloudPlayers = (Map<?, ?>) cloudServerClass.getMethod("getCloudPlayers").invoke(cloudServer);
            Object cloudPlayer = cloudPlayers.get(uniqueID);

            Method kickPlayer = bridgeClass.getMethod("kickPlayer", Class.forName("de.dytanic.cloudnet.lib.player.CloudPlayer"), String.class);
            kickPlayer.invoke(instance, cloudPlayer, reason);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

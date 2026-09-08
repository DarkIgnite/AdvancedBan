package me.leoko.advancedban.bungee.utils;

import me.leoko.advancedban.utils.Permissionable;

import java.lang.reflect.Method;
import java.util.List;

public class CloudNetCloudPermsOfflineUser implements Permissionable {
    private Object permissionUser;

    public CloudNetCloudPermsOfflineUser(String name) {
        try {
            Class<?> driverClass = Class.forName("de.dytanic.cloudnet.driver.CloudNetDriver");
            Object driver = driverClass.getMethod("getInstance").invoke(null);
            Object permMgmt = driverClass.getMethod("getPermissionManagement").invoke(driver);
            Method getUsers = permMgmt.getClass().getMethod("getUsers", String.class);
            List<?> users = (List<?>) getUsers.invoke(permMgmt, name);

            if (users != null && !users.isEmpty()) {
                this.permissionUser = users.get(0);
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        if (permissionUser == null) return false;
        try {
            Method hasPermMethod = permissionUser.getClass().getMethod("hasPermission", String.class);
            Object permCheckResult = hasPermMethod.invoke(permissionUser, permission);
            Method asBoolMethod = permCheckResult.getClass().getMethod("asBoolean");
            return (boolean) asBoolMethod.invoke(permCheckResult);
        } catch (Throwable ignored) {
            return false;
        }
    }
}

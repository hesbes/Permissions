package com.nijikokun.bukkit.Permissions;

import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

public class PrWorldListener extends WorldListener {

    @Override
    public void onWorldLoad(WorldLoadEvent event) {
        try {
            ((Permissions) Permissions.instance).getHandler().loadWorld(event.getWorld().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

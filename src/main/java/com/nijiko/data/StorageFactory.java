package com.nijiko.data;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.util.config.Configuration;

//TODO: Remove duplicated code
public final class StorageFactory {
    private static final Map<String, StorageCreator> creators = new HashMap<String, StorageCreator>();
    private static StorageCreator defaultCreator;

    private StorageFactory() {
    }

    public static boolean registerCreator(String id, StorageCreator creator) {
        if (creators.containsKey(id.toUpperCase()))
            return false;
        creators.put(id.toUpperCase(), creator);
        return true;
    }

    public static boolean registerDefaultCreator(StorageCreator creator) {
        if (defaultCreator != null)
            return false;
        defaultCreator = creator;
        return true;
    }
    
    public static void unregisterCreator(String id, StorageCreator creator) {
        creators.remove(id.toUpperCase());
        if(defaultCreator == creator)
            defaultCreator = null;
    }

    public final static UserStorage getUserStorage(String world, Configuration config) throws Exception {
        if (world == null)
            return null;
        if (config == null)
            return null;
        int reload = 6000;
        boolean autosave = true;
        config.load();
        String parent = config.getString("permissions.storage.worldcopy." + world);
        if (parent != null)
            world = parent;
        String userWorld = config.getString("permissions.storage.user.worldcopy." + world);
        if (userWorld == null)
            userWorld = world;
        parent = null;
        String typename = config.getString("permissions.storage.type");
        String userWorldType = config.getString("permissions.storage.worldtype." + userWorld);
        if (userWorldType == null || userWorldType.isEmpty())
            userWorldType = typename;

        reload = config.getInt("permissions.storage.reload", 6000); // Default
                                                                    // is
                                                                    // 5 minutes
        autosave = config.getBoolean("permissions.storage.autosave", true);

        StorageCreator creator;
//        System.out.println(userWorldType);
        
        if (userWorldType == null)
            creator = defaultCreator;
        else
            creator = creators.get(userWorldType.toUpperCase());
        
        if (creator == null) {
            System.err.println("Error occurred while selecting permissions user config type. Reverting to default creator.");
            creator = defaultCreator;
            if (creator == null) {
                throw new Exception("Error occurred while selecting permissions user config type. Default creator is null.");
            }
        }
        UserStorage us;
        try {
            us = creator.getUserStorage(userWorld, reload, autosave, config);
        } catch (Exception e) {
            e.printStackTrace();
            if (creator == defaultCreator) {
                throw e;
            }
            creator = defaultCreator;
            us = creator.getUserStorage(userWorld, reload, autosave, config);
        }

        return us;
    }

    public final static GroupStorage getGroupStorage(String world, Configuration config) throws Exception {
        if (world == null)
            return null;
        if (config == null)
            return null;
        int reload = 6000;
        boolean autosave = true;
        config.load();
        String parent = config.getString("permissions.storage.worldcopy." + world);
        if (parent != null)
            world = parent;
        String groupWorld = config.getString("permissions.storage.group.worldcopy." + world);
        if (groupWorld == null)
            groupWorld = world;
        parent = null;
        String typename = config.getString("permissions.storage.type");
        String groupWorldType = config.getString("permissions.storage.worldtype." + groupWorld);
        if (groupWorldType == null || groupWorldType.isEmpty())
            groupWorldType = typename;

        reload = config.getInt("permissions.storage.reload", 6000); // Default
                                                                    // is
                                                                    // 5 minutes
        autosave = config.getBoolean("permissions.storage.autosave", true);


        StorageCreator creator;
        if (groupWorldType == null)
            creator = defaultCreator;
        else
            creator = creators.get(groupWorldType);
        
        if (creator == null) {
            System.err.println("Error occurred while selecting permissions group config type. Reverting to default creator.");
            creator = defaultCreator;
            if (creator == null) {
                throw new Exception("Error occurred while selecting permissions group config type. Default creator is null.");
            }
        }
        GroupStorage gs;
        try {
            gs = creator.getGroupStorage(groupWorld, reload, autosave, config);
        } catch (Exception e) {
            e.printStackTrace();
            if (creator == defaultCreator) {
                throw e;
            }
            creator = defaultCreator;
            gs = creator.getGroupStorage(groupWorld, reload, autosave, config);
        }

        return gs;
    }
}

package com.nijiko.data;

import java.io.File;
import java.io.IOException;

import org.bukkit.util.config.Configuration;

import com.nijikokun.bukkit.Permissions.Permissions;

public class YamlCreator implements StorageCreator {

    @Override
    public UserStorage getUserStorage(String world, int reload, boolean autosave, Configuration config) throws Exception {
        if("*".equalsIgnoreCase(world))
            return new NullUserStorage(world);
        boolean global = world.equals("*");
        String worldString = Permissions.instance.getDataFolder().getPath();
        if (!global)
            worldString = worldString + File.separator + world;
        File worldFolder = new File(worldString);
        if (!worldFolder.exists())
            worldFolder.mkdirs();
        if (!worldFolder.isDirectory())
            throw new IOException("World folder for world " + world + " is not a directory.");
        File userFile = new File(worldString, global ? "globalUsers.yml" : "users.yml");
        if (!userFile.exists())
            if (!userFile.createNewFile())
                throw new IOException("Unable to create user config for world '" + world + "'.");
        if (!userFile.isFile())
            throw new IOException("User config for world " + world + " is not a file.");
        if (!userFile.canRead())
            throw new IOException("User config for world " + world + " cannot be read.");
        if (!userFile.canWrite())
            throw new IOException("User config for world " + world + " cannot be written to.");
        return new YamlUserStorage(new Configuration(userFile), world, reload, autosave);
    }

    @Override
    public GroupStorage getGroupStorage(String world, int reload, boolean autosave, Configuration config) throws Exception {
        if("*".equalsIgnoreCase(world))
            return new NullGroupStorage(world);
        boolean global = world.equals("*");
        String worldString = Permissions.instance.getDataFolder().getPath();
        if (!global)
            worldString = worldString + File.separator + world;
        File worldFolder = new File(worldString);
        if (!worldFolder.exists())
            worldFolder.mkdirs();
        if (!worldFolder.isDirectory())
            throw new IOException("World folder for world " + world + " is not a directory.");
        File groupFile = new File(worldString, global ? "globalGroups.yml" : "groups.yml");
        if (!groupFile.exists())
            if (!groupFile.createNewFile())
                throw new IOException("Unable to create user config for world '" + world + "'.");
        if (!groupFile.isFile())
            throw new IOException("Group config for world " + world + " is not a file.");
        if (!groupFile.canRead())
            throw new IOException("Group config for world " + world + " cannot be read.");
        if (!groupFile.canWrite())
            throw new IOException("Group config for world " + world + " cannot be written to.");
        return new YamlGroupStorage(new Configuration(groupFile), world, reload, autosave);
    }

}

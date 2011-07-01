package com.nijikokun.bukkit.Permissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
//import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.MessageHelper;
import com.nijiko.data.GroupWorld;
import com.nijiko.data.StorageFactory;
import com.nijiko.data.YamlCreator;
import com.nijiko.permissions.Entry;
import com.nijiko.permissions.Group;
import com.nijiko.permissions.ModularControl;
import com.nijiko.permissions.PermissionHandler;
import com.nijiko.permissions.User;
import com.nijikokun.bukkit.Permissions.commands.CommandManager;
import com.nijikokun.bukkit.Permissions.commands.PrCommand;

/**
 * Permissions 3.x Copyright (C) 2011 Matt 'The Yeti' Burnett <admin@theyeticave.net> Original Credit & Copyright (C) 2010 Nijikokun <nijikokun@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Permissions Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Permissions Public License for more details.
 * 
 * You should have received a copy of the GNU Permissions Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

public class Permissions extends JavaPlugin {

    protected static final Logger log = Logger.getLogger("Minecraft.Permissions");
    public static Plugin instance;
    // private Configuration storageConfig;
    @Deprecated
    public static final String name = "Permissions";
    public static final String version = "3.2";
    public static final String codename = "Yeti";

    public Listener buildListener = new Listener(this);

    /**
     * Controller for permissions and security. Use getHandler() instead.
     */
    @Deprecated
    public static PermissionHandler Security;

    // private PermissionHandler controller;

    private String defaultWorld = "";
    private static final boolean autoComplete = true;
    private final YamlCreator yamlC;
    private int dist = 10;
    private final PrWorldListener wListener = new PrWorldListener();

    private boolean errorFlag = false;
    private final CommandManager cmdMgr = new CommandManager();

    
    public String getDefaultWorld() {
        return defaultWorld;
    }
    // protected String pluginInternalName = "Permissions";

    public Permissions() {
        yamlC = new YamlCreator();
        StorageFactory.registerDefaultCreator(yamlC);
        StorageFactory.registerCreator("YAML", yamlC);
    }

    @Override
    public void onLoad() {
        instance = this;
        
        cmdMgr.registerCommand("permissions", new PrCommand());
        
        Properties prop = new Properties();
        FileInputStream in = null;
        getDataFolder().mkdirs();
        try {
            in = new FileInputStream(new File("server.properties"));
            prop.load(in);
            defaultWorld = prop.getProperty("level-name");
        } catch (IOException e) {
            System.err.println("[Permissions] Unable to read default world's name from server.properties.");
            e.printStackTrace();
            defaultWorld = "world";
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // PropertyHandler server = new PropertyHandler("server.properties");
        // defaultWorld = server.getString("level-name");

        File storageOpt = new File("plugins" + File.separator + "Permissions" + File.separator, "storageconfig.yml");
        storageOpt.getParentFile().mkdirs();
        if (!storageOpt.exists()) {
            try {
                System.out.println("[Permissions] Creating storageconfig.yml.");
                if (!storageOpt.createNewFile()) {
                    disable("[Permissions] Unable to create storageconfig.yml!");
                }
            } catch (IOException e) {
                e.printStackTrace();
                disable("[Permissions] storageconfig.yml could not be created.");
                return;
            }
        }
        if (!storageOpt.isFile() || !storageOpt.canRead()) {
            disable("[Permissions] storageconfig.yml is not a file or is not readable.");
            return;
        }

        Configuration storageConfig = new Configuration(storageOpt);
        storageConfig.load();
        // this.storageConfig = storageConfig;

        // Setup Permission
        setupPermissions(storageConfig);

        // Enabled
        log.info("[Permissions] (" + codename + ") was initialized.");
    }

    @Override
    public void onDisable() {
        if (!errorFlag) {
            log.info("[Permissions] (" + codename + ") saving data...");
            this.getHandler().closeAll();
            // getHandler() = null;
            log.info("[Permissions] (" + codename + ") saved all data.");
        }
        this.getServer().getScheduler().cancelTasks(this);
        log.info("[Permissions] (" + codename + ") disabled successfully.");
        return;
    }

    private void disable(String error) {
        if (error != null)
            log.severe(error);
        log.severe("[Permissions] Shutting down Permissions due to error(s).");
        this.errorFlag = true;
    }

    /**
     * Returns the PermissionHandler instance.<br />
     * <br />
     * <blockquote>
     * 
     * <pre>
     * Permissions.getHandler()
     * </pre>
     * 
     * </blockquote>
     * 
     * @return PermissionHandler
     */
    public PermissionHandler getHandler() {
        return Permissions.Security;
    }

    public void setupPermissions(Configuration storageConfig) {
//        System.out.println("Setting up Permissions...");
        try {
            Security = new ModularControl(storageConfig);
            getHandler().setDefaultWorld(defaultWorld);
            getHandler().load();
//            System.out.println(getServer().getWorlds());
            for (World w : getServer().getWorlds()) {
                getHandler().loadWorld(w.getName());
            }
        } catch (Throwable t) {
            t.printStackTrace();
            disable("[Permissions] Unable to load permission data.");
            return;
        }
        // getServer().getServicesManager().register(PermissionHandler.class,
        // getHandler(), this, ServicePriority.Normal);
    }

    @Override
    public void onEnable() {
        if (errorFlag) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        StorageFactory.registerDefaultCreator(yamlC);
        StorageFactory.registerCreator("YAML", yamlC);

        PluginDescriptionFile description = getDescription();
        // Enabled
        log.info("[" + description.getName() + "] version [" + description.getVersion() + "] (" + codename + ")  loaded");

        this.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, buildListener, Priority.High, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, buildListener, Priority.High, this);
        this.getServer().getPluginManager().registerEvent(Event.Type.WORLD_LOAD, wListener, Priority.Monitor, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        Player player = null;
        // String commandName = command.getName().toLowerCase();
        PluginDescriptionFile pdfFile = this.getDescription();
        MessageHelper msg = new MessageHelper(sender);
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (Security == null) {
            msg.send("&4[Permissions] Permissions was unable to load data during server load.");
            msg.send("&4[Permissions] Please post the the portion of your server log since the server start/reload in the forums thread.");
            return true;
        }
        if (args.length == 0) {
            if (player != null) {
                msg.send("&7-------[ &fPermissions&7 ]-------");
                msg.send("&7Currently running version: &f[" + pdfFile.getVersion() + "] (" + codename + ")");

                if (getHandler().has(player.getWorld().getName(), player.getName(), "permissions.reload")) {
                    msg.send("&7Reload with: &f/permissions &a-reload &e<world>");
                    msg.send("&fLeave &e<world> blank to reload default world.");
                }

                msg.send("&7-------[ &fPermissions&7 ]-------");
                return true;
            } else {
                sender.sendMessage("[" + pdfFile.getName() + "] version [" + pdfFile.getVersion() + "] (" + codename + ")  loaded");
                return true;
            }
        }
        
        return cmdMgr.onCommand(sender, command, commandLabel, args);
    }

    @Override
    public String toString() {
        PluginDescriptionFile pdf = this.getDescription();
        return pdf.getName() + " version " + pdf.getVersion() + " (" + codename + ")";
    }
}

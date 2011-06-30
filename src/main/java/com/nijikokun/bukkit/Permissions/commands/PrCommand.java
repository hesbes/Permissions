package com.nijikokun.bukkit.Permissions.commands;

import java.util.Collection;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.nijiko.MessageHelper;
import com.nijiko.permissions.Entry;
import com.nijiko.permissions.Group;
import com.nijiko.permissions.User;
import com.nijikokun.bukkit.Permissions.commands.CommandManager.ArgumentHolder;
import com.nijikokun.bukkit.Permissions.commands.CommandManager.CommandHandler;
import com.nijikokun.bukkit.Permissions.commands.CommandManager.StringConverter;

public class PrCommand implements CommandHandler {

    @Override
    public boolean onCommand(ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        String subCommand = holder.getNextArgument();

        if (subCommand == null) {
            return false;
        } else if (subCommand.equalsIgnoreCase("-reload")) {
            String world = holder.getNextArgument();
            subCommandReload(sender, world);
            return true;
        } else if (subCommand.equalsIgnoreCase("-load")) {
            subCommandLoad(holder, sender, msg);
            return true;
        } else if (subCommand.equalsIgnoreCase("-list")) {
            subCommandList(holder, sender, msg);
            return true;
        }
        // TODO Auto-generated method stub
        return false;
    }

    private static void subCommandReload(CommandSender sender, String arg) {
        if (arg == null || arg.equals("")) {
            if (!CommandManager.has(sender, "permissions.reload.default")) {
                sender.sendMessage(ChatColor.RED + "[Permissions] You lack the necessary permissions to perform this action.");
                return;
            }

            CommandManager.getHandler().reload(CommandManager.getDefaultWorld());
            sender.sendMessage(ChatColor.GRAY + "[Permissions] Default world reloaded.");
            return;
        }

        if (arg.equalsIgnoreCase("all")) {
            if (CommandManager.has(sender, "permissions.reload.all")) {
                sender.sendMessage(ChatColor.RED + "[Permissions] You lack the necessary permissions to perform this action.");
                return;
            }

            CommandManager.getHandler().reload();
            sender.sendMessage(ChatColor.GRAY + "[Permissions] All worlds reloaded.");
            return;
        }

        if (CommandManager.has(sender, "permissions.reload." + arg)) {
            sender.sendMessage(ChatColor.RED + "[Permissions] You lack the necessary permissions to perform this action.");
            return;
        }
        if (CommandManager.getHandler().reload(arg))
            sender.sendMessage(ChatColor.GRAY + "[Permissions] Reload of World " + arg + " completed.");
        else
            sender.sendMessage(ChatColor.GRAY + "[Permissions] World " + arg + " does not exist.");
        return;

    }
    
    private static void subCommandLoad(ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        
    }

    private static void subCommandList(ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        String listingType = holder.getNextArgument();

        if (listingType != null) {
            if (listingType.equalsIgnoreCase("worlds")) {
                if (CommandManager.has(sender, "permissions.list.worlds")) {
                    Set<String> worlds = CommandManager.getHandler().getWorlds();
                    CommandManager.list(msg, worlds, "&a[Permissions] Loaded worlds: &b", "&4[Permissions] No worlds loaded.");
                } else {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                }
                return;
            } else {
                String world = holder.getNextArgument();
                if (world == null)
                    world = CommandManager.getDefaultWorld();
                if (listingType.equalsIgnoreCase("users")) {
                    if (CommandManager.has(sender, "permissions.list.users")) {
                        Collection<User> users = CommandManager.getHandler().getUsers(world);
                        CommandManager.list(msg, users, "&a[Permissions] Users: &b", "&4[Permissions] No users in that world.", EntryNameConverter.instance);
                    } else {
                        msg.send("&4[Permissions] You do not have permissions to use this command.");
                    }
                    return;
                } else if (listingType.equalsIgnoreCase("groups")) {
                    if (CommandManager.has(sender, "permissions.list.groups")) {
                        Collection<Group> groups = CommandManager.getHandler().getGroups(world);
                        CommandManager.list(msg, groups, "&a[Permissions] Groups: &b", "&4[Permissions] No groups in that world.", EntryNameConverter.instance);
                    } else {
                        msg.send("&4[Permissions] You do not have permissions to use this command.");
                    }
                    return;
                } else if (listingType.equalsIgnoreCase("tracks")) {
                    if (CommandManager.has(sender, "permissions.list.tracks")) {
                        Set<String> tracks = CommandManager.getHandler().getTracks(world);
                        CommandManager.list(msg, tracks, "&a[Permissions] Tracks: &b", "&4[Permissions] No tracks in that world.", TrackConverter.instance);
                    } else {
                        msg.send("&4[Permissions] You do not have permissions to use this command.");
                    }
                    return;
                }
            }
        }
        msg.send("&7[Permissions] Syntax: ");
        msg.send("&b/permissions &a-list &eworlds.");
        msg.send("&b/permissions &a-list &e[users|groups|tracks] &d(world).");
        return;
    }

    private static class EntryNameConverter implements StringConverter<Entry> {
        public static final EntryNameConverter instance = new EntryNameConverter();

        @Override
        public String convertToString(Entry e) {
            return e.getName();
        }
    }

    private static class TrackConverter implements StringConverter<String> {
        public static final TrackConverter instance = new TrackConverter();
        
        @Override
        public String convertToString(String o) {
            if (o == null)
                return "<Default Track>";
            else
                return o;
        }
    }
}

package com.nijikokun.bukkit.Permissions.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.MessageHelper;
import com.nijiko.permissions.Entry;
import com.nijiko.permissions.Group;
import com.nijiko.permissions.PermissionCheckResult;
import com.nijiko.permissions.PermissionHandler;
import com.nijiko.permissions.User;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.bukkit.Permissions.commands.CommandManager.ArgumentHolder;
import com.nijikokun.bukkit.Permissions.commands.CommandManager.CommandHandler;
import com.nijikokun.bukkit.Permissions.commands.CommandManager.StringConverter;
import com.nijikokun.bukkit.Permissions.commands.CommandManager.TextFormEntry;

public class PrCommand implements CommandHandler {

    @Override
    public boolean onCommand(String command, ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        String subCommand = holder.getNextArgument();

        if (subCommand == null) {
            return false;
        } else if (subCommand.equalsIgnoreCase("reload") || subCommand.equalsIgnoreCase("rld")) {
            subCommandReload(holder, sender, msg);
            return true;
        } else if (subCommand.equalsIgnoreCase("list") || subCommand.equalsIgnoreCase("ls")) {
            subCommandList(holder, sender, msg);
            return true;
        } else if (subCommand.equalsIgnoreCase("select") || subCommand.equalsIgnoreCase("sel")) {
            String world = holder.getNextArgument();
            CommandManager.setWorldFor(sender, world);
            if (world == null)
                msg.send("&7[Permissions]&b Selected world reset.");
            else
                msg.send("&7[Permissions]&b Selected world set to world " + world + ".");
            return true;
        } else if (subCommand.equalsIgnoreCase("perms") || subCommand.equalsIgnoreCase("pr")) {
            subCommandPerms(holder, sender, msg);
        } else if (subCommand.equalsIgnoreCase("parents") || subCommand.equalsIgnoreCase("grp")) {
            subCommandParents(holder, sender, msg);
        } else if (subCommand.equalsIgnoreCase("info") || subCommand.equalsIgnoreCase("i")) {
            subCommandInfo(holder, sender, msg);
        } else if (subCommand.equalsIgnoreCase("debug") || subCommand.equalsIgnoreCase("dbg")) {
            subCommandDebug(holder, sender, msg);
        }
        msg.send("&7[Permissions] Syntax: ");
        msg.send("&b/pr [reload|list|select|perms|parents|info]");
        msg.send("&b/pr [rld|ls|lsall|sel|pr|grp|i]");
        return true;
    }

    private static void subCommandReload(ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        Set<String> worlds = new HashSet<String>();
        while (holder.hasNext()) {
            String world = holder.getNextArgument();
            if (world.equalsIgnoreCase("all")) {
                if (!CommandManager.has(sender, "permissions.reload.all")) {
                    msg.send("&4[Permissions] You lack the necessary permissions to perform this action.");
                    return;
                }

                CommandManager.getHandler().reload();
                msg.send("&7[Permissions]&b All worlds reloaded.");
                return;
            }
            worlds.add(world);
        }
        if (worlds.isEmpty()) {
            String selectedWorld = CommandManager.getWorldFor(sender);
            if (selectedWorld != null)
                worlds.add(selectedWorld);
        }

        for (String worldName : worlds) {
            if (!CommandManager.has(sender, "permissions.reload." + worldName)) {
                msg.send("&4[Permissions] You lack the necessary permissions to reload world " + worldName + ".");
                return;
            }
            if (CommandManager.getHandler().reload(worldName)) {
                msg.send("&7[Permissions]&b World " + worldName + " reloaded.");
            } else {
                msg.send("&7[Permissions]&b Loading world " + worldName + "...");
                try {
                    CommandManager.getHandler().loadWorld(worldName);
                } catch (Throwable t) {
                    t.printStackTrace();
                    msg.send("&4[Permissions]&b World load failed.");
                    continue;
                }
                msg.send("&7[Permissions]&b World " + worldName + " loaded.");
            }
        }
        msg.send(ChatColor.GRAY + "[Permissions] Reload complete.");
        return;
    }

    static void subCommandList(ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
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
                    world = CommandManager.getWorldFor(sender);
                if (listingType.equalsIgnoreCase("users")) {
                    if (CommandManager.has(sender, "permissions.list.users")) {
                        Collection<User> users = CommandManager.getHandler().getUsers(world);
                        CommandManager.list(msg, users, "&a[Permissions] Users: &b", "&4[Permissions] No users in that world.", CommandManager.EntryNameConverter.instance);
                    } else {
                        msg.send("&4[Permissions] You do not have permissions to use this command.");
                    }
                    return;
                } else if (listingType.equalsIgnoreCase("groups")) {
                    if (CommandManager.has(sender, "permissions.list.groups")) {
                        Collection<Group> groups = CommandManager.getHandler().getGroups(world);
                        CommandManager.list(msg, groups, "&a[Permissions] Groups: &b", "&4[Permissions] No groups in that world.", CommandManager.EntryNameConverter.instance);
                    } else {
                        msg.send("&4[Permissions] You do not have permissions to use this command.");
                    }
                    return;
                } else if (listingType.equalsIgnoreCase("tracks")) {
                    if (CommandManager.has(sender, "permissions.list.tracks")) {
                        Set<String> tracks = CommandManager.getHandler().getTracks(world);
                        CommandManager.list(msg, tracks, "&a[Permissions] Tracks: &b", "&4[Permissions] No tracks in that world.", CommandManager.TrackConverter.instance);
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

    private static void subCommandPerms(ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        String permCommand = holder.getNextArgument();
        if (permCommand != null) {
            TextFormEntry tfe = CommandManager.extractEntry(sender, holder, "@", "w:");
            Entry e = CommandManager.getEntry(sender, tfe, "#", false, msg);
            if (e == null) {
                msg.send("&4[Permissions] Target entry not found.");
                msg.send("&b/pr perms <username> ...");
                msg.send("&b/pr perms @<groupname> ...");
                return;
            }

            if (permCommand.equalsIgnoreCase("has") || permCommand.equalsIgnoreCase("?")) {
                if (!CommandManager.has(sender, "permissions.perms.has")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }
                Set<String> perms = extractNodes(holder);
                if (perms.isEmpty()) {
                    msg.send("&4[Permissions] No permission nodes specified.");
                    msg.send("&7[Permissions] Syntax: ");
                    msg.send("&b/pr perms <username> has <node>");
                    msg.send("&b/pr perms @<groupname> has <node>");
                    msg.send("&b/pr perms <username> has <node1> <node2> <node3> ...");
                    msg.send("&b/pr perms @<groupname> has <node1> <node2> <node3> ...");
                    return;
                }
                for (String node : perms) {
                    PermissionCheckResult pcr = e.has(node);
                    if (pcr.getResult())
                        msg.send("&7[Permissions] Entry has the permission node " + node + ".");
                    else
                        msg.send("&7[Permissions] Entry does not have the permission node " + node + ".");

                    msg.send("&7Result Details: " + pcr.toShortString());
                }
            } else if (permCommand.equalsIgnoreCase("list") || permCommand.equalsIgnoreCase("ls")) {
                if (!CommandManager.has(sender, "permissions.perms.list")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }
                Set<String> perms = e.getPermissions();
                CommandManager.list(msg, perms, "&7[Permissions]&b Permissions: &c", "&4[Permissions] User/Group has no non-inherited permissions.");
                return;
            } else if (permCommand.equalsIgnoreCase("listall") || permCommand.equalsIgnoreCase("lsall")) {
                if (!CommandManager.has(sender, "permissions.perms.listall")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }
                Set<String> perms = e.getAllPermissions();
                CommandManager.list(msg, perms, "&7[Permissions]&b All permissions: &c", "&4[Permissions] User/Group has no permissions.");
                return;
            } else if (permCommand.equalsIgnoreCase("add") || permCommand.equalsIgnoreCase("+")) {
                if (!CommandManager.has(sender, "permissions.perms.add")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }
                Set<String> perms = extractNodes(holder);
                if (perms.isEmpty()) {
                    msg.send("&4[Permissions] No permission nodes specified.");
                    msg.send("&7[Permissions] Syntax: ");
                    msg.send("&b/pr perms <username> add <node>");
                    msg.send("&b/pr perms @<groupname> add <node>");
                    msg.send("&b/pr perms <username> add <node1> <node2> <node3> ...");
                    msg.send("&b/pr perms @<groupname> add <node1> <node2> <node3> ...");
                    return;
                }
                Set<String> userPerms = e.getPermissions();
                for (String node : perms) {
                    if (userPerms.contains(node)) {
                        msg.send("&4[Permissions] User/Group already has permission " + node + ".");
                        continue;
                    }
                    e.addPermission(node);
                    msg.send("&7[Permissions] Node " + node + " added.");
                }
                return;
            } else if (permCommand.equalsIgnoreCase("remove") || permCommand.equalsIgnoreCase("-")) {
                if (!CommandManager.has(sender, "permissions.perms.remove")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }
                Set<String> perms = extractNodes(holder);
                if (perms.isEmpty()) {
                    msg.send("&4[Permissions] No permission nodes specified.");
                    msg.send("&7[Permissions] Syntax: ");
                    msg.send("&b/pr perms <username> remove <node>");
                    msg.send("&b/pr perms @<groupname> remove <node>");
                    msg.send("&b/pr perms <username> remove <node1> <node2> <node3> ...");
                    msg.send("&b/pr perms @<groupname> remove <node1> <node2> <node3> ...");
                    return;
                }
                Set<String> userPerms = e.getPermissions();
                for (String node : perms) {
                    if (!userPerms.contains(node)) {
                        msg.send("&4[Permissions] User/Group does not have permission " + node + ".");
                        continue;
                    }
                    e.addPermission(node);
                    msg.send("&7[Permissions] Node " + node + " removed.");
                }
                return;
            }
        }
        msg.send("&7[Permissions] Syntax: ");
        msg.send("&b/pr perms <username> [list|listall]");
        msg.send("&b/pr perms <username> [has|add|remove] <node>");
        msg.send("&b/pr perms <username> [has|add|remove] <node1> <node2> ...");
        msg.send("&b/pr perms @<groupname> [list|listall]");
        msg.send("&b/pr perms @<groupname> [has|add|remove] <node>");
        msg.send("&b/pr perms @<groupname> [has|add|remove] <node1> <node2> ...");
    }
    
    private static void subCommandParents(ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        
    }
    
    private static void subCommandInfo(ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        
    }
    
    private static void subCommandDebug(ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        
    }
    
    private static Set<String> extractNodes(ArgumentHolder holder) {
        Set<String> perms = new HashSet<String>();
        while (holder.hasNext()) {
            String perm = holder.getNextArgument();
            perms.add(perm);
        }
        return perms;
    }
}

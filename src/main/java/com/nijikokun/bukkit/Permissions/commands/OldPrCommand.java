package com.nijikokun.bukkit.Permissions.commands;

import java.util.LinkedHashSet;
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

public class OldPrCommand implements CommandHandler {

    
    
    @Override
    public boolean onCommand(String command, ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
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
            PrCommand.subCommandList(holder, sender, msg);
            return true;
        } else {
            String name = subCommand;

            boolean isGroup = name.startsWith("g:");
            if (isGroup)
                name = name.substring(2);

            String next = holder.getNextArgument();
            String world = null;
            String entryCommand = null;

            if (next != null) {
                if (next.startsWith("w:"))
                    world = next.substring(2);
                else
                    entryCommand = next;

                if (world == null) {
                    msg.send("&4[Permissions] No world specified. Defaulting to default world.");
                    world = CommandManager.getWorldFor(sender);
                }

                if (entryCommand == null) {
                    entryCommand = holder.getNextArgument();
                }

                if (entryCommand != null) {
                    Entry e = CommandManager.getEntry(world, name, isGroup, false);
                    if (entryCommand.equalsIgnoreCase("create")) {
                        if (e != null) {
                            msg.send("&4[Permissions] User/Group already exists.");
                            return true;
                        }
                        if (!CommandManager.has(sender, "permissions.create")) {
                            msg.send("&4[Permissions] You do not have permissions to use this command.");
                            return true;
                        }
                        entryCommandCreate(world, name, isGroup, msg);
                        return true;
                    } else if (e == null) {
                        msg.send("&4[Permissions] User/Group does not exist.");
                        e = CommandManager.tryAutoComplete(world, name, isGroup, msg);
                        if (e == null) {
                            msg.send("&4[Permissions] Autocomplete failed to find/create a matching entry.");
                            return true;
                        }
                    }

                    if (entryCommand.equalsIgnoreCase("delete")) {
                        entryCommandDelete(e, holder, sender, msg);
                        return true;
                    } else if (entryCommand.equalsIgnoreCase("has")) {
                        entryCommandHas(e, holder, sender, msg);
                        return true;
                    } else if (entryCommand.equalsIgnoreCase("perms")) {
                        entryCommandPerms(e, holder, sender, msg);
                        return true;
                    } else if (entryCommand.equalsIgnoreCase("parents")) {
                        entryCommandParents(e, holder, sender, msg);
                        return true;
                    } else if (entryCommand.equalsIgnoreCase("info")) {
                        entryCommandInfo(e, holder, sender, msg);
                        return true;
                    } else if (entryCommand.equalsIgnoreCase("dumpcache")) {
                        entryCommandDumpCache(e, holder, sender, msg);
                        return true;
                    } else if (e instanceof User) {
                        if (entryCommand.equalsIgnoreCase("promote")) {
                            userCommandPromote((User) e, holder, sender, msg);
                            return true;
                        } else if (entryCommand.equalsIgnoreCase("demote")) {
                            userCommandDemote((User) e, holder, sender, msg);
                            return true;
                        }
                    }
                }
                msg.send("&7[Permissions] Syntax: /pr (g:)<name> (w:[optionalworld]) [create|delete|has|perms|parents|info] ...");
            }

        }
        return false;
    }

    private static void subCommandReload(CommandSender sender, String arg) {
        if (arg == null || arg.equals("")) {
            if (!CommandManager.has(sender, "permissions.reload.default")) {
                sender.sendMessage(ChatColor.RED + "[Permissions] You lack the necessary permissions to perform this action.");
                return;
            }

            CommandManager.getHandler().reload(CommandManager.getWorldFor(sender));
            sender.sendMessage(ChatColor.GRAY + "[Permissions] Default world reloaded.");
            return;
        }

        if (arg.equalsIgnoreCase("all")) {
            if (!CommandManager.has(sender, "permissions.reload.all")) {
                sender.sendMessage(ChatColor.RED + "[Permissions] You lack the necessary permissions to perform this action.");
                return;
            }

            CommandManager.getHandler().reload();
            sender.sendMessage(ChatColor.GRAY + "[Permissions] All worlds reloaded.");
            return;
        }

        if (!CommandManager.has(sender, "permissions.reload." + arg)) {
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
        String world = holder.getNextArgument();
        if (world == null)
            world = CommandManager.getWorldFor(sender);

        if (!CommandManager.has(sender, "permissions.load." + world)) {
            sender.sendMessage(ChatColor.RED + "[Permissions] You lack the necessary permissions to perform this action.");
            return;
        }

        try {
            CommandManager.getHandler().forceLoadWorld(world);
        } catch (Exception e) {
            msg.send("&4[Permissions] Error occured while loading world.");
            e.printStackTrace();
            return;
        }
        msg.send("&7[Permissions] World loaded.");
        return;
    }

    

    private static void entryCommandCreate(String world, String name, boolean isGroup, MessageHelper msg) {
        if(CommandManager.getEntry(world, name, isGroup, true) != null) {
            msg.send("&7[Permissions] User/group created.");
            return;            
        } else {
            msg.send("&4[Permissions] Unable to create user/group!");
            return;            
        }
    }


    

    private static void entryCommandDelete(Entry e, ArgumentHolder unused, CommandSender sender, MessageHelper msg) {
        if (!CommandManager.has(sender, "permissions.delete")) {
            msg.send("&4[Permissions] You do not have permissions to use this command.");
            return;
        }

        String text = e.delete() ? "&7[Permissions] User/Group deleted." : "&4[Permissions] Deletion failed.";
        msg.send(text);
        return;
    }

    private static void entryCommandHas(Entry e, ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        if (!CommandManager.has(sender, "permissions.has")) {
            msg.send("&4[Permissions] You do not have permissions to use this command.");
            return;
        }

        String node = holder.getNextArgument();
        if (node == null) {
            msg.send("&4[Permissions] No node specified.");
            msg.send("&7[Permissions] Syntax: /pr (g:)<target> (w:<world>) has <permission>");
            return;
        }
        boolean has = e.hasPermission(node);
        msg.send("&7[Permissions]&b User/Group " + (has ? "has" : "does not have") + " that permission.");
        return;
    }

    private static void entryCommandPerms(Entry e, ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        String permsCommand = holder.getNextArgument();
        if (permsCommand != null) {
            if (permsCommand.equalsIgnoreCase("list")) {
                if (!CommandManager.has(sender, "permissions.perms.list")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }
                Set<String> perms = e.getPermissions();
                CommandManager.list(msg, perms, "&7[Permissions]&b Permissions: &c", "&4[Permissions] User/Group has no non-inherited permissions.");
                return;
            } else if (permsCommand.equalsIgnoreCase("listall")) {
                if (!CommandManager.has(sender, "permissions.perms.listall")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }
                Set<String> perms = e.getAllPermissions();
                CommandManager.list(msg, perms, "&7[Permissions]&b All permissions: &c", "&4[Permissions] User/Group has no permissions.");
                return;
            } else if (permsCommand.equalsIgnoreCase("add")) {
                if (!CommandManager.has(sender, "permissions.perms.add")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }
                Set<String> perms = e.getPermissions();
                String node = holder.getNextArgument();

                if (node == null) {
                    msg.send("&4[Permissions] No node specified.");
                    msg.send("&7[Permissions] Syntax: /pr (g:)<target> (w:<world>) perms add <permission>");
                    return;
                }

                if (perms.contains(node)) {
                    msg.send("&4[Permissions] User/Group already has that permission.");
                    return;
                }

                e.addPermission(node);
                msg.send("&7[Permissions] Node added.");
                return;
            } else if (permsCommand.equalsIgnoreCase("remove")) {
                if (!CommandManager.has(sender, "permissions.perms.remove")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }
                Set<String> perms = e.getPermissions();
                String node = holder.getNextArgument();

                if (node == null) {
                    msg.send("&4[Permissions] No node specified.");
                    msg.send("&7[Permissions] Syntax: /pr (g:)<target> (w:<world>) perms add <permission>");
                    return;
                }

                if (!perms.contains(node)) {
                    msg.send("&4[Permissions] User/Group does not have permission.");
                    return;
                }

                e.removePermission(node);
                msg.send("&7[Permissions] Node removed.");
                return;
            }
        }
        msg.send("&7[Permissions] Syntax: /pr (g:)<target> (w:<world>) perms [list|listall]");
        msg.send("&7[Permissions] Syntax: /pr (g:)<target> (w:<world>) perms [add|remove] <node>");
        return;
    }

    private static class WorldEntryConverter implements StringConverter<Entry> {
        private final String world;

        public WorldEntryConverter(String world) {
            this.world = world;
        }

        @Override
        public String convertToString(Entry o) {
            if (world.equals(o.getWorld())) {
                return o.toStringNameOnly();
            } else
                return o.toString();
        }
    }

    private static void entryCommandParents(Entry e, ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        String parentsCommand = holder.getNextArgument();
        if (parentsCommand != null) {
            if (parentsCommand.equalsIgnoreCase("list")) {
                if (!CommandManager.has(sender, "permissions.parents.list")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }
                LinkedHashSet<Entry> perms = e.getParents();
                CommandManager.list(msg, perms, "&7[Permissions]&b Parents: &c", "&4[Permissions] User/Group has no directly inherited parents.", new WorldEntryConverter(e.getWorld()));
                return;
            } else if (parentsCommand.equalsIgnoreCase("listall")) {
                if (!CommandManager.has(sender, "permissions.parents.listall")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }
                LinkedHashSet<Entry> parents = e.getAncestors();
                CommandManager.list(msg, parents, "&7[Permissions]&b Ancestors: &c", "&4[Permissions] User/Group has no ancestors.", new WorldEntryConverter(e.getWorld()));
                return;
            } else if (parentsCommand.equalsIgnoreCase("add")) {
                if (!CommandManager.has(sender, "permissions.parents.add")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }
                LinkedHashSet<Entry> parents = e.getParents(null);

                String parentName = holder.getNextArgument();
                String parentWorld = holder.getNextArgument();

                if (parentName == null) {
                    msg.send("&4[Permissions] No parent specified.");
                    msg.send("&7[Permissions] Syntax: /pr (g:)<target> (w:<world>) parents add <parentname> (optionalparentworld)");
                    return;
                }

                if (parentWorld == null) {
                    parentWorld = e.getWorld();
                }

                Group g = CommandManager.getGroup(parentWorld, parentName, false);

                if (parents.contains(g)) {
                    msg.send("&4[Permissions] User/Group already has that parent.");
                    return;
                }

                e.addParent(g);
                msg.send("&7[Permissions] Parent added.");
                return;
            } else if (parentsCommand.equalsIgnoreCase("remove")) {
                if (!CommandManager.has(sender, "permissions.parents.remove")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }
                LinkedHashSet<Entry> parents = e.getParents(null);

                String parentName = holder.getNextArgument();
                String parentWorld = holder.getNextArgument();

                Group g = null;

                if (parentName == null) {
                    msg.send("&4[Permissions] No parent specified.");
                    if (e instanceof User) {
                        g = ((User) e).getPrimaryGroup();
                        if (g != null) {
                            msg.send("&7[Permissions] Defaulting to primary group.");
                        } else {
                            msg.send("&4[Permissions] User has no primary groups to default to.");
                            return;
                        }
                    } else
                        return;
                }

                if (parentWorld == null) {
                    parentWorld = e.getWorld();
                }
                if (g == null)
                    g = CommandManager.getGroup(parentWorld, parentName, false);

                if (!parents.contains(g)) {
                    msg.send("&4[Permissions] User/Group does not have that parent!.");
                    return;
                }

                e.removeParent(g);
                msg.send("&7[Permissions] Parent removed.");
                return;
            }
        }
        msg.send("&7[Permissions] Syntax: /pr (g:)<target> (w:<world>) parents [list|listall]");
        msg.send("&7[Permissions] Syntax: /pr (g:)<target> (w:<world>) parents [add|remove] <groupname> (optionalgroupworld)");
        return;
    }

    private static void entryCommandInfo(Entry e, ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        String infoCommand = holder.getNextArgument();
        if (infoCommand != null) {
            if (infoCommand.equalsIgnoreCase("get")) {
                if (!CommandManager.has(sender, "permissions.info.get")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }

                String path = holder.getNextArgument();

                if (path != null) {
                    String data = e.recursiveCheck(new Entry.StringInfoVisitor(path));
                    msg.send("&7[Permissions] Data value for path " + path);
                    msg.send(data);
                    return;
                }
                msg.send("&7[Permissions] Syntax: /pr (g:)<target> (w:<world>) info get <path>");
                return;
            } else if (infoCommand.equalsIgnoreCase("set")) {
                if (!CommandManager.has(sender, "permissions.info.set")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }

                String path = holder.getNextArgument();
                String data = holder.getNextArgument();

                if (path != null) {
                    Object newValue;
                    String type = "";
                    try {
                        if (data == null) {
                            msg.send("&4[Permissions] No data provided. If you want to remove data, use \"/pr <name> info remove <path>\" instead.");
                            return;
                        } else if (data.startsWith("b:")) {
                            newValue = Boolean.valueOf(data.substring(2));
                            type = "Boolean";
                        } else if (data.startsWith("d:")) {
                            newValue = Double.valueOf(data.substring(2));
                            type = "Double";
                        } else if (data.startsWith("i:")) {
                            newValue = Integer.valueOf(data.substring(2));
                            type = "Integer";
                        } else {
                            newValue = data;
                            type = "String";
                        }
                    } catch (NumberFormatException e1) {
                        msg.send("&4[Permissions] Error encountered when parsing value.");
                        return;
                    }
                    e.setData(path, newValue);
                    msg.send("&7[Permissions] &a" + path + "&b set to &a" + type + " &c" + newValue.toString());
                    return;
                }

                msg.send("&7[Permissions] Syntax: /pr (g:)<target> (w:<world>) info set <path> (b:|d:|i:)<data>");
                return;
            } else if (infoCommand.equalsIgnoreCase("remove")) {
                if (!CommandManager.has(sender, "permissions.info.remove")) {
                    msg.send("&4[Permissions] You do not have permissions to use this command.");
                    return;
                }

                String path = holder.getNextArgument();

                if (path != null) {
                    e.removeData(path);
                    msg.send("&7[Permissions]&b &a" + path + "&b cleared.");
                    return;
                }
                msg.send("&7[Permissions] Syntax: /pr (g:)<target> (w:<world>) info remove <path>");
                return;
            }
        }
    }

    private static void entryCommandDumpCache(Entry e, ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        if (!CommandManager.has(sender, "permissions.debug.dumpcache")) {
            msg.send("&4[Permissions] You do not have permissions to use this command.");
            return;
        }
        msg.send("&7[Permissions]&b Cache:");
        msg.send(e.getCache().toString());
        return;
    }

    //TODO: Duplicated code
    private static void userCommandPromote(User u, ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        GroupTrackPair gPair = extractTrackAndGroup(u, holder);
        Group parent = gPair.getGroup();

        if (!CommandManager.has(sender, "permissions.promote."+gPair.getTrack())) {
        	return;
        }
        
        if (parent == null) {
            msg.send("&4[Permissions] Specified parent group does not exist.");
            return;
        }

        if (!u.getParents(null).contains(parent)) {
            msg.send("&4[Permissions] User is not a child of the specified parent group.");
            return;
        }

        if (u.promote(parent, gPair.getTrack())) {
            msg.send("&7[Permissions] Promotion successful.");
        } else {
            msg.send("&4[Permissions] Promotion failed.");
        }
        return;
    }

    private static void userCommandDemote(User u, ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        GroupTrackPair gPair = extractTrackAndGroup(u, holder);
        Group parent = gPair.getGroup();

        if (!CommandManager.has(sender, "permissions.demote."+gPair.getTrack())) {
        	return;
        }
        
        if (parent == null) {
            msg.send("&4[Permissions] Specified parent group does not exist.");
            return;
        }

        if (!u.getParents(null).contains(parent)) {
            msg.send("&4[Permissions] User is not a child of the specified parent group.");
            return;
        }

        if (u.demote(parent, gPair.getTrack())) {
            msg.send("&7[Permissions] Demotion successful.");
        } else {
            msg.send("&4[Permissions] Demotion failed.");
        }
        return;
    }

    private static GroupTrackPair extractTrackAndGroup(User u, ArgumentHolder holder) {
        String trackName = null;
        String groupName = null;
        String groupWorld = u.getWorld();
        Group g = null;

        String next = holder.getNextArgument();

        if (next != null) {
            if (next.startsWith("t:"))
                trackName = next.substring(2);
            else
                groupName = next;

            next = holder.getNextArgument();
            if (next != null) {
                if (groupName == null) {
                    groupName = next;
                    next = holder.getNextArgument();
                    if (next != null) {
                        groupWorld = next;
                    }
                } else
                    groupWorld = next;

                g = CommandManager.getHandler().getGroupObject(groupWorld, groupName);
            }
        }

        if (g == null)
            g = u.getPrimaryGroup(null);

        return new GroupTrackPair(g, trackName);
    }

    private static class GroupTrackPair {
        private final Group group;
        private final String track;

        public GroupTrackPair(Group g, String t) {
            this.group = g;
            this.track = t;
        }

        public Group getGroup() {
            return group;
        }

        public String getTrack() {
            return track;
        }
    }
}

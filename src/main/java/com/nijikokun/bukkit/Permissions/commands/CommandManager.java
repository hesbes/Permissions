package com.nijikokun.bukkit.Permissions.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.MessageHelper;
import com.nijiko.permissions.Entry;
import com.nijiko.permissions.Group;
import com.nijiko.permissions.PermissionHandler;
import com.nijiko.permissions.User;
import com.nijikokun.bukkit.Permissions.Permissions;

public class CommandManager implements CommandExecutor {

    private Map<String, CommandHandler> dispatchMap = new HashMap<String, CommandHandler>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName();

        CommandHandler handler = dispatchMap.get(name);
        if (handler == null)
            return false;

        ArgumentHolder holder = new ArgumentHolder(args);
        MessageHelper msg = new MessageHelper(sender);

        return handler.onCommand(holder, sender, msg);
    }

    public void registerCommand(String commandName, CommandHandler handler) {
        // XXX: putIfAbsent?
        dispatchMap.put(commandName, handler);
    }

    static Entry getEntry(String world, String name, boolean isGroup, boolean create) {
        return isGroup ? getGroup(world, name, create) : getUser(world, name, create);
    }

    static Group getGroup(String world, String name, boolean create) {
        PermissionHandler handler = getHandler();
        Group g;
        if (!create) {
            g = handler.getGroupObject(world, name);
        } else {
            try {
                g = handler.safeGetGroup(world, name);
            } catch (Exception e1) {
                e1.printStackTrace();
                g = null;
            }
        }
        return g;
    }

    static User getUser(String world, String name, boolean create) {
        PermissionHandler handler = getHandler();
        User u;
        if (!create) {
            u = handler.getUserObject(world, name);
        } else {
            try {
                u = handler.safeGetUser(world, name);
            } catch (Exception e1) {
                e1.printStackTrace();
                u = null;
            }
        }
        return u;
    }

    static TextFormEntry extractEntry(ArgumentHolder holder) {
        String name = holder.getNextArgument();
        boolean isGroup = name.startsWith("g:");
        if (isGroup)
            name = name.substring(2);

        // String world = extractQuoted(holder, "w:");
        String world = holder.getNextArgument();
        if (world == null)
            world = getDefaultWorld();

        return new TextFormEntry(world, name, isGroup);
    }

    public static String extractQuoted(ArgumentHolder holder, String prefix) {
        StringBuilder sb = new StringBuilder();
        boolean inQuoted = false;
        for (String arg : holder) {
            if (arg.length() > 0) {
                sb.append(arg);

                if (sb.indexOf(prefix + '"') == 0) {
                    sb.delete(0, prefix.length() + 1);
                    inQuoted = true;
                } else if (!inQuoted) {
                    break;
                }

                if (sb.charAt(sb.length() - 1) == '"') {
                    sb.deleteCharAt(sb.length() - 1);
                    break;
                }
            }

            sb.append(" ");
        }
        return sb.toString();
    }

    static boolean has(CommandSender sender, String node) {
        if (node == null)
            return true;
        if (sender instanceof Player) {
            Player p = (Player) sender;
            return getHandler().has(p, node);
        } else {
            return true;
        }
    }

    static PermissionHandler getHandler() {
        return ((Permissions) Permissions.instance).getHandler();
    }

    static String getDefaultWorld() {
        return ((Permissions) Permissions.instance).getDefaultWorld();
    }

    public interface CommandHandler {
        public boolean onCommand(ArgumentHolder holder, CommandSender sender, MessageHelper msg);
    }

    public static class ArgumentHolder implements Iterable<String> {
        private final String[] args;
        private int index = -1;

        public ArgumentHolder(String[] args) {
            this.args = args.clone();
        }

        public String getNextArgument() {
            if (index < -1 || index >= args.length - 1)
                return null;
            return args[++index];
        }

        public String getCurrentArgument() {
            if (index < 0 || index >= args.length)
                return null;
            return args[index];
        }

        public int getIndex() {
            return index;
        }

        public String[] getArgs() {
            return args.clone();
        }

        @Override
        public Iterator<String> iterator() {
            return new ArrayIterator(args.clone(), index);
        }

        private static class ArrayIterator implements Iterator<String> {
            private final String[] args;
            private int index = -1;

            // public ArrayIterator(String[] args) {
            // this.args = args;
            // }

            public ArrayIterator(String[] args, int index) {
                this.args = args;
                this.index = index;
            }

            @Override
            public boolean hasNext() {
                return index < args.length;
            }

            @Override
            public String next() {
                return args[++index];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    public static class TextFormEntry {
        private final String world;
        private final String name;
        private final boolean isGroup;

        public TextFormEntry(String world, String name, boolean isGroup) {
            this.world = world;
            this.name = name;
            this.isGroup = isGroup;
        }

        public String getWorld() {
            return world;
        }

        public String getName() {
            return name;
        }

        public boolean isGroup() {
            return isGroup;
        }
    }

    static void list(MessageHelper msg, Collection<?> coll, String prefix, String emptyMessage) {
        list(msg, coll, prefix, emptyMessage, null);
    }

    static <T> void list(MessageHelper msg, Collection<T> coll, String prefix, String emptyMessage, StringConverter<? super T> converter) {
        list(msg, coll, prefix, emptyMessage, converter, "&b,&c ");
    }

    static <T> void list(MessageHelper msg, Collection<T> coll, String prefix, String emptyMessage, StringConverter<? super T> converter, String separator) {
        if (msg == null)
            return;
        if (coll == null || coll.isEmpty()) {
            msg.send(emptyMessage);
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (prefix != null)
            sb.append(prefix);

        if (converter == null) {
            for (T o : coll) {
                sb.append(o).append(separator);
            }
        } else {
            for (T o : coll) {
                sb.append(converter.convertToString(o)).append(separator);
            }
        }
        if (sb.length() >= 2)
            sb.delete(sb.length() - separator.length(), sb.length());

        msg.send(sb.toString());
    }

    static <T> Set<String> asStringSet(Collection<T> coll, StringConverter<? super T> converter) {
        Set<String> stringSet = new LinkedHashSet<String>();
        for (T obj : coll) {
            stringSet.add(converter.convertToString(obj));
        }
        return stringSet;
    }

    static String getClosest(String word, Set<String> dict, final int threshold) {
        if (word == null || word.isEmpty() || dict == null || dict.isEmpty()) {
            return null;
        }
        if (dict.contains(word)) {
            return word;
        }
        String result = null;
        int currentDist = threshold;
        String lw = word.toLowerCase();
        for (String s : dict) {
            if (s == null)
                continue;
            String ls = s.toLowerCase();
            int dist;
            if (ls.startsWith(lw)) {
                dist = s.length() - word.length();
                if (currentDist > dist) {
                    result = s;
                    currentDist = dist;
                }
            }
        }
        return result;
    }

    public static interface StringConverter<T> {
        public String convertToString(T o);
    }
}

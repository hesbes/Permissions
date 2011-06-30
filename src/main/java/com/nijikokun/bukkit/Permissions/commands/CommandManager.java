package com.nijikokun.bukkit.Permissions.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.MessageHelper;
import com.nijiko.permissions.Entry;
import com.nijiko.permissions.PermissionHandler;
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

    public static Entry extractEntryObject(ArgumentHolder holder, boolean create) {
        TextFormEntry strE = extractEntry(holder);

        PermissionHandler handler = getHandler();

        Entry e;
        if (strE.isGroup()) {
            try {
                e = create ? handler.safeGetGroup(strE.getWorld(), strE.getName()) : handler.getGroupObject(strE.getWorld(), strE.getName());
            } catch (Exception e1) {
                e1.printStackTrace();
                e = null;
            }
        } else {
            try {
                e = create ? handler.safeGetUser(strE.getWorld(), strE.getName()) : handler.getUserObject(strE.getWorld(), strE.getName());
            } catch (Exception e1) {
                e1.printStackTrace();
                e = null;
            }
        }

        return e;
    }

    public static TextFormEntry extractEntry(ArgumentHolder holder) {
        String name = holder.getNextArgument();
        boolean isGroup = name.startsWith("g:");
        if (isGroup)
            name = name.substring(2);

        String world = extractQuoted(holder, "w:");

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
            this.args = args;
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

            public ArrayIterator(String[] args) {
                this.args = args;
            }

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
                sb.append(o).append(" ,");
            }
        } else {
            for (T o : coll) {
                sb.append(converter.convertToString(o)).append(" ,");
            }
        }
        if (sb.length() >= 2)
            sb.delete(sb.length() - 2, sb.length());
        
        msg.send(sb.toString());
    }

    public static interface StringConverter<T> {
        public String convertToString(T o);
    }
}

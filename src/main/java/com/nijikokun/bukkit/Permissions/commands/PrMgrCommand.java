package com.nijikokun.bukkit.Permissions.commands;

import org.bukkit.command.CommandSender;

import com.nijiko.MessageHelper;
import com.nijikokun.bukkit.Permissions.commands.CommandManager.ArgumentHolder;
import com.nijikokun.bukkit.Permissions.commands.CommandManager.CommandHandler;

public class PrMgrCommand implements CommandHandler {

    @Override
    public boolean onCommand(ArgumentHolder holder, CommandSender sender, MessageHelper msg) {
        String subCommand = holder.getNextArgument();

        if (subCommand == null) {
            return false;
        } else if (subCommand.equalsIgnoreCase("reload")) {
            String world = holder.getNextArgument();
            subCommandReload(sender, world);
            return true;
        } else if (subCommand.equalsIgnoreCase("add")) {
            subCommandAdd(holder, sender, msg);
            return true;
        } else if (subCommand.equalsIgnoreCase("remove")) {
            subCommandRemove(holder, sender, msg);
            return true;
        }
        // TODO Auto-generated method stub
        return false;
    }
    
}
package com.github.wulf.sdmc.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.github.wulf.sdmc.SDMC;

public class CommandMain implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] array) {
		 if (array.length == 0) {
			 return false;
		 }else
		 {
			   if (array[0].equalsIgnoreCase("reload")) {
		            SDMC.inst().getConfigManager().load();
		            sender.sendMessage("插件重载成功");
		            return true;
		        }else
		        {
		        	return false;
		        }
		 }
	}

}

package com.github.wulf.sdmc;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.wulf.sdmc.cmd.CommandMain;
import com.github.wulf.sdmc.config.ConfigManager;
import com.github.wulf.sdmc.event.DIDamageListener;
import com.github.wulf.sdmc.event.SDamageEvents;

import ru.nightexpress.divineitems.DivineItems;

public class SDMC extends JavaPlugin{
	
	private static SDMC plugin;
	private PluginManager pluginManage;
	public static DivineItems divineItems;
	private ConfigManager configManager;
	private static int DiVer;
	private static String strMMVer;
	private int minecraftVersion;
	private String bukkitVersion;
	
    // Fired when plugin is first enabled
    @Override
    public void onEnable() {
    	setPlugin(this);
    	this.pluginManage = this.getServer().getPluginManager();
	    this.bukkitVersion = Bukkit.getServer().getClass().getPackage().getName().substring(23);
	    try
	    {
	      String[] split = bukkitVersion.split("_");
	      this.minecraftVersion = Integer.parseInt(split[1]);
	    } catch (Exception ex) {
	      this.minecraftVersion = 11;
	      ex.printStackTrace();
	    }
		
	    if (this.minecraftVersion<13) {
	    	getLogger().warning("Bukkit 1.13 or higher is required!");
	    	getPluginLoader().disablePlugin(SDMC.plugin);
	    	return;
	    }
	    getLogger().info("载入DivineItems插件");    
	    if(getServer().getPluginManager().isPluginEnabled("DivineItemsRPG")) 
	    {
	    	strMMVer = Bukkit.getServer().getPluginManager().getPlugin("DivineItemsRPG").getDescription().getVersion().replaceAll("[\\D]", "");
	    	DiVer = Integer.valueOf(strMMVer);
			if (DiVer < 391) {
				getLogger().warning("DivineItemsRPG Version not supported.");
				getPluginLoader().disablePlugin(SDMC.plugin);
				return;
			}
	    	
	    	divineItems = DivineItems.getInstance();
	    	if(divineItems != null)
	    	{
	    		boolean res = ClearDiDamageMonitor();
	    		getLogger().info("DivineItems伤害监测清除"+ (res?"成功":"失败"));
	    	}
	    	
	    	getLogger().info("DivineItems插件载入成功");
	    }
	    else{
	    	getLogger().warning("DivineItems载入失败,插件关闭");
	    	getPluginLoader().disablePlugin(SDMC.plugin);
	    	return;
	    }
	    /*
		if (getServer().getPluginManager().isPluginEnabled("MythicMobs")) {
	    	strMMVer = Bukkit.getServer().getPluginManager().getPlugin("MythicMobs").getDescription().getVersion().replaceAll("[\\D]", "");
			mmVer = Integer.valueOf(strMMVer);
			if (mmVer < 400) {
				getLogger().warning("MythicMobs Version not supported.");
				getPluginLoader().disablePlugin(SDMC.plugin);
				return;
			}
		} else {
			getLogger().warning("MythicMobs is not avaible.");
			getPluginLoader().disablePlugin(SDMC.plugin);
			return;
		}*/
	    getLogger().info("初始化事件SDMC检测器");
	    divineItems.getPluginManager().registerEvents(new DIDamageListener(divineItems),this);
	    pluginManage.registerEvents(new SDamageEvents(divineItems),this);
		getLogger().info("插件初始化完毕");
		(this.configManager = new ConfigManager(this)).setup();
		this.getCommand("sm").setExecutor(new CommandMain());
    }
    // Fired when plugin is disabled
    @Override
    public void onDisable() {

    }


	public static SDMC inst() {
		return plugin;
	}
	
	public static void setPlugin(SDMC plugin) {
		SDMC.plugin = plugin;
	}
	public boolean ClearDiDamageMonitor() {
    	List<RegisteredListener> registeredListeners = HandlerList.getRegisteredListeners(divineItems);
    	for(RegisteredListener registeredListener : registeredListeners)
    	{
    		if(registeredListener.getListener().toString().contains("DamageListener"))
    		{
    			HandlerList.unregisterAll(registeredListener.getListener());
    			return true;
    		}
    	}
		return false;
	}
	
	public ConfigManager getConfigManager() {
		return configManager;
	}
}

package com.github.wulf.sdmc.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.github.wulf.sdmc.SDMC;
import com.github.wulf.sdmc.types.Sskill;

import ru.nightexpress.divineitems.types.DamageType;


public class ConfigManager {

	 private SDMC plugin;
	 private FileConfiguration config;
	 
	 public static HashMap<String, Sskill> skill_types;
	 public static double AOE_DAMAGE_RANG;
	 public static double AOE_DAMAGE_CAPABILITY;
	 public static double VAMPIRISM_CAPABILITY;
	 public static Map<DamageType, Double> DefaultSkill;
	 public ConfigManager(SDMC plugin) {
	        this.plugin = plugin;
	        this.config = plugin.getConfig();
	}
	 
	 public void setup() {
	     this.config.set("AOE_DAMAGE.Rang", 3.0);
	     this.config.set("AOE_DAMAGE.Capability", 30.0);   
	     this.config.set("VAMPIRISM.Capability", 10.0);    
	     
	     //Map<String,Sskill> default_type = new HashMap<String,Sskill>();
	     //default_type.put("DEFAULT", new Sskill("DEFAULT", 1.0, SDMC.divineItems.getCFG().getDamageTypes().get("Physical")));
	     this.config.set("Skill.Default_Percent", 100.0);   
	     
	     Map<String,List<String>> default_type = new HashMap<String,List<String>>();
	     //default_type.put("Name", "DEFAULT");
	     default_type.put("DamageTypes", Arrays.asList("Physical#100.0"));
	     this.config.createSection("Skill.SKILL_TYPE.DEFAULT", default_type);
	     
	     this.config.options().copyDefaults(true);
	     this.plugin.saveConfig();
	     this.load();
	 }
	 
	 public void load() {
		 this.plugin.reloadConfig();
		 this.config = plugin.getConfig();
		 
		 AOE_DAMAGE_RANG = this.config.getDouble("AOE_DAMAGE.Rang");
	     AOE_DAMAGE_CAPABILITY = this.config.getDouble("AOE_DAMAGE.Capability");
	     VAMPIRISM_CAPABILITY = this.config.getDouble("VAMPIRISM.Capability");
	     
	     Double defalut_Percent = this.config.getDouble("Skill.Default_Percent");      
	     skill_types = new HashMap<String, Sskill>();
	     for (String skill_type : config.getConfigurationSection("Skill.SKILL_TYPE").getKeys(false)) {
            String Skill = "Skill.SKILL_TYPE." + skill_type + ".";
            //String skillName = config.getString(Skill + "Name");
           // Double skillPercent = config.getDouble(Skill + "Percent");
            List<String> damageTypeList = config.getStringList(Skill + "DamageTypes");
            Map<DamageType,Double> damageType = new HashMap<DamageType,Double>();
            for(String damage : damageTypeList)
            {
            	Double percent = defalut_Percent / 100;
            	if(damage.contains("#"));
            	{
            		percent = Double.valueOf(damage.split("#")[1]) / 100;
            		damage = damage.split("#")[0];	
            	}
            	if(SDMC.divineItems.getCFG().getDamageTypes().get(damage.toLowerCase()) == null)
            	{
            		this.plugin.getLogger().info("技能[ "+ skill_type +" ]攻击类型[ " + damage +" ]获取失败");
            	}else {
            		damageType.put(SDMC.divineItems.getCFG().getDamageTypes().get(damage.toLowerCase() ),percent);
            		this.plugin.getLogger().info("技能[ "+ skill_type +" ]攻击类型[ " + damage +" ]加载成功");
            	}
            }
            
            //DamageType damageType = SDMC.divineItems.getCFG().getDamageTypes().get(config.getString(Skill + "DamageTypes"));
            //String translateAlternateColorCodes2 = ChatColor.translateAlternateColorCodes('&', config.getString(String.valueOf(Skill) + "Name"));
            Sskill sskill = new Sskill(skill_type, damageType);
            skill_types.put(sskill.getName(), sskill);
	     }  
	     DefaultSkill = skill_types.get("DEFAULT").getDamageType();
	 }
}

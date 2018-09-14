package com.github.wulf.sdmc.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.wulf.sdmc.config.ConfigManager;
import com.github.wulf.sdmc.types.Sskill;
import com.sucy.skill.api.skills.Skill;

import ru.nightexpress.divineitems.DivineItems;
import ru.nightexpress.divineitems.api.DivineItemsAPI;
import ru.nightexpress.divineitems.api.EntityAPI;
import ru.nightexpress.divineitems.api.ItemAPI;
import ru.nightexpress.divineitems.attributes.AttributeT;
import ru.nightexpress.divineitems.types.ArmorType;
import ru.nightexpress.divineitems.types.DamageType;
import ru.nightexpress.divineitems.utils.Utils;

public class SItemUtils {
	
    private static DivineItems plugin;
    private static Random r;
    private static ScriptEngineManager mgr;
    private static ScriptEngine engine;
    
    static {
        SItemUtils.plugin = DivineItems.instance;
        SItemUtils.r = new Random();
        SItemUtils.mgr = new ScriptEngineManager();
        SItemUtils.engine = SItemUtils.mgr.getEngineByName("JavaScript");
    }
    
    public static double dmgReducer(double n, double n2,double n3) {
        if (n < n3) {
            double n4 = 1.0 - SItemUtils.plugin.getCM().getCFG().getDamageCDReduce();
            n2 *= 1.0 - (1.0 - n / n3);
            n2 *= n4;
        }
        else {
            n2 += n - n3;
        }
        return n2;
    }
    
    public static double calcSkillDamageByFormula(LivingEntity critMeta, LivingEntity livingDamager, double damage, ItemStack itemStack, Skill skill) {
        if (!SItemUtils.plugin.getCM().getCFG().allowAttributesToMobs() && !(livingDamager instanceof Player)) {
            return damage;
        }
        /*HashMap<AttributeT, Double> hashMap = new HashMap<AttributeT, Double>();
        if (critMeta.hasMetadata("DIVINE_ARROW_ID")) {
            for (ArrowManager.ArrowAttribute arrowAttribute : SItemUtils.plugin.getMM().getArrowManager().getArrowById(critMeta.getMetadata("DIVINE_ARROW_ID").get(0).asString()).getAttributes().values()) {
                double value = arrowAttribute.getValue();
                if (arrowAttribute.getAction() == ArrowManager.AttributeAction.MINUS) {
                    value = -value;
                }
                hashMap.put(arrowAttribute.getAttribute(), value);
            }
        }
        AttributeT[] values;
        for (int length = (values = AttributeT.values()).length, i = 0; i < length; ++i) {
            AttributeT attributeT = values[i];
            if (!hashMap.containsKey(attributeT)) {
                hashMap.put(attributeT, 0.0);
            }
        }*/
        double critical = 1.0;
        if (SItemUtils.r.nextInt(100) <= EntityAPI.getAttribute(livingDamager, AttributeT.CRITICAL_RATE)) {
        	critical = EntityAPI.getAttribute(livingDamager, AttributeT.CRITICAL_DAMAGE);
            if (critical == 0.0) {
            	critical = 1.0;
            }
            if (SItemUtils.plugin.getMM().getCombatLogManager().isActive()) {
                SItemUtils.plugin.getMM().getCombatLogManager().setCritMeta(critMeta);
            }
        }
        HashMap<DamageType, Double> damageTypes = new HashMap<DamageType, Double>();
        HashMap<DamageType, Double> equtDamageTypes = ItemAPI.getDamageTypes(livingDamager);
        Map<DamageType,Double> skill_type = ConfigManager.DefaultSkill;
        if(ConfigManager.skill_types.containsKey(skill.getName()))
        {
        	skill_type = ConfigManager.skill_types.get(skill.getName()).getDamageType();
        }
        	//Double blendDamage = 0.0;
        	//Double armsdamage = 0.0;
    	Double sumDamage = 0.0;
    	//for(Map.Entry<DamageType,Double> damagetype : ConfigManager.skill_types.get(skill.getName()).getDamageType().entrySet())
    	for(Map.Entry<DamageType,Double> damagetype : skill_type.entrySet())
    	{
    		DamageType damage_type = damagetype.getKey();
    		//blendDamage = blendDamage + damage * damage_type.getDamageModifierByBiome(livingDamager.getLocation().getBlock().getBiome().name());
    		Double equtDamage = equtDamageTypes.get(damage_type);
    		if(equtDamage == null)
    		{
    			equtDamage = 0.0;
    		}
    		//Double equtDamage = (equtDamageTypes.get(damage_type) != null) ?equtDamageTypes.get(damage_type):0.0;
    		Double blendDamage = (equtDamage * damagetype.getValue() + damage) * damage_type.getDamageModifierByBiome(livingDamager.getLocation().getBlock().getBiome().name());
    		sumDamage = sumDamage + blendDamage;
    		damageTypes.put(damage_type, blendDamage);
    	}
    	damage = sumDamage;
        	/*
        	Double blendDamage = 0.0;
        	Double armsdamage = 0.0;
        	for(Map.Entry<DamageType,Double> damagetype : ConfigManager.skill_types.get(skill.getName()).getDamageType().entrySet())
        	{
        		DamageType damage_type = damagetype.getKey();
        		//damage = (damage + ItemAPI.getItemDamage(damage_type.getId(), itemStack) * damagetype.getValue()) * damage_type.getDamageModifierByBiome(livingDamager.getLocation().getBlock().getBiome().name());
        		blendDamage = blendDamage + damage * damage_type.getDamageModifierByBiome(livingDamager.getLocation().getBlock().getBiome().name());
        		Double Ddamage = ItemAPI.getItemDamage(damage_type.getId(), itemStack);//有错！！！！！！！！！！！！！！
        		if (plugin.getMM().getGemManager().isActive()) {
                    HashMap<DamageType, Double> itemGemDamages = plugin.getMM().getGemManager().getItemGemDamages(itemStack, false);
                    if (itemGemDamages.containsKey(damage_type)) {
                    	Ddamage += itemGemDamages.get(damage_type);
                    }
                    HashMap<DamageType, Double> itemGemDamages2 = plugin.getMM().getGemManager().getItemGemDamages(itemStack, true);
                    if (itemGemDamages2.containsKey(damage_type)) {
                    	Ddamage *= 1.0 + itemGemDamages2.get(damage_type) / 100.0;
                    }
                }
        		armsdamage = armsdamage + Ddamage * damagetype.getValue() * damage_type.getDamageModifierByBiome(livingDamager.getLocation().getBlock().getBiome().name());
        	}
        	damage = armsdamage + blendDamage;*/
        /*else {
        	//damage = damage * ConfigManager.DefaultSkill.
        	Double sumDamage = 0.0;
			for(Map.Entry<DamageType,Double> damagetype : ConfigManager.DefaultSkill.entrySet())
        	{
				Double blendDamage =  damage * damagetype.getKey().getDamageModifierByBiome(livingDamager.getLocation().getBlock().getBiome().name());
				sumDamage = sumDamage + blendDamage;
				damageTypes.put(damagetype.getKey(), blendDamage);
        	}
			damage = sumDamage;
        }*/
        /*
        if (damage == 0.0 && itemStack != null) {
        	damage = ItemAPI.getItemTotalDamage(itemStack);
            if (damage == 0.0) {
            	damage = ItemAPI.getDefaultDamage(itemStack);
            }
        }*/
        //double dmgReducer = dmgReducer(damage, ItemAPI.getItemTotalDamage(itemStack), ItemAPI.getDefaultDamage(itemStack));
        //double direct = dmgReducer * (ItemAPI.getAttribute(itemStack, AttributeT.DIRECT_DAMAGE) / 100.0);
      //  double n5;
       // double n4 = n5 = dmgReducer - direct;      
        double direct = damage * (ItemAPI.getAttribute(itemStack, AttributeT.DIRECT_DAMAGE) / 100.0);
       // double finalDamage;
        //double originDamage = finalDamage= damage - direct;
        AttributeT A_pve_defense = AttributeT.PVE_DEFENSE;
        AttributeT A_pve_damage = AttributeT.PVE_DAMAGE;
        if (critMeta instanceof Player && livingDamager instanceof Player) {
        	A_pve_defense = AttributeT.PVP_DEFENSE;
        	A_pve_damage = AttributeT.PVP_DAMAGE;
        }
        double penetration = EntityAPI.getAttribute(livingDamager, AttributeT.PENETRATION);
        double pve_defense = EntityAPI.getAttribute(critMeta, A_pve_defense);
        double pve_damage = EntityAPI.getAttribute(livingDamager, A_pve_damage);
        //HashMap<DamageType, Double> damageTypes = ItemAPI.getDamageTypes(livingDamager);
        HashMap<ArmorType, Double> defenseTypes = ItemAPI.getDefenseTypes(critMeta);
      /*  if (damageTypes.isEmpty()) {
            for (DamageType damageType : SItemUtils.plugin.getCFG().getDamageTypes().values()) {
                if (damageType.isDefault()) {
                    String name = livingDamager.getLocation().getBlock().getBiome().name();
                    damageTypes.put(damageType, originDamage * damageType.getDamageModifierByBiome(name));
                    finalDamage = originDamage * damageType.getDamageModifierByBiome(name);
                    break;
                }
            }
        }*/
        Label_OUT: {
            if (defenseTypes.isEmpty()) {
                for (DamageType damageType2 : damageTypes.keySet()) {
                    if (!damageType2.isDefault()) {
                        continue;
                    }
                    for (ArmorType armorType : SItemUtils.plugin.getCFG().getArmorTypes().values()) {
                        if (armorType.getBlockDamageTypes().contains(damageType2.getId())) {
                            defenseTypes.put(armorType, ItemAPI.getDefaultDefense(critMeta));
                            break Label_OUT;
                        }
                    }
                }
            }
        }
        String dmgString = "";
        for (DamageType damageType3 : damageTypes.keySet()) {
            String dmgString2 = "";
            double midDamage = damageTypes.get(damageType3);
            /*if (!damageType3.isDefault() || n9 != finalDamage) {
                n9 = dmgReducer(damage, n9, ItemAPI.getDefaultDamage(itemStack));
            }*/
            for (ArmorType armorType2 : defenseTypes.keySet()) {
                double doubleValue = 0.0;
                if (armorType2.getBlockDamageTypes().contains(damageType3.getId())) {
                    doubleValue = defenseTypes.get(armorType2);
                }
                midDamage *= 1.0 - Math.min(20.0, EntityAPI.getEnchantedDefense(livingDamager, critMeta)) / 25.0;
                dmgString2 = String.valueOf(dmgString2) + "(" + midDamage + " - " + armorType2.getFormula().replace("%crit%", new StringBuilder(String.valueOf(critical)).toString()).replace("%def%", new StringBuilder(String.valueOf(doubleValue)).toString()).replace("%dmg%", new StringBuilder(String.valueOf(midDamage)).toString()).replace("%penetrate%", new StringBuilder(String.valueOf(penetration)).toString()) + ") + ";
            }
            if (dmgString2.isEmpty()) {
            	dmgString2 = "0";
            }
            if (dmgString2.length() > 3) {
            	dmgString2 = dmgString2.substring(0, dmgString2.length() - 2).trim();
            }
            String string = String.valueOf(midDamage) + " - (" + dmgString2 + ")";
            double typeFinalDamage = 0.0;
            try {
            	typeFinalDamage = direct + Math.max(0.0, (double)SItemUtils.engine.eval(string)) * critical;
            }
            catch (ScriptException ex) {}
            dmgString = String.valueOf(dmgString) + "(" + typeFinalDamage + ") + ";
            if (SItemUtils.plugin.getMM().getCombatLogManager().isActive()) {
                SItemUtils.plugin.getMM().getCombatLogManager().setDTMeta(critMeta, damageType3.getId(), typeFinalDamage);
            }
            DivineItemsAPI.executeActions(livingDamager, damageType3.getActions(), itemStack);
        }
        if (dmgString.length() > 3) {
        	dmgString = dmgString.substring(0, dmgString.length() - 2).trim();
        }
        double n11 = 0.0;
        if (Utils.getRandDouble(0.0, 100.0) <= EntityAPI.getAttribute(critMeta, AttributeT.BLOCK_RATE)) {
            n11 = EntityAPI.getAttribute(critMeta, AttributeT.BLOCK_DAMAGE);
            if (SItemUtils.plugin.getMM().getCombatLogManager().isActive()) {
                SItemUtils.plugin.getMM().getCombatLogManager().setBlockMeta(critMeta, n11);
            }
        }
        String replace = SItemUtils.plugin.getCM().getCFG().getFormulaDamage().replace("%dmg%", dmgString).replace("%other%", SItemUtils.plugin.getCM().getCFG().getFormulaOther().replace("%pvpe_dmg%", new StringBuilder(String.valueOf(pve_damage)).toString()).replace("%pvpe_def%", new StringBuilder(String.valueOf(pve_defense)).toString()).replace("%crit%", new StringBuilder(String.valueOf(critical)).toString()).replace("%block%", new StringBuilder(String.valueOf(n11)).toString())).replace("%crit%", new StringBuilder(String.valueOf(critical)).toString()).replace("%block%", new StringBuilder(String.valueOf(n11)).toString());
        double n12 = 1.0;
        try {
            n12 = direct + Math.max(0.0, (double)SItemUtils.engine.eval(replace));
        }
        catch (ScriptException ex2) {}
        critMeta.removeMetadata("DIVINE_ARROW_ID", SItemUtils.plugin);
        double attribute = EntityAPI.getAttribute(livingDamager, AttributeT.BLEED_RATE);
        if (attribute > 0.0 && Utils.getRandDouble(0.0, 100.0) <= attribute) {
            SItemUtils.plugin.getTM().addBleedEffect(critMeta, n12);
        }
        return n12;
    }
}

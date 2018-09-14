package com.github.wulf.sdmc.event;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.github.wulf.sdmc.config.ConfigManager;
import com.github.wulf.sdmc.evenContainer.CSkillDamageEvent;
import com.github.wulf.sdmc.utils.SItemUtils;
import com.sucy.skill.api.event.SkillDamageEvent;
import com.sucy.skill.api.skills.Skill;

import ru.nightexpress.divineitems.DivineItems;
import ru.nightexpress.divineitems.api.EntityAPI;
import ru.nightexpress.divineitems.api.ItemAPI;
import ru.nightexpress.divineitems.attributes.AttributeT;
import ru.nightexpress.divineitems.attributes.DisarmRateSettings;
import ru.nightexpress.divineitems.hooks.Hook;
import ru.nightexpress.divineitems.utils.ItemUtils;
import ru.nightexpress.divineitems.utils.Utils;


public class SDamageEvents implements Listener {

	private static CSkillDamageEvent catchskillevent = null;
	
    private DivineItems plugin;
    
    public SDamageEvents(DivineItems plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void catchOnSkillDamage(SkillDamageEvent event) {
    	catchskillevent = new CSkillDamageEvent(event);
    	//System.out.print("有人在放技能"+catchskillevent.getSkill().getName());
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMeleeDamage(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        Entity entity = entityDamageByEntityEvent.getEntity();
        Entity damager = entityDamageByEntityEvent.getDamager();
        if (!(entity instanceof LivingEntity) || entity == null) {
            return;
        }
        if (!(damager instanceof LivingEntity) || damager == null) {
            return;
        }
        if (Hook.WORLD_GUARD.isEnabled() && !this.plugin.getHM().getWorldGuard().canFights(entity, damager)) {
            entityDamageByEntityEvent.setCancelled(true);
            return;
        }
        if (Hook.CITIZENS.isEnabled() && this.plugin.getHM().getCitizens().isNPC(entity)) {
            return;
        }
        LivingEntity livingDamager = (LivingEntity)damager;
        LivingEntity livingEntity = (LivingEntity)entity;
        ItemStack itemStack = null;
        if (livingDamager.getEquipment().getItemInMainHand() != null) {
            itemStack = new ItemStack(livingDamager.getEquipment().getItemInMainHand());
        }
        if (itemStack == null) {
            return;
        }
        if(!Skill.isSkillDamage()) {
	        if (itemStack.getType() == Material.BOW) {
	            return;
	        }
	        double attribute = ItemAPI.getAttribute(itemStack, AttributeT.RANGE);
	        if (attribute > 0.0 && livingDamager.getWorld().equals(livingEntity.getWorld()) && livingDamager.getLocation().distance(livingEntity.getLocation()) > attribute) {
	            entityDamageByEntityEvent.setCancelled(true);
	            return;
	        }
	        this.processDmg(itemStack, livingDamager, livingEntity, entityDamageByEntityEvent);
        }else
        {
        	 this.cProcessDmg(itemStack, livingDamager, livingEntity, entityDamageByEntityEvent);
        }
    }
    
    private void cProcessDmg(ItemStack itemStack, LivingEntity livingDamager, LivingEntity dodgeMeta, EntityDamageByEntityEvent entityDamageByEntityEvent) {
    	double dodge_rate = EntityAPI.getAttribute(dodgeMeta, AttributeT.DODGE_RATE);
        double accuracy_rate = EntityAPI.getAttribute(livingDamager, AttributeT.ACCURACY_RATE);
        double dodge_randDouble = Utils.getRandDouble(0.0, 100.0);
        double accuracy_randDouble = Utils.getRandDouble(0.0, 100.0);
        if (dodge_randDouble <= dodge_rate && accuracy_randDouble > accuracy_rate) {
        	entityDamageByEntityEvent.setDamage(0.0);
        	entityDamageByEntityEvent.setCancelled(true);
        	if (this.plugin.getMM().getCombatLogManager().isActive()) {
        		this.plugin.getMM().getCombatLogManager().setDodgeMeta(dodgeMeta);
        	}
        	return;
        }
        if (entityDamageByEntityEvent.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
        	entityDamageByEntityEvent.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0.0);
        }
        double damage = entityDamageByEntityEvent.getDamage();
        if (Utils.getRandDouble(0.0, 100.0) <= ItemAPI.getAttribute(itemStack, AttributeT.BURN_RATE)) {
        	dodgeMeta.setFireTicks(100);
        }
        double range = ConfigManager.AOE_DAMAGE_RANG;
        //double n = damage * (ItemAPI.getAttribute(itemStack, AttributeT.AOE_DAMAGE) / 100.0);
        double n = damage * Math.min((ConfigManager.AOE_DAMAGE_CAPABILITY / 100.0), (ItemAPI.getAttribute(itemStack, AttributeT.AOE_DAMAGE) / 100.0));
        if (n > 0.0) {
        	if (dodgeMeta.hasMetadata("AOE_FIX")) {
        		dodgeMeta.removeMetadata("AOE_FIX", this.plugin);
        	}
        	else {
        		for (Entity entity : dodgeMeta.getNearbyEntities(range, range, range)) {
        			if (!(entity instanceof LivingEntity)) {
        				continue;
        			}
        			LivingEntity livingEntity2 = (LivingEntity)entity;
        			if (Hook.CITIZENS.isEnabled() && this.plugin.getHM().getCitizens().isNPC((Entity)livingEntity2)) {
        				continue;
        			}
        			if (livingEntity2.equals(livingDamager)) {
        				continue;
        			}
        			if (Hook.WORLD_GUARD.isEnabled() && !this.plugin.getHM().getWorldGuard().canFights(livingEntity2, livingDamager)) {
        				continue;
        			}
        			livingEntity2.setMetadata("AOE_FIX", new FixedMetadataValue(this.plugin, "yes"));
        			livingEntity2.damage(n, livingDamager);
        		}
        	}
        }
        double calcDamageByFormula = SItemUtils.calcSkillDamageByFormula(dodgeMeta, livingDamager, damage, itemStack, catchskillevent.getSkill());
        entityDamageByEntityEvent.setDamage(calcDamageByFormula);
        if (Hook.MYTHIC_MOBS.isEnabled()) {
            this.plugin.getHM().getMythicHook().setSkillDamage(livingDamager, calcDamageByFormula);
        }	
	}
    

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDBows(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        Entity entity = entityDamageByEntityEvent.getEntity();
        Entity damager = entityDamageByEntityEvent.getDamager();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        if (!(damager instanceof Projectile)) {
            return;
        }
        if (Hook.WORLD_GUARD.isEnabled() && !this.plugin.getHM().getWorldGuard().canFights(entity, damager)) {
            entityDamageByEntityEvent.setCancelled(true);
            return;
        }
        if (Hook.CITIZENS.isEnabled() && this.plugin.getHM().getCitizens().isNPC(entity)) {
            return;
        }
        Projectile projectile = (Projectile)damager;
        if (!projectile.hasMetadata("DIItem") || !(projectile.getShooter() instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity)entity;
        LivingEntity livingEntity2 = (LivingEntity)projectile.getShooter();
        if (livingEntity == null || livingEntity2 == null) {
            return;
        }
        if (projectile.hasMetadata("DIVINE_ARROW_ID")) {
            livingEntity.setMetadata("DIVINE_ARROW_ID", new FixedMetadataValue(this.plugin, projectile.getMetadata("DIVINE_ARROW_ID").get(0).asString()));
        }
        this.processDmg((ItemStack)projectile.getMetadata("DIItem").get(0).value(), livingEntity2, livingEntity, entityDamageByEntityEvent);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVampirism(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        if (!(entityDamageByEntityEvent.getEntity() instanceof LivingEntity)) {
            return;
        }
        if (entityDamageByEntityEvent.getEntity() instanceof ArmorStand) {
            return;
        }
        Entity damager = entityDamageByEntityEvent.getDamager();
        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile)damager;
            if (projectile.getShooter() != null && projectile.getShooter() instanceof Entity) {
                damager = (Entity)projectile.getShooter();
            }
        }
        if (!(damager instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity)damager;
        double n = entityDamageByEntityEvent.getFinalDamage() * (EntityAPI.getAttribute(livingEntity, AttributeT.VAMPIRISM) / 100.0);
        if(Skill.isSkillDamage())
        {
        	n = Math.min(n, ConfigManager.VAMPIRISM_CAPABILITY);
        }
        EntityRegainHealthEvent entityRegainHealthEvent = new EntityRegainHealthEvent(livingEntity, n, EntityRegainHealthEvent.RegainReason.CUSTOM);
        this.plugin.getPluginManager().callEvent(entityRegainHealthEvent);
        if (!entityRegainHealthEvent.isCancelled() && livingEntity.getHealth() + n <= livingEntity.getMaxHealth()) {
            livingEntity.setHealth(livingEntity.getHealth() + n);
        }
    }
    
    private void processDmg(ItemStack itemStack, LivingEntity livingEntity, LivingEntity dodgeMeta, EntityDamageByEntityEvent entityDamageByEntityEvent) {
        double attribute = EntityAPI.getAttribute(dodgeMeta, AttributeT.DODGE_RATE);
        double attribute2 = EntityAPI.getAttribute(livingEntity, AttributeT.ACCURACY_RATE);
        double randDouble = Utils.getRandDouble(0.0, 100.0);
        double randDouble2 = Utils.getRandDouble(0.0, 100.0);
        if (randDouble <= attribute && randDouble2 > attribute2) {
            entityDamageByEntityEvent.setDamage(0.0);
            entityDamageByEntityEvent.setCancelled(true);
            if (this.plugin.getMM().getCombatLogManager().isActive()) {
                this.plugin.getMM().getCombatLogManager().setDodgeMeta(dodgeMeta);
            }
            return;
        }
        if (entityDamageByEntityEvent.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
            entityDamageByEntityEvent.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0.0);
        }
        double attribute3 = ItemAPI.getAttribute(itemStack, AttributeT.DISARM_RATE);
        if (attribute3 > 0.0 && Utils.getRandDouble(0.0, 100.0) <= attribute3) {
            boolean b = true;
            ItemStack itemStack2 = dodgeMeta.getEquipment().getItemInMainHand();
            if (itemStack2 == null || itemStack2.getType() == Material.AIR) {
                itemStack2 = dodgeMeta.getEquipment().getItemInOffHand();
                b = false;
            }
            if (itemStack2 != null && itemStack2.getType() != Material.AIR) {
                if (b) {
                    dodgeMeta.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
                }
                else {
                    dodgeMeta.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));
                }
                dodgeMeta.getWorld().dropItemNaturally(dodgeMeta.getLocation(), itemStack2).setPickupDelay(40);
                DisarmRateSettings disarmRateSettings = (DisarmRateSettings)AttributeT.DISARM_RATE.getSettings();
                Utils.playEffect(disarmRateSettings.getEffect(), 0.2, 0.4, 0.2, 0.1, 25, dodgeMeta.getLocation());
                if (livingEntity instanceof Player) {
                    ((Player)livingEntity).sendMessage(disarmRateSettings.getMsgToDamager().replace("%s%", Utils.getEntityName(dodgeMeta)));
                }
                if (dodgeMeta instanceof Player) {
                    ((Player)dodgeMeta).sendMessage(disarmRateSettings.getMsgToEntity().replace("%s%", Utils.getEntityName(livingEntity)));
                }
            }
        }
        double damage = entityDamageByEntityEvent.getDamage();
        if (Utils.getRandDouble(0.0, 100.0) <= ItemAPI.getAttribute(itemStack, AttributeT.BURN_RATE)) {
            dodgeMeta.setFireTicks(100);
        }
        double attribute4 = EntityAPI.getAttribute(livingEntity, AttributeT.RANGE);
        if (attribute4 <= 0.0) {
            attribute4 = 3.0;
        }
        double n = damage * (ItemAPI.getAttribute(itemStack, AttributeT.AOE_DAMAGE) / 100.0);
        if (n > 0.0) {
            if (dodgeMeta.hasMetadata("AOE_FIX")) {
                dodgeMeta.removeMetadata("AOE_FIX", this.plugin);
            }
            else {
                for (Entity entity : dodgeMeta.getNearbyEntities(attribute4, attribute4, attribute4)) {
                    if (!(entity instanceof LivingEntity)) {
                        continue;
                    }
                    LivingEntity livingEntity2 = (LivingEntity)entity;
                    if (Hook.CITIZENS.isEnabled() && this.plugin.getHM().getCitizens().isNPC((Entity)livingEntity2)) {
                        continue;
                    }
                    if (livingEntity2.equals(livingEntity)) {
                        continue;
                    }
                    if (Hook.WORLD_GUARD.isEnabled() && !this.plugin.getHM().getWorldGuard().canFights(livingEntity2, livingEntity)) {
                        continue;
                    }
                    livingEntity2.setMetadata("AOE_FIX", new FixedMetadataValue(this.plugin, "yes"));
                    livingEntity2.damage(n, livingEntity);
                }
            }
        }
        double calcDamageByFormula = ItemUtils.calcDamageByFormula(dodgeMeta, livingEntity, damage, itemStack);
        entityDamageByEntityEvent.setDamage(calcDamageByFormula);
        if (Hook.MYTHIC_MOBS.isEnabled()) {
            this.plugin.getHM().getMythicHook().setSkillDamage(livingEntity, calcDamageByFormula);
        }
    }
}

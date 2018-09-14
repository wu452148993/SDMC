package com.github.wulf.sdmc.event;

import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.nightexpress.divineitems.DivineItems;
import ru.nightexpress.divineitems.api.EntityAPI;
import ru.nightexpress.divineitems.api.ItemAPI;
import ru.nightexpress.divineitems.attributes.AttributeT;
import ru.nightexpress.divineitems.hooks.Hook;
import ru.nightexpress.divineitems.types.ArmorType;
import ru.nightexpress.divineitems.utils.ItemUtils;
import ru.nightexpress.divineitems.utils.Utils;

public class DIDamageListener implements Listener
{
    private DivineItems plugin;
    
    public DIDamageListener(DivineItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRangeDamage(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }
        if (playerInteractEvent.getAction().toString().contains("RIGHT")) {
            return;
        }
        Player player = playerInteractEvent.getPlayer();
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand == null || itemInMainHand.getType() == Material.BOW || !ItemUtils.isWeapon(itemInMainHand)) {
            return;
        }
        double attribute = ItemAPI.getAttribute(itemInMainHand, AttributeT.RANGE);
        double buff = this.plugin.getMM().getBuffManager().getBuff(player, AttributeT.RANGE);
        if (buff > 0.0) {
            if (attribute <= 0.0) {
                attribute = 3.0;
            }
            attribute *= 1.0 + buff / 100.0;
        }
        if (attribute <= 0.0) {
            return;
        }
        LivingEntity entityTargetByRange = EntityAPI.getEntityTargetByRange(player, attribute);
        if (entityTargetByRange != null) {
            entityTargetByRange.damage(ItemAPI.getDefaultDamage(itemInMainHand), player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectLaunch(ProjectileLaunchEvent projectileLaunchEvent) {
        Projectile entity = projectileLaunchEvent.getEntity();
        if (entity == null || !(entity instanceof Projectile)) {
            return;
        }
        Projectile projectile = entity;
        if (projectile.getShooter() == null || !(projectile.getShooter() instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity)projectile.getShooter();
        if (livingEntity.getEquipment().getItemInMainHand() == null) {
            return;
        }
        ItemStack itemStack = new ItemStack(livingEntity.getEquipment().getItemInMainHand());
        if (!(livingEntity instanceof Player) && !this.plugin.getCM().getCFG().allowAttributesToMobs()) {
            return;
        }
        ItemUtils.setProjectileData(projectile, livingEntity, itemStack);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSimpleDamage(EntityDamageEvent entityDamageEvent) {
        if (!(entityDamageEvent.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity)entityDamageEvent.getEntity();
        double finalDamage = entityDamageEvent.getFinalDamage();
        if (livingEntity instanceof Player) {
            if (this.plugin.getCM().getCFG().getPlayerDmgModifiers().containsKey(entityDamageEvent.getCause())) {
                double n = finalDamage * this.plugin.getCM().getCFG().getPlayerDmgModifiers().get(entityDamageEvent.getCause());
            }
        }
        else if (this.plugin.getCM().getCFG().getMobDmgModifiers().containsKey(entityDamageEvent.getCause())) {
            double n2 = finalDamage * this.plugin.getCM().getCFG().getMobDmgModifiers().get(entityDamageEvent.getCause());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFinalDamage(EntityDamageEvent entityDamageEvent) {
        if (entityDamageEvent instanceof EntityDamageByEntityEvent) {
            return;
        }
        if (!(entityDamageEvent.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity)entityDamageEvent.getEntity();
        double finalDamage = entityDamageEvent.getFinalDamage();
        if (!(livingEntity instanceof Player) && !this.plugin.getCFG().allowAttributesToMobs()) {
            return;
        }
        String name = entityDamageEvent.getCause().name();
        HashMap<ArmorType, Double> defenseTypes = ItemAPI.getDefenseTypes(livingEntity);
        for (ArmorType armorType : defenseTypes.keySet()) {
            double doubleValue = 0.0;
            if (armorType.getBlockDamageSources().contains(name)) {
                doubleValue = defenseTypes.get(armorType);
            }
            entityDamageEvent.setDamage(ItemUtils.calc(armorType.getFormula().replace("%def%", new StringBuilder(String.valueOf(doubleValue)).toString()).replace("%dmg%", new StringBuilder(String.valueOf(finalDamage)).toString()).replace("%penetrate%", "0")));
        }
        if (this.plugin.getMM().getCombatLogManager().isActive()) {
            this.plugin.getMM().getCombatLogManager().setDSMeta(livingEntity, name, Utils.round3(finalDamage));
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent playerFishEvent) {
        if (!this.plugin.getCM().getCFG().allowFishHookDamage()) {
            return;
        }
        Entity caught = playerFishEvent.getCaught();
        if (!(caught instanceof LivingEntity)) {
            return;
        }
        Player player = playerFishEvent.getPlayer();
        LivingEntity livingEntity = (LivingEntity)caught;
        double itemTotalDamage = ItemAPI.getItemTotalDamage(player.getInventory().getItemInMainHand());
        double distance = player.getLocation().distance(livingEntity.getLocation());
        double n;
        if (distance < 10.0) {
            n = distance / 10.0;
        }
        else {
            n = 1.5;
        }
        livingEntity.damage(itemTotalDamage * n, player);
    }
    
    @EventHandler
    public void onWallFall(EntityChangeBlockEvent entityChangeBlockEvent) {
        if (entityChangeBlockEvent.getEntity() instanceof FallingBlock && entityChangeBlockEvent.getEntity().hasMetadata("DIFall")) {
            FallingBlock fallingBlock = (FallingBlock)entityChangeBlockEvent.getEntity();
            Utils.playEffect("BLOCK_CRACK:" + fallingBlock.getMaterial().name(), 0.30000001192092896, 0.0, 0.30000001192092896, 0.30000001192092896, 15, fallingBlock.getLocation());
            entityChangeBlockEvent.getEntity().remove();
            entityChangeBlockEvent.setCancelled(true);
            Entity entity = entityChangeBlockEvent.getEntity();
            Player player = Bukkit.getPlayer(entity.getMetadata("LauncherZ").get(0).asString());
            if (player == null) {
                return;
            }
            List<Entity> nearbyEntities = entityChangeBlockEvent.getEntity().getNearbyEntities(3.0, 3.0, 3.0);
            if (nearbyEntities.isEmpty()) {
                return;
            }
            ItemStack itemStack = (ItemStack)entity.getMetadata("DIItem").get(0).value();
            for (Entity entity2 : nearbyEntities) {
                if (!(entity2 instanceof LivingEntity)) {
                    continue;
                }
                LivingEntity livingEntity = (LivingEntity)entity2;
                if (Hook.CITIZENS.isEnabled() && this.plugin.getHM().getCitizens().isNPC(livingEntity)) {
                    continue;
                }
                if (livingEntity.equals(player)) {
                    continue;
                }
                if (livingEntity.equals(entity)) {
                    continue;
                }
                if (Hook.WORLD_GUARD.isEnabled() && !this.plugin.getHM().getWorldGuard().canFights(livingEntity, player)) {
                    continue;
                }
                livingEntity.damage(ItemUtils.calcDamageByFormula(livingEntity, player, 1.0, itemStack), player);
            }
        }
    }
}

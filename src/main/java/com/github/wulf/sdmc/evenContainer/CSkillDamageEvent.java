package com.github.wulf.sdmc.evenContainer;

import org.bukkit.entity.LivingEntity;

import com.sucy.skill.api.event.SkillDamageEvent;
import com.sucy.skill.api.skills.Skill;

public class CSkillDamageEvent {
    private LivingEntity damager;
    private LivingEntity target;
    private Skill        skill;
    private double       damage;
    
    public CSkillDamageEvent(Skill skill, LivingEntity damager, LivingEntity target, double damage)
    {
        this.skill = skill;
        this.damager = damager;
        this.target = target;
        this.damage = damage;
    }
    
    public CSkillDamageEvent(SkillDamageEvent event)
    {
        this.skill = event.getSkill();
        this.damager = event.getDamager();
        this.target = event.getTarget();
        this.damage = event.getDamage();
    }
    
    /**
     * @return skill used to deal the damage
     */
    public Skill getSkill() {
        return skill;
    }

    /**
     * Retrieves the entity that dealt the damage
     *
     * @return entity that dealt the damage
     */
    public LivingEntity getDamager()
    {
        return damager;
    }

    /**
     * Retrieves the entity that received the damage
     *
     * @return entity that received the damage
     */
    public LivingEntity getTarget()
    {
        return target;
    }

    /**
     * Retrieves the amount of damage dealt
     *
     * @return amount of damage dealt
     */
    public double getDamage()
    {
        return damage;
    }

    /**
     * Sets the amount of damage dealt
     *
     * @param amount amount of damage dealt
     */
    public void setDamage(double amount)
    {
        damage = amount;
    }
}

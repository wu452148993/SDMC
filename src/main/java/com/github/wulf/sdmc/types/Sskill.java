package com.github.wulf.sdmc.types;

import java.util.Map;

import ru.nightexpress.divineitems.types.DamageType;

public class Sskill {
	 private String name;
	 //private Double percent;
	 private Map<DamageType,Double> damageTypes;
	 
    public Sskill(String name, Map<DamageType,Double> damageTypes) {
    	this.name = name;
    	//this.percent = percent;
    	this.damageTypes = damageTypes;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Map<DamageType,Double> getDamageType() {
        return this.damageTypes;
    }
	 
}

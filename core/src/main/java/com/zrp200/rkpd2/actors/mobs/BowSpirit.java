package com.zrp200.rkpd2.actors.mobs;

import com.watabou.utils.Random;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.sprites.ScorpioSprite;

public class BowSpirit extends Mob {
    
    int MINDMG;
    int MAXDMG;
    float ACC;
    float DLY;

    {
        //TODO: fix sprite
		spriteClass = ScorpioSprite.class;
		
		HP = HT = Dungeon.hero.lvl;
		defenseSkill = 0;
		viewDistance = Light.DISTANCE;
		
		EXP = 0;
		maxLvl = 30;
		
		loot = Generator.Category.MISSILE;
		lootChance = 0f;
        super.alignment = Alignment.ALLY;

        properties.add(Property.IMMOVABLE);
        // awaken bow when it spawns
        // dunno if this should be wandering or passive
        state = WANDERING;

	}

    // Must be initialized using this constructor
    public BowSpirit(SpiritBow bow) {
        this.MINDMG = bow.min();
        this.MAXDMG = bow.max();
        this.ACC = bow.ACC;
        this.DLY = bow.DLY;
    }
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( MINDMG, MAXDMG );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return (MINDMG + MAXDMG) / 2;
	}
	
	@Override
	public boolean canAttack(Char enemy) {
        if (buff(ChampionEnemy.Paladin.class) != null){
            return false;
        }
		return super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
	}
	
	
	@Override
	public void aggro(Char ch) {
		//cannot be aggroed to something it can't see
		//skip this check if FOV isn't initialized
		if (ch == null || fieldOfView == null
				|| fieldOfView.length != Dungeon.level.length() || fieldOfView[ch.pos]) {
			super.aggro(ch);
		}
	}
}

package com.zrp200.rkpd2.actors.mobs;

import com.watabou.utils.Callback;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.huntress.SpiritHawk.HawkSprite;
import com.zrp200.rkpd2.actors.mobs.npcs.DirectableAlly;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.utils.GLog;

public class BowSpirit extends DirectableAlly {
    
    int MINDMG;
    int MAXDMG;
    float ACC;
    float DLY;

	SpiritBow bow;

    {
        //TODO: fix sprite
		spriteClass = HawkSprite.class;
		
		HP = HT = Dungeon.hero.lvl;
		defenseSkill = 0;
		viewDistance = Light.DISTANCE;

		flying = true;

		// this wont work because it makes the bow jump away from it
        //properties.add(Property.IMMOVABLE);
		baseSpeed = 0f; // instead we set the base speed to 0

		//TODO: fix talent once i actually add it
		baseSpeed = Dungeon.hero.pointsInTalent(Talent.FARSIGHT);
	}

    // Must be initialized using this constructor
    public BowSpirit(SpiritBow bow) {
        super();
		this.bow = bow;
		this.MINDMG = bow.min();
        this.MAXDMG = bow.max();
        this.ACC = bow.ACC;
        this.DLY = bow.DLY;
    }
	
	@Override
	public int damageRoll() {
		return bow.damageRoll(this);
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

	private void updateBow(){
		if (bow == null) {
			bow = Dungeon.hero.belongings.getItem(SpiritBow.class);
		}
		
		//same dodge as the hero
		defenseSkill = (Dungeon.hero.lvl+4);
		if (bow == null) return;
		HT = 20 + 8*(bow.level() + Math.max(0, Dungeon.hero.lvl-30));
	}

	@Override
	protected boolean act() {
		updateBow();
		
		if (!isAlive()) {
			return true;
		}
		return super.act();
	}

	@Override
	public int attackSkill(Char target) {
		
		//same accuracy as the hero.
		int acc = Dungeon.hero.lvl + 9;
		
		if (bow != null){
			acc *= bow.accuracyFactor( this, target );
		}
		
		return acc;
	}
	
	@Override
	public float attackDelay() {
		float delay = super.attackDelay();
		if (bow != null){
			delay *= bow.delayFactor(this);
		}
		return delay;
	}

	@Override
	public boolean canAttack(Char enemy) {
        if (buff(ChampionEnemy.Paladin.class) != null){
            return false;
        }
		return super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
	}
	
	@Override
	public int attackProc(Char enemy, int damage) {
		damage = super.attackProc(enemy, damage);
		if (bow != null) {
			damage = bow.proc(this, enemy, damage);
			if (!enemy.isAlive() && enemy == Dungeon.hero) {
				Dungeon.fail(this);
				GLog.n(Messages.capitalize(Messages.get(Char.class, "kill", name())));
			}
		}
		return damage;
	}

	@Override
	protected boolean doAttack( Char enemy ) {
		if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
			((MissileSprite) sprite.parent.recycle(MissileSprite.class)).
					reset(sprite,
							enemy.sprite,
							new SpiritBow().knockArrow(),
							new Callback() {
								@Override
								public void call() {
									attack(enemy,
											1,
											0, 1);
									spend(attackDelay());
									next();
								}
							});
			return false;
		} else {
			return super.doAttack(enemy);
		}
	}
	
	@Override
	public void damage(int dmg, Object src) {
		super.damage( dmg, src );
		
		//for the bow status indicator
		Item.updateQuickslot();
	}

	@Override
	public boolean interact(Char c) {
		// kills the bow spirit so that the bow can be collected
		die(c);
		return true;
	}

	@Override
	public void die(Object cause) {
		
		super.die(cause);
	}		
}
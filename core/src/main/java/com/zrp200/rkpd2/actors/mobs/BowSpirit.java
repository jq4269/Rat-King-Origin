package com.zrp200.rkpd2.actors.mobs;

import com.watabou.utils.Bundle;
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

	public static SpiritBow bow;
	private static boolean heroAttacked;

    {
        //TODO: fix sprite
		spriteClass = HawkSprite.class;
		
		HP = HT = Dungeon.hero.lvl;
		defenseSkill = (Dungeon.hero.lvl+4);
		viewDistance = Light.DISTANCE;

		flying = true;

		baseSpeed = 1f;

		if (!Dungeon.hero.hasTalent(Talent.DRIFTING_SPIRIT)) {
			rooted = true;
		} else {
			rooted = false;
			baseSpeed = 1/(5 - (float) Dungeon.hero.pointsInTalent(Talent.DRIFTING_SPIRIT));
		}

	}

	public BowSpirit() {
		super();
	}

    public BowSpirit(SpiritBow bow) {
        super();
		BowSpirit.bow = bow;
		heroAttacked = false;
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

	public void setMovement(int spiritBowPos) {
		if (Dungeon.hero.hasTalent(Talent.DRIFTING_SPIRIT)) {
			followHero();
		} else {
			defendPos(spiritBowPos);
		}
	}

	// if the hero attacks then we call this so that spirit doesnt attack as well
	// for balancing reasons
	public static void heroAttacked() {
		heroAttacked = true;
	}

	@Override
	protected boolean act() {
		boolean b = super.act();
		heroAttacked = false;
		return b;
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
        if (heroAttacked || buff(ChampionEnemy.Paladin.class) != null){
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
		if (bow != null) Dungeon.level.drop( bow, pos ).sprite.drop();
		super.die(cause);
	}
	private static final String SPIRIT_BOW = "spirit_bow";
	private static final String HERO_ATTACKED = "hero_attacked";  

	@Override  
	public void storeInBundle(Bundle bundle) {  
		super.storeInBundle(bundle);  
		if (bow != null) {  
			bundle.put(SPIRIT_BOW, bow); 
			bundle.put(HERO_ATTACKED, heroAttacked);
		}  
	}  
	
	@Override  
	public void restoreFromBundle(Bundle bundle) {  
		super.restoreFromBundle(bundle);  
		if (bundle.contains(SPIRIT_BOW)) {  
			bow = (SpiritBow) bundle.get(SPIRIT_BOW);   
		}  
		heroAttacked = bundle.getBoolean(HERO_ATTACKED);
	}
}
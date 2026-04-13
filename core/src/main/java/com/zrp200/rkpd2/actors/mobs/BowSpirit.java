package com.zrp200.rkpd2.actors.mobs;

import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Healing;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.huntress.SpiritHawk.HawkSprite;
import com.zrp200.rkpd2.actors.mobs.npcs.DirectableAlly;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.utils.GLog;

public class BowSpirit extends DirectableAlly {

	public static SpiritBow bow;
	private static boolean heroAttacked;
	private double dmgMultiplier;
	private float turnsNotAttacked;
	private float lastActTime;

    {
        //TODO: fix sprite
		spriteClass = HawkSprite.class;
		
		int biggerBowPoints = Dungeon.hero.pointsInTalent(Talent.BIGGER_BOW);

		HP = HT = (int) (Dungeon.hero.lvl * (1 + (0.5 * biggerBowPoints)));
		defenseSkill = (Dungeon.hero.lvl+4);
		viewDistance = Light.DISTANCE;

		dmgMultiplier = 0.25 * (1 + biggerBowPoints);

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
		turnsNotAttacked = 0f;
		lastActTime = now();
    }
	
	@Override
	public int damageRoll() {
		int dmg = (int) (bow.damageRoll(this) * dmgMultiplier);
		if (Dungeon.hero.pointsInTalent(Talent.PATIENT_BOW) > 2) {
			dmg *= 1 + (turnsNotAttacked * 0.01);
		}
		return dmg;
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
		turnsNotAttacked += (now() - lastActTime);
		lastActTime = now();
		heroAttacked = false;
		if (state != HUNTING) {
			
			if (HP < HT && Dungeon.hero.hasTalent(Talent.PATIENT_BOW)) {
				Buff.affect(this, Healing.class).setHeal(1, 0, 1);
			}
		}
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
			int patientBowPoints = Dungeon.hero.pointsInTalent(Talent.PATIENT_BOW);
			if (patientBowPoints > 1) {
				float procChance = (float) ((patientBowPoints - 1) * 0.025 * turnsNotAttacked);
				if (Random.Float() < procChance) {
					// caps at 300% knockback
					float powerMulti = (float) Math.min(3.0, Math.max(1.0, procChance));

					//trace a ballistica to our target (which will also extend past them)
					Ballistica trajectory = new Ballistica(this.pos, enemy.pos, Ballistica.STOP_TARGET);
					//trim it to just be the part that goes past them
					trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
					//knock them back along that ballistica
					WandOfBlastWave.throwChar(enemy,
							trajectory,
							Math.round(2 * powerMulti),
							false,
							true,
							this);
				}
			}
			if (!enemy.isAlive() && enemy == Dungeon.hero) {
				Dungeon.fail(this);
				GLog.n(Messages.capitalize(Messages.get(Char.class, "kill", name())));
			}
		}
		turnsNotAttacked = turnsNotAttacked - (int) turnsNotAttacked; //reset the counter if it procs
		return damage;
	}

	@Override
	protected boolean doAttack( Char enemy ) {
		turnsNotAttacked--;
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
		bow.doPickUp(Dungeon.hero, this.pos);
		bow = null;
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
	private static final String TURNS_NOT_ATTACKED = "turns_not_attacked";
	  

	@Override  
	public void storeInBundle(Bundle bundle) {  
		super.storeInBundle(bundle);  
		if (bow != null) {  
			bundle.put(SPIRIT_BOW, bow); 
			bundle.put(HERO_ATTACKED, heroAttacked);
			bundle.put(TURNS_NOT_ATTACKED, turnsNotAttacked);
		}  
	}  
	
	@Override  
	public void restoreFromBundle(Bundle bundle) {  
		super.restoreFromBundle(bundle);  
		if (bundle.contains(SPIRIT_BOW)) {  
			bow = (SpiritBow) bundle.get(SPIRIT_BOW);   
		}  
		heroAttacked = bundle.getBoolean(HERO_ATTACKED);
		turnsNotAttacked = bundle.getInt(TURNS_NOT_ATTACKED);
	}
}
package com.zrp200.rkpd2.actors.hero.spells;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

import java.util.Arrays;
import java.util.List;

public class MessisPotentiae extends ClericSpell {
    public static final MessisPotentiae INSTANCE = new MessisPotentiae();

    @Override
    public int icon() {
        return HeroIcon.MESSIS_POTENTIAE;
    }

    @Override
    public void tintIcon(HeroIcon icon) {
        // todo make icon
        if (SpellEmpower.isActive()) icon.tint(0, .33f);
    }

    @Override
    public Talent talent() {
        return Talent.MESSIS_POTENTIAE;
    }

    @Override
    public float chargeUse(Hero hero) {
        return 2f;
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.shiftedPoints(talent()) > (SpellEmpower.isActive() ? 0 : 1);
    }

    public static int duration(){
        int base = 10 * (1 + MessisPotentiae.INSTANCE.scalingPoints());
        if (SpellEmpower.isActive()){
            base *= 2;
        }
        return base;
    }

    @Override
    protected List<Object> getDescArgs() {
        return Arrays.asList(duration(), 0.5f);
    }

    @Override
    public int aspectRequirement() {
        return 1;
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {

        Buff.affect(hero, EmpowerBuff.class, duration());

        hero.sprite.burst(0xAAFFFF00, 15);
        hero.busy();
        hero.sprite.operate(hero.pos);
        Sample.INSTANCE.play(Assets.Sounds.READ);
        Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC);

        onSpellCast(tome, hero);
    }

    public static class EmpowerBuff extends FlavourBuff {
        {
            type = buffType.POSITIVE;
            announced = true;
        }

        @Override
        public int icon() {
            return BuffIndicator.MESSIS_POTENTIAE;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, 1 - visualcooldown() / (1 + duration()));
        }

        @Override
        public void fx(boolean on) {
            if (on) {
                target.sprite.aura( 0xFFFFAA );
                target.sprite.add(CharSprite.State.GLOWING);
            } else {
                target.sprite.clearAura();
                target.sprite.remove(CharSprite.State.GLOWING);
            }
        }
    }
}

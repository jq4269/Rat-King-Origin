package com.zrp200.rkpd2.actors.hero.spells;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Transmuting;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PetitioMagica extends ClericSpell {
    public static final PetitioMagica INSTANCE = new PetitioMagica();

    @Override
    public int icon() {
        return HeroIcon.PETITIO_MAGICA;
    }

    @Override
    public void tintIcon(HeroIcon icon) {
        // todo make icon
        if (SpellEmpower.isActive()) icon.tint(0, .33f);
    }

    @Override
    public Talent talent() {
        return Talent.PETITIO_MAGICA;
    }

    @Override
    public float chargeUse(Hero hero) {
        return 4;
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.shiftedPoints(talent()) > (SpellEmpower.isActive() ? 0 : 1);
    }

    @Override
    public int aspectRequirement() {
        return 3;
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        ArrayList<ClericSpell> currentSpells = new ArrayList<>();
        for (int i = 1; i <= 3; i++)
            currentSpells.addAll(ClericSpell.getSpellList(hero, i));
        ArrayList<ClericSpell> allSpells = ClericSpell.getAllSpells();
        ClericSpell spell = null;
        while (spell == null){
            ClericSpell testSpell = Random.element(allSpells);
            if (currentSpells.contains(testSpell)) {
                continue;
            }
            if (testSpell.talent() != null && testSpell.talent().maxPoints() > 3)
                continue;
            spell = testSpell;
        }
        Buff.affect(hero, SpellHolder.class, duration()).spell = spell;

        new Transmuting(new HeroIcon(this), new HeroIcon(spell)).show(hero);
        Sample.INSTANCE.play(Assets.Sounds.READ);
        GLog.p(Messages.get(this, "spell", Messages.titleCase(spell.name())));

        hero.spend( 1f );
        hero.busy();
        hero.sprite.operate(hero.pos);

        onSpellCast(tome, hero);
    }

    public static int duration(){
        int base = 50 * (1 + PetitioMagica.INSTANCE.scalingPoints());
        if (SpellEmpower.isActive()){
            base *= 1.5f;
        }
        return base;
    }

    @Override
    protected List<Object> getDescArgs() {
        return Collections.singletonList(duration());
    }

    public static boolean containsSpell(Hero cleric, ClericSpell spell){
        PetitioMagica.SpellHolder spellHolder;

        if ((spellHolder = cleric.buff(PetitioMagica.SpellHolder.class)) != null){
            return spellHolder.spell.getClass().isInstance(spell);
        }
        return false;
    }

    public static class SpellHolder extends FlavourBuff {

        {
            type = buffType.POSITIVE;
        }

        public ClericSpell spell;

        @Override
        public int icon() {
            return BuffIndicator.PETITIO_MAGICA;
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (duration() - visualcooldown()) / duration());
        }

        private static final String SPELL = "spell";

        @Override
        public String desc() {
            return Messages.get(this, "desc", Messages.titleCase(spell.name()), spell.desc(), dispTurns());
        }

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(SPELL, spell.getClass().getName());
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            spell = (ClericSpell) Reflection.newInstance(Reflection.forName(bundle.getString(SPELL)));
        }
    }
}

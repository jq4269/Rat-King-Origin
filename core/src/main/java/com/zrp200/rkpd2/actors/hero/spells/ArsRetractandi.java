package com.zrp200.rkpd2.actors.hero.spells;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Flare;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.Transmuting;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.RenderedTextBlock;
import com.zrp200.rkpd2.ui.TalentButton;
import com.zrp200.rkpd2.ui.TalentsPane;
import com.zrp200.rkpd2.ui.Window;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.IconTitle;
import com.zrp200.rkpd2.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.zrp200.rkpd2.actors.hero.Talent.*;

public class ArsRetractandi extends ClericSpell {
    public static final ArsRetractandi INSTANCE = new ArsRetractandi();

    @Override
    public int icon() {
        return HeroIcon.ARS_RETRACTANDI;
    }

    @Override
    public void tintIcon(HeroIcon icon) {
        // todo make icon
        if (SpellEmpower.isActive()) icon.tint(0, .33f);
    }

    @Override
    public float chargeUse(Hero hero) {
        int base = 5 - hero.pointsInTalent(Talent.ARS_RETRACTANDI);
        if (SpellEmpower.isActive())
            base *= 2;
        return base;
    }

    @Override
    public boolean canCast(Hero hero) {
        return super.canCast(hero) && hero.shiftedPoints(Talent.ARS_RETRACTANDI) > (SpellEmpower.isActive() ? 0 : 1);
    }

    @Override
    public void onCast(HolyTome tome, Hero hero) {
        if (SpellEmpower.isActive()){
            GameScene.show(new WndOptions(
                    new HeroIcon(this),
                    Messages.titleCase(name()),
                    Messages.get(ArsRetractandi.class, "select_tier"),
                    Messages.titleCase(Messages.get(TalentsPane.class, "tier", 1)),
                    Messages.titleCase(Messages.get(TalentsPane.class, "tier", 2)),
                    Messages.titleCase(Messages.get(TalentsPane.class, "tier", 3))
            ){
                @Override
                protected void onSelect(int index) {
                    super.onSelect(index);
                    Aspect mostCommonAspect;

                    if (index != -1){
                        hero.spend(1f);
                        hero.busy();
                        hero.sprite.operate(hero.pos);
                        TreeMap<Aspect, Integer> existingAspects = new TreeMap<>();
                        LinkedHashMap<Talent, Integer> decidedTier = Dungeon.hero.talents.get(index);

                        for (Map.Entry<Talent, Integer> talentData : decidedTier.entrySet()){
                            existingAspects.put(talentData.getKey().aspect,
                                    !existingAspects.containsKey(talentData.getKey().aspect) ?
                                            1 : existingAspects.get(talentData.getKey().aspect) + 1);
                        }
                        mostCommonAspect = existingAspects.lastEntry().getKey();
                        LinkedHashMap<Talent, Integer> newTier = new LinkedHashMap<>();
                        for (Map.Entry<Talent, Integer> talentData : decidedTier.entrySet()){
                            Talent newTalent;
                            Talent oldTalent = talentData.getKey();
                            while (true){
                                Talent testTalent = Random.element(Talent.values());
                                if (decidedTier.containsKey(testTalent))
                                    continue;
                                if (newTier.containsKey(testTalent))
                                    continue;
                                if (testTalent.aspect != mostCommonAspect)
                                    continue;
                                if (testTalent.maxPoints() == 3 && Random.Int(3) != 0)
                                    continue;
                                if (testTalent.maxPoints() == 4 && Random.Int(5) != 0)
                                    continue;
                                newTalent = testTalent;
                                break;
                            }
                            newTier.put(newTalent, Math.min(newTalent.maxPoints(), talentData.getValue()));
                            if (!hero.metamorphedTalents.containsValue(oldTalent)){
                                hero.metamorphedTalents.put(oldTalent, newTalent);

                                //if what we're replacing is already a value, we need to simplify the data structure
                            } else {
                                //a->b->a, we can just remove the entry entirely
                                if (hero.metamorphedTalents.get(newTalent) == oldTalent){
                                    hero.metamorphedTalents.remove(newTalent);

                                    //a->b->c, we need to simplify to a->c
                                } else {
                                    for (Talent t2 : hero.metamorphedTalents.keySet()){
                                        if (hero.metamorphedTalents.get(t2) == oldTalent){
                                            hero.metamorphedTalents.put(t2, newTalent);
                                        }
                                    }
                                }
                            }
                        }
                        hero.talents.set(index, newTier);

                        onSpellCast(tome, hero);

                        Sample.INSTANCE.play( Assets.Sounds.READ );
                        for (int i = 0; i < Dungeon.hero.talents.get(index).size(); i++){
                            Sample.INSTANCE.playDelayed(Assets.Sounds.LEVELUP, 0.1f + i * 0.1f, 0.7f, 1.2f);
                        }
                        new Flare( 6, 32 ).color(0xFFFF00, true).show( hero.sprite, 2f );
                        hero.sprite.emitter().start(Speck.factory(Speck.CHANGE), 0.1f, 25);
                        GLog.p(Messages.get(ArsRetractandi.class, "transmutation_complete", mostCommonAspect));
                    }
                }
            });
        } else {
            GameScene.show(new WndChoose());
        }
    }

    public static void onChange( Talent oldTalent, Talent newTalent ){
        Dungeon.hero.sprite.emitter().start(Speck.factory(Speck.CHANGE), 0.2f, 10);
        Transmuting.show(Dungeon.hero, oldTalent, newTalent);

        if (Dungeon.hero.hasTalent(newTalent)) {
            Talent.onTalentUpgraded(Dungeon.hero, newTalent);
        }

        Dungeon.hero.spend(1f);
        Dungeon.hero.busy();
        Dungeon.hero.sprite.operate(Dungeon.hero.pos);

        INSTANCE.onSpellCast(Dungeon.hero.belongings.getItem(HolyTome.class), Dungeon.hero);
    }

    public static class WndChoose extends Window {

        public static WndChoose INSTANCE;

        TalentsPane pane;

        public WndChoose(){
            super();

            INSTANCE = this;

            float top = 0;

            IconTitle title = new IconTitle(new HeroIcon(ArsRetractandi.INSTANCE), Messages.titleCase(Messages.get(ArsRetractandi.class, "name")));
            title.color( TITLE_COLOR );
            title.setRect(0, 0, 120, 0);
            add(title);

            top = title.bottom() + 2;

            RenderedTextBlock text = PixelScene.renderTextBlock(Messages.get(ArsRetractandi.class, "choose_desc"), 6);
            text.maxWidth(120);
            text.setPos(0, top);
            add(text);

            top = text.bottom() + 2;

            ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
            initClassTalents(Dungeon.hero.heroClass, talents, Dungeon.hero.metamorphedTalents);

            for (LinkedHashMap<Talent, Integer> tier : talents){
                for (Talent talent : tier.keySet()){
                    tier.put(talent, Dungeon.hero.pointsInTalent(talent));
                }
            }

            for (int i = 0; i < talents.size(); i++){
                LinkedHashMap<Talent, Integer> heroTalents = Dungeon.hero.talents.get(i);
                LinkedHashMap<Talent, Integer> lolTalents = talents.get(i);
                for (Talent talent : heroTalents.keySet()){
                    if (!lolTalents.containsKey(talent)){
                        if ((i == 3 &&
                                !(talent == HEROIC_RATINESS || talent == HEROIC_ARCHERY ||
                                        talent == HEROIC_ENDURANCE || talent == HEROIC_STAMINA || talent == HEROIC_WIZARDRY))){
                            continue;
                        }
                        if (i == 2) continue;
                        lolTalents.put(talent, Dungeon.hero.pointsInTalent(talent));
                    }
                }
            }

            pane = new TalentsPane(TalentButton.Mode.ASPECT_REROLL, talents);
            add(pane);
            pane.setPos(0, top);
            pane.setSize(120, pane.content().height());
            resize((int)pane.width(), (int)pane.bottom());
            pane.setPos(0, top);
        }

        @Override
        public void hide() {
            super.hide();
            INSTANCE = null;
        }

        @Override
        public void offset(int xOffset, int yOffset) {
            super.offset(xOffset, yOffset);
            pane.setPos(pane.left(), pane.top()); //triggers layout
        }
    }
}

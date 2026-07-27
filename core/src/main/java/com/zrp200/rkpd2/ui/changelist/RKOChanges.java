package com.zrp200.rkpd2.ui.changelist;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.ChangesScene;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.Image;

import java.util.ArrayList;

import static com.zrp200.rkpd2.actors.hero.HeroSubClass.*;
import static com.zrp200.rkpd2.actors.hero.Talent.*;
import static com.zrp200.rkpd2.messages.Messages.get;
import static com.zrp200.rkpd2.sprites.CharSprite.*;
import static com.zrp200.rkpd2.ui.Icons.INFO;
import static com.zrp200.rkpd2.ui.Icons.PREFS;
import static com.zrp200.rkpd2.ui.Icons.get;
import static com.zrp200.rkpd2.ui.Window.TITLE_COLOR;
import static java.util.Arrays.asList;

public enum RKOChanges {

    v1(() -> new ChangeInfo[][]{
        {
           new ChangeInfo("RKO-v1.0.0", true, TITLE_COLOR
                    ),
                    NewContent(
                            new ChangeButton(new Image(new HeroIcon(CHANNELER)), "Huntress's Secret Subclass",
                                    list("Replaced Warlock with a new secret subclass for Huntress, _Channeler_!",
                                            "_Channeler_ can covert its Spirit Bow into a unique ally that attacks on its own, with new talents that support it in various ways.",
                                            "The spirit bow doesn't attack if the Channeler also attacked during the same turn for balancing reasons... for now.",
                                            "Other classes cannot roll Channeler from Kromer Crown because it relies on the Spirit Bow.",
                                            "I'm expecting this subclass to be a bit more difficult to play than Sniper and the other subclasses, so I don't mind buffing it more once I get some feedback on it.",
                                            "The spawned spirit bow ally is internally called '_BowSpirit_', which is the name used in the Scroll Of Debug."
                                        )
                            )
                    ),
                    Changes(
                            
                            misc(list(
                                    "Changed title banner and app name/icon",
                                    "Updated GitHub link to new repository",
                                    "Removed Cenobite from Kromer Crown's subclass list"
                            )),
                            bugFixes("Caused by v1.0.0:\n"+list(
                                    "Spirit Bow disappearing when throwing it",
                                    "Spirit Bow disappearing when it dies",
                                    "Spirit Bow disappearing when it reloads a save",
                                    "... and many more bugs related to Spirit Bow and the new subclass before release"
                            ))
                    ),
                    Buffs(
                        new ChangeButton(WARDEN,
                            "Reworked some of Warden's talents: \n" +
                            list(
                                "Nature's Better Aid now spawns a friendly Rot Lasher when triggering Rejuvenating Steps instead of giving extra seeds and dew.",
                                "Indirect Benefits also applies buffs to the spawned Rot Lasher when the Warden steps on a plant."
                            )
                        )
                    )
        }
    });

    private final ChangeLog changes;
    RKOChanges(ChangeLog changes) { this.changes = changes; }

    @Override
    public String toString() {
        return name()
                .replace('$', ' ')
                .replace('_', '/');
    }

    public void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        for (ChangeInfo[] section : changes.getChanges()) changeInfos.addAll(asList(section));
    }

    // utility
    private static ChangeButton bugFixes(String... messages) {
        return new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), get(ChangesScene.class, "bugfixes"), messages);
    }

    private static ChangeButton misc(String... messages) {
        return new ChangeButton(get(PREFS), get(ChangesScene.class, "misc"), messages);
    }

    // section types
    private static ChangeInfo NewContent(ChangeButton... buttons) {
        return new ChangeInfo(
                Messages.get(ChangesScene.class, "new"),
                false, TITLE_COLOR,
                "",
                buttons);
    }

    private static ChangeInfo Buffs(ChangeButton... buttons) {
        return new ChangeInfo(
                Messages.get(ChangesScene.class, "buffs"),
                false, POSITIVE,
                "",
                buttons);
    }

    private static ChangeInfo Changes(ChangeButton... buttons) {
        return new ChangeInfo(
                Messages.get(ChangesScene.class, "changes"),
                false, WARNING, "",
                buttons);
    }

    private static ChangeInfo Nerfs(ChangeButton... buttons) {
        return new ChangeInfo(
                Messages.get(ChangesScene.class, "nerfs"),
                false, NEGATIVE,
                "",
                buttons);
    }

    private static ChangeButton info(String message) {
        return new ChangeButton(get(INFO), "Developer Commentary", message);
    }

    // more utils

    /**
     * makes a list in the standard PD style.
     * [lineSpace] determines the number of spaces between each list item.
     * If you want to append extra spaces, you should do it at the end of the previous item, rather than at the start of that item.
     */
    private static String list(String... items) {
        return list(1, items);
    }

    private static String list(int lineSpace, String... items) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < lineSpace; j++) builder.append('\n');
        for (String item : items) {
            builder.append("_-_ ").append(item);
            for (int j = 0; j < lineSpace; j++) builder.append('\n');
        }
        return builder.toString();
    }
}
package com.zrp200.rkpd2.sprites;

import com.watabou.noosa.TextureFilm;
import com.zrp200.rkpd2.Assets;

public class BowSpiritSprite extends MobSprite {

    public BowSpiritSprite() {
        super();

        texture(Assets.Sprites.BOW_SPIRIT);

        TextureFilm frames = new TextureFilm(texture, 16, 20);
		
        idle = new Animation(2, true);
        idle.frames(frames, 0, 0, 1, 0, 0, 2, 0);

        run = new Animation(5, true);
        run.frames(frames, 0, 1, 0, 2);

        attack = new Animation(10, false);
        attack.frames(frames, 0);

        die = new Animation(12, false);
        die.frames(frames, 3);

        play(idle);
    }
}

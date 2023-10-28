package moves;

import ru.ifmo.se.pokemon.*;

public class BulletSeed extends PhysicalMove {
    public BulletSeed() {
        super(Type.GRASS, 5, 100);
    }

    @Override
    protected void applySelfEffects(Pokemon p) {
        p.setMod(Stat.SPEED, -1);
    }

    @Override
    protected String describe() {
        return "is using Bullet Seed!";
    }
}

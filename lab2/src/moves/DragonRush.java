package moves;

import ru.ifmo.se.pokemon.*;

public class DragonRush extends PhysicalMove {
    public DragonRush() {
        super(Type.DRAGON, 120, 80);
    }

    @Override
    protected void applySelfEffects(Pokemon p) {
        p.setMod(Stat.SPECIAL_DEFENSE, -1);
    }

    @Override
    protected String describe() {
        return "is using Dragon Rush!";
    }
}
package moves;

import ru.ifmo.se.pokemon.*;

public class Facade extends PhysicalMove {
    public Facade() {
        super(Type.NORMAL, 70, 100);
    }

    @Override
    protected void applySelfEffects(Pokemon p) {
        p.setMod(Stat.ATTACK, 2);
    }

    @Override
    protected String describe() {
        return "is using Facade!";
    }
}

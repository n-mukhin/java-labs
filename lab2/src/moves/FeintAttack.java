package moves;

import ru.ifmo.se.pokemon.*;

public class FeintAttack extends PhysicalMove {
    public FeintAttack() {
        super(Type.NORMAL, 40, 100);
    }

    @Override
    protected void applyOppEffects(Pokemon p) {
        p.setMod(Stat.SPEED, 0);
    }

    @Override
    protected String describe() {
        return "is using Feint Attack!";
    }
}


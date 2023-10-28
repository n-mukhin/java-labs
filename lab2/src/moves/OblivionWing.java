package moves;

import ru.ifmo.se.pokemon.*;

public class OblivionWing extends SpecialMove {
    public OblivionWing() {
        super(Type.DARK, 180, 100);
    }

    @Override
    protected void applyOppEffects(Pokemon p) {
        p.setMod(Stat.HP, -75);
    }

    @Override
    protected String describe() {
        return "is using Oblivion Wing!";
    }
}


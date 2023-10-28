package moves;

import ru.ifmo.se.pokemon.*;

public class RockTomb extends PhysicalMove {
    public RockTomb() {
        super(Type.ROCK, 60, 80);
    }

    @Override
    protected void applySelfEffects(Pokemon p) {
        p.setMod(Stat.SPEED, -2);
    }

    @Override
    protected String describe() {
        return "is using Rock Tomb!";
    }
}

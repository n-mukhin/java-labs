package moves;

import ru.ifmo.se.pokemon.*;

public class AerialAce extends PhysicalMove {
    public AerialAce() {
        super(Type.FLYING, 60, 100);
    }

    @Override
    protected void applyOppEffects(Pokemon p) {
        p.setMod(Stat.SPEED, -1);
    }

    @Override
    protected String describe() {
        return "is using Aerial Ace!";
    }
}
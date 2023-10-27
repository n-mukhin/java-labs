package moves;

import ru.ifmo.se.pokemon.*;

public class BugBuzz extends SpecialMove {
    public BugBuzz() {
        super(Type.BUG, 90, 100);
    }

    @Override
    protected void applyOppEffects(Pokemon p) {
        p.setMod(Stat.SPECIAL_DEFENSE, -1);
    }

    @Override
    protected String describe() {
        return "is using Bug Buzz!";
    }
}

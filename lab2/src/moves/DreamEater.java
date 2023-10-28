package moves;

import ru.ifmo.se.pokemon.*;

public class DreamEater extends SpecialMove {
    public DreamEater() {
        super(Type.PSYCHIC, 100, 100);
    }


    @Override
    protected void applySelfEffects(Pokemon p) {
        p.setMod(Stat.HP, 50);
    }

    @Override
    protected String describe() {
        return "is using Dream Eater!";
    }
}


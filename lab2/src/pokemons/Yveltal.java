package pokemons;

import moves.*;
import ru.ifmo.se.pokemon.*;

public class Yveltal extends Pokemon {
    public Yveltal(String name, int level) {
        super(name, level);
        setStats(126, 131, 95, 131, 98, 99);
        setType(Type.DARK, Type.FLYING);
        setMove(new OblivionWing(), new DreamEater(), new DragonRush(), new AerialAce());
    }
}
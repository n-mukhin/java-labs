package pokemons;

import moves.*;
import ru.ifmo.se.pokemon.*;

public class Flygon extends Pokemon {
    public Flygon(String name, int level) {
        super(name, level);
        setStats(80, 100, 80, 80, 80, 100);
        setType(Type.GROUND, Type.DRAGON);
        setMove(new FeintAttack(), new RockTomb(), new BugBuzz(), new DracoMeteor());
    }
}
import pokemons.*;
import ru.ifmo.se.pokemon.*;

public class Main {
    public static void main(String[] args) {
        Battle b = new Battle ();
        Yveltal p1 = new Yveltal("Ивельталь", 5);
        Sunkern p2 = new Sunkern("Сункерн", 5);
        Sunflora p3 = new Sunflora("Подсолнечник", 5);
        Trapinch p4 = new Trapinch("Трапинч", 5);
        Vibrava p5 = new Vibrava("Вибрава", 5);
        Flygon p6 = new Flygon("Флайгон", 5);
        b.addAlly(p1);
        b.addAlly(p2);
        b.addAlly(p3);
        b.addFoe(p4);
        b.addFoe(p5);
        b.addFoe(p6);
        b.go();
    }
}
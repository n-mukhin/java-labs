import java.util.List;

public class Main {
    public static void main(String[] args) {
        Dreamer znayka = new Dreamer("Знайка");

        Rocket rocketInDream = new Rocket();
        String lunarGnomesInDream = "лунные коротышки";
        List<String> otherInterestingThingsInDream = List.of("Еще много разных интересных вещей");

        Journey journey = new Journey();
        Dream dream = new Dream(rocketInDream, lunarGnomesInDream, otherInterestingThingsInDream);
        znayka.setDream(dream);

        if (!znayka.hasGoneToSunCity()) {
            znayka.goToSunCity();
            journey.startJourney(znayka);
        }
    }
}




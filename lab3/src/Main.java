
import java.util.List;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Dreamer znayka = new Dreamer("Знайка");

        Rocket rocketInDream = new Rocket();
        String lunarGnomesInDream = "лунные коротышки";
        List<String> otherInterestingThingsInDream = List.of("Еще много разных интересных вещей");

        Journey journey = new Journey(znayka);;
        Dream dream = new Dream(rocketInDream, lunarGnomesInDream, otherInterestingThingsInDream);
        // Вывод текста из Dream
        dream.startDream(znayka);

        // Вывод текста из Dreamer
        znayka.goToSunCity();

        // Вывод текста из Journey
        if (znayka.isJourneySuccessful()) {
            journey.startJourney();
        } else {
        }


    }
}




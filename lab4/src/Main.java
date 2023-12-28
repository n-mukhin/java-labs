import java.util.List;

public class Main {

    public static void main(String[] args) {

        // Создаем объекты
        Dreamer znayka = new Dreamer("Знайка");
        Weightless weightless = new Weightless();
        Rocket rocket = new Rocket();
        Moon moon = new Moon();

        weightless.printText();
        System.out.println(new Weightless().getText());
        System.out.println(rocket.getText());
        System.out.println(moon.getText());

        moon.printTextWithVariation();

        // Создаем объекты классов `Rocket`, `String` и `List`.
        Rocket rocketInDream = new Rocket();
        String lunarGnomesInDream = "лунные коротышки";
        List<String> otherInterestingThingsInDream = List.of("Еще много разных интересных вещей");

        // Создаем объекты классов `Journey` и `Dream`.
        Journey journey = new Journey(znayka);
        Dream dream = new Dream(rocketInDream, lunarGnomesInDream, otherInterestingThingsInDream);



            dream.startDream(znayka);
            znayka.goToSunCity();

            // Проверяем, был ли успешным полет Знайки.
            // Если да, то запускаем путешествие.
            if (znayka.isJourneySuccessful()) {
                journey.startJourney();
            } else {
                // Бросаем исключение
                }
    }
}





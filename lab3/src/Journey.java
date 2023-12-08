import java.util.List;
import java.util.Random;

public class Journey {

    enum JourneyType {
        GROUND,
        AIR,
        SPACE,

    }

    private final Dreamer dreamer;
    private final JourneyType journeyType;
    private boolean hasGoneToSunCity;

    public Journey(Dreamer dreamer) {
        this.dreamer = dreamer;
        this.journeyType = JourneyType.values()[dreamer.getRandomNumber()];

    }

    public void startJourney(boolean hasGoneToSunCity) {
        // Проверяем, отправился ли Знайка уже в Солнечный город
        if (hasGoneToSunCity) {
            // Знайка не отправился в Солнечный город
            // Не выводим текст
        }
        else {
            // Знайка отправился в Солнечный город
            switch (journeyType) {
                case GROUND:
                    System.out.println(dreamer + " отправился в Солнечный город на своем личном автомобиле.");
                    break;
                case AIR:
                    System.out.println(dreamer + " отправился в Солнечный город на своем личном самолете.");
                    break;
                case SPACE:
                    System.out.println(dreamer + " отправился в Солнечный город на своей личной космической ракете.");
                    break;
            }
            // Проводим путешествие
            // Генерируем случайное значение для переменной successful
            boolean successful = new Random().nextBoolean();

            // Выводим сообщение об успешности или неудаче путешествия
            if (successful) {
                System.out.println(dreamer + " успешно добрался до Солнечного города.");
            }
            else {
                System.out.println(dreamer + " разбился.");
            }
        }
        // Знайка начал путешествие
        hasGoneToSunCity = true;
    }
}

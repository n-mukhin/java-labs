import java.util.Random;

public class Journey {

    enum JourneyType {
        GROUND,
        AIR,
        SPACE,
        EARTH
    }

    private final Dreamer dreamer;
    private final JourneyType journeyType;
    private boolean hasGoneToSunCity;

    public Journey(Dreamer dreamer) {
        this.dreamer = dreamer;
        this.journeyType = JourneyType.values()[new Random().nextInt(JourneyType.values().length)];

        // Устанавливаем значение hasGoneToSunCity в false по умолчанию
        this.hasGoneToSunCity = false;
    }

    public void startJourney() {
        int randomNumber2 = new Random().nextInt(2);
        switch (journeyType) {
            case GROUND:
                int randomNumber = new Random().nextInt(2);
                if (randomNumber == 1) {
                    this.hasGoneToSunCity = true;
                    System.out.println("Знайка отправился в Солнечный город на своем личном автомобиле.");
                } else {
                    this.hasGoneToSunCity = false;
                }
                break;
            case AIR:
                randomNumber = new Random().nextInt(2);
                if (randomNumber == 1) {
                    this.hasGoneToSunCity = true;
                    System.out.println("Знайка отправился в Солнечный город на своем личном самолете.");
                } else {
                    this.hasGoneToSunCity = false;
                }
                break;
            case SPACE:
                randomNumber = new Random().nextInt(2);
                if (randomNumber == 1) {
                    this.hasGoneToSunCity = true;
                    System.out.println("Знайка отправился в Солнечный город на своей личной космической ракете.");
                } else {
                    this.hasGoneToSunCity = false;
                }
                break;
            case EARTH:
                randomNumber2 = new Random().nextInt(2);
                if (randomNumber2 == 0) {
                    this.hasGoneToSunCity = true;
                    System.out.println("Знайка не добрался до Солнечного города. Он ошибся координатами полетел в строну Земли.");
                } else {
                    this.hasGoneToSunCity = false;
                }
                break;
        }
    }

    public boolean hasGoneToSunCity() {
        return hasGoneToSunCity;
    }
}










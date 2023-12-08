import java.util.Random;
public class Dreamer {
    public boolean journeySuccessful;
    public boolean hasGoneToSunCity;
    private String name;

    public Dreamer(String name) {
        this.name = name;
        this.journeySuccessful = false;
        this.hasGoneToSunCity = true;
        this.journeyStarted = true;
    }

    public void setJourneySuccessful(boolean journeySuccessful) {
        this.journeySuccessful = journeySuccessful;
    }

    @Override
    public String toString() {
        return name;
    }
    public int getRandomNumber() {
        Random random = new Random();
        return random.nextInt(2);
    }
    public void onJourney() {
        // Выводим изначальный текст
        System.out.println("А наутро Знайка исчез. К завтраку он не явился,");
        System.out.println("а когда коротышки пришли к нему в комнату, они увидели на столе записку,");
        System.out.println("на которой было всего три слова: \"В Солнечный город\", и подпись: \"Знайка\"");
        boolean journeyStarted = getRandomNumber() == 0;
        // Выводим дополнительную информацию
        if (!journeyStarted) {
            if (getRandomNumber() == 0) {
                this.hasGoneToSunCity = false;
                System.out.println("Знайка просто решил прогуляться.");
            }
            else {
                this.hasGoneToSunCity = false;
                System.out.println("Знайка исчез без следа.");
            }
        } else {
            // Знайка уже начал путешествие
            Journey journey = new Journey(this);
            journey.startJourney(true);

        }


        // Определяем результат путешествия
        this.hasGoneToSunCity();
    }
    // Возвращает значение свойства hasGoneToSunCity
    public boolean hasGoneToSunCity() {
           return this.hasGoneToSunCity;
    }
    public boolean journeyStarted; // указывает, началось ли путешествие
}



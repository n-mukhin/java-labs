import java.util.Random;

public class Dreamer {

    public Dream getDream() {
        return dream;
    }

    public String getName() {
        return name;
    }

    private String name;
    private Dream dream;
    private boolean hasGoneToSunCity;
    private boolean journeyStarted;

    public Dreamer(String name) {
        this.name = name;
        this.hasGoneToSunCity = false;
        this.journeyStarted = false;
    }

    public void setDream(Dream dream) {
        this.dream = dream;
    }

    public boolean hasGoneToSunCity() {
        // Логика определения, уехал ли Знайка в Солнечный город
        return journeyStarted && hasGoneToSunCity;
    }

    public void goToSunCity() {
        this.journeyStarted = true;
        Random random = new Random();
        int randomNumber = random.nextInt(3);
        switch (randomNumber) {
            case 0:
                onJourney();
                System.out.println(name + " решил просто пойти гулять.");
                this.hasGoneToSunCity = false;
                break;
            case 1:
                onJourney();
                System.out.println(name + " отправился в Солнечный город на машине.");
                this.hasGoneToSunCity = true;
                break;
            case 2:
                onJourney();
                System.out.println(name + " пропал безвести.");
                this.hasGoneToSunCity = false;
                break;
        }
    }

    private void onJourney() {
        // Логика вывода информации о путешествии
        System.out.println("А наутро " + name + " исчез. К завтраку он не явился,");
        System.out.println("а когда коротышки пришли к нему в комнату, они увидели на столе записку,");
        System.out.println("в которой было всего три слова: \"В Солнечный город\", и подпись:\"" + name + "\"");
    }

    @Override
    public String toString() {
        return "Знайка";
    }

    // Добавляем новый метод
    public boolean isJourneySuccessful() {
        return hasGoneToSunCity;
    }
}







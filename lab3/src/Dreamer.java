public class Dreamer {
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
        return false;
    }

    public void goToSunCity() {
        this.hasGoneToSunCity = true;
    }

    public Dream getDream() {
        return dream;
    }

    public boolean isJourneyStarted() {
        return journeyStarted;
    }

    public void startJourney() {
        this.journeyStarted = true;
    }

    @Override
    public String toString() {
        return "Знайка";
    }
}





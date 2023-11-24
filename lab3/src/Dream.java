import java.util.List;

// Класс Dream, представляющий сон
class Dream {
    private Rocket rocketInDream;
    private String lunarGnomesInDream;
    private List<String> otherInterestingThingsInDream;
    private boolean hasPrintedDreamInfo = false;

    public Dream(Rocket rocketInDream, String lunarGnomesInDream, List<String> otherInterestingThingsInDream) {
        this.rocketInDream = rocketInDream;
        this.lunarGnomesInDream = lunarGnomesInDream;
        this.otherInterestingThingsInDream = otherInterestingThingsInDream;
    }

    public void startDream(Dreamer dreamer) {
        if (!hasPrintedDreamInfo) {
            System.out.println("Размечтавшись, " + dreamer + " не заметил, как погрузился в сон.");
            System.out.println("И во сне " + this);
            hasPrintedDreamInfo = true;
        }
    }

    @Override
    public String toString() {
        return "ему снилась космическая ракета, и Луна, и лунные коротышки, и еще много разных интересных вещей.";
    }
}






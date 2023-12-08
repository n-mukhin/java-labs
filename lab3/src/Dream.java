import java.util.List;
import java.util.Random;

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
            Random random = new Random();
            int randomNumber = random.nextInt(5);
            switch (randomNumber) {
                case 0:
                    System.out.println("Размечтавшись, " + dreamer + " не заметил, как погрузился в сон.");
                    System.out.println("И во сне ему снился сон, в котором он летал на космическом корабле по галактике.");
                    break;
                case 1:
                    System.out.println("Размечтавшись, " + dreamer + " не заметил, как погрузился в сон.");
                    System.out.println("И во сне ему снилось, что он встретил инопланетян.");
                    break;
                case 2:
                    System.out.println("Размечтавшись, " + dreamer + " не заметил, как погрузился в сон.");
                    System.out.println("И во сне ему снилось, что он стал супергероем.");
                    break;
                case 3:
                    System.out.println("Размечтавшись, " + dreamer + " не заметил, как погрузился в сон.");
                    System.out.println("И во сне ему снилось, что он выиграл в лотерею.");
                    break;
                case 4:
                    System.out.println("Размечтавшись, " + dreamer + " не заметил, как погрузился в сон.");
                    System.out.println("И во сне ему снилось, что он побывал в прошлом.");
                    break;
                case 5:
                    System.out.println("Размечтавшись, " + dreamer + " не заметил, как задремал.");
                    break;
            }

            hasPrintedDreamInfo = true;
        }
    }
}







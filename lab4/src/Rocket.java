import java.util.Random;
// Класс Rocket, представляющий космическую ракету
public class Rocket {

    public String getText() {
        String text = "";

        text += "Ведь сейчас, чтоб разогнать ракету до нужной скорости, приходится брать огромнейший запас топлива;\n" +
                "если же ракета не будет ничего весить, то топлива понадобится совсем немного,\n" +
                "и вместо запасов топлива можно взять побольше пассажиров и побольше пищи для них.\n";

        // Создаем анонимный класс, который расширяет класс `StringBuilder`
        StringBuilder builder = new StringBuilder(text);

        // Создаем локальный класс, который реализует интерфейс `Randomizer`
        class Randomizer {

            public String getRandomVariation() {
                int randomNumber = new Random().nextInt(5);

                switch (randomNumber) {
                    case 0:
                        return "В таком случае можно будет летать дальше и быстрее.";
                    case 1:
                        return "В таком случае можно будет перевозить больше людей и грузов.";
                    case 2:
                        return "В таком случае можно будет проводить более длительные космические экспедиции.";
                    case 3:
                        return "В таком случае можно будет осуществлять более частые запуски ракет.";
                    case 4:
                        return "В таком случае можно будет колонизировать другие планеты.";
                }
                return "";
            }
        }

        // Получаем вариант текста из локального класса
        Randomizer randomizer = new Randomizer();
        String variation = randomizer.getRandomVariation();

        // Добавляем вариант текста к строке
        builder.append(variation);

        return builder.toString();
    }
}
import java.util.Random;

public class Moon {

    // Поле для хранения объекта класса Random
    private Random random = new Random();

    // Конструктор класса
    public Moon() {
    }

    // Метод для получения текста о Луне
    public String getText() {


        String text = "Вот когда можно будет совершить длительную экспедицию на " + getRandomPlanet() + ".\n";
        text += "проникнуть в её недра и, может быть, даже познакомиться с " + getRandomCreature() + ".";

        return text;
    }

    // Метод для получения случайного существа
    private String getRandomCreature() {

        // Создаем массив существ
        String[] creatures = {"лунными коротышками", "марсианами", "инопланетянами", "рептилоидами", "пришельцами", "чужими"};

        // Получаем случайное существо
        return creatures[random.nextInt(6)];
    }

    // Метод для получения случайной планеты
    private String getRandomPlanet() {

        // Возвращаем случайную планету из класса Planets
        return Planets.getRandomPlanet();
    }

    // Метод для проверки исключений
    public void checkExceptions() {

        // Проверяем checked исключение
        try {
            throw new MyCheckedException("Это checked исключение");
        } catch (MyCheckedException e) {
            // Обрабатываем checked исключение
            System.out.println("Обработка checked исключения: " + e.getMessage());
        }

        // Проверяем unchecked исключение
        try {
            throw new MyUncheckedException("Это unchecked исключение");
        } catch (MyUncheckedException e) {
            // Обрабатываем unchecked исключение
            System.out.println("Обработка unchecked исключения: " + e.getMessage());
        }
    }

    // Метод для вывода текста с вариацией
    public void printTextWithVariation() {

        int randomNumber = random.nextInt(3);

        // В зависимости от случайного числа выводим один из вариантов текста
        switch (randomNumber) {
            case 0:
                System.out.println("А еще можно будет построить базу.");
                break;
            case 1:
                System.out.println("Или начать добывать ресурсы, которые могут быть очень ценными.");
                break;
            case 2:
                System.out.println("А может быть, даже создать целую колонию.");
                break;
        }
    }

    // Статический класс для получения случайной планеты
    static class Planets {

        // Поле для хранения объекта класса Random
        private static Random random = new Random();

        // Массив планет
        public static final String[] PLANETS = {"Луну", "Меркурий", "Марс", "Европу, спутник Юпитера", "Планету Х"};

        // Метод для получения случайной планеты
        public static String getRandomPlanet() {

            // Получаем случайное число от 0 до 4
            int randomNumber = random.nextInt(5);

            // Возвращаем планету с соответствующим индексом
            return PLANETS[randomNumber];
        }
    }
}

// Класс для проверки checked исключений
class MyCheckedException extends Exception {

    // Конструктор класса
    public MyCheckedException(String message) {
        super(message);
    }
}

// Класс для проверки unchecked исключений
class MyUncheckedException extends RuntimeException {

    // Конструктор класса
    public MyUncheckedException(String message) {
        super(message);
    }
}

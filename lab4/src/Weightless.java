import java.util.Random;
import java.lang.reflect.InvocationTargetException;

public class Weightless {

    private String text = "В эту ночь Знайка долго не мог заснуть:\n" +
            "все думал, какую пользу может принести состояние невесомости.";

    public void printText() {
        System.out.println(text);
    }

    public double randomValue;

    public Weightless() {
        // Генерация случайного числа для определения класса для вывода текста
        randomValue = Math.random();
    }

    public static String getText() {
// Определение класса для вывода текста
        Class<?> clazz = null;
        double randomValue = Math.random();

        if (randomValue < 0.3) {
            // Создаем экземпляр статического вложенного класса,
            // который реализует TextProvider
            clazz = StaticNestedTextProvider.class;
        } else if (randomValue < 0.6) {
            // Создаем экземпляр вложенного класса
            clazz = NonStaticNestedClass.class;

        } else {
            // Возвращаем значение по умолчанию
            return "Гравитации не существует, математика лже-наука.";
        }
        // Вывод текста
        try {
            return ((TextProvider) clazz.newInstance()).getText();
        } catch (InstantiationException | IllegalAccessException e) {
            // Обработка исключений
            String text = "Гравитации не существует, математика лже-наука.";
            // Возврат значения по умолчанию
            return text;
        }
    }


    // Интерфейс для получения текста
    interface TextProvider {
        String getText();
    }

    // Статический вложенный класс
    static class StaticNestedTextProvider implements TextProvider {

        public static String text = "Как хорошо, что ее можно ощутить не только в космосе.";

        @Override
        public String getText() {
            return text;
        }
    }


    // Анонимный класс
    static class StaticNestedClass implements TextProvider {
        public String getText() {
            // Всегда возвращаем первое предложение
            String firstText = "Невесомость – это огромная сила, если знать, как подступиться к ней, – размышлял он.\n";

            // Генерация случайного числа от 0 до 4
            int randomNumber = new Random().nextInt(5);

            // Возврат соответствующего предложения
            switch (randomNumber) {
                case 0:
                    return firstText +
                            "– С помощью невесомости можно поднимать и передавать огромные тяжести.";
                case 1:
                    return firstText +
                            "– А с большой силой приходит большая ответственность.";
                case 2:
                    return firstText +
                            "– Она дает возможность бесконечного полета.";
                case 3:
                    return firstText +
                            "– Она открывает путь к новым технологиям.";
                case 4:
                    return firstText +
                            "За ней будущее человечества.";
                default:
                    return null;
            }

        }
    }

    public class WeightlessException extends RuntimeException {
        private String message;

        public WeightlessException(String message) {
            super(message);
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    // Вложенный класс (нестатический)
    public class NonStaticNestedClass implements TextProvider {

        public NonStaticNestedClass() {
            // В конструкторе не нужно бросать исключение NoSuchMethodException
        }

        public NonStaticNestedClass(Object obj) {
            // В этом конструкторе мы сохраняем значение параметра obj в поле name
            this.name = obj.toString();
        }

        @Override
        public String getText() throws WeightlessException {
            // В данном примере бросаем исключение класса WeightlessException
            throw new WeightlessException("Исключение, связанное с состоянием невесомости");
        }

        private String name;
    }
}

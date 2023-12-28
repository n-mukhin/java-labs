import java.util.Random;

public class Weightless {

    private String texttext = "В эту ночь Знайка долго не мог заснуть:\n" +
            "все думал, какую пользу может принести состояние невесомости.";

    public void printText() {
        // Создаем анонимный класс, который будет выполнять метод printText()
        new Object() {
            public void run() {
                System.out.println(texttext);
            }
        }.run();
    }

    public double randomValue;

    public Weightless() {
        // Генерация случайного числа для определения класса для вывода текста
        randomValue = Math.random();
    }

    public String getText() {
        // Определение класса для вывода текста
        double randomValue = Math.random();

        //Локальный класс
        class TextProvider {
            public String getText() {
                if (randomValue < 0.3) {
                    return StaticNestedTextProvider.text;
                } else if (randomValue < 0.6) {
                    return new NonStaticNestedClass(new Object()).getText();
                } else {
                    return "Гравитации не существует, математика лже-наука.";
                }
            }
        }

        return new TextProvider().getText();
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


    // Статический вложенный класс
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

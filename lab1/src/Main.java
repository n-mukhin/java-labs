public class Main {
    public static void main(String[] args) {
        // Создаем массив c типа long размером 11 элементов
        long[] c = new long[11];

        // Инициализируем переменную num значением 21
        int num = 21;

        // Заполняем массив нечетными числами от 21 до 1 включительно в порядке убывания
        for (int i = 0; i < 11; i++) {
            c[i] = num;
            num -= 2;
        }

        // Создаем массив x для хранения 20 случайных чисел
        float[] x = new float[20];

        // Заполняем массив x случайными числами в диапазоне от -15.0 до 12.0
        for (int i = 0; i < x.length; i++) {
            // Генерируем случайное число с использованием Math.random() и масштабируем его в нужный диапазон
            float randomNumber = (float) (Math.random() * (12.0 - (-15.0)) - 15.0);
            x[i] = randomNumber;
        }
        int gSizeI = 11; // Задаем количество строк массива g
        int gSizeJ = 20; // Задаем количество столбцов массива g
        double[][] g = new double[gSizeI][gSizeJ]; // Создаем двумерный массив g

        for (int i = 0; i < gSizeI; i++) { // Перебираем строки массива
            for (int j = 0; j < gSizeJ; j++) { // Перебираем столбцы массива
                if (c[i] == 15) { // Если элемент c[i] равен 15
                    // Вычисляем значение элемента g[i][j] с использованием математических операций
                    g[i][j] = Math.pow(Math.pow(Math.exp(x[i]) / 2, 2), Math.sin(x[i]) * ((Math.pow(x[i], (x[i] / (x[i] + 0.5))) + 1) / 2));
                } else if (c[i] == 3 || c[i] == 5 || c[i] == 17 || c[i] == 19 || c[i] == 21) {
                    // Вычисляем значение элемента g[i][j] с другой формулой
                    g[i][j] = Math.pow(4 + Math.sqrt(Math.pow(x[i], 2 / (1 - x[i]))), 3) / (0.5 * Math.log10(Math.abs(x[i]))) * Math.log10(Math.pow(Math.abs(x[i]) / (3 + Math.abs(x[i])), 2));
                } else {
                    // Вычисляем значение элемента g[i][j] с третьей формулой
                    g[i][j] = Math.atan(Math.exp(Math.sqrt(Math.pow((-1) * Math.sin(x[i]), 2)) / 2));
                }
            }
        }

        for (int i = 0; i < gSizeI; i++) { // Перебираем строки массива
            for (int j = 0; j < gSizeJ; j++) { // Перебираем столбцы массива
                System.out.printf("%.4f ", g[i][j]); // Выводим элемент g[i][j] с четырьмя знаками после запятой
            }
            System.out.println(); // Переход на новую строку после вывода всех элементов в строке
        }
    }
}

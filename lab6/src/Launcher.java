import java.util.Scanner;

public class Launcher {
    private static volatile boolean running = true;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String[] mainClasses = {
                "Server.MainServer",
                "Client.MainClient"
        };

        System.out.println("Выберите класс для запуска:\n");

        for (int i = 0; i < mainClasses.length; i++) {
            System.out.println((i + 1) + ". " + mainClasses[i]);
        }

        // Печать строки запроса
        System.out.print("\nВведите число для выбора:\n");

        // Запуск потока для мигающего символа
        Thread blinkThread = new Thread(() -> {
            while (running) {
                try {
                    System.out.print(">");
                    Thread.sleep(500);
                    System.out.print("\b \b");

                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        blinkThread.start();

        int choice = scanner.nextInt();
        running = false; // Остановить мигающий поток
        scanner.nextLine(); // чтобы прочитать символ новой строки после ввода числа

        // Очистка строки после выбора
        System.out.print("\rВведите число для выбора: " + choice + "  \n");

        if (choice < 1 || choice > mainClasses.length) {
            System.out.println("Неверный выбор, программа завершена.");
            return;
        }

        String className = mainClasses[choice - 1];
        try {
            Class<?> clazz = Class.forName(className);
            java.lang.reflect.Method mainMethod = clazz.getMethod("main", String[].class);
            String[] params = {}; // аргументы командной строки
            mainMethod.invoke(null, (Object) params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
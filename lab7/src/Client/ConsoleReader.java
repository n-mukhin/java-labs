package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleReader {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    // Метод для чтения строки из консоли
    public static String readLine() {
        String input = null;
        try {
            input = reader.readLine();

            // Handle EOF (Ctrl+D) by returning null
            if (input == null) {
                return null;
            }

            // Check for Linux command protection
            if (input.trim().startsWith("sudo") || input.trim().startsWith("rm") || input.trim().startsWith("shutdown")) {
                System.out.println("Вы ввели недопустимую команду.");
                return null;
            }

        } catch (IOException e) {
            System.err.println("Ошибка чтения из консоли: " + e.getMessage());
        }
        return input;
    }

    // Метод для закрытия потока ввода
    public static void close() {
        try {
            reader.close();
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии потока ввода: " + e.getMessage());
        }
    }
}

import Collection.CollectionManager;
import EnvKey.EnvKey;
import ConsoleReader.ConsoleReader;
import FileHandler.FileHandler;
import Filler.Filler;
import CommandManager.CommandManager;
import Collection.Vehicle;

import java.io.IOException;
import java.util.PriorityQueue;

public class Main {
    /**
     * Метод, запускающий программу для управления коллекцией транспортных средств.
     * Программа предназначена для чтения и обработки команд пользователя.
     *
     * @param args Параметры командной строки (не используются)
     * @throws IOException            Если происходит ошибка ввода-вывода при чтении данных
     * @throws ClassNotFoundException Если класс не найден при чтении данных
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // Инициализация объектов для чтения данных с консоли, заполнения коллекции и управления коллекцией
        ConsoleReader reader = new ConsoleReader();
        Filler filler = new Filler();
        CollectionManager collection = new CollectionManager();
        boolean eofMessageDisplayed = false; // Флаг для отслеживания сообщения о конце файла (EOF)

        // Запрос пользователя ввести имя ключа окружения
        String userInput; // Объявляем переменную здесь

        // Запрос пользователя ввести имя ключа окружения
        do {
            System.out.print("Enter the name of the environment key: ");
            userInput = reader.readLine();

            // Проверяем наличие Ctrl+D (EOF)
            if (userInput == null) {
                System.out.println("Received end-of-file (Ctrl+D). Program terminated.");
                return; // Просто завершаем программу
            }
        } while (userInput.isEmpty() || System.getenv(userInput) == null);

        // Создаем экземпляр EnvKey и устанавливаем ключ окружения
        EnvKey envKey = new EnvKey(userInput);
        // Проверяем существует ли envKey
        if (envKey == null || envKey.getKey().isEmpty() || System.getenv(envKey.getKey()) == null) {
            // Вводим переменную для хранения введенного пользователем значения
            String newEnvKeyInput;

            // Запрос у пользователя ввода существующего envKey
            do {
                System.out.print("Enter the name of the existing environment key: ");
                newEnvKeyInput = reader.readLine();

                // Проверяем наличие Ctrl+D (EOF)
                if (newEnvKeyInput == null) {
                    System.out.println("Received end-of-file (Ctrl+D). Please enter a valid environment key or exit.");
                    return; // Просто завершаем программу
                }
            } while (newEnvKeyInput.isEmpty() || System.getenv(newEnvKeyInput) == null);

            // Устанавливаем введенное значение как новый envKey
            envKey = new EnvKey(newEnvKeyInput);
        }

        // Создаем экземпляр CommandManager с установленным envKey
        CommandManager manager = new CommandManager(collection, filler, envKey);

        try {
            // Читаем транспортные средства из файла, используя envKey
            PriorityQueue<Vehicle> loadedVehicles = FileHandler.readFromFile(envKey);
            if (loadedVehicles != null) {
                collection.vehicles = loadedVehicles;
            } else {
                System.out.println("File " + envKey.getKey() + " is empty. Creating a new collection of vehicles.");
            }
            System.out.println("Welcome to Cars 4!");
            System.out.println("Type \"help\" for assistance.");
        } catch (Exception e) {
            System.out.println("Failed to load collection. Creating a new collection of vehicles.");
        }

        // Цикл обработки команд пользователя
        while (true) {
            if (!eofMessageDisplayed) {
                System.out.print("> ");
            }
            String commandLine = reader.readLine();

            // Обработка Ctrl+D (EOF)
            if (commandLine == null) {
                if (!eofMessageDisplayed) {
                    System.out.println("Received end-of-file (Ctrl+D). Please enter a command or 'exit' to quit.");
                    eofMessageDisplayed = true;
                    continue; // Продолжаем цикл для запроса новой команды
                } else {
                    // Если Ctrl+D нажат снова после отображения сообщения EOF,
                    // завершаем цикл для завершения программы
                    break;
                }
            } else {
                eofMessageDisplayed = false; // Сброс флага при получении допустимого ввода
            }

            if ("exit".equals(commandLine)) {
                break;
            }

            if (commandLine != null && !commandLine.isEmpty()) {
                System.out.print("> ");
                if (commandLine.equals("exit")) {
                    break;
                }
                try {
                    manager.executeCommand(commandLine);
                } catch (Exception e) {
                    System.err.println("Ошибка: " + e.getMessage());
                }
            }
        }
    }
}

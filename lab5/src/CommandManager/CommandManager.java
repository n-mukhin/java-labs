package CommandManager;

import Collection.*;
import FileHandler.*;
import ConsoleReader.ConsoleReader;
import Filler.*;
import EnvKey.EnvKey;
import Validator.Validator;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Set;

public class CommandManager {

        private CollectionManager collectionManager;
        private Filler filler;
        EnvKey envKey;

        public CommandManager(CollectionManager collectionManager, Filler filler, EnvKey envKey) {
            this.collectionManager = collectionManager;
            this.filler = filler;
            this.envKey = envKey;
        }


    // Метод для обработки команд пользователя
    public void executeCommand(String command) {
        try {
            String[] parts = command.split("\\s+", 2);
            String commandName = parts[0];
            String commandArgs = parts.length > 1 ? parts[1] : "";

            switch (commandName) {
                case "help":
                    showHelp();
                    break;
                case "info":
                    showCollectionInfo();
                    break;
                case "show":
                    showAllVehicles();
                    break;
                case "add":
                    addVehicle(commandArgs);
                    break;
                case "update_id":
                    updateVehicle(commandArgs);
                    break;
                case "remove_by_id":
                    removeVehicleById(commandArgs);
                    break;
                case "clear":
                    clearCollection();
                    break;
                case "save":
                    saveCollectionToFile();
                    break;
                case "exit":
                    exitProgram();
                    break;
                case "execute_script":
                    executeScript(commandArgs);
                    break;
                case "remove_first":
                    removeFirstVehicle();
                    break;
                case "remove_head":
                    removeHeadVehicle();
                    break;
                case "remove_greater":
                    removeVehiclesGreaterThan(commandArgs);
                    break;
                case "sum_of_capacity":
                    sumOfCapacities();
                    break;
                case "print_field_ascending_engine_power":
                    printEnginePowerAscending();
                    break;
                case "print_field_descending_fuel_type":
                    printFuelTypeDescending();
                    break;
                default:
                    System.out.println("Неизвестная команда. Напишите 'help' чтобы посмотреть доступные команды");
                    break;
            }
        } catch (NoSuchElementException e) {
            System.out.println("Программа завершена. Введен конец файла (Ctrl+D или EOF).");
            exitProgram(); // Assuming you have an exitProgram method to gracefully exit the program.
        }
    }

    // Метод для вывода справки по доступным командам
    private void showHelp() {
        System.out.println("Доступные команды:");
        System.out.println("help : вывести справку по доступным командам");
        System.out.println("info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)");
        System.out.println("show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
        System.out.println("add {element} : добавить новый элемент в коллекцию");
        System.out.println("update id {element} : обновить значение элемента коллекции, id которого равен заданному");
        System.out.println("remove_by_id id : удалить элемент из коллекции по его id");
        System.out.println("clear : очистить коллекцию");
        System.out.println("save : сохранить коллекцию в файл");
        System.out.println("exit : завершить программу (без сохранения в файл)");
        System.out.println("execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.");
        System.out.println("remove_first : удалить первый элемент из коллекции");
        System.out.println("remove_head :  вывести первый элемент коллекции и удалить его");
        System.out.println("remove_greater {element} : удалить из коллекции все элементы, превышающие заданный");
        System.out.println("sum_of_capacity : вывести сумму значений поля capacity для всех элементов коллекции");
        System.out.println("print_field_ascending_engine_power : вывести значения поля enginePower всех элементов в порядке возрастания");
        System.out.println("print_field_descending_fuel_type : вывести значения поля fuelType всех элементов в порядке убывания");
    }

    private void clearCollection() {
        collectionManager.clearCollection();
    }

    private void showAllVehicles() {
        collectionManager.showAllVehicles();
    }
    // Метод для вывода информации о коллекции
    private void showCollectionInfo() {
        System.out.println("Тип коллекции: PriorityQueue<Vehicle>");
        System.out.println("Количество элементов: " + collectionManager.getCollectionSize());
        System.out.println("Дата инициализации: " + collectionManager.getInitializationDate());
    }

    // Метод для добавления нового транспортного средства в коллекцию
    private void addVehicle(String name) {
        ConsoleReader reader = new ConsoleReader();
        Vehicle vehicle = filler.readVehicle(reader, name, envKey);
        if (vehicle != null) {
            collectionManager.addVehicle(vehicle);
            // Сохранение коллекции в файл после добавления нового элемента
            FileHandler.writeToFile(envKey, collectionManager.getVehicles());
            System.out.println("Машина успешно добавлена.");
        } else {
            System.out.println("Не удалось добавить машину. Неправильный формат ввода");
        }
    }

    // Метод для обновления информации о транспортном средстве в коллекции
    public void updateVehicle(String commandArgs) {
        ConsoleReader reader = new ConsoleReader();
        try {
            // Извлекаем идентификатор из строки команды
            String idString = commandArgs.trim(); // Удаляем лишние пробелы
            // Парсим идентификатор
            long id = Long.parseLong(idString);

            // Запрашиваем у пользователя имя нового транспортного средства
            System.out.println("Введите имя нового транспортного средства:");
            String newVehicleName = reader.readLine();

            // Считываем новую информацию о транспортном средстве
            System.out.println("Введите новую информацию о транспортном средстве:");
            Vehicle updatedVehicle = filler.readVehicle(reader, newVehicleName, envKey); // Передаем имя нового транспортного средства

            if (updatedVehicle != null) {
                // Вызываем метод updateVehicle из CollectionManager для обновления транспортного средства
                collectionManager.updateVehicle(id, updatedVehicle);
                // Также можем сохранить изменения в файл, если необходимо
                FileHandler.writeToFile(envKey, collectionManager.getVehicles());
                System.out.println("Транспортное средство с ID " + id + " успешно обновлено.");
            } else {
                System.out.println("Не удалось обновить транспортное средство. Неверный формат ввода.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID транспортного средства. Пожалуйста, введите корректный номер.");
        }
    }


    // Метод для удаления транспортного средства из коллекции по его id
    private void removeVehicleById(String commandArgs) {
        long id = Long.parseLong(commandArgs);
        collectionManager.removeVehicleById(id);
        System.out.println("Машина с id " + id + " успешно удалена.");
    }

    // Метод для сохранения коллекции в файл
    private void saveCollectionToFile() {
        FileHandler.writeToFile(envKey, collectionManager.getVehicles());
        System.out.println("Коллекция сохранена в фаил.");
    }


    // Метод для удаления и вывода первого транспортного средства из коллекции
    private void removeHeadVehicle() {
        Vehicle headVehicle = collectionManager.removeFirstVehicle();
        if (headVehicle != null) {
            System.out.println("First vehicle removed: " + headVehicle);
        } else {
            System.out.println("Collection is empty. No vehicle removed.");
        }
    }

    // Метод для удаления транспортных средств с мощностью двигателя больше указанной
    private void removeVehiclesGreaterThan(String commandArgs) {
        try {
            double threshold = Double.parseDouble(commandArgs);
            int removedCount = collectionManager.removeVehiclesWithEnginePowerGreaterThan(threshold);
            System.out.println("Удалено " + removedCount + " транспортных средств с мощностью двигателя больше " + threshold);
        } catch (NumberFormatException e) {
            System.out.println("Неверное значение порога. Пожалуйста, введите корректное число.");
        }
    }

    // Метод для вычисления суммы значений поля вместимость для всех транспортных средств в коллекции
    private void sumOfCapacities() {
        double sum = collectionManager.sumOfCapacities();
        System.out.println("Сумма вместимостей: " + sum);
    }

    // Метод для вывода значений поля мощность двигателя всех транспортных средств в коллекции в порядке возрастания
    private void printEnginePowerAscending() {
        collectionManager.printEnginePowerAscending();
    }

    // Метод для вывода значений поля тип топлива всех транспортных средств в коллекции в порядке убывания
    private void printFuelTypeDescending() {
        collectionManager.printFuelTypeDescending();
    }
    // Метод для выхода из программы
    private void exitProgram() {
        System.exit(0);
    }

    // Метод для удаления первого транспортного средства из коллекции
    private void removeFirstVehicle() {
        Vehicle removedVehicle = collectionManager.removeFirstVehicle();
        if (removedVehicle != null) {
            System.out.println("Первое транспортное средство успешно удалено: " + removedVehicle);
        } else {
            System.out.println("Коллекция пуста. Ни одно транспортное средство не удалено.");
        }
    }
    // Метод для выполнения скрипта из файла
    private Set<String> executedScripts = new HashSet<>();


        private String scriptDirectory = "/home/studs/s409203/java-labs/lab5/"; // specify the directory where scripts are stored

    public void executeScript(String fileName) {
        if (executedScripts.contains(fileName)) {
            System.err.println("Script '" + fileName + "' is already executed. Skipping to prevent recursion.");
            return;
        }
        executedScripts.add(fileName);
        File scriptFile = new File(scriptDirectory + fileName);
        if (!scriptFile.exists()) {
            System.err.println("Script '" + fileName + "' not found.");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(scriptFile))) {
            String line;
            PriorityQueue<Vehicle> existingVehicles = FileHandler.readFromFile(envKey);
            long currentId = existingVehicles.isEmpty() ? 1L : existingVehicles.peek().getId() + 1;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                String commandArgs = parts.length > 1 ? parts[1] : "";

                if (parts.length == 0) {
                    System.err.println("Invalid command: " + line);
                    continue;
                }

                switch (parts[0]) {
                    case "help":
                        showHelp();
                        break;
                    case "info":
                        showCollectionInfo();
                        break;
                    case "show":
                        showAllVehicles();
                        break;
                    case "remove_by_id":
                        removeVehicleById(commandArgs);
                        break;
                    case "clear":
                        clearCollection();
                        break;
                    case "save":
                        saveCollectionToFile();
                        break;
                    case "exit":
                        exitProgram();
                        break;
                    case "execute_script":
                        if (parts.length < 2) {
                            System.err.println("Usage: execute_script <script_file>");
                            continue;
                        }
                        // Execute the script recursively
                        String scriptFileName = parts[1];
                        executeScript(scriptFileName);
                        break;
                    case "remove_first":
                        removeFirstVehicle();
                        break;
                    case "remove_head":
                        removeHeadVehicle();
                        break;
                    case "remove_greater":
                        removeVehiclesGreaterThan(commandArgs);
                        break;
                    case "sum_of_capacity":
                        sumOfCapacities();
                        break;
                    case "print_field_ascending_engine_power":
                        printEnginePowerAscending();
                        break;
                    case "print_field_descending_fuel_type":
                        printFuelTypeDescending();
                        break;
                    case "add":
                        if (parts.length >= 2) {
                            String name = parts[1];
                            if (!Validator.validateName(name)) {
                                continue;
                            }

                            String coordinatesLine = reader.readLine();
                            if (coordinatesLine == null) {
                                System.err.println("Отсутсвуют координаты");
                                continue;
                            }
                            String[] coordinatesParts = coordinatesLine.split(",");
                            if (coordinatesParts.length != 2) {
                                System.err.println("Неправильный формат ввода координат.");
                                continue;
                            }
                            float x = Float.parseFloat(coordinatesParts[0]);
                            float y = Float.parseFloat(coordinatesParts[1]);
                            Coordinates coordinates = new Coordinates(x, y);
                            if (!Validator.validateCoordinates(coordinates)) {
                                continue;
                            }

                            String enginePowerLine = reader.readLine();
                            if (enginePowerLine == null) {
                                System.err.println("Нету мощности двигателя.");
                                continue;
                            }
                            double enginePower = Double.parseDouble(enginePowerLine);
                            if (!Validator.validateEnginePower(enginePower)) {
                                continue;
                            }

                            Double capacity = null;
                            String capacityLine = reader.readLine();
                            if (capacityLine != null && !capacityLine.isEmpty()) {
                                capacity = Double.parseDouble(capacityLine);
                                if (!Validator.validateCapacity(capacity)) {
                                    continue;
                                }
                            }

                            Float distanceTravelled = null;
                            String distanceTravelledLine = reader.readLine();
                            if (distanceTravelledLine != null && !distanceTravelledLine.isEmpty()) {
                                distanceTravelled = Float.parseFloat(distanceTravelledLine);
                                if (!Validator.validateDistanceTravelled(distanceTravelled)) {
                                    continue;
                                }
                            }

                            String fuelTypeLine = reader.readLine();
                            FuelType fuelType = null;
                            if (fuelTypeLine != null && !fuelTypeLine.isEmpty()) {
                                int choice = Integer.parseInt(fuelTypeLine);
                                if (choice > 0 && choice <= FuelType.values().length) {
                                    fuelType = FuelType.values()[choice - 1];
                                }
                            }

                            long id = currentId++; // Генерация уникального ID
                            Vehicle vehicle = new Vehicle(id, name, coordinates, java.time.LocalDate.now(), enginePower, capacity, distanceTravelled, fuelType);
                            collectionManager.addVehicle(vehicle);
                            break;
                        }
                    case "update_id":
                        if (parts.length >= 2 & parts[0].equals("update_id")) {
                            long id = Long.parseLong(parts[1]);

                            // Читаем следующую строку для имени транспортного средства
                            String name = reader.readLine();
                            if (name == null) {
                                System.err.println("Имя отсутсвует.");
                                continue;
                            }
                            if (!Validator.validateName(name)) {
                                continue;
                            }

                            // Читаем следующую строку для координат
                            String coordinatesLine = reader.readLine();
                            if (coordinatesLine == null) {
                                System.err.println("Координаты отсутсвуют.");
                                continue;
                            }

                            // Парсим координаты
                            String[] coordinatesParts = coordinatesLine.split(",");
                            if (coordinatesParts.length != 2) {
                                System.err.println("Неправильный формат данных.");
                                continue;
                            }
                            float x = Float.parseFloat(coordinatesParts[0]);
                            float y = Float.parseFloat(coordinatesParts[1]);
                            Coordinates coordinates = new Coordinates(x, y);
                            if (!Validator.validateCoordinates(coordinates)) {
                                continue;
                            }

                            // Читаем строку для мощности двигателя
                            String enginePowerLine = reader.readLine();
                            if (enginePowerLine == null) {
                                System.err.println("Нету мощности двигателя.");
                                continue;
                            }
                            double enginePower = Double.parseDouble(enginePowerLine);
                            if (!Validator.validateEnginePower(enginePower)) {
                                continue;
                            }

                            // Читаем строку для вместимости
                            String capacityLine = reader.readLine();
                            Double capacity = null;
                            if (capacityLine != null && !capacityLine.isEmpty()) {
                                capacity = Double.parseDouble(capacityLine);
                                if (!Validator.validateCapacity(capacity)) {
                                    continue;
                                }
                            }

                            // Читаем строку для пройденного расстояния
                            String distanceTravelledLine = reader.readLine();
                            Float distanceTravelled = null;
                            if (distanceTravelledLine != null && !distanceTravelledLine.isEmpty()) {
                                distanceTravelled = Float.parseFloat(distanceTravelledLine);
                                if (!Validator.validateDistanceTravelled(distanceTravelled)) {
                                    continue;
                                }
                            }

                            // Читаем строку для типа топлива
                            String fuelTypeLine = reader.readLine();
                            FuelType fuelType = null;
                            if (fuelTypeLine != null && !fuelTypeLine.isEmpty()) {
                                int choice = Integer.parseInt(fuelTypeLine);
                                if (choice > 0 && choice <= FuelType.values().length) {
                                    fuelType = FuelType.values()[choice - 1];
                                }
                            }

                            // Создаем новый объект Vehicle с обновленной информацией
                            Vehicle updatedVehicle = new Vehicle(id, name, coordinates, java.time.LocalDate.now(), enginePower, capacity, distanceTravelled, fuelType);

                            // Обновляем машину в коллекции
                            collectionManager.updateVehicle(id, updatedVehicle);
                            // Обновляем информацию в файле
                            FileHandler.writeToFile(envKey, collectionManager.getVehicles());
                        }
                        break;
                    default:
                        System.out.println("Неизвестная команда. Напишите 'help' чтобы посмотреть доступные команды");
                        break;

                }
                // Сохраняем коллекцию в файл после обработки скрипта
                FileHandler.writeToFile(envKey, collectionManager.getVehicles());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Script file not found: " + fileName);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } finally {
            executedScripts.remove(fileName); // Remove the script name after execution
        }
    }

}





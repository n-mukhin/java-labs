package Server;

import Client.Collection.Coordinates;
import Client.Collection.FuelType;
import Client.Collection.Vehicle;
import Client.Data.FileHandler;
import Client.Validator;
import Common.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CommandExecutor {
    private static final ConcurrentHashMap<String, Set<String>> userExecutedScripts = new ConcurrentHashMap<>();
    private static final String scriptDirectory = "/home/studs/s409203/java-labs/lab6/";
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void execute(Command command, CollectionManager collectionManager, String userId, CommandCallback callback) {
        executorService.submit(() -> {
            Response response = null;
            try {
                response = executeCommand(command, collectionManager, userId);
            } catch (Exception e) {
                response = new Response("Error executing command: " + e.getMessage(), ResponseType.ERROR);
            }
            callback.onCommandExecuted(response);
        });
    }

    private static Response executeCommand(Command command, CollectionManager collectionManager, String userId) throws InterruptedException {
        // Добавляем задержку 10 мс перед выполнением команды
        Thread.sleep(10);

        switch (command.getType()) {
            case ADD:
                return addVehicle(command, collectionManager);
            case REMOVE_BY_ID:
                return removeVehicleById(command, collectionManager);
            case SHOW:
                return showVehicles(collectionManager);
            case INFO:
                return getInfo(collectionManager);
            case CLEAR:
                return clearCollection(collectionManager);
            case SUM_OF_CAPACITIES:
                return getSumOfCapacities(collectionManager);
            case PRINT_ENGINE_POWER_ASCENDING:
                return new Response(collectionManager.printEnginePowerAscending(), ResponseType.SUCCESS);
            case PRINT_FUEL_TYPE_DESCENDING:
                return new Response(collectionManager.printFuelTypeDescending(), ResponseType.SUCCESS);
            case REMOVE_FIRST:
                return removeFirstVehicle(collectionManager);
            case REMOVE_HEAD:
                return removeHeadVehicle(collectionManager);
            case REMOVE_GREATER:
                return removeVehiclesGreaterThan(command, collectionManager);
            case EXECUTE_SCRIPT:
                return executeScript(command, collectionManager, userId);
            case UPDATE_ID:
                return updateVehicle(command, collectionManager);
            case EXIT:
                return new Response("Server shutting down", ResponseType.SUCCESS);
            case HELP:
                return showHelp();
            default:
                return new Response("Unknown command", ResponseType.ERROR);
        }
    }

    private static Response addVehicle(Command command, CollectionManager collectionManager) {
        Vehicle vehicle = (Vehicle) command.getPayload();
        collectionManager.addVehicle(vehicle);
        return new Response("Vehicle added successfully", ResponseType.SUCCESS);
    }

    private static Response removeVehicleById(Command command, CollectionManager collectionManager) {
        if (collectionManager.getCollectionSize() == 0) {
            return new Response("Collection is empty. Cannot remove vehicle.", ResponseType.ERROR);
        }
        long id = (long) command.getPayload();
        collectionManager.removeVehicleById(id);
        return new Response("Vehicle removed successfully", ResponseType.SUCCESS);
    }

    private static Response showVehicles(CollectionManager collectionManager) {
        if (collectionManager.getCollectionSize() == 0) {
            return new Response("Collection is empty.", ResponseType.ERROR);
        }

        List<Vehicle> sortedVehicles = collectionManager.getSortedVehiclesByLocation();

        String result = sortedVehicles.stream()
                .map(Vehicle::toString)
                .collect(Collectors.joining("\n"));

        return new Response(result.isEmpty() ? "No vehicles found." : result, ResponseType.SUCCESS);
    }

    private static Response getInfo(CollectionManager collectionManager) {
        if (collectionManager.getCollectionSize() == 0) {
            return new Response("Collection is empty.", ResponseType.ERROR);
        }
        String info = "Collection type: " + collectionManager.getVehicles().getClass().getSimpleName() +
                "\nInitialization date: " + collectionManager.getInitializationDate() +
                "\nNumber of elements: " + collectionManager.getCollectionSize();
        return new Response(info, ResponseType.SUCCESS);
    }

    private static Response clearCollection(CollectionManager collectionManager) {
        if (collectionManager.getCollectionSize() == 0) {
            return new Response("Collection is already empty.", ResponseType.ERROR);
        }
        collectionManager.clearCollection();
        return new Response("Collection cleared", ResponseType.SUCCESS);
    }

    private static Response getSumOfCapacities(CollectionManager collectionManager) {
        if (collectionManager.getCollectionSize() == 0) {
            return new Response("Collection is empty.", ResponseType.ERROR);
        }
        double sum = collectionManager.sumOfCapacities();
        return new Response("Sum of capacities: " + sum, ResponseType.SUCCESS);
    }

    private static Response removeFirstVehicle(CollectionManager collectionManager) {
        if (collectionManager.getCollectionSize() == 0) {
            return new Response("Collection is empty. No vehicle to remove.", ResponseType.ERROR);
        }
        Vehicle vehicle = collectionManager.removeFirstVehicle();
        return new Response("First vehicle removed: " + vehicle, ResponseType.SUCCESS);
    }

    private static Response removeHeadVehicle(CollectionManager collectionManager) {
        if (collectionManager.getCollectionSize() == 0) {
            return new Response("Collection is empty. No vehicle to remove.", ResponseType.ERROR);
        }
        Vehicle vehicle = collectionManager.removeFirstVehicle();
        return new Response("Head vehicle removed: " + vehicle, ResponseType.SUCCESS);
    }

    private static Response removeVehiclesGreaterThan(Command command, CollectionManager collectionManager) {
        if (collectionManager.getCollectionSize() == 0) {
            return new Response("Collection is empty. No vehicles to remove.", ResponseType.ERROR);
        }
        double threshold = (double) command.getPayload();
        int removedCount = collectionManager.removeVehiclesWithEnginePowerGreaterThan(threshold);
        return new Response(removedCount + " vehicles removed with engine power greater than " + threshold, ResponseType.SUCCESS);
    }

    private static Response executeScript(Command command, CollectionManager collectionManager, String userId) throws InterruptedException {
        String fileName = (String) command.getPayload();
        System.out.println("Executing script: " + fileName);

        Set<String> userScripts = userExecutedScripts.computeIfAbsent(userId, k -> new HashSet<>());

        if (userScripts.contains(fileName)) {
            System.out.println("Script '" + fileName + "' is already executed by user " + userId + ". Skipping to prevent recursion.");
            return new Response("Script '" + fileName + "' is already executed by user " + userId + ". Skipping to prevent recursion.", ResponseType.ERROR);
        }

        userScripts.add(fileName);
        File scriptFile = new File(scriptDirectory + fileName);
        if (!scriptFile.exists()) {
            System.out.println("Script '" + fileName + "' not found.");
            return new Response("Script '" + fileName + "' not found.", ResponseType.ERROR);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(scriptFile))) {
            String line;
            PriorityQueue<Vehicle> existingVehicles = FileHandler.readFromFile();
            long currentId = existingVehicles.isEmpty() ? 1L : existingVehicles.peek().getId() + 1;
            List<Command> commands = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }
                System.out.println("Reading line: " + line);
                String[] parts = line.split("\\s+", 2);
                String commandName = parts[0];
                String commandArgs = parts.length > 1 ? parts[1] : "";

                Command scriptCommand = null;
                try {
                    // Add a delay of 10 ms before executing each command in the script
                    Thread.sleep(10);

                    switch (commandName.toLowerCase()) {
                        case "add":
                            scriptCommand = createAddCommand(reader, commandArgs, currentId++);
                            break;
                        case "remove_by_id":
                            scriptCommand = new Command(CommandType.REMOVE_BY_ID, Long.parseLong(commandArgs.trim()));
                            break;
                        case "show":
                            scriptCommand = new Command(CommandType.SHOW, null);
                            break;
                        case "info":
                            scriptCommand = new Command(CommandType.INFO, null);
                            break;
                        case "clear":
                            scriptCommand = new Command(CommandType.CLEAR, null);
                            break;
                        case "sum_of_capacities":
                            scriptCommand = new Command(CommandType.SUM_OF_CAPACITIES, null);
                            break;
                        case "print_field_ascending_engine_power":
                            scriptCommand = new Command(CommandType.PRINT_ENGINE_POWER_ASCENDING, null);
                            break;
                        case "print_field_descending_fuel_type":
                            scriptCommand = new Command(CommandType.PRINT_FUEL_TYPE_DESCENDING, null);
                            break;
                        case "remove_first":
                            scriptCommand = new Command(CommandType.REMOVE_FIRST, null);
                            break;
                        case "remove_head":
                            scriptCommand = new Command(CommandType.REMOVE_HEAD, null);
                            break;
                        case "remove_greater":
                            scriptCommand = new Command(CommandType.REMOVE_GREATER, Double.parseDouble(commandArgs.trim()));
                            break;
                        case "execute_script":
                            scriptCommand = new Command(CommandType.EXECUTE_SCRIPT, commandArgs);
                            break;
                        case "update_id":
                            scriptCommand = createUpdateCommand(reader, commandArgs);
                            break;
                        case "exit":
                            scriptCommand = new Command(CommandType.EXIT, null);
                            break;
                        case "help":
                            scriptCommand = new Command(CommandType.HELP, null);
                            break;
                        default:
                            System.err.println("Unknown command: " + commandName);
                            break;
                    }

                    if (scriptCommand != null) {
                        commands.add(scriptCommand);
                    }

                } catch (Exception e) {
                    System.err.println("Error executing command: " + commandName + " with args: " + commandArgs);
                    e.printStackTrace();
                }
            }

            // Execute commands in sequence
            for (Command cmd : commands) {
                executeCommand(cmd, collectionManager, userId);
            }

            collectionManager.saveCollection("lab6");
            System.out.println("Received response: Script executed successfully.");
            return new Response("Script executed successfully.", ResponseType.SUCCESS);

        } catch (FileNotFoundException e) {
            System.out.println("Script file not found: " + fileName);
            return new Response("Script file not found: " + fileName, ResponseType.ERROR);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return new Response("Error reading file: " + e.getMessage(), ResponseType.ERROR);
        } finally {
            userScripts.remove(fileName);
        }
    }

    private static Command createAddCommand(BufferedReader reader, String name, long id) throws IOException {
        if (!Validator.validateName(name)) {
            System.err.println("Invalid vehicle name: " + name);
            return null;
        }

        String xLine = reader.readLine();
        if (xLine == null) {
            return null;
        }
        float x;
        try {
            x = Float.parseFloat(xLine.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid format for X coordinate: " + xLine);
            return null;
        }

        String yLine = reader.readLine();
        if (yLine == null) {
            return null;
        }
        float y;
        try {
            y = Float.parseFloat(yLine.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid format for Y coordinate: " + yLine);
            return null;
        }

        Coordinates coordinates = new Coordinates(x, y);
        if (!Validator.validateCoordinates(coordinates)) {
            System.err.println("Invalid coordinates: " + coordinates);
            return null;
        }

        String enginePowerLine = reader.readLine();
        if (enginePowerLine == null) {
            return null;
        }
        double enginePower;
        try {
            enginePower = Double.parseDouble(enginePowerLine.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid format for engine power: " + enginePowerLine);
            return null;
        }
        if (!Validator.validateEnginePower(enginePower)) {
            System.err.println("Invalid engine power: " + enginePower);
            return null;
        }

        Double capacity = null;
        String capacityLine = reader.readLine();
        if (capacityLine != null) {
            try {
                capacity = Double.parseDouble(capacityLine.trim());
                if (!Validator.validateCapacity(capacity)) {
                    capacity = null;
                }
            } catch (NumberFormatException e) {
                capacity = null;
            }
        }

        Float distanceTravelled = null;
        String distanceTravelledLine = reader.readLine();
        if (distanceTravelledLine != null) {
            try {
                distanceTravelled = Float.parseFloat(distanceTravelledLine.trim());
                if (!Validator.validateDistanceTravelled(distanceTravelled)) {
                    distanceTravelled = null;
                }
            } catch (NumberFormatException e) {
                distanceTravelled = null;
            }
        }

        FuelType fuelType = null;
        String fuelTypeLine = reader.readLine();
        if (fuelTypeLine != null) {
            try {
                int choice = Integer.parseInt(fuelTypeLine.trim());
                if (choice > 0 && choice <= FuelType.values().length) {
                    fuelType = FuelType.values()[choice - 1];
                }
            } catch (NumberFormatException e) {
                fuelType = null;
            }
        }

        Vehicle vehicle = new Vehicle(id, name, coordinates, java.time.LocalDate.now(), enginePower, capacity, distanceTravelled, fuelType);
        System.out.println("Created vehicle: " + vehicle);
        return new Command(CommandType.ADD, vehicle);
    }

    private static Command createUpdateCommand(BufferedReader reader, String commandArgs) throws IOException {
        long id;
        try {
            id = Long.parseLong(commandArgs.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid format for ID: " + commandArgs);
            return null;
        }

        PriorityQueue<Vehicle> existingVehicles = FileHandler.readFromFile();
        Vehicle vehicleToUpdate = existingVehicles.stream()
                .filter(vehicle -> vehicle.getId() == id)
                .findFirst()
                .orElse(null);

        if (vehicleToUpdate == null) {
            System.err.println("Vehicle with ID " + id + " not found.");
            return null;
        }

        String name = reader.readLine();
        if (name == null || !Validator.validateName(name)) {
            System.err.println("Invalid vehicle name: " + name);
            return null;
        }

        String xLine = reader.readLine();
        if (xLine == null) {
            return null;
        }
        float x;
        try {
            x = Float.parseFloat(xLine.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid format for X coordinate: " + xLine);
            return null;
        }

        String yLine = reader.readLine();
        if (yLine == null) {
            return null;
        }
        float y;
        try {
            y = Float.parseFloat(yLine.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid format for Y coordinate: " + yLine);
            return null;
        }

        Coordinates coordinates = new Coordinates(x, y);
        if (!Validator.validateCoordinates(coordinates)) {
            System.err.println("Invalid coordinates: " + coordinates);
            return null;
        }

        String enginePowerLine = reader.readLine();
        if (enginePowerLine == null) {
            return null;
        }
        double enginePower;
        try {
            enginePower = Double.parseDouble(enginePowerLine.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid format for engine power: " + enginePowerLine);
            return null;
        }
        if (!Validator.validateEnginePower(enginePower)) {
            System.err.println("Invalid engine power: " + enginePower);
            return null;
        }

        Double capacity = null;
        String capacityLine = reader.readLine();
        if (capacityLine != null) {
            try {
                capacity = Double.parseDouble(capacityLine.trim());
                if (!Validator.validateCapacity(capacity)) {
                    capacity = null;
                }
            } catch (NumberFormatException e) {
                capacity = null;
            }
        }

        Float distanceTravelled = null;
        String distanceTravelledLine = reader.readLine();
        if (distanceTravelledLine != null) {
            try {
                distanceTravelled = Float.parseFloat(distanceTravelledLine.trim());
                if (!Validator.validateDistanceTravelled(distanceTravelled)) {
                    distanceTravelled = null;
                }
            } catch (NumberFormatException e) {
                distanceTravelled = null;
            }
        }

        FuelType fuelType = null;
        String fuelTypeLine = reader.readLine();
        if (fuelTypeLine != null) {
            try {
                int choice = Integer.parseInt(fuelTypeLine.trim());
                if (choice > 0 && choice <= FuelType.values().length) {
                    fuelType = FuelType.values()[choice - 1];
                }
            } catch (NumberFormatException e) {
                fuelType = null;
            }
        }

        Vehicle updatedVehicle = new Vehicle(id, name, coordinates, java.time.LocalDate.now(), enginePower, capacity, distanceTravelled, fuelType);
        System.out.println("Updated vehicle: " + updatedVehicle);
        return new Command(CommandType.UPDATE_ID, updatedVehicle);
    }

    private static Response updateVehicle(Command command, CollectionManager collectionManager) {
        if (collectionManager.getCollectionSize() == 0) {
            return new Response("Collection is empty. Cannot update vehicle.", ResponseType.ERROR);
        }
        Vehicle updatedVehicle = (Vehicle) command.getPayload();
        Vehicle existingVehicle = collectionManager.findVehicleById(updatedVehicle.getId());
        if (existingVehicle == null) {
            return new Response("Vehicle with ID " + updatedVehicle.getId() + " not found.", ResponseType.ERROR);
        }
        collectionManager.updateVehicle(updatedVehicle.getId(), updatedVehicle);
        return new Response("Vehicle updated successfully", ResponseType.SUCCESS);
    }

    private static Response showHelp() {
        String helpMessage = "Доступные команды:\n" +
                "help : вывести справку по доступным командам\n" +
                "info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)\n" +
                "show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении\n" +
                "add {element} : добавить новый элемент в коллекцию\n" +
                "update id {element} : обновить значение элемента коллекции, id которого равен заданному\n" +
                "remove_by_id id : удалить элемент из коллекции по его id\n" +
                "clear : очистить коллекцию\n" +
                "exit : завершить программу (без сохранения в файл)\n" +
                "execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.\n" +
                "remove_first : удалить первый элемент из коллекции\n" +
                "remove_head :  вывести первый элемент коллекции и удалить его\n" +
                "remove_greater {element} : удалить из коллекции все элементы, превышающие заданный\n" +
                "sum_of_capacity : вывести сумму значений поля capacity для всех элементов коллекции\n" +
                "print_field_ascending_engine_power : вывести значения поля enginePower всех элементов в порядке возрастания\n" +
                "print_field_descending_fuel_type : вывести значения поля fuelType всех элементов в порядке убывания";

        return new Response(helpMessage, ResponseType.SUCCESS);
    }

    public interface CommandCallback {
        void onCommandExecuted(Response response);
    }
}

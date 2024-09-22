package Server;

import Collection.*;
import Data.DatabaseHandler;
import Client.Validator;
import Common.*;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CommandExecutor {
    private static final ConcurrentHashMap<String, Set<String>> userExecutedScripts = new ConcurrentHashMap<>();
    private static final String scriptDirectory = "/home/studs/s409203/Java/lab7/scripts";
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public static void execute(Command command, CollectionManager collectionManager, int userId, CommandCallback callback) {
        executorService.submit(() -> {
            Response response;
            try {
                response = executeCommand(command, collectionManager, userId);
            } catch (Exception e) {
                response = new Response("Error executing command: " + e.getMessage(), ResponseType.ERROR);
            }
            callback.onCommandExecuted(response);
        });
    }

    private static Response executeCommand(Command command, CollectionManager collectionManager, int userId) throws InterruptedException {
        Thread.sleep(10);

        switch (command.getType()) {
            case ADD:
                return addVehicle(command, collectionManager, userId);
            case REMOVE_BY_ID:
                return removeVehicleById(command, collectionManager, userId);
            case SHOW:
                return showVehicles(collectionManager, userId);
            case INFO:
                return getInfo(collectionManager, userId);
            case CLEAR:
                return clearCollection(collectionManager, userId);
            case SUM_OF_CAPACITIES:
                return getSumOfCapacities(collectionManager, userId);
            case PRINT_ENGINE_POWER_ASCENDING:
                return new Response(collectionManager.printEnginePowerAscending(userId), ResponseType.SUCCESS);
            case PRINT_FUEL_TYPE_DESCENDING:
                return new Response(collectionManager.printFuelTypeDescending(userId), ResponseType.SUCCESS);
            case REMOVE_FIRST:
                return removeFirstVehicle(collectionManager, userId);
            case REMOVE_HEAD:
                return removeHeadVehicle(collectionManager, userId);
            case REMOVE_GREATER:
                return removeVehiclesGreaterThan(command, collectionManager, userId);
            case EXECUTE_SCRIPT:
                return executeScript(command, collectionManager, userId);
            case UPDATE_ID:
                return updateVehicle(command, collectionManager, userId);
            case EXIT:
                return new Response("Server shutting down", ResponseType.SUCCESS);
            case HELP:
                return showHelp();
            default:
                return new Response("Unknown command", ResponseType.ERROR);
        }
    }

    private static Response addVehicle(Command command, CollectionManager collectionManager, int userId) {
        Vehicle vehicle = (Vehicle) command.getPayload();
        vehicle.setId(collectionManager.getNextId());
        boolean success = collectionManager.addVehicle(userId, vehicle);
        if (success) {
            return new Response("Vehicle added successfully with ID: " + vehicle.getId(), ResponseType.SUCCESS);
        } else {
            return new Response("Failed to add vehicle.", ResponseType.ERROR);
        }
    }

    private static Response removeVehicleById(Command command, CollectionManager collectionManager, int userId) {
        long id = (long) command.getPayload();
        boolean success = collectionManager.removeVehicleById(userId, id);
        if (success) {
            return new Response("Vehicle with ID " + id + " removed successfully.", ResponseType.SUCCESS);
        } else {
            return new Response("Failed to remove vehicle. You do not own this vehicle or it does not exist.", ResponseType.ERROR);
        }
    }

    private static Response showVehicles(CollectionManager collectionManager, int userId) {
        Collection<Vehicle> allVehicles = collectionManager.getAllVehicles();
        if (allVehicles.isEmpty()) {
            return new Response("Global collection is empty.", ResponseType.ERROR);
        }

        List<Vehicle> sortedAllVehicles = collectionManager.getSortedVehiclesByLocation();

        String allVehiclesStr = sortedAllVehicles.stream()
                .map(Vehicle::toString)
                .collect(Collectors.joining("\n"));

        Collection<Vehicle> userVehicles = collectionManager.getUserVehicles(userId);
        String username = collectionManager.getUsernameByUserId(userId);

        String userVehiclesStr = userVehicles.stream()
                .map(Vehicle::toString)
                .collect(Collectors.joining("\n"));

        String result = "All Vehicles:\n" + allVehiclesStr + "\n\n" + username + "'s Vehicles:\n" + userVehiclesStr;

        return new Response(result.isEmpty() ? "No vehicles found." : result, ResponseType.SUCCESS);
    }

    private static Response getInfo(CollectionManager collectionManager, int userId) {
        Collection<Vehicle> allVehicles = collectionManager.getAllVehicles();
        Collection<Vehicle> userVehicles = collectionManager.getUserVehicles(userId);
        String username = collectionManager.getUsernameByUserId(userId);

        String info = "Collection Type: " + allVehicles.getClass().getSimpleName() +
                "\nInitialization Date: " + collectionManager.getGlobalInitializationDate() +
                "\nNumber of Elements (Global): " + allVehicles.size() +
                "\nNumber of Elements (" + username + "'s Vehicles): " + userVehicles.size();

        return new Response(info, ResponseType.SUCCESS);
    }


    private static Response clearCollection(CollectionManager collectionManager, int userId) {
        boolean success = collectionManager.clearCollection(userId);
        if (success) {
            return new Response("Your collection has been cleared.", ResponseType.SUCCESS);
        } else {
            return new Response("Failed to clear your collection.", ResponseType.ERROR);
        }
    }

    private static Response getSumOfCapacities(CollectionManager collectionManager, int userId) {
        double sum = collectionManager.sumOfCapacities(userId);
        return new Response("Sum of capacities: " + sum, ResponseType.SUCCESS);
    }

    private static Response removeFirstVehicle(CollectionManager collectionManager, int userId) {
        Vehicle vehicle = collectionManager.removeFirstVehicle(userId);
        if (vehicle != null) {
            return new Response("First vehicle removed: " + vehicle, ResponseType.SUCCESS);
        } else {
            return new Response("Failed to remove first vehicle. You may not own any vehicles.", ResponseType.ERROR);
        }
    }

    private static Response removeHeadVehicle(CollectionManager collectionManager, int userId) {
        Vehicle vehicle = collectionManager.removeFirstVehicle(userId);
        if (vehicle != null) {
            return new Response("Head vehicle removed: " + vehicle, ResponseType.SUCCESS);
        } else {
            return new Response("Failed to remove head vehicle. You may not own any vehicles.", ResponseType.ERROR);
        }
    }

    private static Response removeVehiclesGreaterThan(Command command, CollectionManager collectionManager, int userId) {
        double threshold = (double) command.getPayload();
        int removedCount = collectionManager.removeVehiclesWithEnginePowerGreaterThan(userId, threshold);
        if (removedCount > 0) {
            return new Response(removedCount + " vehicles removed with engine power greater than " + threshold, ResponseType.SUCCESS);
        } else {
            return new Response("No vehicles with engine power greater than " + threshold + " were found or you do not own them.", ResponseType.ERROR);
        }
    }

    private static Response executeScript(Command command, CollectionManager collectionManager, int userId) throws InterruptedException {
        String fileName = (String) command.getPayload();
        System.out.println("Executing script: " + fileName);

        Set<String> userScripts = userExecutedScripts.computeIfAbsent(String.valueOf(userId), k -> new HashSet<>());

        if (userScripts.contains(fileName)) {
            System.out.println("Script '" + fileName + "' is already executed by user " + userId + ". Skipping to prevent recursion.");
            return new Response("Script '" + fileName + "' is already executed by you. Skipping to prevent recursion.", ResponseType.ERROR);
        }

        userScripts.add(fileName);
        File scriptFile = new File(scriptDirectory + File.separator + fileName);
        if (!scriptFile.exists()) {
            System.out.println("Script '" + fileName + "' not found.");
            return new Response("Script '" + fileName + "' not found.", ResponseType.ERROR);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(scriptFile))) {
            String line;
            PriorityQueue<Vehicle> existingVehicles = collectionManager.getAllVehiclesAsPriorityQueue();
            long currentId = collectionManager.getNextId();
            List<Command> commands = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                System.out.println("Reading line: " + line);
                String[] parts = line.split("\\s+", 2);
                String commandName = parts[0];
                String commandArgs = parts.length > 1 ? parts[1] : "";

                Command scriptCommand = null;
                try {
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
                        case "print_engine_power_ascending":
                            scriptCommand = new Command(CommandType.PRINT_ENGINE_POWER_ASCENDING, null);
                            break;
                        case "print_fuel_type_descending":
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
                            scriptCommand = createUpdateCommand(reader, commandArgs, userId, collectionManager);
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

            for (Command cmd : commands) {
                executeCommand(cmd, collectionManager, userId);
            }

            boolean saved = collectionManager.saveCollection(userId);
            if (saved) {
                System.out.println("Script executed and collection saved successfully.");
                return new Response("Script executed and collection saved successfully.", ResponseType.SUCCESS);
            } else {
                return new Response("Script executed but failed to save the collection.", ResponseType.ERROR);
            }

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
        if (capacityLine != null && !capacityLine.trim().isEmpty()) {
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
        if (distanceTravelledLine != null && !distanceTravelledLine.trim().isEmpty()) {
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
        if (fuelTypeLine != null && !fuelTypeLine.trim().isEmpty()) {
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

    private static Command createUpdateCommand(BufferedReader reader, String commandArgs, int userId, CollectionManager collectionManager) throws IOException, SQLException {
        long id;
        try {
            id = Long.parseLong(commandArgs.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid format for ID: " + commandArgs);
            return null;
        }

        Vehicle existingVehicle = collectionManager.findVehicleById(userId, id);
        if (existingVehicle == null) {
            System.err.println("Vehicle with ID " + id + " does not exist or you do not own it.");
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
        if (capacityLine != null && !capacityLine.trim().isEmpty()) {
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
        if (distanceTravelledLine != null && !distanceTravelledLine.trim().isEmpty()) {
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
        if (fuelTypeLine != null && !fuelTypeLine.trim().isEmpty()) {
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

    private static Response updateVehicle(Command command, CollectionManager collectionManager, int userId) {
        Vehicle updatedVehicle = (Vehicle) command.getPayload();
        boolean success = collectionManager.updateVehicle(userId, updatedVehicle.getId(), updatedVehicle);
        if (success) {
            return new Response("Vehicle updated successfully.", ResponseType.SUCCESS);
        } else {
            return new Response("Failed to update vehicle. You do not own this vehicle or it does not exist.", ResponseType.ERROR);
        }
    }

    private static Response showHelp() {
        String helpMessage = "Available commands:\n" +
                "help : Show available commands\n" +
                "info : Display information about the global collection (type, initialization date, number of elements, etc.)\n" +
                "show : Display all vehicles in the global collection\n" +
                "add {element} : Add a new vehicle to the global collection\n" +
                "update_id {element} : Update the vehicle with the specified ID (only if you own it)\n" +
                "remove_by_id {id} : Remove the vehicle with the specified ID (only if you own it)\n" +
                "clear : Clear your own vehicles from the global collection\n" +
                "exit : Terminate the program (without saving to file)\n" +
                "execute_script {file_name} : Execute commands from the specified script file. The script contains commands as you would enter them interactively.\n" +
                "remove_first : Remove the first vehicle you own from the global collection\n" +
                "remove_head : Display and remove the first vehicle you own from the global collection\n" +
                "remove_greater {engine_power} : Remove all your vehicles with engine power greater than the specified value\n" +
                "sum_of_capacities : Display the sum of the 'capacity' field for all your vehicles\n" +
                "print_engine_power_ascending : Display the 'enginePower' of all vehicles in ascending order\n" +
                "print_fuel_type_descending : Display the 'fuelType' of all vehicles in descending order";

        return new Response(helpMessage, ResponseType.SUCCESS);
    }

    public interface CommandCallback {
        void onCommandExecuted(Response response);
    }
}

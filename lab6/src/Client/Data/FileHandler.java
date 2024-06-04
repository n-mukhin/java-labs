package Client.Data;

import Client.Validator;
import Client.Collection.Vehicle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.time.LocalDate;
import java.util.PriorityQueue;

/**
 * Класс для чтения и записи коллекции транспортных средств из/в файл.
 */
public class FileHandler {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    // Определяем переменную envKey
    private static final String ENV_KEY = "lab6";

    /**
     * Метод для чтения коллекции из файла.
     * @return PriorityQueue<Vehicle>, содержащая транспортные средства из файла
     */
    public static PriorityQueue<Vehicle> readFromFile() {
        PriorityQueue<Vehicle> vehicles = new PriorityQueue<>();
        String fileName = System.getenv(ENV_KEY);
        if (fileName == null || fileName.isEmpty()) {
            System.err.println("Environment variable not set.");
            return vehicles;
        }
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(fileName))) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024]; // Размер буфера
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, charsRead);
            }
            Vehicle[] vehicleArray = gson.fromJson(content.toString(), Vehicle[].class);
            for (Vehicle vehicle : vehicleArray) {
                // Проверяем каждое поле объекта vehicle с помощью валидатора
                if (Validator.validateName(vehicle.getName()) &&
                        Validator.validateCoordinates(vehicle.getCoordinates()) &&
                        Validator.validateEnginePower(vehicle.getEnginePower()) &&
                        (vehicle.getCapacity() == null || Validator.validateCapacity(vehicle.getCapacity())) &&
                        (vehicle.getDistanceTravelled() == null || Validator.validateDistanceTravelled(vehicle.getDistanceTravelled())) &&
                        (vehicle.getFuelType() == null || Validator.validateFuelType(vehicle.getFuelType()))) {
                    // Если все поля валидны, добавляем объект в PriorityQueue
                    vehicles.add(vehicle);
                } else {
                    System.out.println("Транспортное средство не прошло валидацию и будет проигнорировано: " + vehicle.getName());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return vehicles;
    }

    /**
     * Метод для записи коллекции в файл.
     * @param vehicles Коллекция транспортных средств для записи
     */
    public static void writeToFile(PriorityQueue<Vehicle> vehicles) {
        String fileName = System.getenv(ENV_KEY);
        if (fileName == null || fileName.isEmpty()) {
            System.err.println("Environment variable not set.");
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            String json = gson.toJson(vehicles.toArray());
            writer.write(json);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}

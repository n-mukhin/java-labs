package Server;

import Client.Collection.*;
import Client.Data.FileHandler;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class CollectionManager {
    private PriorityQueue<Vehicle> vehicles;
    private static final CollectionManager instance = new CollectionManager();

    private CollectionManager() {
        this.vehicles = new PriorityQueue<>();
    }

    public static CollectionManager getInstance(String envKey) {
        return instance;
    }

    public void initializeCollectionIfNeeded(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            System.out.println("Файл пустой или не существует, инициализация коллекции...");
            this.vehicles = new PriorityQueue<>();
            saveCollection(filePath);
        } else {
            loadCollection();
        }
    }

    public void loadCollection() {
        this.vehicles = FileHandler.readFromFile();
    }

    public void saveCollection(String filePath) {
        FileHandler.writeToFile(this.vehicles);
    }

    public PriorityQueue<Vehicle> getVehicles() {
        return vehicles;
    }

    public void addVehicle(Vehicle vehicle) {
        vehicle.setId(getNextId());
        vehicles.add(vehicle);
        saveCollection(null);
    }

    public void removeVehicleById(long id) {
        if (vehicles.isEmpty()) {
            System.out.println("Collection is empty. Cannot remove vehicle.");
            return;
        }
        boolean removed = vehicles.removeIf(vehicle -> vehicle.getId() == id);
        if (removed) {
            saveCollection(null);
        } else {
            System.out.println("Vehicle with ID " + id + " not found.");
        }
    }

    public void clearCollection() {
        if (vehicles.isEmpty()) {
            System.out.println("Collection is already empty.");
            return;
        }
        vehicles.clear();
        saveCollection(null);
    }

    public double sumOfCapacities() {
        if (vehicles.isEmpty()) {
            System.out.println("Collection is empty.");
            return 0;
        }
        double sum = vehicles.stream()
                .mapToDouble(vehicle -> vehicle.getCapacity() != null ? vehicle.getCapacity() : 0)
                .sum();
        if (sum == 0) {
            System.out.println("No vehicles with capacities found.");
        }
        return sum;
    }

    public int getCollectionSize() {
        return vehicles.size();
    }

    public String getInitializationDate() {
        if (vehicles.isEmpty()) {
            return "Collection is empty.";
        }
        return vehicles.stream()
                .min(Comparator.comparing(Vehicle::getCreationDate))
                .map(vehicle -> vehicle.getCreationDate().toString())
                .orElse("Collection is empty.");
    }

    public void updateVehicle(long id, Vehicle updatedVehicle) {
        if (vehicles.isEmpty()) {
            System.out.println("Collection is empty. Cannot update vehicle.");
            return;
        }
        AtomicBoolean updated = new AtomicBoolean(false);
        vehicles = vehicles.stream()
                .map(vehicle -> {
                    if (vehicle.getId() == id) {
                        updated.set(true);
                        return updatedVehicle;
                    }
                    return vehicle;
                })
                .collect(Collectors.toCollection(PriorityQueue::new));
        if (updated.get()) {
            saveCollection(null);
        } else {
            System.out.println("Vehicle with ID " + id + " not found.");
        }
    }

    public int removeVehiclesWithEnginePowerGreaterThan(double enginePower) {
        if (vehicles.isEmpty()) {
            System.out.println("Collection is empty. No vehicles to remove.");
            return 0;
        }
        int initialSize = vehicles.size();
        vehicles.removeIf(vehicle -> vehicle.getEnginePower() > enginePower);
        int removedCount = initialSize - vehicles.size();
        if (removedCount > 0) {
            saveCollection(null);
        } else {
            System.out.println("No vehicles with engine power greater than " + enginePower + " found.");
        }
        return removedCount;
    }

    public Vehicle removeFirstVehicle() {
        if (vehicles.isEmpty()) {
            System.out.println("Collection is empty. No vehicle to remove.");
            return null;
        }
        Vehicle removedVehicle = vehicles.poll();
        saveCollection(null);
        return removedVehicle;
    }

    public long getNextId() {
        if (vehicles.isEmpty()) {
            return 1L;
        }
        return vehicles.stream().mapToLong(Vehicle::getId).max().orElse(0L) + 1;
    }

    public Vehicle findVehicleById(long id) {
        if (vehicles.isEmpty()) {
            System.out.println("Collection is empty. No vehicle found with ID " + id);
            return null;
        }
        return vehicles.stream().filter(vehicle -> vehicle.getId() == id).findFirst().orElse(null);
    }

    public String printEnginePowerAscending() {
        if (getCollectionSize() == 0) {
            return "Collection is empty.";
        }
        String result = vehicles.stream()
                .filter(vehicle -> vehicle.getEnginePower() > 0)
                .sorted(Comparator.comparingDouble(Vehicle::getEnginePower))
                .map(vehicle -> String.valueOf(vehicle.getEnginePower()))
                .collect(Collectors.joining("\n"));
        if (result.isEmpty()) {
            return "No vehicles with engine power found.";
        }
        return result;
    }

    public String printFuelTypeDescending() {
        if (getCollectionSize() == 0) {
            return "Collection is empty.";
        }
        try {
            List<String> fuelTypes = vehicles.stream()
                    .map(vehicle -> vehicle.getFuelType())
                    .filter(Objects::nonNull)
                    .sorted(Comparator.reverseOrder())
                    .map(FuelType::toString)
                    .collect(Collectors.toList());

            if (fuelTypes.isEmpty()) {
                return "No vehicles with fuel type found.";
            }

            return String.join("\n", fuelTypes);
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while processing the command: " + e.getMessage();
        }
    }
    public List<Vehicle> getSortedVehiclesByLocation() {
        if (vehicles.isEmpty()) {
            System.out.println("Collection is empty.");
            return Collections.emptyList();
        }
        return vehicles.stream()
                .sorted(Comparator.comparingDouble((Vehicle v) -> v.getCoordinates().getX() + v.getCoordinates().getY()))
                .collect(Collectors.toList());
    }
}

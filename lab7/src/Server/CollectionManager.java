package Server;

import Collection.*;
import Data.DatabaseHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class CollectionManager {
    private static final Logger logger = LogManager.getLogger(CollectionManager.class);
    private final ConcurrentMap<Long, Vehicle> allVehicles = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Set<Long>> userVehiclesMap = new ConcurrentHashMap<>();
    private static final CollectionManager instance;

    static {
        try {
            instance = new CollectionManager();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final DatabaseHandler dbHandler = DatabaseHandler.getInstance();

    private CollectionManager() throws SQLException, IOException {
        loadAllVehicles();
        loadVehicleOwners();
    }

    public static CollectionManager getInstance() {
        return instance;
    }

    private boolean executeWithRetry(DatabaseOperation operation) {
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                operation.execute();
                return true;
            } catch (SQLException e) {
                logger.error("SQLException: " + e.getMessage(), e);
                retryCount++;
                if (retryCount >= 3) {
                    logger.error("Operation failed after " + retryCount + " attempts.");
                    return false;
                }
            }
        }
        return false;
    }

    @FunctionalInterface
    private interface DatabaseOperation {
        void execute() throws SQLException;
    }

    public boolean loadAllVehicles() {
        try {
            PriorityQueue<Vehicle> vehicles = dbHandler.loadAllVehicles();
            vehicles.forEach(vehicle -> allVehicles.put(vehicle.getId(), vehicle));
            logger.info("Loaded " + allVehicles.size() + " vehicles into global collection.");
            return true;
        } catch (Exception e) {
            logger.error("Error loading all vehicles", e);
            return false;
        }
    }

    private void loadVehicleOwners() throws SQLException {
        Map<Long, Integer> vehicleOwners = dbHandler.loadVehicleOwners();
        vehicleOwners.forEach((vehicleId, userId) -> {
            userVehiclesMap.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(vehicleId);
            logger.debug("Assigned Vehicle ID " + vehicleId + " to User ID " + userId);
        });
        logger.info("Loaded vehicle ownership mappings. Total users with vehicles: " + userVehiclesMap.size());
    }

    public boolean registerUser(String username, String password) {
        return dbHandler.registerUser(username, password);
    }

    public int authenticateUser(String username, String password) {
        int userId = dbHandler.authenticateUserAndGetId(username, password);
        if (userId != -1) {
            userVehiclesMap.putIfAbsent(userId, ConcurrentHashMap.newKeySet());
        }
        return userId;
    }

    public void removeUserId(int userId) {
        userVehiclesMap.remove(userId);
        logger.info("Removed user ID " + userId + " from memory after successful save.");
    }

    public Collection<Vehicle> getAllVehicles() {
        return allVehicles.values();
    }

    public PriorityQueue<Vehicle> getAllVehiclesAsPriorityQueue() {
        return new PriorityQueue<>(allVehicles.values());
    }

    public Collection<Vehicle> getUserVehicles(int userId) {
        Set<Long> vehicleIds = userVehiclesMap.getOrDefault(userId, Collections.emptySet());
        return vehicleIds.stream()
                .map(allVehicles::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public String getGlobalInitializationDate() {
        return allVehicles.values().stream()
                .map(Vehicle::getCreationDate)
                .min(Comparator.naturalOrder())
                .map(LocalDate::toString)
                .orElse("Collection is empty.");
    }

    public int getUserId(String username) {
        return dbHandler.getUserIdByUsername(username);
    }

    public boolean addVehicle(int userId, Vehicle vehicle) {
        boolean success = executeWithRetry(() -> dbHandler.addVehicle(userId, vehicle));
        if (success) {
            allVehicles.put(vehicle.getId(), vehicle);
            userVehiclesMap.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(vehicle.getId());
            logger.info("Vehicle added to global and user-specific collections for userId: " + userId);
            return true;
        }
        return false;
    }

    public boolean updateVehicle(int userId, long id, Vehicle updatedVehicle) {
        if (!userVehiclesMap.getOrDefault(userId, Collections.emptySet()).contains(id)) {
            logger.warn("User " + userId + " attempted to update vehicle " + id + " which they do not own.");
            return false;
        }
        boolean success = executeWithRetry(() -> dbHandler.updateVehicle(updatedVehicle, userId));
        if (success) {
            allVehicles.put(id, updatedVehicle);
            logger.info("Vehicle with ID " + id + " updated in global and user-specific collections for user: " + userId);
            return true;
        }
        logger.warn("Failed to update vehicle with ID " + id + " for user: " + userId);
        return false;
    }

    public boolean deleteVehicle(long vehicleId, int userId) {
        if (!userVehiclesMap.getOrDefault(userId, Collections.emptySet()).contains(vehicleId)) {
            logger.warn("User " + userId + " attempted to delete vehicle " + vehicleId + " which they do not own.");
            return false;
        }
        boolean success = executeWithRetry(() -> dbHandler.deleteVehicle(vehicleId, userId));
        if (success) {
            allVehicles.remove(vehicleId);
            Set<Long> userVehicles = userVehiclesMap.get(userId);
            if (userVehicles != null) {
                userVehicles.remove(vehicleId);
            }
            logger.info("Vehicle with ID " + vehicleId + " removed from global and user-specific collections for user: " + userId);
            return true;
        }
        logger.warn("Failed to delete vehicle with ID " + vehicleId + " for user: " + userId);
        return false;
    }

    public boolean clearCollection(int userId) {
        boolean success = executeWithRetry(() -> dbHandler.deleteAllVehiclesForUser(userId));
        if (success) {
            Set<Long> userVehicles = userVehiclesMap.get(userId);
            if (userVehicles != null) {
                userVehicles.forEach(allVehicles::remove);
                userVehicles.clear();
            }
            logger.info("Collection cleared for user: " + userId);
            return true;
        }
        return false;
    }

    public double sumOfCapacities(int userId) {
        return getUserVehicles(userId).stream()
                .mapToDouble(vehicle -> vehicle.getCapacity() != null ? vehicle.getCapacity() : 0)
                .sum();
    }

    public int getCollectionSize(int userId) {
        return allVehicles.size();
    }

    public String getInitializationDate(int userId) {
        return getGlobalInitializationDate();
    }

    public int removeVehiclesWithEnginePowerGreaterThan(int userId, double enginePower) {
        boolean success = executeWithRetry(() -> dbHandler.deleteVehiclesWithEnginePowerGreaterThan(userId, enginePower));
        if (success) {
            Set<Long> userVehicles = userVehiclesMap.get(userId);
            if (userVehicles != null) {
                int initialSize = userVehicles.size();
                userVehicles.removeIf(vehicleId -> {
                    Vehicle vehicle = allVehicles.get(vehicleId);
                    return vehicle != null && vehicle.getEnginePower() > enginePower;
                });
                int removedCount = initialSize - userVehicles.size();
                userVehicles.forEach(allVehicles::remove);
                logger.info(removedCount + " vehicles removed with engine power greater than " + enginePower + " for user: " + userId);
                return removedCount;
            }
        }
        return 0;
    }

    public Vehicle removeFirstVehicle(int userId) {
        Set<Long> userVehicles = userVehiclesMap.get(userId);
        if (userVehicles == null || userVehicles.isEmpty()) {
            logger.warn("No vehicles to remove for userId: " + userId);
            return null;
        }
        Optional<Long> firstVehicleId = userVehicles.stream().findFirst();
        if (firstVehicleId.isPresent()) {
            long vehicleId = firstVehicleId.get();
            Vehicle removedVehicle = allVehicles.get(vehicleId);
            boolean success = deleteVehicle(vehicleId, userId);
            if (success) {
                logger.info("First vehicle removed: ID " + vehicleId + " for userId: " + userId);
                return removedVehicle;
            }
            logger.warn("Failed to remove first vehicle from DB.");
        }
        return null;
    }

    public long getNextId() {
        return dbHandler.getNextId();
    }

    public Vehicle findVehicleById(int userId, long id) {
        return allVehicles.get(id);
    }

    public String printEnginePowerAscending(int userId) {
        return getAllVehicles().stream()
                .filter(vehicle -> vehicle.getEnginePower() > 0)
                .sorted(Comparator.comparingDouble(Vehicle::getEnginePower))
                .map(vehicle -> String.valueOf(vehicle.getEnginePower()))
                .collect(Collectors.joining("\n"));
    }

    public String printFuelTypeDescending(int userId) {
        return getAllVehicles().stream()
                .map(Vehicle::getFuelType)
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .map(FuelType::toString)
                .collect(Collectors.joining("\n"));
    }

    public List<Vehicle> getSortedVehiclesByLocation() {
        return getAllVehicles().stream()
                .sorted(Comparator.comparingDouble(v -> v.getCoordinates().getX() + v.getCoordinates().getY()))
                .collect(Collectors.toList());
    }

    public boolean saveCollection(int userId) {
        Collection<Vehicle> userVehicles = getUserVehicles(userId);
        boolean success = executeWithRetry(() -> dbHandler.saveVehiclesToDatabase(new PriorityQueue<>(userVehicles), userId));
        if (success) {
            logger.info("Collection saved successfully for user: " + userId);
        }
        return success;
    }

    public boolean removeVehicleById(int userId, long vehicleId) {
        return deleteVehicle(vehicleId, userId);
    }

    public String getUsernameByUserId(int userId) {
        return dbHandler.getUsernameByUserId(userId);
    }
}

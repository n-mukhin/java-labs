package Data;

import Collection.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;

public class DatabaseHandler {
    private static DatabaseHandler instance;
    private HikariDataSource dataSource;
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;

    private DatabaseHandler() {
        loadConfig();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMaximumPoolSize(10);
        config.setAutoCommit(true);
        dataSource = new HikariDataSource(config);
    }

    public static synchronized DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }

    private void loadConfig() {
        Properties prop = new Properties();
        String configPath = "/home/studs/s409203/Java/lab7/secrets/config.properties";
        try (FileInputStream input = new FileInputStream(configPath)) {
            prop.load(input);
            DB_URL = prop.getProperty("db.url");
            DB_USER = prop.getProperty("db.user");
            DB_PASSWORD = prop.getProperty("db.password");
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load configuration file", ex);
        }
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Error obtaining database connection", e);
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public boolean registerUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Error during user registration", e);
        }
    }

    public int authenticateUserAndGetId(String username, String password) {
        String sql = "SELECT id, password_hash FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (verifyPassword(password, storedHash)) {
                        return rs.getInt("id");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error during user authentication", e);
        }
        return -1;
    }

    public int getUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    throw new RuntimeException("User not found: " + username);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving user ID for username: " + username, e);
        }
    }

    public PriorityQueue<Vehicle> loadAllVehicles() {
        PriorityQueue<Vehicle> vehicles = new PriorityQueue<>();
        String sql = "SELECT v.id, v.name, v.engine_power, v.capacity, v.distance_travelled, v.fuel_type, " +
                "(v.coordinates).x AS x, (v.coordinates).y AS y, v.creation_date " +
                "FROM vehicles v";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Vehicle vehicle = extractVehicleFromResultSet(rs);
                vehicles.add(vehicle);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading all vehicles", e);
        }
        return vehicles;
    }


    public Map<Long, Integer> loadVehicleOwners() {
        Map<Long, Integer> vehicleOwners = new HashMap<>();
        String sql = "SELECT vehicle_id, user_id FROM vehicle_owners";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                long vehicleId = rs.getLong("vehicle_id");
                int userId = rs.getInt("user_id");
                vehicleOwners.put(vehicleId, userId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading vehicle owners", e);
        }
        return vehicleOwners;
    }

    public PriorityQueue<Vehicle> loadUserVehicles(int userId) {
        PriorityQueue<Vehicle> vehicles = new PriorityQueue<>();
        String sql = "SELECT v.id, v.name, v.engine_power, v.capacity, v.distance_travelled, v.fuel_type, " +
                "(v.coordinates).x AS x, (v.coordinates).y AS y, v.creation_date " +
                "FROM vehicles v INNER JOIN vehicle_owners vo ON v.id = vo.vehicle_id WHERE vo.user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicle vehicle = extractVehicleFromResultSet(rs);
                    vehicles.add(vehicle);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading user vehicles", e);
        }
        return vehicles;
    }


    public void saveVehiclesToDatabase(PriorityQueue<Vehicle> vehicles, int userId) throws SQLException {
        String sqlInsertOrUpdateVehicle = "INSERT INTO vehicles (id, name, coordinates, engine_power, capacity, distance_travelled, fuel_type) " +
                "VALUES (?, ?, ROW(?, ?), ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, " +
                "coordinates = EXCLUDED.coordinates, " +
                "engine_power = EXCLUDED.engine_power, " +
                "capacity = EXCLUDED.capacity, " +
                "distance_travelled = EXCLUDED.distance_travelled, " +
                "fuel_type = EXCLUDED.fuel_type";
        String sqlInsertVehicleOwner = "INSERT INTO vehicle_owners (vehicle_id, user_id, ownership_date) " +
                "VALUES (?, ?, CURRENT_DATE) " +
                "ON CONFLICT (vehicle_id, user_id) DO NOTHING";
        String sqlCheckVehicleExists = "SELECT id FROM vehicles WHERE id = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Vehicle vehicle : vehicles) {
                    long vehicleId = vehicle.getId();
                    boolean vehicleExists = true;
                    while (vehicleExists) {
                        try (PreparedStatement stmtCheckVehicleExists = conn.prepareStatement(sqlCheckVehicleExists)) {
                            stmtCheckVehicleExists.setLong(1, vehicleId);
                            try (ResultSet rs = stmtCheckVehicleExists.executeQuery()) {
                                vehicleExists = rs.next();
                            }
                        }
                        if (vehicleExists) {
                            vehicleId++;
                        }
                    }
                    try (PreparedStatement stmtInsertOrUpdateVehicle = conn.prepareStatement(sqlInsertOrUpdateVehicle)) {
                        stmtInsertOrUpdateVehicle.setLong(1, vehicleId);
                        stmtInsertOrUpdateVehicle.setString(2, vehicle.getName());
                        stmtInsertOrUpdateVehicle.setFloat(3, vehicle.getCoordinates().getX());
                        stmtInsertOrUpdateVehicle.setFloat(4, vehicle.getCoordinates().getY());
                        stmtInsertOrUpdateVehicle.setDouble(5, vehicle.getEnginePower());
                        stmtInsertOrUpdateVehicle.setObject(6, vehicle.getCapacity());
                        stmtInsertOrUpdateVehicle.setObject(7, vehicle.getDistanceTravelled());
                        stmtInsertOrUpdateVehicle.setString(8, vehicle.getFuelType() != null ? vehicle.getFuelType().toString() : null);
                        stmtInsertOrUpdateVehicle.executeUpdate();
                        try (PreparedStatement stmtInsertVehicleOwner = conn.prepareStatement(sqlInsertVehicleOwner)) {
                            stmtInsertVehicleOwner.setLong(1, vehicleId);
                            stmtInsertVehicleOwner.setInt(2, userId);
                            stmtInsertVehicleOwner.executeUpdate();
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new SQLException("Error saving vehicles to database", e);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean addVehicle(int userId, Vehicle vehicle) {
        if (userId == -1) {
            return false;
        }

        String sqlInsertVehicle = "INSERT INTO vehicles (id, name, coordinates, engine_power, capacity, distance_travelled, fuel_type) VALUES (?, ?, ROW(?, ?), ?, ?, ?, ?)";
        String sqlInsertVehicleOwner = "INSERT INTO vehicle_owners (vehicle_id, user_id, ownership_date) VALUES (?, ?, CURRENT_DATE)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            long vehicleId = getNextId();

            boolean success = false;
            while (!success) {
                try (PreparedStatement stmtInsertVehicle = conn.prepareStatement(sqlInsertVehicle, Statement.RETURN_GENERATED_KEYS)) {
                    stmtInsertVehicle.setLong(1, vehicleId); // Use the next available vehicle ID
                    stmtInsertVehicle.setString(2, vehicle.getName());
                    stmtInsertVehicle.setFloat(3, vehicle.getCoordinates().getX());
                    stmtInsertVehicle.setFloat(4, vehicle.getCoordinates().getY());
                    stmtInsertVehicle.setDouble(5, vehicle.getEnginePower());
                    stmtInsertVehicle.setObject(6, vehicle.getCapacity());
                    stmtInsertVehicle.setObject(7, vehicle.getDistanceTravelled());
                    stmtInsertVehicle.setString(8, vehicle.getFuelType() != null ? vehicle.getFuelType().toString() : null);

                    int rowsAffected = stmtInsertVehicle.executeUpdate();
                    if (rowsAffected > 0) {
                        try (ResultSet generatedKeys = stmtInsertVehicle.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                // Vehicle ID is successfully generated
                                vehicleId = generatedKeys.getLong(1);
                                try (PreparedStatement stmtInsertVehicleOwner = conn.prepareStatement(sqlInsertVehicleOwner)) {
                                    stmtInsertVehicleOwner.setLong(1, vehicleId);
                                    stmtInsertVehicleOwner.setInt(2, userId);
                                    stmtInsertVehicleOwner.executeUpdate();
                                }
                                conn.commit();
                                success = true;  // Mark as successful to break out of the loop
                                return true;
                            }
                        }
                    }
                    conn.rollback();
                    return false;
                } catch (SQLException e) {

                    if (e.getSQLState().equals("23505")) {
                        vehicleId++;
                    } else {
                        conn.rollback();
                        throw new RuntimeException("Error adding vehicle to database", e);
                    }
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during addVehicle", e);
        }
        return false;
    }

    public boolean updateVehicle(Vehicle vehicle, int userId) {
        String sql = "UPDATE vehicles SET name = ?, coordinates = ROW(?, ?), engine_power = ?, capacity = ?, distance_travelled = ?, fuel_type = ? " +
                "WHERE id = ? AND EXISTS (SELECT 1 FROM vehicle_owners WHERE vehicle_id = ? AND user_id = ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vehicle.getName());
            stmt.setFloat(2, vehicle.getCoordinates().getX());
            stmt.setFloat(3, vehicle.getCoordinates().getY());
            stmt.setDouble(4, vehicle.getEnginePower());
            stmt.setObject(5, vehicle.getCapacity());
            stmt.setObject(6, vehicle.getDistanceTravelled());
            stmt.setString(7, vehicle.getFuelType() != null ? vehicle.getFuelType().toString() : null);
            stmt.setLong(8, vehicle.getId());
            stmt.setLong(9, vehicle.getId());
            stmt.setInt(10, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating vehicle in database", e);
        }
    }

    public boolean deleteVehicle(long vehicleId, int userId) {
        String sqlDeleteVehicleOwner = "DELETE FROM vehicle_owners WHERE vehicle_id = ? AND user_id = ?";
        String sqlDeleteVehicle = "DELETE FROM vehicles WHERE id = ? AND NOT EXISTS (SELECT 1 FROM vehicle_owners WHERE vehicle_id = ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtDeleteVehicleOwner = conn.prepareStatement(sqlDeleteVehicleOwner)) {
                stmtDeleteVehicleOwner.setLong(1, vehicleId);
                stmtDeleteVehicleOwner.setInt(2, userId);
                int rowsDeletedFromOwners = stmtDeleteVehicleOwner.executeUpdate();
                if (rowsDeletedFromOwners > 0) {
                    try (PreparedStatement stmtDeleteVehicle = conn.prepareStatement(sqlDeleteVehicle)) {
                        stmtDeleteVehicle.setLong(1, vehicleId);
                        stmtDeleteVehicle.setLong(2, vehicleId);
                        stmtDeleteVehicle.executeUpdate();
                    }
                }
                conn.commit();
                return rowsDeletedFromOwners > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error deleting vehicle", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during deleteVehicle", e);
        }
    }

    public boolean deleteAllVehiclesForUser(int userId) {
        String sqlDeleteVehicleOwners = "DELETE FROM vehicle_owners WHERE user_id = ?";
        String sqlDeleteVehicles = "DELETE FROM vehicles WHERE id NOT IN (SELECT vehicle_id FROM vehicle_owners)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtDeleteVehicleOwners = conn.prepareStatement(sqlDeleteVehicleOwners)) {
                stmtDeleteVehicleOwners.setInt(1, userId);
                int rowsDeletedFromOwners = stmtDeleteVehicleOwners.executeUpdate();
                try (PreparedStatement stmtDeleteVehicles = conn.prepareStatement(sqlDeleteVehicles)) {
                    stmtDeleteVehicles.executeUpdate();
                }
                conn.commit();
                return rowsDeletedFromOwners > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error deleting vehicles for user", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during deleteAllVehiclesForUser", e);
        }
    }

    public long getNextId() {
        String sql = "SELECT MAX(id) AS max_id FROM vehicles";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                long maxId = rs.getLong("max_id");
                return maxId + 1;
            } else {
                return 1L;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting next vehicle ID", e);
        }
    }

    public void deleteVehiclesWithEnginePowerGreaterThan(int userId, double enginePower) {
        String deleteVehicleOwnersSQL = "DELETE FROM vehicle_owners WHERE user_id = ? AND vehicle_id IN (SELECT id FROM vehicles WHERE engine_power > ?)";
        String deleteVehiclesSQL = "DELETE FROM vehicles WHERE id NOT IN (SELECT vehicle_id FROM vehicle_owners)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtDeleteVehicleOwners = conn.prepareStatement(deleteVehicleOwnersSQL)) {
                stmtDeleteVehicleOwners.setInt(1, userId);
                stmtDeleteVehicleOwners.setDouble(2, enginePower);
                stmtDeleteVehicleOwners.executeUpdate();
                try (PreparedStatement stmtDeleteVehicles = conn.prepareStatement(deleteVehiclesSQL)) {
                    stmtDeleteVehicles.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error deleting vehicles with engine power greater than " + enginePower, e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during deleteVehiclesWithEnginePowerGreaterThan", e);
        }
    }

    public String getUsernameByUserId(int userId) {
        String sql = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                } else {
                    return "Unknown User";
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving username for userId: " + userId, e);
        }
    }


    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private boolean verifyPassword(String password, String storedHash) {
        String hashedPassword = hashPassword(password);
        return hashedPassword.equals(storedHash);
    }

    private Vehicle extractVehicleFromResultSet(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        double enginePower = rs.getDouble("engine_power");
        Double capacity = (Double) rs.getObject("capacity");
        Float distanceTravelled = (Float) rs.getObject("distance_travelled");
        String fuelTypeStr = rs.getString("fuel_type");
        FuelType fuelType = fuelTypeStr != null ? FuelType.fromString(fuelTypeStr) : null;
        float x = rs.getFloat("x");
        float y = rs.getFloat("y");
        Coordinates coordinates = new Coordinates(x, y);
        Date creationDate = rs.getDate("creation_date");
        return new Vehicle(id, name, coordinates, creationDate.toLocalDate(),
                enginePower, capacity, distanceTravelled, fuelType);
    }
}

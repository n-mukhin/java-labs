package Collection;

import java.util.PriorityQueue;
import Collection.Vehicle;

/**
 * Класс, управляющий коллекцией объектов типа Vehicle.
 */
public class CollectionManager {
    public PriorityQueue<Vehicle> vehicles; // Коллекция объектов типа Vehicle

    /**
     * Конструктор для создания объекта CollectionManager.
     */
    public CollectionManager() {
        vehicles = new PriorityQueue<>();
    }

    // Метод для получения количества объектов типа Vehicle в коллекции
    public PriorityQueue<Vehicle> getVehicles() {
        return vehicles;
    }
    /**
     * Метод для добавления нового объекта типа Vehicle в коллекцию.
     * @param vehicle Транспортное средство для добавления
     */
    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    /**
     * Метод для удаления объекта типа Vehicle из коллекции по его id.
     * @param id Уникальный идентификатор транспортного средства для удаления
     */
    public void removeVehicleById(long id) {
        vehicles.removeIf(vehicle -> vehicle.getId() == id);
    }

    /**
     * Метод для удаления первого объекта типа Vehicle из коллекции.
     * @return Удаленный объект типа Vehicle
     */
    public Vehicle removeFirstVehicle() {
        return vehicles.poll();
    }

    /**
     * Метод для вывода всех объектов типа Vehicle в коллекции.
     */
    public void showAllVehicles() {
        for (Vehicle vehicle : vehicles) {
            System.out.println(vehicle);
        }
    }

    /**
     * Метод для очистки коллекции.
     */
    public void clearCollection() {
        vehicles.clear();
    }

    /**
     * Метод для обновления транспортного средства по его идентификатору.
     * @param id Идентификатор транспортного средства для обновления
     * @param newVehicle Новое транспортное средство
     */
    public void updateVehicle(long id, Vehicle newVehicle) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getId() == id) {
                vehicle.setName(newVehicle.getName());
                vehicle.setCoordinates(newVehicle.getCoordinates());
                vehicle.setCreationDate(newVehicle.getCreationDate());
                vehicle.setEnginePower(newVehicle.getEnginePower());
                vehicle.setCapacity(newVehicle.getCapacity());
                vehicle.setDistanceTravelled(newVehicle.getDistanceTravelled());
                vehicle.setFuelType(newVehicle.getFuelType());
                break;
            }
        }
    }

    /**
     * Метод для получения суммы значений поля capacity для всех объектов типа Vehicle в коллекции.
     * @return Сумма значений поля capacity
     */
    public double sumOfCapacities() {
        double sum = 0;
        for (Vehicle vehicle : vehicles) {
            Double capacity = vehicle.getCapacity();
            if (capacity != null) {
                sum += capacity;
            }
        }
        return sum;
    }

    /**
     * Метод для получения количества объектов типа Vehicle в коллекции.
     * @return Количество объектов в коллекции
     */
    public int getCollectionSize(){
        return vehicles.size();
    }

    /**
     * Метод для получения даты инициализации коллекции (даты создания первого элемента в коллекции).
     * @return Дата инициализации коллекции
     */
    public String getInitializationDate(){
        if (!vehicles.isEmpty()) {
            return vehicles.peek().getCreationDate().toString();
        } else {
            return "Collection is empty";
        }
    }

    /**
     * Метод для удаления объектов типа Vehicle из коллекции, у которых мощность двигателя превышает заданное значение.
     * @param enginePower Значение мощности двигателя
     * @return Количество удаленных объектов
     */
    public int removeVehiclesWithEnginePowerGreaterThan(double enginePower) {
        int removedCount = 0;
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getEnginePower() > enginePower) {
                vehicles.remove(vehicle);
                removedCount++;
            }
        }
        return removedCount;
    }

    /**
     * Метод для вывода значений поля enginePower всех объектов типа Vehicle в коллекции в порядке возрастания.
     */
    public void printEnginePowerAscending() {
        vehicles.stream()
                .sorted((v1, v2) -> Double.compare(v1.getEnginePower(), v2.getEnginePower()))
                .forEach(vehicle -> System.out.println(vehicle.getEnginePower()));
    }

    /**
     * Метод для вывода значений поля fuelType всех объектов типа Vehicle в коллекции в порядке убывания.
     */
    public void printFuelTypeDescending() {
        vehicles.stream()
                .sorted((v1, v2) -> v2.getFuelType().compareTo(v1.getFuelType()))
                .forEach(vehicle -> System.out.println(vehicle.getFuelType()));
    }
}

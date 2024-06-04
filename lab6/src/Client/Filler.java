package Client;

import Client.Collection.Vehicle;
import Client.Collection.Coordinates;
import Client.Collection.FuelType;
import Client.Data.FileHandler;

import java.util.PriorityQueue;

public class Filler {
    private static PriorityQueue<Vehicle> vehicles = new PriorityQueue<>();
    private static long currentId = 1L; // Инициализация счетчика ID

    public static Vehicle readVehicle(ConsoleReader reader, String name, String envKey) {
        if (!Validator.validateName(name)) {
            System.out.println("Название транспортного средства не прошло валидацию.");
            return null;
        }

        Coordinates coordinates;
        float x, y;
        while (true) {
            try {
                System.out.println("Введите координату X:");
                String inputX = reader.readLine();
                if (inputX == null) {
                    System.out.println("Ввод завершен.");
                    return null;
                }
                x = Float.parseFloat(inputX);

                System.out.println("Введите координату Y:");
                String inputY = reader.readLine();
                if (inputY == null) {
                    System.out.println("Ввод завершен.");
                    return null;
                }
                y = Float.parseFloat(inputY);

                coordinates = new Coordinates(x, y);
                if (Validator.validateCoordinates(coordinates)) {
                    break;
                } else {
                    System.out.println("Координаты не прошли валидацию. Попробуйте снова.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Неверный формат координат. Попробуйте снова.");
            } catch (Exception e) {
                System.out.println("Ошибка ввода: " + e.getMessage());
                e.printStackTrace();
            }
        }

        double enginePower;
        while (true) {
            try {
                System.out.println("Введите мощность двигателя:");
                enginePower = Double.parseDouble(reader.readLine());
                if (Validator.validateEnginePower(enginePower)) {
                    break;
                } else {
                    System.out.println("Мощность двигателя не прошла валидацию. Попробуйте снова.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Неверный формат мощности двигателя. Попробуйте снова.");
            } catch (Exception e) {
                System.out.println("Ошибка ввода: " + e.getMessage());
                e.printStackTrace();
            }
        }

        Double capacity = null;
        while (true) {
            try {
                System.out.println("Введите вместимость (null, если не задано):");
                String capacityStr = reader.readLine();
                if (capacityStr != null && !capacityStr.isEmpty()) {
                    capacity = Double.parseDouble(capacityStr);
                    if (Validator.validateCapacity(capacity)) {
                        break;
                    } else {
                        System.out.println("Вместимость не прошла валидацию. Попробуйте снова.");
                    }
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Неверный формат вместимости. Попробуйте снова.");
            } catch (Exception e) {
                System.out.println("Ошибка ввода: " + e.getMessage());
                e.printStackTrace();
            }
        }

        Float distanceTravelled = null;
        while (true) {
            try {
                System.out.println("Введите пройденное расстояние (null, если не задано):");
                String distanceTravelledStr = reader.readLine();
                if (distanceTravelledStr != null && !distanceTravelledStr.isEmpty()) {
                    distanceTravelled = Float.parseFloat(distanceTravelledStr);
                    if (Validator.validateDistanceTravelled(distanceTravelled)) {
                        break;
                    } else {
                        System.out.println("Пройденное расстояние не прошло валидацию. Попробуйте снова.");
                    }
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Неверный формат пройденного расстояния. Попробуйте снова.");
            } catch (Exception e) {
                System.out.println("Ошибка ввода: " + e.getMessage());
                e.printStackTrace();
            }
        }

        FuelType fuelType = null;
        while (true) {
            try {
                System.out.println("Выберите тип топлива (null, если не задано):");
                System.out.println("Виды топлива:");
                for (int i = 0; i < FuelType.values().length; i++) {
                    System.out.println((i + 1) + ". " + FuelType.values()[i].name());
                }
                String input = reader.readLine();
                if (!input.isEmpty()) {
                    int choice = Integer.parseInt(input);
                    if (choice > 0 && choice <= FuelType.values().length) {
                        fuelType = FuelType.values()[choice - 1];
                        if (Validator.validateFuelType(fuelType)) {
                            break;
                        } else {
                            System.out.println("Тип топлива не прошел валидацию. Попробуйте снова.");
                        }
                    } else {
                        System.out.println("Неверный выбор. Попробуйте снова.");
                    }
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите число. Попробуйте снова.");
            } catch (Exception e) {
                System.out.println("Ошибка ввода: " + e.getMessage());
                e.printStackTrace();
            }
        }

        PriorityQueue<Vehicle> existingVehicles = FileHandler.readFromFile();
        long id = existingVehicles.isEmpty() ? currentId : existingVehicles.peek().getId() + 1;
        Vehicle vehicle = new Vehicle(id, name, coordinates, java.time.LocalDate.now(), enginePower, capacity, distanceTravelled, fuelType);
        vehicles.add(vehicle);

        return vehicle;
    }
}

package Filler;

import Collection.*;

import EnvKey.EnvKey;
import Validator.Validator;
import ConsoleReader.ConsoleReader;
import FileHandler.FileHandler;

import java.io.EOFException;
import java.io.IOException;
import java.util.PriorityQueue;

/**
 * Класс для заполнения коллекции транспортных средств.
 */
public class Filler {
    private static PriorityQueue<Vehicle> vehicles = new PriorityQueue<>();
    private static long currentId = 1L; // Инициализация счетчика ID

    /**
     * Метод для чтения данных о транспортном средстве с консоли и добавления его в коллекцию.
     * @param reader Объект для чтения с консоли
     * @param name Название транспортного средства
     * @param envKey Ключ окружения
     * @return Созданное транспортное средство
     */
    public static Vehicle readVehicle(ConsoleReader reader, String name, EnvKey envKey) {
        // Проверяем, что имя машины не пустое
        if (!Validator.validateName(name)) {
            System.out.println("Название транспортного средства не прошло валидацию.");
            return null;
        }

        // Запрашиваем координаты
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
                    break; // Выходим из цикла, если ввод корректен
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

        // Запрашиваем мощность двигателя
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

        // Запрашиваем вместимость
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
                    break; // Если ввод пустой, выходим из цикла
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Неверный формат вместимости. Попробуйте снова.");
            } catch (Exception e) {
                System.out.println("Ошибка ввода: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Запрашиваем пройденное расстояние
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

        // Запрашиваем тип топлива
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

        // Создаем транспортное средство
        PriorityQueue<Vehicle> existingVehicles = FileHandler.readFromFile(envKey);
        long id = existingVehicles.isEmpty() ? currentId : existingVehicles.peek().getId() + 1;
        Vehicle vehicle = new Vehicle(id, name, coordinates, java.time.LocalDate.now(), enginePower, capacity, distanceTravelled, fuelType);
        vehicles.add(vehicle);
        FileHandler.writeToFile(envKey, vehicles); // Сохранение в файл

        return vehicle;
    }

}


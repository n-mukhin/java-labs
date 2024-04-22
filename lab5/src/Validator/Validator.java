package Validator;

import Collection.Coordinates;
import Collection.FuelType;

import java.util.regex.Pattern;

/**
 * Класс, содержащий методы для валидации данных транспортных средств.
 */
public class Validator {

    /**
     * Метод для проверки названия транспортного средства.
     * @param name Название транспортного средства
     * @return true, если название допустимо, иначе false
     */
    public static boolean validateName(String name) {
        if (name == null || name.isEmpty()) {
            System.err.println("Название не может быть пустым.");
            return false;
        }
        return true;
    }

    /**
     * Метод для проверки координат.
     * @param coordinates Объект координат
     * @return true, если координаты допустимы, иначе false
     */
    public static boolean validateCoordinates(Coordinates coordinates) {
        if (coordinates == null) {
            System.err.println("Координаты не могут быть пустыми.");
            return false;
        }

        Float x = coordinates.getX();
        Float y = coordinates.getY();

        if (x == null || y == null) {
            System.err.println("Координаты не могут содержать пустые значения.");
            return false;
        }

        if (x < 0 || x > 700 || y < 0 || y > 793) {
            System.err.println("Координаты должны быть числами в диапазоне от 0 до 700 для x и от 0 до 793 для y.");
            return false;
        }

        return true;
    }

    /**
     * Метод для проверки мощности двигателя.
     * @param enginePower Мощность двигателя
     * @return true, если мощность допустима, иначе false
     */
    public static boolean validateEnginePower(double enginePower) {
        if (enginePower <= 0) {
            System.err.println("Мощность двигателя должна быть положительным числом.");
            return false;
        }
        // Дополнительная проверка на наличие дробной части
        if (enginePower != (long) enginePower) {
            System.err.println("Мощность двигателя должна быть целым числом.");
            return false;
        }
        return true;
    }

    /**
     * Метод для проверки вместимости.
     * @param capacity Вместимость
     * @return true, если вместимость допустима, иначе false
     */
    public static boolean validateCapacity(double capacity) {
        if (capacity <= 0) {
            System.err.println("Вместимость должна быть положительным числом.");
            return false;
        }
        // Дополнительная проверка на наличие дробной части
        if (capacity != (long) capacity) {
            System.err.println("Вместимость должна быть целым числом.");
            return false;
        }
        return true;
    }

    /**
     * Метод для проверки пройденного расстояния.
     * @param distanceTravelled Пройденное расстояние
     * @return true, если расстояние допустимо, иначе false
     */
    public static boolean validateDistanceTravelled(float distanceTravelled) {
        if (distanceTravelled <= 0) {
            System.err.println("Пройденное расстояние должно быть положительным числом.");
            return false;
        }
        // Дополнительная проверка на наличие дробной части
        if (distanceTravelled != (int) distanceTravelled) {
            System.err.println("Пройденное расстояние должно быть целым числом.");
            return false;
        }
        return true;
    }

    /**
     * Метод для проверки типа топлива.
     * @param fuelType Тип топлива
     * @return true, если тип топлива допустим, иначе false
     */
    public static boolean validateFuelType(FuelType fuelType) {
        if (fuelType == null) {
            return true;
        } else {
            int fuelTypeValue = fuelType.ordinal() + 1;
            if (fuelTypeValue >= 1 && fuelTypeValue <= 4) {
                return true;
            } else {
                System.err.println("Недопустимое значение для типа топлива.");
                return false;
            }
        }
    }
}

package Client;

import Collection.Coordinates;
import Collection.FuelType;


public class Validator {


    public static boolean validateName(String name) {
            if (name == null || name.trim().isEmpty()) {
                System.err.println("Название не может быть пустым.");
                return false;
            }
            return true;
        }

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

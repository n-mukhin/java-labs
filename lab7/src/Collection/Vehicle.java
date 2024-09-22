package Collection;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Класс, представляющий транспортное средство.
 */
public class Vehicle implements Comparable<Vehicle>, Serializable {
    private static final long serialVersionUID = 1L;

    private long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private java.time.LocalDate creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private double enginePower; //Значение поля должно быть больше 0
    private Double capacity; //Поле может быть null, Значение поля должно быть больше 0
    private Float distanceTravelled; //Поле может быть null, Значение поля должно быть больше 0
    private FuelType fuelType; //Поле может быть null


    /**
     * Конструктор для создания транспортного средства.
     * @param id Уникальный идентификатор транспортного средства
     * @param name Название транспортного средства
     * @param coordinates Координаты транспортного средства
     * @param creationDate Дата создания транспортного средства
     * @param enginePower Мощность двигателя транспортного средства
     * @param capacity Вместимость транспортного средства
     * @param distanceTravelled Пройденное расстояние транспортного средства
     * @param fuelType Тип топлива транспортного средства
     */

    public Vehicle(long id, String name, Coordinates coordinates, LocalDate creationDate,
                   double enginePower, Double capacity, Float distanceTravelled, FuelType fuelType) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.enginePower = enginePower;
        this.capacity = capacity;
        this.distanceTravelled = distanceTravelled;
        this.fuelType = fuelType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public java.time.LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(java.time.LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public double getEnginePower() {
        return enginePower;
    }

    public void setEnginePower(double enginePower) {
        this.enginePower = enginePower;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public Float getDistanceTravelled() {
        return distanceTravelled;
    }

    public void setDistanceTravelled(Float distanceTravelled) {
        this.distanceTravelled = Float.valueOf(distanceTravelled);
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    @Override
    public int compareTo(Vehicle o) {
        int result = Long.compare(this.id, o.id);
        if (result == 0) {
            result = this.name.compareTo(o.name);
        }
        if (result == 0) {
            result = this.coordinates.compareTo(o.coordinates);
        }
        if (result == 0) {
            result = this.creationDate.compareTo(o.creationDate);
        }
        if (result == 0) {
            result = Double.compare(this.enginePower, o.enginePower);
        }
        if (result == 0) {
            if (this.fuelType != null && o.fuelType != null) {
                result = this.fuelType.compareTo(o.fuelType);
            } else {
                result = this.fuelType == o.fuelType ? 0 : 1;
            }
        }
        if (result == 0) {
            if (this.capacity != null && o.capacity != null) {
                result = Double.compare(this.capacity, o.capacity);
            } else {
                result = this.capacity == o.capacity ? 0 : 1;
            }
        }
        if (result == 0) {
            if (this.distanceTravelled != null && o.distanceTravelled != null) {
                result = Double.compare(this.distanceTravelled, o.distanceTravelled);
            } else {
                result = this.distanceTravelled == o.distanceTravelled ? 0 : 1;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vehicle{")
                .append("id=").append(id)
                .append(", name='").append(name).append('\'')
                .append(", coordinates=").append(coordinates)
                .append(", creationDate=").append(creationDate)
                .append(", enginePower=").append(enginePower);

        if (capacity != null) {
            sb.append(", capacity=").append(capacity);
        }

        if (distanceTravelled != null) {
            sb.append(", distanceTravelled=").append(distanceTravelled);
        }

        if (fuelType != null) {
            sb.append(", fuelType=").append(fuelType);
        }

        sb.append('}');
        return sb.toString();
    }
}

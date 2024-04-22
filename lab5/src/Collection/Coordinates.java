package Collection;

public class Coordinates {
    private Float x; //Максимальное значение поля: 700, Поле не может быть null
    private Float y; //Максимальное значение поля: 793, Поле не может быть null

    public Coordinates(Float x, Float y) {
        this.x = x;
        this.y = y;
    }

    public Float getX() {
        return x;
    }

    public Float getY() {
        return y;
    }

    public int compareTo(Coordinates o) {
        int result = Float.compare(this.x, o.x);
        if (result == 0) {
            result = Float.compare(this.y, o.y);
        }
        return result;
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

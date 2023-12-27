// Абстрактный класс Person
public abstract class Person {
    protected String name;

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

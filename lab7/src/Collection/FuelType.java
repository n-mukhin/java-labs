package Collection;

import java.io.Serializable;

public enum FuelType implements Serializable {
    GASOLINE,
    KEROSENE,
    NUCLEAR,
    ANTIMATTER;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public static FuelType fromString(String str) {
        return switch (str.toUpperCase()) {
            case "GASOLINE" -> GASOLINE;
            case "KEROSENE" -> KEROSENE;
            case "NUCLEAR" -> NUCLEAR;
            case "ANTIMATTER" -> ANTIMATTER;
            default -> throw new IllegalArgumentException();
        };
    }

    public static FuelType fromInt(int i) {
        return switch (i) {
            case 0 -> GASOLINE;
            case 1 -> KEROSENE;
            case 2 -> NUCLEAR;
            case 3 -> ANTIMATTER;
            default -> throw new IllegalArgumentException();
        };
    }
}

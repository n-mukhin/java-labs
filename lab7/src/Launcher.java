import java.util.Scanner;

public class Launcher {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String[] mainClasses = {
                "Server.MainServer",
                "Client.MainClient"
        };

        System.out.println("Выберите класс для запуска:");

        for (int i = 0; i < mainClasses.length; i++) {
            System.out.println((i + 1) + ". " + mainClasses[i]);
        }

        System.out.print("\nВведите число для выбора: ");

        int choice = scanner.nextInt();

        if (choice < 1 || choice > mainClasses.length) {
            System.out.println("Неверный выбор, программа завершена.");
            return;
        }

        String className = mainClasses[choice - 1];
        try {
            Class<?> clazz = Class.forName(className);
            java.lang.reflect.Method mainMethod = clazz.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) new String[]{});
        } catch (ClassNotFoundException e) {
            System.err.println("Класс " + className + " не найден.");
        } catch (NoSuchMethodException e) {
            System.err.println("Метод main не найден в классе " + className + ".");
        } catch (Exception e) {
            System.err.println("Произошла ошибка при запуске " + className + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}

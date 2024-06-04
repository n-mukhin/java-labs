import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LauncherGUI {
    private static final String[] mainClasses = {
            "Server.MainServer",
            "Client.MainClient",
            "GenerateScript"
    };

    private static final String[] classNames = {
            "Сервер",
            "Клиент",
            "Скрипт генерации"
    };

    public static void main(String[] args) {
        // Создание окна
        JFrame frame = new JFrame("Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BorderLayout(10, 10));

        // Создание панели заголовка
        JPanel headerPanel = new JPanel();
        JLabel headerLabel = new JLabel("Выберите класс для запуска:");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(headerLabel);
        frame.add(headerPanel, BorderLayout.NORTH);

        // Создание панели выбора класса
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JComboBox<String> comboBox = new JComboBox<>(classNames);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(comboBox);

        frame.add(panel, BorderLayout.CENTER);

        // Добавление кнопки запуска
        JButton startButton = new JButton("Запуск");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        frame.add(startButton, BorderLayout.SOUTH);

        // Обработчик нажатия кнопки запуска
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = comboBox.getSelectedIndex();
                String selectedClass = mainClasses[selectedIndex];

                // Запуск выбранного класса в отдельном потоке
                new Thread(() -> {
                    try {
                        Class<?> clazz = Class.forName(selectedClass);
                        java.lang.reflect.Method mainMethod = clazz.getMethod("main", String[].class);
                        String[] params = {}; // аргументы командной строки
                        mainMethod.invoke(null, (Object) params);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();

                frame.dispose(); // Закрытие окна
            }
        });

        // Установка видимости окна
        frame.setVisible(true);
    }
}

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GenerateScriptGUI extends JFrame {
    private JCheckBox addCheckBox;
    private JCheckBox updateCheckBox;
    private JTextField countField;
    private JTextArea outputArea;
    private JButton selectFileButton;
    private JLabel fileLabel;
    private File selectedFile;

    public GenerateScriptGUI() {
        setTitle("Car Script Generator");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        addCheckBox = new JCheckBox("Add");
        updateCheckBox = new JCheckBox("Update ID");
        countField = new JTextField(10);
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        selectFileButton = new JButton("Select File");
        selectFileButton.setPreferredSize(new Dimension(120, 30));
        fileLabel = new JLabel("No file selected");
        fileLabel.setPreferredSize(new Dimension(400, 30));
        fileLabel.setForeground(Color.BLUE);

        Font font = new Font("Arial", Font.PLAIN, 14);
        addCheckBox.setFont(font);
        updateCheckBox.setFont(font);
        countField.setFont(font);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        selectFileButton.setFont(font);
        fileLabel.setFont(font);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.add(new JLabel("Commands:"));
        inputPanel.add(new JPanel());
        inputPanel.add(addCheckBox);
        inputPanel.add(updateCheckBox);
        inputPanel.add(new JLabel("Count:"));
        inputPanel.add(countField);

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        filePanel.add(selectFileButton);
        filePanel.add(fileLabel);

        JButton generateButton = new JButton("Generate Script");
        generateButton.setPreferredSize(new Dimension(160, 30));
        generateButton.setFont(font);
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateScriptInBackground();
            }
        });

        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectFile();
            }
        });

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        add(filePanel, BorderLayout.SOUTH);
        add(generateButton, BorderLayout.EAST);
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            fileLabel.setText("Selected file: " + selectedFile.getAbsolutePath());
        }
    }

    private void generateScriptInBackground() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                generateScript();
                return null;
            }
        };
        worker.execute();
    }

    private void generateScript() {
        boolean isAddSelected = addCheckBox.isSelected();
        boolean isUpdateSelected = updateCheckBox.isSelected();
        if (!isAddSelected && !isUpdateSelected) {
            JOptionPane.showMessageDialog(this, "Please select at least one command (Add or Update ID)");
            return;
        }

        int count;
        try {
            count = Integer.parseInt(countField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for count");
            return;
        }

        StringBuilder script = new StringBuilder();
        Random random = new Random();

        if (isAddSelected) {
            for (int i = 0; i < count; i++) {
                script.append(generateAddCommand(random)).append("\n\n\n\n\n");
            }
        }

        if (isUpdateSelected) {
            for (int i = 0; i < count; i++) {
                if (isAddSelected) {
                    script.append(generateUpdateCommand(random, count)).append("\n\n\n\n\n");
                } else {
                    script.append(generateUpdateCommand(random, i + 1)).append("\n\n\n\n\n");
                }
            }
        }

        outputArea.setText(script.toString());

        if (selectedFile != null) {
            try (FileWriter writer = new FileWriter(selectedFile)) {
                writer.write(script.toString());
                JOptionPane.showMessageDialog(this, "Script saved to " + selectedFile.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving script: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "No file selected for saving");
        }
    }

    private String generateAddCommand(Random random) {
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("add ");
        commandBuilder.append(generateRandomName()).append("\n");
        commandBuilder.append(random.nextInt(99) + 1).append("\n");
        commandBuilder.append(random.nextInt(99) + 1).append("\n");
        commandBuilder.append(random.nextInt(99) + 1).append("\n");
        if (random.nextBoolean()) {
            commandBuilder.append(random.nextInt(99) + 1).append("\n");
        } else {
            commandBuilder.append("\n");
        }
        if (random.nextBoolean()) {
            commandBuilder.append(random.nextInt(99) + 1).append("\n");
        } else {
            commandBuilder.append("\n");
        }

        if (random.nextBoolean()) {
            commandBuilder.append(random.nextInt(4) + 1).append("\n");
        } else {
            commandBuilder.append("\n");
        }
        return commandBuilder.toString();
    }

    private String generateUpdateCommand(Random random, int count) {
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("update_id ");
        commandBuilder.append(random.nextInt(count) + 1).append("\n");
        commandBuilder.append(generateRandomName()).append("\n");
        commandBuilder.append(random.nextInt(99) + 1).append("\n");
        commandBuilder.append(random.nextInt(99) + 1).append("\n");
        commandBuilder.append(random.nextInt(99) + 1).append("\n");
        if (random.nextBoolean()) {
            commandBuilder.append(random.nextInt(99) + 1).append("\n");
        } else {
            commandBuilder.append("\n");
        }
        if (random.nextBoolean()) {
            commandBuilder.append(random.nextInt(99) + 1).append("\n");
        } else {
            commandBuilder.append("\n");
        }
        if (random.nextBoolean()) {
            commandBuilder.append(random.nextInt(4) + 1).append("\n");
        } else {
            commandBuilder.append("\n");
        }
        return commandBuilder.toString();
    }

    private String generateRandomName() {
        String[] names = {
                "Tesla Model S", "Tesla Model 3", "Tesla Model X", "Tesla Model Y",
                "Ford Mustang Mach-E", "Ford F-150 Lightning", "Chevrolet Bolt EV",
                "Chevrolet Corvette", "BMW i4", "BMW iX", "Audi e-tron GT", "Audi Q4 e-tron",
                "Porsche Taycan", "Mercedes-Benz EQS", "Mercedes-Benz EQC",
                "Rivian R1T", "Rivian R1S", "Lucid Air", "Polestar 2", "Hyundai Ioniq 5",
                "Hyundai Kona Electric", "Kia EV6", "Kia Niro EV", "Nissan Leaf",
                "Volkswagen ID.4", "Volvo XC40 Recharge", "Jaguar I-Pace",
                "Mazda MX-30", "Subaru Solterra", "Toyota bZ4X"
        };

        Random random = new Random();
        return names[random.nextInt(names.length)];
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GenerateScriptGUI().setVisible(true);
            }
        });
    }
}

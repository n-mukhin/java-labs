import com.github.javafaker.Faker;

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
    private Faker faker;

    public GenerateScriptGUI() {
        setTitle("Car Script Generator");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Initialize Faker
        faker = new Faker();

        // Create components
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

        // Set font and colors
        Font font = new Font("Arial", Font.PLAIN, 14);
        addCheckBox.setFont(font);
        updateCheckBox.setFont(font);
        countField.setFont(font);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        selectFileButton.setFont(font);
        fileLabel.setFont(font);

        // Panel for inputs
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.add(new JLabel("Commands:"));
        inputPanel.add(new JPanel()); // Empty cell
        inputPanel.add(addCheckBox);
        inputPanel.add(updateCheckBox);
        inputPanel.add(new JLabel("Count:"));
        inputPanel.add(countField);

        // Panel for file selection
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        filePanel.add(selectFileButton);
        filePanel.add(fileLabel);

        // Generate button
        JButton generateButton = new JButton("Generate Script");
        generateButton.setPreferredSize(new Dimension(160, 30));
        generateButton.setFont(font);
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateScriptInBackground();
            }
        });

        // Add file selection button listener
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectFile();
            }
        });

        // Add components to frame
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
                script.append(generateAddCommand(random)).append("\n\n\n\n\n"); // 10-line gap
            }
        }

        if (isUpdateSelected) {
            for (int i = 0; i < count; i++) {
                if (isAddSelected) {
                    script.append(generateUpdateCommand(random, count)).append("\n\n\n\n\n"); // 10-line gap
                } else {
                    script.append(generateUpdateCommand(random, i + 1)).append("\n\n\n\n\n"); // 10-line gap
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
            commandBuilder.append("\n"); // Empty field
        }
        if (random.nextBoolean()) {
            commandBuilder.append(random.nextInt(99) + 1).append("\n");
        } else {
            commandBuilder.append("\n"); // Empty field
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
        // Handle two penultimate fields which can be empty
        if (random.nextBoolean()) {
            commandBuilder.append(random.nextInt(99) + 1).append("\n"); // Ensure non-zero
        } else {
            commandBuilder.append("\n"); // Empty field
        }
        if (random.nextBoolean()) {
            commandBuilder.append(random.nextInt(99) + 1).append("\n"); // Ensure non-zero
        } else {
            commandBuilder.append("\n"); // Empty field
        }
        // Handle the last field which can be empty or a number from 1 to 4
        if (random.nextBoolean()) {
            commandBuilder.append(random.nextInt(4) + 1).append("\n"); // Number from 1 to 4
        } else {
            commandBuilder.append("\n"); // Empty field
        }
        return commandBuilder.toString();
    }

    private String generateRandomName() {
        String name;
        name = faker.ancient().hero();
        return name;
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

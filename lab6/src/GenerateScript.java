import com.github.javafaker.Faker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class GenerateScript {
    private File selectedFile;
    private Faker faker;

    public GenerateScript() {
     
        faker = new Faker();
    }

    private void selectFile() {
        Scanner scanner = new Scanner(System.in);
        File currentDirectory = new File(System.getProperty("user.dir"));
        File[] files = currentDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files != null && files.length > 0) {
            System.out.println("Available .txt files in the current directory:");
            for (int i = 0; i < files.length; i++) {
                System.out.println((i + 1) + ". " + files[i].getName());
            }
            while (true) {
                System.out.print("Enter the number corresponding to the file or press Enter to skip: ");
                String fileIndexInput = scanner.nextLine().trim();
                if (!fileIndexInput.isEmpty()) {
                    try {
                        int fileIndex = Integer.parseInt(fileIndexInput);
                        if (fileIndex >= 1 && fileIndex <= files.length) {
                            selectedFile = files[fileIndex - 1];
                            break;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                    System.out.println("Invalid input. Please enter a valid number or press Enter to skip.");
                } else {
                    System.out.println("Skipping file selection.");
                    return;
                }
            }
        } else {
            System.out.println("No .txt files found in the current directory. Using default file name.");
        }
        selectedFile = new File("default_script.txt");
    }

    private int promptNumberOfCommands() {
        Scanner scanner = new Scanner(System.in);
        int count;
        while (true) {
            System.out.print("Enter the number of commands to generate: ");
            try {
                count = Integer.parseInt(scanner.nextLine().trim());
                break;
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
        return count;
    }

    private boolean promptAddCommands() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Do you want to generate 'Add' commands? (yes/no): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (choice.equals("yes")) {
                return true;
            } else if (choice.equals("no")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'yes' or 'no'.");
            }
        }
    }

    private boolean promptUpdateCommands() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Do you want to generate 'Update ID' commands? (yes/no): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (choice.equals("yes")) {
                return true;
            } else if (choice.equals("no")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'yes' or 'no'.");
            }
        }
    }

    private void generateScript(boolean isAddSelected, boolean isUpdateSelected, int count) {
        if (!isAddSelected && !isUpdateSelected) {
            System.out.println("Please select at least one command (Add or Update ID)");
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

        System.out.println(script.toString());

        if (selectedFile != null) {
            try (FileWriter writer = new FileWriter(selectedFile)) {
                writer.write(script.toString());
                System.out.println("Script saved to " + selectedFile.getAbsolutePath());
            } catch (IOException ex) {
                System.out.println("Error saving script: " + ex.getMessage());
            }
        } else {
            System.out.println("No file selected for saving");
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
        commandBuilder.append(random.nextInt(99) + 1).append("\n");
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
        return faker.ancient().hero();
    }

    public static void main(String[] args) {
        GenerateScript generator = new GenerateScript();
        generator.selectFile();
        int count = generator.promptNumberOfCommands();
        boolean isAddSelected = generator.promptAddCommands();
        boolean isUpdateSelected = generator.promptUpdateCommands();
        generator.generateScript(isAddSelected, isUpdateSelected, count);
    }
}

package Server;

import java.util.Scanner;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ConsoleListener implements Runnable {
    private final CollectionManager collectionManager;
    private static final Logger logger = LogManager.getLogger(ConsoleListener.class);

    public ConsoleListener(CollectionManager collectionManager, String envKey) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if ("save".equalsIgnoreCase(input.trim())) {
                collectionManager.saveCollection("envKey");
                logger.info("Collection saved via console command");
            } else {
                logger.warn("Unknown command: " + input);
            }
        }
    }
}

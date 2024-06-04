package Client;

import Client.Collection.Vehicle;
import Common.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainClient {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final String ENV_KEY = "lab6"; // Replace with your environment key
    private static final int CHUNK_SIZE = 20480;
    private static Selector selector;
    private static SocketChannel clientChannel;
    private static ScheduledExecutorService scheduler;
    private static boolean isReconnecting = false;
    private static boolean isFirstTime = true;

    public static void main(String[] args) throws ClassNotFoundException {
        if (isFirstTime) {
            printWelcomeMessage();
            isFirstTime = false;
        }
        String fileName = System.getenv(ENV_KEY);
        if (fileName == null || fileName.isEmpty()) {
            System.err.println("Environment variable not set.");
            return;
        }

        try {
            selector = Selector.open();
            scheduler = Executors.newScheduledThreadPool(1);
            setupConnection();

            Executors.newCachedThreadPool().submit(MainClient::handleUserInput);
            Executors.newCachedThreadPool().submit(MainClient::handleServerResponse);
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            scheduleReconnect();
        }
    }

    private static void printWelcomeMessage() {
        System.out.println("\nWelcome to Cars 4!");
        System.out.println("Type \"help\" for assistance.\n");
    }

    private static void setupConnection() {
        try {
            if (clientChannel != null && clientChannel.isOpen()) {
                clientChannel.close();
            }
            clientChannel = SocketChannel.open();
            clientChannel.configureBlocking(false);
            clientChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            clientChannel.register(selector, SelectionKey.OP_CONNECT);
            System.out.println("Connection setup initiated.");
        } catch (IOException e) {
            System.err.println("Connection setup failed: " + e.getMessage());
            scheduleReconnect();
        }
    }

    private static void handleUserInput() {
        try {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                Command command = parseCommand(input);

                if (command != null) {
                    sendCommand(command);
                    if (command.getType() == CommandType.EXIT) {
                        clientChannel.close();
                        System.exit(0);
                    }
                } else {
                    System.out.println("Invalid command");
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling user input: " + e.getMessage());
            scheduleReconnect();
        }
    }

    private static void handleServerResponse() {
        try {
            while (true) {
                int readyChannels = selector.select(1000); // Подождать до таймаута 1 секунду
                if (readyChannels == 0) {
                    // Продолжить цикл, если нет готовых каналов
                    continue;
                }

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isConnectable()) {
                        connect(key);
                    } else if (key.isReadable()) {
                        try {
                            read(key);
                        } catch (IOException e) {
                            System.err.println("Error reading from server: " + e.getMessage());
                            key.channel().close();
                            scheduleReconnect();
                        }
                    } else if (key.isWritable()) {
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }

                // Проверяем, не было ли разрыва соединения, чтобы вызвать повторное подключение
                if (!clientChannel.isOpen()) {
                    scheduleReconnect();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling server response: " + e.getMessage());
            scheduleReconnect();
        }
    }

    private static void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (channel.isConnectionPending()) {
                channel.finishConnect();
            }
            channel.configureBlocking(false);
            channel.register(key.selector(), SelectionKey.OP_WRITE);
            System.out.println("Connected to server");
            resetReconnectState(); // Устанавливаем флаг в false после успешного подключения
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            scheduleReconnect();
        }
    }

    private static void read(SelectionKey key) throws IOException, ClassNotFoundException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ByteBuffer buffer = ByteBuffer.allocate(CHUNK_SIZE);
        int bytesRead;
        while ((bytesRead = channel.read(buffer)) > 0) {
            buffer.flip();
            byteArrayOutputStream.write(buffer.array(), 0, bytesRead);
            buffer.clear();
        }

        if (bytesRead == -1) {
            channel.close();
            scheduleReconnect(); // Переподключаемся, если сервер разорвал соединение
            return;
        }

        byte[] responseBytes = byteArrayOutputStream.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(responseBytes);
        ObjectInputStream ois = new ObjectInputStream(byteArrayInputStream);
        Response response = (Response) ois.readObject();
        if (response != null) {
            System.out.println("Received response: " + response.getMessage());
        } else {
            System.out.println("No response from server.");
        }
        System.out.print("> ");
    }

    private static void sendCommand(Command command) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
        oos.writeObject(command);
        oos.flush();

        byte[] commandBytes = byteArrayOutputStream.toByteArray();
        int offset = 0;

        while (offset < commandBytes.length) {
            int length = Math.min(CHUNK_SIZE, commandBytes.length - offset);
            ByteBuffer buffer = ByteBuffer.allocate(length);
            buffer.put(commandBytes, offset, length);
            buffer.flip();
            while (buffer.hasRemaining()) {
                clientChannel.write(buffer);
            }
            offset += length;
        }
    }

    private static Command parseCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String commandName = parts[0];
        String commandArgs = parts.length > 1 ? parts[1] : "";

        switch (commandName) {
            case "add":
                if (Validator.validateName(commandArgs)) {
                    Vehicle vehicle = createVehicleWithFiller(commandArgs);
                    if (vehicle != null) {
                        return new Command(CommandType.ADD, vehicle);
                    }
                } else {
                    System.out.println("Invalid vehicle name.");
                }
                return null;
            case "remove_by_id":
                try {
                    return new Command(CommandType.REMOVE_BY_ID, Long.parseLong(commandArgs));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid ID format.");
                    return null;
                }
            case "show":
                return new Command(CommandType.SHOW, null);
            case "info":
                return new Command(CommandType.INFO, null);
            case "clear":
                return new Command(CommandType.CLEAR, null);
            case "sum_of_capacity":
                return new Command(CommandType.SUM_OF_CAPACITIES, null);
            case "print_field_ascending_engine_power":
                return new Command(CommandType.PRINT_ENGINE_POWER_ASCENDING, null);
            case "print_field_descending_fuel_type":
                return new Command(CommandType.PRINT_FUEL_TYPE_DESCENDING, null);
            case "remove_first":
                return new Command(CommandType.REMOVE_FIRST, null);
            case "remove_head":
                return new Command(CommandType.REMOVE_HEAD, null);
            case "remove_greater":
                try {
                    return new Command(CommandType.REMOVE_GREATER, Double.parseDouble(commandArgs));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid engine power format.");
                    return null;
                }
            case "execute_script":
                return new Command(CommandType.EXECUTE_SCRIPT, commandArgs);
            case "update_id":
                long idToUpdate;
                try {
                    idToUpdate = Long.parseLong(commandArgs);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid ID format.");
                    return null;
                }

                System.out.println("Please provide the new name for the vehicle:");
                String newName = scanner.nextLine();
                if (!Validator.validateName(newName)) {
                    System.out.println("Invalid vehicle name.");
                    return null;
                }

                System.out.println("Please provide the updated details for the vehicle:");
                Vehicle updatedVehicle = createVehicleWithFiller(newName);
                if (updatedVehicle != null) {
                    updatedVehicle.setId(idToUpdate); // Set the same ID for the updated vehicle
                    return new Command(CommandType.UPDATE_ID, updatedVehicle);
                } else {
                    System.out.println("Invalid vehicle data.");
                }
                return null;
            case "exit":
                return new Command(CommandType.EXIT, null);
            case "help":
                return new Command(CommandType.HELP, null);
            default:
                return null;
        }
    }

    private static Vehicle createVehicleWithFiller(String name) {
        ConsoleReader reader = new ConsoleReader();
        return Filler.readVehicle(reader, name, ENV_KEY);
    }

    private static void scheduleReconnect() {
        if (!isReconnecting) {
            isReconnecting = true;
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdownNow(); // Остановить предыдущий планировщик
            }
            scheduler = Executors.newScheduledThreadPool(1); // Создать новый планировщик
            scheduler.scheduleWithFixedDelay(() -> {
                try {
                    System.out.println("Attempting to reconnect to the server...");
                    setupConnection();
                } catch (Exception e) {
                    System.err.println("Reconnect attempt failed: " + e.getMessage());
                }
            }, 0, 5, TimeUnit.SECONDS);
        }
    }

    private static void resetReconnectState() {
        isReconnecting = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        scheduler = Executors.newScheduledThreadPool(1);
    }
}

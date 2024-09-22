package Client;

import Common.*;
import Collection.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

public class MainClient {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final int CHUNK_SIZE = 10240;
    private static Selector selector;
    private static SocketChannel clientChannel;
    private static ScheduledExecutorService scheduler;
    private static boolean isReconnecting = false;
    private static boolean isAuthenticated = false;
    private static int connectionAttempts = 0;


    private static volatile boolean isWaitingForResponse = false;

    public static void main(String[] args) throws InterruptedException {
        try {
            selector = Selector.open();
            scheduler = Executors.newScheduledThreadPool(1);

            attemptConnectionUntilSuccessful();

            Thread.sleep(1000);
            authenticateUser();

            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.submit(MainClient::handleUserInput);
            executor.submit(MainClient::handleServerResponse);

        } catch (IOException | InterruptedException e) {
            System.err.println("Client error: " + e.getMessage());
            scheduleReconnect();
            Thread.sleep(1000);
        }
    }

    private static void attemptConnectionUntilSuccessful() throws InterruptedException {
        while (true) {
            try {
                setupConnection();
                if (waitForConnection()) {
                    System.out.println("Connected to the server after " + connectionAttempts + " attempts.");
                    break;
                }
            } catch (IOException e) {
                connectionAttempts++;
                System.err.println("Connection attempt #" + connectionAttempts + " failed: " + e.getMessage());
                Thread.sleep(1000);
            }
        }
    }

    private static void setupConnection() throws IOException, InterruptedException {
        if (clientChannel != null && clientChannel.isOpen()) {
            clientChannel.close();
        }
        clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);
        clientChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        clientChannel.register(selector, SelectionKey.OP_CONNECT);
        if (connectionAttempts == 0) {
            System.out.println("Connection setup initiated.");
            Thread.sleep(1000);
        }
    }

    private static boolean waitForConnection() throws IOException, InterruptedException {
        while (true) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (key.isConnectable()) {
                    return connect(key);
                }
            }
        }
    }

    private static boolean connect(SelectionKey key) throws IOException, InterruptedException {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (channel.isConnectionPending()) {
                channel.finishConnect();
            }
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            System.out.println("Connected to server");
            resetReconnectState();
            return true;
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            Thread.sleep(1000);
            return false;
        }
    }

    private static void authenticateUser() throws IOException {
        while (!isAuthenticated) {
            System.out.println("Please log in or register:");

            String command = "";
            while (true) {
                System.out.print("Enter '1' for login or '2' for register: ");
                command = scanner.nextLine().trim();

                if (command.equals("1") || command.equals("2")) {
                    break;
                } else {
                    System.out.println("Invalid input. Please enter '1' for login or '2' for register.");
                }
            }

            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            Command authCommand;
            if (command.equals("2")) {
                authCommand = new Command(CommandType.REGISTER, new UserCredentials(username, password));
            } else {
                authCommand = new Command(CommandType.LOGIN, new UserCredentials(username, password));
            }

            sendCommand(authCommand);
            isWaitingForResponse = true;
            handleAuthenticationResponse();
        }

        printWelcomeMessage();
    }


    private static void handleAuthenticationResponse() throws IOException {
        long startTime = System.currentTimeMillis();
        long timeout = 10000;

        try {
            while (isWaitingForResponse) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime > timeout) {
                    System.out.println("Authentication timeout. Please try again.");
                    isWaitingForResponse = false;
                    return;
                }

                int readyChannels = selector.select(1000);
                if (readyChannels == 0) {
                    continue;
                }

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isReadable()) {
                        Response response = readResponse(key);
                        if (response != null) {
                            processAuthenticationResponse(response);
                            return;
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling authentication response: " + e.getMessage());
            scheduleReconnect();
        }
    }




    private static Vehicle createVehicleWithFiller(String name) throws SQLException, IOException, ClassNotFoundException {
        ConsoleReader reader = new ConsoleReader();
        return Filler.readVehicle(reader, name);
    }

    public static Response readResponse(SelectionKey key) throws IOException, ClassNotFoundException {
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
            scheduleReconnect();
            return null;
        }

        byte[] responseBytes = byteArrayOutputStream.toByteArray();
        if (responseBytes.length == 0) {
            return null;
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(responseBytes);
        ObjectInputStream ois = new ObjectInputStream(byteArrayInputStream);
        Response response = (Response) ois.readObject();

        return response;
    }

    private static void processAuthenticationResponse(Response response) throws IOException {
        if (response.getType() == ResponseType.AUTH_SUCCESS) {
            isAuthenticated = true;
            isWaitingForResponse = false;
            System.out.println("Authentication successful: " + response.getMessage());
        } else if (response.getType() == ResponseType.AUTH_FAILURE) {
            isAuthenticated = false;
            isWaitingForResponse = false;
            System.out.println("Authentication failed: " + response.getMessage());

            authenticateUser();
        } else {
            System.out.println("Received unexpected response: " + response.getMessage());
            isWaitingForResponse = false;
        }
    }




    private static void sendCommand(Command command) throws IOException {
        if (isWaitingForResponse) {
            System.out.println("Please wait for the server's response before sending a new command.");
            return;
        }

        isWaitingForResponse = true;
        System.out.println("Sending command to the server: " + command.getType());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
        oos.writeObject(command);
        oos.flush();

        byte[] commandBytes = byteArrayOutputStream.toByteArray();
        ByteBuffer buffer = ByteBuffer.wrap(commandBytes);

        while (buffer.hasRemaining()) {
            int bytesWritten = clientChannel.write(buffer);
            if (bytesWritten == 0) {
                clientChannel.register(selector, SelectionKey.OP_WRITE);
                break;
            }
        }


        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Command sent. Waiting for server response.");
    }




    private static void printWelcomeMessage() {
        System.out.println("\nWelcome to Cars 4!");
        System.out.println("Type \"help\" for assistance.\n");
    }

    private static void handleUserInput() {
        try {
            while (true) {
                if (!isAuthenticated) {
                    System.out.println("You are not authenticated. Please log in or register.");
                    authenticateUser();
                    continue;
                }

                System.out.print("> ");
                String input = scanner.nextLine();

                if (isWaitingForResponse) {
                    System.out.println("Waiting for the server's response. Please wait.");
                    continue;
                }

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
        } catch (IOException | SQLException e) {
            System.err.println("Error handling user input: " + e.getMessage());
            scheduleReconnect();
        }
    }


    private static void handleServerResponse() {
        try {
            while (true) {
                int readyChannels = selector.select(1000);
                if (readyChannels == 0) {
                    continue;
                }

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isReadable()) {
                        try {
                            Response response = readResponse(key);
                            if (response != null) {
                                processServerResponse(response);
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            System.err.println("Error reading from server: " + e.getMessage());
                            key.channel().close();
                            scheduleReconnect();
                        }
                    } else if (key.isWritable()) {
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }

                if (!clientChannel.isOpen()) {
                    scheduleReconnect();
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling server response: " + e.getMessage());
            scheduleReconnect();
        }
    }

    private static void processServerResponse(Response response) throws IOException {
        if (response.getType() == ResponseType.AUTH_SUCCESS) {
            isAuthenticated = true;
            System.out.println("Authentication successful: " + response.getMessage());
        } else if (response.getType() == ResponseType.AUTH_FAILURE) {
            isAuthenticated = false;
            System.out.println("Authentication failed: " + response.getMessage());
            authenticateUser();
        } else {
            System.out.println("Received response: " + response.getMessage());
        }

        isWaitingForResponse = false;
        System.out.print("> ");
    }



    private static Command parseCommand(String input) throws SQLException {
        String[] parts = input.split("\\s+", 2);
        String commandName = parts[0];
        String commandArgs = parts.length > 1 ? parts[1] : "";

        switch (commandName.toLowerCase()) {
            case "add":
                if (Validator.validateName(commandArgs)) {
                    try {
                        Vehicle vehicle = createVehicleWithFiller(commandArgs);
                        if (vehicle != null) {
                            return new Command(CommandType.ADD, vehicle);
                        }
                    } catch (SQLException e) {
                        System.err.println("Error creating vehicle: " + e.getMessage());
                    } catch (IOException | ClassNotFoundException e) {
                        System.err.println("Error creating vehicle: " + e.getMessage());
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
                try {
                    Vehicle updatedVehicle = createVehicleWithFiller(newName);
                    if (updatedVehicle != null) {
                        updatedVehicle.setId(idToUpdate);
                        return new Command(CommandType.UPDATE_ID, updatedVehicle);
                    } else {
                        System.out.println("Invalid vehicle data.");
                    }
                } catch (SQLException e) {
                    System.err.println("Error updating vehicle: " + e.getMessage());
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error updating vehicle: " + e.getMessage());
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

    private static void scheduleReconnect() {
        if (!isReconnecting) {
            isReconnecting = true;
            isAuthenticated = false;
            isWaitingForResponse = false;
            scheduler.scheduleWithFixedDelay(() -> {
                try {
                    connectionAttempts++;
                    System.out.println("Attempting to reconnect to the server... (Attempt #" + connectionAttempts + ")");
                    attemptConnectionUntilSuccessful();
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
        isWaitingForResponse = false;
    }
}

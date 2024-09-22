package Server;

import Collection.Vehicle;
import Common.Command;
import Common.CommandType;
import Common.Response;
import Common.ResponseType;
import Common.UserCredentials;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler {
    private final SocketChannel client;
    private final CollectionManager collectionManager;
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);
    private final ExecutorService commandExecutor;
    private final ExecutorService responseExecutor;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(10240);

    private int userId = -1;
    private static final ConcurrentHashMap<Integer, AtomicInteger> userAuthMap = new ConcurrentHashMap<>();

    public ClientHandler(SocketChannel client, CollectionManager collectionManager,
                         ExecutorService commandExecutor, ExecutorService responseExecutor) {
        this.client = client;
        this.collectionManager = collectionManager;
        this.commandExecutor = commandExecutor;
        this.responseExecutor = responseExecutor;
    }

    public void handleRead() {
        try {
            readBuffer.clear();
            int bytesRead = client.read(readBuffer);
            if (bytesRead == -1) {
                handleClientDisconnection("Client disconnected normally");
                return;
            }

            if (bytesRead == 0) {
                return;
            }

            readBuffer.flip();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(readBuffer.array(), 0, bytesRead);
            ObjectInputStream ois = new ObjectInputStream(byteArrayInputStream);
            Command command = (Command) ois.readObject();

            logger.info("Received command: " + command.getType());

            if (!isUserAuthenticated() && (command.getType() != CommandType.LOGIN && command.getType() != CommandType.REGISTER)) {
                sendResponse(new Response("User not authenticated. Please log in or register.", ResponseType.AUTH_FAILURE));
                return;
            }

            if (command.getType() == CommandType.EXIT) {
                handleClientDisconnection("Client requested exit.");
                return;
            }

            commandExecutor.submit(() -> executeCommand(command));

        } catch (EOFException e) {
            logger.info("Client disconnected abruptly.");
            handleClientDisconnection("Client disconnected abruptly.");
        } catch (IOException e) {
            logger.error("IOException while reading from client: " + e.getMessage(), e);
            handleClientDisconnection("Client disconnected with IOException: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error("Received unknown object from client.", e);
            sendResponse(new Response("Received unknown command.", ResponseType.ERROR));
        }
    }

    private void executeCommand(Command command) {
        try {
            if (command.getType() != CommandType.LOGIN && command.getType() != CommandType.REGISTER) {
                if (userId == -1) {
                    sendResponse(new Response("User ID is invalid. Please log in again.", ResponseType.ERROR));
                    return;
                }
            }
            switch (command.getType()) {
                case REGISTER:
                    handleRegister(command);
                    break;
                case LOGIN:
                    handleLogin(command);
                    break;
                default:
                    if (userId != -1) {
                        CommandExecutor.execute(command, collectionManager, userId, this::sendResponse);
                    } else {
                        sendResponse(new Response("Error: User not authenticated.", ResponseType.ERROR));
                    }
                    break;
            }
        } catch (Exception e) {
            logger.error("Command execution error: ", e);
            sendResponse(new Response("Command execution error: " + e.getMessage(), ResponseType.ERROR));
        }
    }

    private void handleRegister(Command command) {
        UserCredentials credentials = (UserCredentials) command.getArgument();
        boolean success = collectionManager.registerUser(credentials.getUsername(), credentials.getPassword());
        if (success) {
            userId = collectionManager.authenticateUser(credentials.getUsername(), credentials.getPassword());
            if (userId != -1) {
                incrementUserAuth(userId);
                initializeUserCollection();
                sendResponse(new Response("Registration successful.", ResponseType.AUTH_SUCCESS));
            } else {
                sendResponse(new Response("Registration failed during authentication.", ResponseType.AUTH_FAILURE));
            }
        } else {
            sendResponse(new Response("Registration failed. Username may already be taken.", ResponseType.AUTH_FAILURE));
        }
    }

    private void handleLogin(Command command) {
        UserCredentials credentials = (UserCredentials) command.getArgument();
        int authenticatedUserId = collectionManager.authenticateUser(credentials.getUsername(), credentials.getPassword());

        if (authenticatedUserId != -1) {
            this.userId = authenticatedUserId;
            incrementUserAuth(userId);
            initializeUserCollection();
            sendResponse(new Response("Login successful.", ResponseType.AUTH_SUCCESS));
        } else {
            sendResponse(new Response("Login failed. Incorrect username or password.", ResponseType.AUTH_FAILURE));
        }
    }

    private void initializeUserCollection() {
        logger.info("Collection initialized successfully for user: " + userId);
    }

    private void sendResponse(Response response) {
        responseExecutor.submit(() -> {
            try {
                if (!client.isOpen()) {
                    logger.warn("Attempted to send response to a closed channel.");
                    return;
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
                oos.writeObject(response);
                oos.flush();

                byte[] responseBytes = byteArrayOutputStream.toByteArray();
                ByteBuffer writeBuffer = ByteBuffer.wrap(responseBytes);

                while (writeBuffer.hasRemaining()) {
                    client.write(writeBuffer);
                }

                logger.info("Sent response: " + response.getMessage());

            } catch (ClosedChannelException e) {
                logger.error("Error sending response: Channel is closed", e);
                handleClientDisconnection("Error sending response: Channel is closed");
            } catch (IOException e) {
                logger.error("IOException sending response: " + e.getMessage(), e);
                handleClientDisconnection("Error sending response: " + e.getMessage());
            }
        });
    }

    private void handleClientDisconnection(String message) {
        try {
            if (client.isOpen()) {
                client.close();
            }
            logger.info("ClientHandler: " + message + " for userId: " + userId);
        } catch (IOException e) {
            logger.error("Error closing client connection: ", e);
        } finally {
            if (userId != -1) {
                boolean shouldSave = decrementUserAuth(userId);
                if (shouldSave) {
                    collectionManager.saveCollection(userId);
                    logger.info("Collection saved for userId: " + userId);
                }
            }
        }
    }

    private boolean isUserAuthenticated() {
        if (userId == -1) return false;
        AtomicInteger count = userAuthMap.get(userId);
        return count != null && count.get() > 0;
    }

    private void incrementUserAuth(int userId) {
        userAuthMap.compute(userId, (key, count) -> {
            if (count == null) {
                return new AtomicInteger(1);
            } else {
                count.incrementAndGet();
                return count;
            }
        });
        logger.info("UserAuthMap updated: userId=" + userId + ", count=" + userAuthMap.get(userId).get());
    }

    private boolean decrementUserAuth(int userId) {
        return userAuthMap.computeIfPresent(userId, (key, count) -> {
            if (count.decrementAndGet() <= 0) {
                logger.info("No more connections for userId: " + userId);
                return null;
            }
            logger.info("UserAuthMap updated: userId=" + userId + ", count=" + count.get());
            return count;
        }) == null;
    }
}

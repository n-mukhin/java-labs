package Server;

import Common.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler implements Runnable {
    private final SocketChannel client;
    private final CollectionManager collectionManager;
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);
    private final String envKey;
    private final String userId;
    private final Selector selector;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ByteBuffer readBuffer = ByteBuffer.allocate(10240);

    public ClientHandler(SocketChannel client, CollectionManager collectionManager, String envKey, String userId, Selector selector) {
        this.client = client;
        this.collectionManager = collectionManager;
        this.envKey = envKey;
        this.userId = userId;
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ, this);
            logger.info("Client connected: " + client.getRemoteAddress());
        } catch (IOException e) {
            logger.error("Error configuring client channel: ", e);
        }
    }

    public void handleRead() {
        try {
            readBuffer.clear();
            int bytesRead = client.read(readBuffer);
            if (bytesRead == -1) {
                handleClientDisconnection("Client disconnected normally");
                return;
            }

            readBuffer.flip();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(readBuffer.array(), 0, bytesRead);
            ObjectInputStream ois = new ObjectInputStream(byteArrayInputStream);
            Command command = (Command) ois.readObject();

            logger.info("Received command: " + command.getType());

            
            executorService.submit(() -> {
                try {
                    CommandExecutor.execute(command, collectionManager, userId, this::sendResponse);
                } catch (Exception e) {
                    logger.error("Error executing command: ", e);
                    sendResponse(new Response("Error executing command: " + e.getMessage(), ResponseType.ERROR));
                }
            });
        } catch (IOException | ClassNotFoundException e) {
            handleClientDisconnection("Client disconnected with error: " + e.getMessage());
        }
    }

    private synchronized void sendResponse(Response response) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
            oos.writeObject(response);
            oos.flush();

            byte[] responseBytes = byteArrayOutputStream.toByteArray();
            int offset = 0;
            int chunkSize = 20480;

            while (offset < responseBytes.length) {
                int length = Math.min(chunkSize, responseBytes.length - offset);
                ByteBuffer writeBuffer = ByteBuffer.allocate(length);
                writeBuffer.put(responseBytes, offset, length);
                writeBuffer.flip();

                while (writeBuffer.hasRemaining()) {
                    client.write(writeBuffer);
                }

                offset += length;
            }

            logger.info("Sent response for command: " + response.getMessage());
            client.register(selector, SelectionKey.OP_READ, this);
        } catch (IOException e) {
            logger.error("Error sending response: ", e);
            handleClientDisconnection("Error sending response: " + e.getMessage());
        }
    }

    private void handleClientDisconnection(String message) {
        try {
            if (client.isOpen()) {
                client.close();
            }
            collectionManager.saveCollection(envKey);
            logger.info(message);
        } catch (IOException e) {
            logger.error("Error closing client connection: ", e);
        }
    }
}

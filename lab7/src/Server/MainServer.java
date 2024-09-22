package Server;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {
    private static final Logger logger = LogManager.getLogger(MainServer.class);
    private static final CollectionManager collectionManager = CollectionManager.getInstance();
    private static final ExecutorService readExecutor = Executors.newFixedThreadPool(10);
    private static final ExecutorService commandExecutor = Executors.newCachedThreadPool();
    private static final ExecutorService responseExecutor = Executors.newCachedThreadPool();
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        BasicConfigurator.configure();
        new MainServer().start();
    }

    public void start() {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocket = ServerSocketChannel.open()) {

            configureServerSocket(serverSocket);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            logger.info("Server started on port " + SERVER_PORT);

            while (true) {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    try {
                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isAcceptable()) {
                            acceptConnection(selector, serverSocket);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        }
                    } catch (CancelledKeyException e) {
                        logger.warn("Cancelled key encountered and skipped.", e);
                        key.cancel();
                    } catch (IOException e) {
                        logger.error("Error processing selection key:", e);
                        key.cancel();
                    }
                }

                selectedKeys.clear();
            }

        } catch (IOException e) {
            logger.error("Server encountered an error:", e);
        } finally {
            shutdownExecutorServices();
        }
    }

    private void configureServerSocket(ServerSocketChannel serverSocket) throws IOException {
        serverSocket.bind(new InetSocketAddress(SERVER_PORT));
        serverSocket.configureBlocking(false);
    }

    private void acceptConnection(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel clientChannel = serverSocket.accept();
        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            ClientHandler clientHandler = new ClientHandler(clientChannel, collectionManager, commandExecutor, responseExecutor);
            clientChannel.register(selector, SelectionKey.OP_READ, clientHandler);
            logger.info("New connection from " + clientChannel.getRemoteAddress());
        }
    }

    private void handleRead(SelectionKey key) {
        ClientHandler clientHandler = (ClientHandler) key.attachment();
        if (clientHandler != null) {
            readExecutor.submit(() -> clientHandler.handleRead());
        }
    }

    private void shutdownExecutorServices() {
        readExecutor.shutdown();
        commandExecutor.shutdown();
        responseExecutor.shutdown();
    }
}

package Server;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {
    private static final Logger logger = LogManager.getLogger(MainServer.class);
    public static final String ENV_KEY = "lab6";
    private static final CollectionManager collectionManager = CollectionManager.getInstance(ENV_KEY);
    private static final ExecutorService clientPool = Executors.newCachedThreadPool();
    private static final String COLLECTION_FILE_PATH = System.getenv(ENV_KEY);

    public static void main(String[] args) {
        BasicConfigurator.configure();

        
        collectionManager.initializeCollectionIfNeeded(COLLECTION_FILE_PATH);

        
        new Thread(new ConsoleListener(collectionManager, ENV_KEY)).start();

        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocket = ServerSocketChannel.open()) {

            serverSocket.bind(new InetSocketAddress(12345));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            logger.info("Server started on port 12345");

            while (true) {
                selector.select();
                var selectedKeys = selector.selectedKeys();
                var iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    var key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        acceptConnection(selector, serverSocket);
                    } else if (key.isReadable()) {
                        ClientHandler handler = (ClientHandler) key.attachment();
                        if (handler != null) {
                            handler.handleRead();
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Server error: ", e);
        } finally {
            clientPool.shutdown();
        }
    }

    private static void acceptConnection(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        if (client != null) {
            client.configureBlocking(false);
            String userId = UUID.randomUUID().toString();
            ClientHandler clientHandler = new ClientHandler(client, collectionManager, ENV_KEY, userId, selector);
            client.register(selector, SelectionKey.OP_READ, clientHandler);
            clientPool.submit(clientHandler);
            logger.info("New connection from " + client.getRemoteAddress());
        }
    }
}

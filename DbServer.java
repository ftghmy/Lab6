package org.dima.server;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Класс серверверного приложения
 */
public class DbServer {
    public final static Logger logger = LoggerFactory.getLogger(DbServer.class);

    private Selector selector;

    private Map<SocketChannel, DbWorker> workers = new LinkedHashMap<SocketChannel, DbWorker>();

    private final static int DEFAULT_PORT = 9090;

    private InetAddress hostAddress = null;

    private int port;

    private MoviesDB moviesDB;

    public DbServer(MoviesDB db) throws IOException {
        this(DEFAULT_PORT, db);
    }

    /**
     * Конструктор создающий сервер
     * @param port порт
     * @param db база данных
     * @throws IOException
     */
    public DbServer(int port, MoviesDB db) throws IOException {
        this.moviesDB = db;
        this.port = port;
        selector = SelectorProvider.provider().openSelector();

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        InetSocketAddress isa = new InetSocketAddress(port);
        serverChannel.socket().bind(isa);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * главный цикл сервера ,который принимает подключение и команды
     */
    private void loop() {
            while (true) {
            try {
                selector.select();
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
//                        write(key);
                    }
                }
            } catch (Exception e) {
                logger.error("Main loop error", e);
                System.exit(1);
            }
        }
    }

    /**
     * Метод принятия подключения создающий новый канал для работы с клиентом
     * @param key ключ селектора
     * @throws IOException
     */
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        workers.put(socketChannel, new DbWorker(socketChannel, moviesDB));

        logger.info("Client is connected (clients = " + workers.size() + ")");
    }

    /**
     * Метод читающий сообщения
     * @param key ключ селектора
     * @throws IOException
     */
    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        DbWorker worker = workers.get(socketChannel);

        if(worker != null) {
            if(worker.read() < 0) {
                key.channel().close();
                key.cancel();
                workers.remove(socketChannel);
                logger.info("Client is disconnected (clients = " + workers.size() + ")");
            }
        }

    }

    /**
     * Главный метод сервера
     * @param args Аргументы путь к файлу базы данных, порт сервера
     */
    public static void main(String[] args) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);

        if (args.length < 2) {
            System.err.println("Usage: Lab6Server path port");
            System.exit(-1);
        }

        String file_name = args[0];
        int port = 0;
        try {
            port = Integer.parseInt(args[1]);
        } catch(Exception e) {
            System.err.println("Usage: Lab6Server path port");
            System.err.println("Port must be integer");
            System.exit(-1);
        }

        Path path = Paths.get(file_name);
        if(!Files.exists(path)) {
            System.out.println("File " + path + " oes not exists. Creating new ...");
            try {
                Files.createFile(path);
                System.out.println("File " + path + " is created.");
            } catch (IOException e) {
                System.out.println("File " + path + " can not be created.");
                e.printStackTrace();
                System.err.println("Port must be integer");
            }
        }

        try {
            MoviesDB db = new MoviesDB(path);
            DbServer server = new DbServer(port, db);
            logger.info("Server started");
            server.loop();
            logger.info("Server stopped");
            db.save();
        } catch (java.net.BindException e) {
            logger.error("Server error: Порт " + port + " занят. Возможно экземпляр сервера уже запущен.");
        } catch (Exception e) {
            logger.error("Server stopped", e);
        }
    }
}

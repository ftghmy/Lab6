package org.dima.server;

import org.dima.commands.*;
import org.dima.movies.Movie;
import org.dima.tools.ObjectSizeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Основной класс обработки команд
 */
public class DbWorker {
    public final static Logger logger = LoggerFactory.getLogger(DbWorker.class);

    private static final int BUFFER_SIZE = 1024;

    private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    SocketChannel socketChannel;

    private MoviesDB moviesDB;

    /**
     * Конструктор обработчика команд
     * @param channel серверный канал
     * @param db база данных
     * @throws IOException
     */
    public DbWorker(SocketChannel channel, MoviesDB db) throws IOException {
        moviesDB = db;
        socketChannel = channel;
        socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
    }

    /**
     * Метод обработки сообщений
     * @return в случае успеха колличество принятых байт отрицательные числа в случае разрыва канала
     */
    public int read()  {
        // Clear out our read buffer so it's ready for new data
        readBuffer.clear();

        // Attempt to read off the channel
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int numRead;
        try {
            while((numRead = socketChannel.read(readBuffer)) > 0) {
                baos.write(readBuffer.array());
                readBuffer.clear();
            }
         } catch (IOException e) {
            logger.warn("Client Forceful shutdown");
            return -2;
        }

        if (numRead == -1) {
            logger.info("Client Graceful shutdown");
            return -1;
        }

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object object = ois.readObject();
            if(!(object instanceof MovieCommand)) {
                throw new ClassNotFoundException("Invalid command");
            }
            logger.info("Receive command: " + object + " (size=" + bais.available() + ")");
            CommandResult result = call((MovieCommand) object);
            if(result != null) {
                ByteArrayOutputStream res_baos = new ByteArrayOutputStream();
                ObjectOutputStream res_oos = new ObjectOutputStream(res_baos);
                res_oos.writeObject(result);
                ByteBuffer writeBuffer = ByteBuffer.allocate(res_baos.size());
                writeBuffer.put(res_baos.toByteArray());
                writeBuffer.flip();
                int num_bytes = 0;
                while(writeBuffer.hasRemaining()) {
                    num_bytes += socketChannel.write(writeBuffer);
                }
                logger.info("Send answer: " + result.getType() + " (size=" + num_bytes + ")");
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Reading error", e);
        }

        return numRead;
    }

    /**
     * Метод вызова команды базы данных
     * @param command команда
     * @return результат исполнения команды
     */
    private CommandResult call(MovieCommand command) {
        if(command instanceof TestCommand) {
            return new CommandResult();
        } else if(command instanceof ShowCommand) {
            ArrayList<Movie> movies  = new ArrayList<Movie>(moviesDB.getMovies().values());
            movies.sort(new ObjectSizeComparator());
            return new CommandResultWithObject(movies);
        } else if(command instanceof InfoCommand) {
            return new CommandResultWithObject(moviesDB.getInformation());
        } else if(command instanceof FindByNameCommand) {
            Long id  = moviesDB.findByName(((FindByNameCommand) command).getKey());
            if(id == null) {
                return new CommandResultWithObject(CommandResult.Type.WARNING, "Not found");
            } else {
                Movie movie =  moviesDB.findById(id);
                return new CommandResultWithObject(movie);
            }
        } else if(command instanceof FindByIdCommand) {
            Movie movie = moviesDB.findById(((FindByIdCommand) command).getId());
            if(movie == null) {
                return new CommandResultWithObject(CommandResult.Type.WARNING, "Not found");
            } else {
                return new CommandResultWithObject(movie);
            }
        } else if(command instanceof ClearCommand) {
            try {
                moviesDB.getMovies().clear();
                moviesDB.save();
                return new CommandResult();
            } catch (Exception e) {
                return new CommandResult(CommandResult.Type.ERROR, e.getMessage());
            }
        } else if(command instanceof InsertCommand) {
            try {
                moviesDB.insert(((InsertCommand) command).getMovie());
                moviesDB.save();
                return new CommandResult();
            } catch (Exception e) {
                return new CommandResult(CommandResult.Type.ERROR, e.getMessage());
            }
        } else if(command instanceof UpdateCommand) {
            try {
                moviesDB.update(((UpdateCommand) command).getId(), ((UpdateCommand) command).getMovie());
                moviesDB.save();
                return new CommandResult();
            } catch (Exception e) {
                return new CommandResult(CommandResult.Type.ERROR, e.getMessage());
            }
        } else if(command instanceof RemoveCommand) {
            try {
                if(moviesDB.remove(((RemoveCommand) command).getKey())) {
                    moviesDB.save();
                    return new CommandResult();
                } else {
                    return new CommandResult(CommandResult.Type.WARNING, "There is no Record with key " + ((RemoveCommand) command).getKey());
                }
            } catch (Exception e) {
                return new CommandResult(CommandResult.Type.ERROR, e.getMessage());
            }
        } else if(command instanceof RemoveLowerKeyCommand) {
            try {
                if(moviesDB.removeLowerKey(((RemoveLowerKeyCommand) command).getKey()) > 0) {
                    moviesDB.save();
                    return new CommandResult();
                } else {
                    return new CommandResult(CommandResult.Type.WARNING, "There is no Records with key lower then " + ((RemoveLowerKeyCommand) command).getKey());
                }
            } catch (Exception e) {
                return new CommandResult(CommandResult.Type.ERROR, e.getMessage());
            }
        } else if(command instanceof FilterContainsNameCommand) {
            ArrayList<Movie> movies  = new ArrayList<Movie>(moviesDB.filterByName(((FilterContainsNameCommand) command).getKey()));
            movies.sort(new ObjectSizeComparator());
            return new CommandResultWithObject(movies);
        } else if(command instanceof PrintFieldAscendingGenreCommand) {
            ArrayList<Movie> movies  = new ArrayList<Movie>(moviesDB.filterByGenre(((PrintFieldAscendingGenreCommand) command).getGenre()));
            movies.sort(new ObjectSizeComparator());
            return new CommandResultWithObject(movies);
        } else if(command instanceof RemoveLowerCommand) {
            try {
                if(moviesDB.removeLower(((RemoveLowerCommand) command).getMovie()) > 0) {
                    moviesDB.save();
                    return new CommandResult();
                } else {
                    return new CommandResult(CommandResult.Type.WARNING, "There is no Records with key lower then " + ((RemoveLowerCommand) command).getMovie());
                }
            } catch (Exception e) {
                return new CommandResult(CommandResult.Type.ERROR, e.getMessage());
            }
        } else if(command instanceof ReplaceIfGreaterCommand) {
            try {
                if(moviesDB.replaceIfGreater(((ReplaceIfGreaterCommand) command).getKey(),((ReplaceIfGreaterCommand) command).getMovie())) {
                    moviesDB.save();
                    return new CommandResult();
                } else {
                    return new CommandResult(CommandResult.Type.WARNING, "There is no Records with key greater then " + ((ReplaceIfGreaterCommand) command).getKey());
                }
            } catch (Exception e) {
                return new CommandResult(CommandResult.Type.ERROR, e.getMessage());
            }

        } else if(command instanceof MaxByNameCommand) {
            Movie movie = moviesDB.maxByName();
            if(movie != null) {
                return new CommandResultWithObject(movie);
            } else {
                return new CommandResultWithObject(CommandResult.Type.WARNING, "Collection is empty");

            }
        }else {
            logger.error("Unknown command => " + command);
        }
        return null;
    }
}

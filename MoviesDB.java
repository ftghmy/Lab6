package org.dima.server;

import org.dima.movies.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Этот класс реализует взаимодействие с коллекцией
 */
public class MoviesDB {
    final static Logger logger = LoggerFactory.getLogger(DbWorker.class);
    final public static Locale defaultLocale = new Locale("ru");;

    private MoviesDbInformation information;

    /**
     * Конструктор класса Базы Данных
     *
     * @param source Файл источника данных XML
     * @throws XmlParseException Неверный формат данных
     */
    public MoviesDB(Path source) throws XmlParseException {
        Locale.setDefault(defaultLocale);
        this.source = source;
        this.movies = new LinkedHashMap<Long, Movie>();
        this.nameIndex = new TreeMap<String, Long>();
        parseXmlFile(source);

        //runTests();
    }

    /**
     * Парсер Базы данных из XML
     *
     * @param source Файл источника данных XML
     * @throws XmlParseException Неверный формат данных
     */
    private void parseXmlFile(Path source) throws XmlParseException {
        try (Scanner scanner = new Scanner(source)) {
            scanner.findWithinHorizon("<MOVIES>", 0);
            scanner.useDelimiter("</MOVIES>");
            while (scanner.hasNext()) {
                if (scanner.findWithinHorizon("<MOVIE>", 0) == null) {
                    break;
                }
                scanner.useDelimiter("</MOVIE>");
                if (scanner.hasNext()) {
                    Movie movie = Movie.fromXml(scanner);
                    insert(movie);
                }
            }
        } catch (IllegalArgumentException | DateTimeParseException | IOException e) {
            throw new XmlParseException(e.getMessage());
        }
    }

    /**
     * Запись данных в XML
     *
     * @param source Файл получателя XML
     * @throws Exception ошибка записи в файл
     */
    public void printToXmlFile(Path source) throws Exception {
        try (PrintWriter writer = new PrintWriter(source.toFile())) {
            writer.println("<MOVIES>");
            for (Movie movie : movies.values()) {
                writer.println("\t<MOVIE>");
                movie.toXml(writer, "\t\t");
                writer.println("\t</MOVIE>");
            }
            writer.println("</MOVIES>");
        }
    }

    public void save() throws Exception {
        printToXmlFile(getSource());
    }

    /**
     * получить объект структуры данных связанных с фильмами
     *
     * @return список фильмов
     */
    public LinkedHashMap<Long, Movie> getMovies() {
        return movies;
    }

    /**
     * получить информацию о списке фильмов
     *
     * @return возвращает объект information
     */
    public MoviesDbInformation getInformation() {
        return new MoviesDbInformation(
                movies.getClass().toString(),
                LocalDateTime.now(),
                movies.size(),
                movies.size() > 0 ? Collections.max(movies.keySet()) : new Long(0)
        );
    }

    /**
     * получить файл источника данных
     *
     * @return возвращает соответствующий файл
     */
    public Path getSource() {
        return source;
    }

    /**
     * втавить фильм
     *
     * @param movie фильм
     */
    public void insert(Movie movie) throws IllegalArgumentException {
        movie.setId(getInformation().getMax_id() + 1);
        movie.setCreationDate(LocalDate.now());
        if (movie.validate()) {
            movies.put(movie.getId(), movie);
            nameIndex.put(movie.getName(), movie.getId());
        } else {
            logger.debug("Movie is invalid");
            throw new IllegalArgumentException("Movie is invalid");
        }
    }

    /**
     * найти по индентификатору
     *
     * @param id индентификатор
     * @return фильм
     */
    public Movie findById(Long id) {
        return movies.get(id);
    }

    /**
     * найти фильм по названию
     *
     * @param name название фильма
     * @return фильм
     */
    public Long findByName(String name) {
        return nameIndex.get(name);
    }

    /**
     * обновить элемент коллекции по индентификатору
     *
     * @param id    индентификатор
     * @param movie новый элемент
     */
    public void update(Long id, Movie movie) throws IllegalArgumentException {
        if (movie != null && movie.validate()) {
            Movie old = findById(id);
            if (old != null) {
                nameIndex.remove(findById(id).getName());
            }
            movies.put(id, movie);
            nameIndex.put(movie.getName(), movie.getId());
        } else {
            logger.debug("Movie is invalid");
            throw new IllegalArgumentException("Movie is invalid");
        }
    }

    /**
     * удалить элемент коллекции
     *
     * @param key значение ключа
     * @return True-если заменил,False-если не заменил
     */
    public boolean remove(String key) {
        Long id = findByName(key);
        if (id != null) {
            movies.remove(id);
            nameIndex.remove(key);
            return true;
        }
        return false;
    }

    /**
     * вывести любой объект из коллекции, значение поля name которого является максимальным
     *
     * @return индентификатор фильма
     */
    public Movie maxByName() {
        try {
            Long id = nameIndex.get(nameIndex.lastKey());
            return findById(id);
        } catch(NoSuchElementException e) {
            return null;
        }
    }

    /**
     * удалить из коллекции все элементы, ключ которых меньше, чем заданный
     *
     * @param key заданный ключ
     * @return колличество удаленных элементов
     */
    public int removeLowerKey(String key) {
        nameIndex.keySet().stream()
                .filter(item -> item.compareTo(key) < 0)
                .collect(Collectors.toList())
                .forEach(this::remove);
        return 1;
    }

    /**
     * вывести элементы, значение поля name которых содержит заданную подстроку
     *
     * @param key заданная подстрока
     * @return отфильтрованный список
     */
    public List<Movie> filterByName(String key) {
        return nameIndex.keySet().stream()
                .filter(item -> item.toUpperCase().contains(key.toUpperCase()))
                .collect(Collectors.toMap(Function.identity(), nameIndex::get))
                .values().stream()
                .filter(movies::containsKey)
                .collect(Collectors.toMap(Function.identity(), movies::get)).values()
                .stream().sorted().collect(Collectors.toList());
    }

    /**
     * вывести значения поля genre всех элементов в порядке возрастания
     *
     * @param genre жанр
     * @return отфильтрованный список
     */
    public List<Movie> filterByGenre(MovieGenre genre) {
        return movies.values().stream()
                .filter(item -> item.getGenre() == genre)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * удалить из коллекции все элементы, меньшие, чем заданный
     *
     * @param movie заданный элемент
     * @return колличество удаленных
     */
    public int removeLower(Movie movie) {
        movies.values().stream()
                .filter(item -> item.compareTo(movie) < 0)
                .map(Movie::getName)
                .collect(Collectors.toList())
                .forEach(this::remove);
        return 1;
    }

    /**
     * заменить значение по ключу, если новое значение больше старого
     *
     * @param key   заданный ключ
     * @param movie заданное значение
     * @return True-если заменил,False-если не заменил
     */
    public boolean replaceIfGreater(String key, Movie movie) {
        if (key == null) {
            return false;
        }
        Long id = nameIndex.get(key);
        Movie old = movies.get(id);
        if (old != null && old.compareTo(movie) <= 0) {
            movie.setId(id);
            movie.setCreationDate(LocalDate.now());
            update(id, movie);
            return true;
        }
        return false;
    }

    private final Path source;
    private final LinkedHashMap<Long, Movie> movies;
    private final TreeMap<String, Long> nameIndex;


   /* private Movie test;
    void runTests() {
        try {
            test = new Movie();
            test.setId(getInformation().max_id + 1);
            test.setName("AAA");
            test.setGenre(MovieGenre.COMEDY);
            test.setCreationDate(LocalDate.now());
            Person director = new Person();
            director.setBirthday(ZonedDateTime.now());
            director.setHairColor(Color.BROWN);
            director.setName("AAAA");
            director.setPassportID("237528735623856");
            Location location = new Location();
            location.setName("AAAAAA");
            location.setX(123);
            location.setY(456);
            director.setLocation(location);
            test.setDirector(director);
            test.setMpaaRating(MpaaRating.G);
            test.setOscarsCount(2);
            Coordinates coordinates = new Coordinates();
            coordinates.setX(123);
            coordinates.setY(567);
            test.setCoordinates(coordinates);

            PrintStream std =  System.out;
            System.setOut(new PrintStream("test.out"));

            testInsert();
            testFindById();
            testFindByName();
            testUpdate();
            testRemove();
            testInsert();
            testMaxByName();
            testRemoveLowerKey();
            testInsert();
            testFilterByName();
            testFilterByGenre();
            testRemoveLower();
            testInsert();
            testReplaceIfGreater();


            System.setOut(std);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    void testInsert() {
        System.err.print("Test insert ...");

        insert(new Movie());
        insert(test);

        System.err.println("OK");
    }

    void testFindById() {
        System.err.print("Test find by id ...");
        Movie movie = findById(getInformation().max_id);
        movie.getId();
        movie = findById(getInformation().max_id + 1);
        if(movie != null) {
            throw new NullPointerException();
        }
        System.err.println("OK");
    }

    void testFindByName() {
        System.err.print("Test find by name ...");
        Long id = findByName("AAA");
        id.toString();

        id = findByName("54274");
        if(id != null) {
            throw new NullPointerException();
        }
        System.err.println("OK");
    }

    void testUpdate() {
        System.err.print("Test update ...");

        update(new Long(213), new Movie());
        update(test.getId(), test);

        System.err.println("OK");
    }

    void testRemove() {
        System.err.print("Test remove ...");
        remove("54274");
        remove("AAA");
        System.err.println("OK");
    }

    void testMaxByName() {
        System.err.print("Test max by name ...");
        maxByName();
        System.err.println("OK");
    }

    void testRemoveLowerKey() {
        System.err.print("Test remove lower key ...");
        removeLowerKey("54274");
        removeLowerKey("AAA");
        System.err.println("OK");
    }

    void testFilterByName() {
        System.err.print("Test filter by name ...");
        findByName("54274");
        findByName("AAA");
        System.err.println("OK");
    }

    void testFilterByGenre() {
        System.err.print("Test filter by genre ...");
        filterByGenre(MovieGenre.COMEDY);
        System.err.println("OK");
    }

    void testRemoveLower() {
        System.err.print("Test remove lower ...");
        Movie movie = new Movie();
        movie.setId(getInformation().max_id + 1);
        movie.setName("BBB");
        removeLower(new Movie());
        removeLower(movie);
        System.err.println("OK");
    }

    void testReplaceIfGreater() {
        System.err.print("Test replace If Greater ...");

        replaceIfGreater("AA", new Movie());
        replaceIfGreater("AA", test);
        replaceIfGreater(null, test);

        System.err.println("OK");
    }

*/


}

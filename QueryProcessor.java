package org.dima.client;

import org.dima.commands.*;
import org.dima.movies.*;
import org.dima.commands.CommandResult;
import org.dima.commands.CommandResultWithObject;
import org.dima.movies.MoviesDbInformation;

import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * класс реализующий обработку запросов пользователей
 */
public class QueryProcessor {
    private Stack<String> scripts = new Stack<>();
    private DbClient client;

    /**
     * конструктор класса
     *
     * @param client база данных к которой идут запросы
     */
    public QueryProcessor(DbClient client) {
        this.client = client;
        //     runTests();
    }

    /**
     * метод обработки запросов
     *
     * @param scanner объект из которого читаются запросы
     * @return завершить обработку запросов или нет
     * @throws IOException
     */

    public boolean process(Scanner scanner) throws IOException {
        if (scanner.hasNext()) {
            String cmd = scanner.next();
            switch (cmd.trim().toUpperCase()) {
                case "EXIT":
                    return true;
                case "HELP":
                    help();
                    break;
                case "SHOW":
                    show();
                    break;
                case "INFO":
                    info();
                    break;
//                case "SAVE":
//                    save();
//                    break;
                case "CLEAR":
                    clear();
                    break;
                case "INSERT":
                    String name = scanner.nextLine();
                    if (name == null || name.isEmpty()) {
                        System.out.println("Ошибка: Поле name не может быть пустым. Повторите ввод.");
                    } else {
                        insert(name.trim());
                    }
                    break;
                case "UPDATE": {
                    Long id = scanner.nextLong();
                    update(id);
                    break;
                }
                case "REMOVE": {
                    String key = scanner.nextLine();
                    if (key == null || key.isEmpty()) {
                        System.out.println("Ошибка: Поле name не может быть пустым. Повторите ввод.");
                    } else {
                        remove(key.trim());
                    }
                    break;
                }
                case "EXECUTE_SCRIPT":
                    String file_name = scanner.nextLine();
                    if(scripts.search(file_name) != -1) {
                        System.out.println("Рекурсивный вызов скрипта.");
                        break;
                    }
                    try (Scanner file_scanner = new Scanner(new File(file_name.trim()))) {
                        scripts.push(file_name);
                        while (file_scanner.hasNext()) {
                            process(file_scanner);
                        }
                    } catch (Exception e) {
                        System.out.println("Ошибка чтения файла.");
                    } finally {
                        scripts.pop();
                    }
                    break;
                case "MAX_BY_NAME": {
                    Movie movie = maxByName();
                    if (movie != null) {
                        System.out.println(movie);
                    }
                    break;
                }
                case "REMOVE_LOWER_KEY": {
                    String key = scanner.nextLine();
                    if (key == null || key.isEmpty()) {
                        System.out.println("Ошибка: Поле name не может быть пустым. Повторите ввод.");
                    } else {
                        removeLowerKey(key.trim());
                    }
                    break;
                }
                case "FILTER_CONTAINS_NAME": {
                    String key = scanner.nextLine();
                    if (key == null || key.isEmpty()) {
                        System.out.println("Ошибка: Поле name не может быть пустым. Повторите ввод.");
                        break;
                    }
                    filterByName(key.trim());
                    break;
                }
                case "PRINT_FIELD_ASCENDING_GENRE": {
                    try {
                        String str = scanner.nextLine();
                        MovieGenre genre = MovieGenre.valueOf(str.trim().toUpperCase());
                        filterByGenre(genre);
                    } catch (IllegalArgumentException ex) {
                        System.out.println("Ошибка: Поле может быть значением только [COMEDY, TRAGEDY, THRILLER]. Повторите ввод.");
                    }
                    break;
                }
                case "REMOVE_LOWER": {
                    Movie movie = new Movie();
                    input(movie, null);
                    removeLower(movie);
                    break;
                }
                case "REPLACE_IF_GREATER": {
                    String key = scanner.nextLine();
                    if (key == null || key.isEmpty()) {
                        System.out.println("Ошибка: Поле name не может быть пустым. Повторите ввод.");
                        break;
                    }
                    Movie movie = new Movie();
                    input(movie, null);
                    replaceIfGreater(key.trim(), movie);
                    break;
                }
                default:
                    System.out.println("!!! Invalid command: " + cmd);
            }
        }
        return false;
    }

    private void help() {
        String help_string = "clear : очистить коллекцию\n" +
                "execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.\n" +
                "exit : завершить программу (без сохранения в файл)\n" +
                "filter_contains_name name : вывести элементы, значение поля name которых содержит заданную подстроку\n" +
                "info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.) \n" +
                "insert key {element} : добавить новый элемент с заданным ключом\n" +
                "max_by_name : вывести любой объект из коллекции, значение поля name которого является максимальным\n" +
                "print_field_ascending_genre genre : вывести значения поля genre в порядке возрастания\n" +
                "replace_if_greater key {element} : заменить значение по ключу, если новое значение больше старого\n" +
                "remove_key key : удалить элемент из коллекции по его ключу\n" +
                "remove_lower {element} : удалить из коллекции все элементы, меньшие, чем заданный\n" +
                "remove_lower_key key : удалить из коллекции все элементы, ключ которых меньше, чем заданный\n" +
                "save : сохранить коллекцию в файл\n" +
                "show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении\n" +
                "update id {element} : обновить значение элемента коллекции, id которого равен заданному\n";


        System.out.println(help_string);
    }

    private void filterByName(String name) throws IOException {
        try {
            List<Movie> movies = (List<Movie>) ((CommandResultWithObject) client.exec(new FilterContainsNameCommand(name))).getObject();
            for (Movie movie : movies) {
                System.out.println(movie);
            }
        } catch (ClassNotFoundException e) {
        }
    }

    private void filterByGenre(MovieGenre genre) throws IOException {
        try {
            List<Movie> movies = (List<Movie>) ((CommandResultWithObject) client.exec(new PrintFieldAscendingGenreCommand(genre))).getObject();
            for (Movie movie : movies) {
                System.out.println(movie);
            }
        } catch (ClassNotFoundException e) {
        }
    }

    private Movie maxByName() throws IOException {
        try {
           CommandResultWithObject result = (CommandResultWithObject) client.exec(new MaxByNameCommand());
           if(result.getType() == CommandResult.Type.SUCCESS) {
               return (Movie) result.getObject();
           } else {
               return null;
           }

        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private void show() throws IOException {
        try {
            List<Movie> movies = (List<Movie>) ((CommandResultWithObject) client.exec(new ShowCommand())).getObject();
            for (Movie movie : movies) {
                System.out.println(movie);
            }
        } catch (ClassNotFoundException e) {
        }
    }

    private void info() throws IOException {
        try {
            MoviesDbInformation info = (MoviesDbInformation) ((CommandResultWithObject) client.exec(new InfoCommand())).getObject();
            System.out.println("Collection     : " + info.getCollection_type());
            System.out.println("Init date      : " + info.getInit_time());
            System.out.println("Elements count : " + info.getElements_count());
            System.out.println("Maximum id     : " + info.getMax_id());
        } catch (ClassNotFoundException e) {
        }
    }

/*    private void save() {
        try {
            db.printToXmlFile(db.getSource());
            System.out.println("Save completed.");
        } catch (Exception e) {
            System.out.println("Save error");
            e.printStackTrace();
        }
    }
*/

    private void clear() throws IOException {
        try {
            CommandResult result = (CommandResult) client.exec(new ClearCommand());
            if (result.getType() == CommandResult.Type.SUCCESS) {
                System.out.println("Clear completed.");
            } else {
                System.out.println("Error: " + result.getError());
            }
        } catch (ClassNotFoundException e) {
        }
    }

    private void insert(String key) throws IOException {
        try {
            CommandResultWithObject result = (CommandResultWithObject) client.exec(new FindByNameCommand(key));
            if (result.getType() == CommandResult.Type.SUCCESS) {
                System.out.println("Movie with name (" + key + ") already exists.");
            } else {
                Movie movie = new Movie();
                movie.setName(key);
                input(movie, key);
                CommandResult res = (CommandResult) client.exec(new InsertCommand(movie));
                if (res.getType() == CommandResult.Type.SUCCESS) {
                    System.out.println("Movie: " + key + " was appended.");
                } else {
                    System.out.println("Error: " + res.getError());
                }
            }
        } catch (ClassNotFoundException e) {
        }
    }


    private void update(Long id) throws IOException {
        try {
            CommandResultWithObject result = (CommandResultWithObject) client.exec(new FindByIdCommand(id));
            if (result.getType() != CommandResult.Type.SUCCESS) {
                System.out.println("Нет записи с идентификатором " + id + ".");
            } else {
                Movie movie = (Movie) result.getObject();
                input(movie, null);
                CommandResult res = (CommandResult) client.exec(new UpdateCommand(id, movie));
                if (res.getType() == CommandResult.Type.SUCCESS) {
                    System.out.println("Запись  с идентификатором " + id + " изменена.");
                } else {
                    System.out.println("Error: " + res.getError());
                }
            }
        } catch (ClassNotFoundException e) {
        }
    }

    private void replaceIfGreater(String key, Movie movie) throws IOException {
        try {
            CommandResult result = (CommandResult) client.exec(new ReplaceIfGreaterCommand(key, movie));
            if (result.getType() == CommandResult.Type.SUCCESS) {
                System.out.println("Запись заменена");
            } else {
                    System.out.println("Error: " + result.getError());
            }

        } catch (ClassNotFoundException e) {
        }
    }

    private void remove(String key) throws IOException {
        try {
            CommandResult res = (CommandResult) client.exec(new RemoveCommand(key));
            if (res.getType() == CommandResult.Type.SUCCESS) {
                System.out.println("Запись  с идентификатором " + key + " удалена.");
            } else {
                System.out.println("Error: " + res.getError());
            }
        } catch (ClassNotFoundException e) {
        }
    }

    private void removeLower(Movie movie) throws IOException {
        try {
            CommandResult res = (CommandResult) client.exec(new RemoveLowerCommand(movie));
            if (res.getType() == CommandResult.Type.SUCCESS) {
                System.out.println("Записи  меньше " + movie + " удалены.");
            } else {
                System.out.println("Error: " + res.getError());
            }
        } catch (IOException | ClassNotFoundException e) {
        }
    }

    private void removeLowerKey(String key) throws IOException {
        try {
            CommandResult res = (CommandResult) client.exec(new RemoveLowerKeyCommand(key));
            if (res.getType() == CommandResult.Type.SUCCESS) {
                System.out.println("Запись  с идентификатороми меньше " + key + " удалены.");
            } else {
                System.out.println("Error: " + res.getError());
            }
        } catch (ClassNotFoundException e) {
        }
    }

    private void input(Movie movie, String key) {
        Scanner scanner = new Scanner(System.in);

        //Input movie name
        String name = key;
        while (name == null) {
            System.out.print("Введите название: ");
            name = scanner.nextLine();
            if (name.isEmpty()) {
                System.out.println("Ошибка: Поле name не может быть пустым. Повторите ввод.");
            }
        }
        movie.setName(name);

        //Input movie coordinates
        System.out.println("Введите координаты: ");
        Coordinates coordinates = null;
        while (coordinates == null) {
            coordinates = new Coordinates();
            while (true) { //Пока не будет введен коректный X
                System.out.print("Введите X (<=266): ");
                if (!scanner.hasNextDouble()) {
                    scanner.nextLine();
                    System.out.println("Ошибка: Поле x должно быть числом с плавающей точкой. Повторите ввод.");
                    continue;
                }
                double x = scanner.nextDouble();
                if (x > 266) {
                    System.out.println("Ошибка: Поле x не может > 266. Повторите ввод.");
                    continue;
                }
                coordinates.setX(x);
                break;
            }
            scanner.nextLine();

            while (true) { //Пока не будет введен коректный Y
                System.out.print("Введите Y: ");
                if (!scanner.hasNextInt()) {
                    scanner.nextLine();
                    System.out.println("Ошибка: Поле y должно быть целым числом. Повторите ввод.");
                    continue;
                }
                int y = scanner.nextInt();
                coordinates.setY(y);
                break;
            }
            scanner.nextLine();
        }
        movie.setCoordinates(coordinates);

        //Input movie oscarscount
        Long oscars = null;
        while (true) { //Пока не будет введен коректный Y
            System.out.print("Введите колличество оскаров (>0): ");
            if (!scanner.hasNextLong()) {
                scanner.next();
                System.out.println("Ошибка: Поле y должно быть целым числом. Повторите ввод.");
                continue;
            }
            oscars = scanner.nextLong();
            if (oscars <= 0) {
                System.out.println("Ошибка: Поле y должно быть целым числом больше нуля. Повторите ввод.");
                continue;
            }
            break;
        }
        movie.setOscarsCount(oscars);
        scanner.nextLine();

        //Input movie genre
        MovieGenre genre = null;
        while (genre == null) {
            try {
                System.out.print("Введите жанр [COMEDY, TRAGEDY, THRILLER]: ");
                String str = scanner.next();
                if (str.isEmpty()) {
                    break;
                }
                genre = MovieGenre.valueOf(str.toUpperCase());

            } catch (IllegalArgumentException ex) {
                System.out.println("Ошибка: Поле может быть значением только [COMEDY, TRAGEDY, THRILLER]. Повторите ввод.");
            }
        }
        movie.setGenre(genre);
        scanner.nextLine();

        //Input movie rating
        MpaaRating rating = null;
        while (rating == null) {
            try {
                System.out.print("Введите рейтинг [ G, PG_13, R]: ");
                rating = MpaaRating.valueOf(scanner.next().toUpperCase());

            } catch (IllegalArgumentException ex) {
                System.out.println("Ошибка: Поле может быть значением только [ G, PG_13, R]. Повторите ввод.");
            }
        }
        movie.setMpaaRating(rating);
        scanner.nextLine();

        Person person = new Person();
        input(person);
        movie.setDirector(person);
    }

    private void input(Person person) {
        Scanner scanner = new Scanner(System.in);

        //Input movie name
        String name = person.getName();
        while (name == null) {
            System.out.print("Введите имя: ");
            name = scanner.nextLine();
            if (name.isEmpty()) {
                System.out.println("Ошибка: Поле name не может быть пустым. Повторите ввод.");
            }
        }
        person.setName(name);

        ZonedDateTime date = null;
        while (date == null) {
            try {
                System.out.print("Введите дату рождения(Y-M-D H:m): ");
                ZoneId zoneId = ZonedDateTime.now().getZone();
                DateTimeFormatter format = DateTimeFormatter.ofPattern("y-M-d H:m").withZone(zoneId);
                date = ZonedDateTime.parse(scanner.nextLine(), format);
            } catch (DateTimeParseException ex) {
                System.out.println("Ошибка: Некоректная дата рождения. Повторите ввод.");
            }
        }
        person.setBirthday(date);

        //Input movie name
        String id = null;
        while (id == null) {
            System.out.print("Введите идентификационный номер: ");
            id = scanner.nextLine();
            if (id.isEmpty()) {
                scanner.next();
                System.out.println("Ошибка: Поле ID не может быть пустым. Повторите ввод.");
                continue;
            }
            if (id.length() > 39) {
                System.out.println("Ошибка: Поле ID не может превышать по длинне 39 символов. Повторите ввод.");
                id = null;
            }
        }
        person.setPassportID(id);

        //Input movie rating
        Color color = null;
        while (color == null) {
            try {
                System.out.print("Введите цвет волос [YELLOW, WHITE, BROWN]: ");
                String str = scanner.next();
                if (str.isEmpty()) {
                    color = null;
                    break;
                }
                color = Color.valueOf(str.toUpperCase());

            } catch (IllegalArgumentException ex) {
                System.out.println("Ошибка: Поле может быть значением только [YELLOW, WHITE, BROWN]. Повторите ввод.");
            }
        }
        person.setHairColor(color);
        System.out.println("Введите геолокацию: ");

        //Input movie coordinates
        Location location = null;
        while (location == null) {
            location = new Location();
            while (true) { //Пока не будет введен коректный X
                System.out.print("Введите X: ");
                if (!scanner.hasNextInt()) {
                    scanner.nextLine();
                    System.out.println("Ошибка: Поле x должно быть целым числом. Повторите ввод.");
                    continue;
                }
                int x = scanner.nextInt();

                location.setX(x);
                break;
            }
            scanner.nextLine();

            while (true) { //Пока не будет введен коректный Y
                System.out.print("Введите Y: ");
                if (!scanner.hasNextLong()) {
                    scanner.nextLine();
                    System.out.println("Ошибка: Поле y должно быть целым числом. Повторите ввод.");
                    continue;
                }
                long y = scanner.nextLong();
                location.setY(y);
                break;
            }
            scanner.nextLine();

            String locationName = null;
            while (locationName == null) {
                System.out.print("Введите название места: ");
                locationName = scanner.nextLine();
                if (locationName.isEmpty()) {
                    System.out.println("Ошибка: Поле name не может быть пуст || locationName.isBlank()ым. Повторите ввод.");
                }
            }
            location.setName(locationName);

        }
        person.setLocation(location);

    }


    ///Testing


 /*   void testHelp() {
        System.err.print("Test help ...");
        help();
        System.err.println("OK");
    }

    void testInfo() {
        System.err.print("Test info ...");
        info();
        System.err.println("OK");
    }

    void testShow() {
        System.err.print("Test show ...");
        show();
        System.err.println("OK");
    }

    void testSave() {
        System.err.print("Test save ...");
        save();
        System.err.println("OK");
    }

    void testClear() {
        System.err.print("Test clear ...");
        clear();
        System.err.println("OK");
    }





    void runTests() {
        try {
            PrintStream std =  System.out;
            System.setOut(new PrintStream("test.out"));

            testHelp();
            testInfo();
            testShow();
            testSave();
            testClear();

            System.setOut(std);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
*/
}

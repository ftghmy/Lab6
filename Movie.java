package org.dima.movies;


import java.io.PrintWriter;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Scanner;

/**
 * класс описания фильма
 */
public class Movie  implements CanValidate, Comparable<Movie>, Serializable {
    /**
     * получает идентификационный номер
     * @return возвращает получаемый идентификационный номер
     */
    public Long getId() {
        return id;
    }

    /**
     *устанавливает идентификационный номер
     * @param id устанавливаемый идентификационный номер
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     *получает название фильма
     * @return получаемое название фильма
     */
    public String getName() {
        return name;
    }

    /**
     *устанавливает название фильма
     * @param name устанавливаемое название фильма
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *получает координату хранения фильма
     * @return возвращает получаемую координату
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     *устанавливает координату хранения фильма
     * @param coordinates устанавливаемая координата
     */
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    /**
     *получает дату создания
     * @return возвращает порлучаемую дату
     */
    public LocalDate getCreationDate() {
        return creationDate;
    }

    /**
     *устанавливает дату создания фильма
     * @param creationDate устанавливаемая дата
     */
    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    /**
     *получает колличество оскоров
     * @return возвращает колличество оскоров
     */
    public long getOscarsCount() {
        return oscarsCount;
    }

    /**
     *устанавливает колличество оскоров
     * @param oscarsCount устанавливаемое значение
     */
    public void setOscarsCount(long oscarsCount) {
        this.oscarsCount = oscarsCount;
    }

    /**
     *получает жанр фильма
     * @return возвращает жанр фильма
     */
    public MovieGenre getGenre() {
        return genre;
    }

    /**
     *устанавливает жанр фильму
     * @param genre устанавливаемое значение
     */
    public void setGenre(MovieGenre genre) {
        this.genre = genre;
    }

    /**
     *получает рейтинг фильма
     * @return возвращает рейтинг фильма
     */
    public MpaaRating getMpaaRating() {
        return mpaaRating;
    }

    /**
     *устанавливает рейтинг фильму
     * @param mpaaRating устанавливаемое значение
     */
    public void setMpaaRating(MpaaRating mpaaRating) {
        this.mpaaRating = mpaaRating;
    }

    /**
     * получает информацию о режисере
     * @return возвращает полученную информацию
     */
    public Person getDirector() {
        return director;
    }

    /**
     * устанавливает информацию о режисере
     * @param director устанавливаемое значение
     */
    public void setDirector(Person director) {
        this.director = director;
    }

    /**
     * преобразовывает к строке
     * @return преобразованную строку
     */
    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", oscarsCount=" + oscarsCount +
                ", genre=" + genre +
                ", mpaaRating=" + mpaaRating +
                ", director=" + director +
                '}';
    }

    /**
     * считывание XML файла
     * @param scanner файл источника данных
     * @return возвращает экземпляр класса
     * @throws XmlParseException Неверный формат данных
     */
    public static Movie fromXml(Scanner scanner) throws XmlParseException {
        Movie movie = new Movie();
        if(scanner.findWithinHorizon("<ID>", 0) != null) {
            scanner.useDelimiter("</ID>");
            if (scanner.hasNextLong()) {
                movie.setId(scanner.nextLong());
            } else {
                throw new XmlParseException();
            }
        } else {
            throw new XmlParseException();
        }
        //Read name
        if(scanner.findWithinHorizon("<NAME>", 0) == null) {
            throw new XmlParseException();
        }
        scanner.useDelimiter("</NAME>");
        if (scanner.hasNext()) {
            movie.setName(scanner.next());
        } else {
            throw new XmlParseException();
        }
        if(scanner.findWithinHorizon("<COORDINATES>", 0) == null){
            throw new XmlParseException();
        }
        scanner.useDelimiter("</COORDINATES>");
        if (scanner.hasNext()) {
            movie.coordinates = Coordinates.fromXml(scanner);
        }
        if(scanner.findWithinHorizon("<CREATION>", 0) == null){
            throw new XmlParseException();
        }
        scanner.useDelimiter("</CREATION>");
        if (scanner.hasNext()) {
            LocalDate date = LocalDate.parse(scanner.next());
            movie.setCreationDate(date);
        } else {
            throw new XmlParseException();
        }
        if(scanner.findWithinHorizon("<OSCARS>", 0) != null) {
            scanner.useDelimiter("</OSCARS>");
            if (scanner.hasNextLong()) {
                movie.setOscarsCount(scanner.nextLong());
            } else {
                throw new XmlParseException();
            }
        } else {
            throw new XmlParseException();
        }

        if(scanner.findWithinHorizon("<GENRE>", 0) != null) {
            scanner.useDelimiter("</GENRE>");
            if (scanner.hasNext()) {
                movie.setGenre(MovieGenre.valueOf(scanner.next()));
            } else {
                throw new XmlParseException();
            }
        } else {
            throw new XmlParseException();
        }

        if(scanner.findWithinHorizon("<RATING>", 0) != null) {
            scanner.useDelimiter("</RATING>");
            if (scanner.hasNext()) {
                movie.setMpaaRating(MpaaRating.valueOf(scanner.next()));
            } else {
                throw new XmlParseException();
            }
        } else {
            throw new XmlParseException();
        }

        if(scanner.findWithinHorizon("<DIRECTOR>", 0) == null){
            throw new XmlParseException();
        }
        scanner.useDelimiter("</DIRECTOR>");
        if (scanner.hasNext()) {
            Person person = Person.fromXml(scanner);
            movie.setDirector(person);
        }

        return movie;
    }

    /**
     *  вывод в поток в виде XML
     *  @param writer поток вывода
     *  @param shift сдвиг для форматирования файла
     */
    public void toXml(PrintWriter writer, String shift) {
        writer.printf(shift + "<ID>%d</ID>\n", id);
        writer.printf(shift + "<NAME>%s</NAME>\n", name);
        writer.println(shift + "<COORDINATES>");
        coordinates.toXml(writer, shift + "\t");
        writer.println(shift + "</COORDINATES>");
        writer.printf(shift + "<CREATION>%s</CREATION>\n",creationDate);
        writer.printf(shift + "<OSCARS>%d</OSCARS>\n",oscarsCount);
        writer.printf(shift + "<GENRE>%s</GENRE>\n",genre);
        writer.printf(shift + "<RATING>%s</RATING>\n",mpaaRating);
        writer.println(shift + "<DIRECTOR>");
        director.toXml(writer,shift + "\t");
        writer.println(shift + "</DIRECTOR>");
    }

    /**
     * проверка на правильность вводимых данных
     * @return True-поля заполнены верно,False-поля заполнены неверно
     */
    @Override
    public boolean validate() {
        if(id == null || id <= 0) {
            return false;
        }
        if(name == null || name.isEmpty()) {
            return false;
        }
        if(coordinates == null || !coordinates.validate()) {
            return false;
        }
        if(creationDate == null) {
            return false;
        }
        if(oscarsCount <= 0) {
            return false;
        }
        if(genre == null || mpaaRating == null) {
            return false;
        }
        if(director == null || !director.validate()) {
            return false;
        }
        return true;
    }

    private Long id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private LocalDate creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private long oscarsCount; //Значение поля должно быть больше 0
    private MovieGenre genre; //Поле может быть null
    private MpaaRating mpaaRating; //Поле не может быть null
    private Person director; //Поле не может быть null

    /**
     * реализация интерфейса для сравнения объектов
     * @param movie экземпляр класса
     * @return результат сравнения
     */
    @Override
    public int compareTo(Movie movie) {
        if(movie.name != null) {
            return name.compareTo(movie.name);
        } else {
            return -1;
        }
    }
}

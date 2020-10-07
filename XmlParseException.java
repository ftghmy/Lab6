package org.dima.movies;

/**
 *Исключение выбрасываемое при разборе базы данных
 */
public class XmlParseException extends Exception {
    public XmlParseException() {
        super("Error while parsing xml");
    }

    /**
     *конструктор исключения
     * @param message текст исключения
     */
    public XmlParseException(String message) {
        super(message);
    }
}

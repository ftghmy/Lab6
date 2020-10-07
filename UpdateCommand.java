package org.dima.commands;

import org.dima.movies.Color;
import org.dima.movies.Movie;

import java.io.Serializable;

/**
 * Класс для сериализации в поток команды update
 */
public class UpdateCommand extends MovieCommand {
    private final Movie movie;
    private final Long id;

    /**
     * Конструктор - создание нового объекта с определенными значениями
     * @param id номер фильма в коллекции
     * @param movie объект фильма
     */
    public UpdateCommand(Long id, Movie movie) {
        this.movie = movie;
        this.id = id;
    }

    /**
     * Функция получения значения поля id
     * @return возвращает значение поля id
     */
    public Long getId() {
        return id;
    }

    /**
     * Функция получения значения поля movie
     * @return возвращает значение поля movie
     */
    public Movie getMovie() {
        return movie;
    }

}

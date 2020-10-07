package org.dima.commands;

import org.dima.movies.Color;
import org.dima.movies.Movie;

import java.io.Serializable;

/**
 * Класс для сериализации в поток команды RemoveLower
 */
public class RemoveLowerCommand extends MovieCommand {
    private final Movie movie;

    /**
     * Конструктор - создание нового объекта с определенными значениями
     * @param movie объект фильма
     */
    public RemoveLowerCommand(Movie movie) {
        this.movie = movie;
    }

    /**
     * Функция получения значения поля movie
     * @return возвращает значение поля movie
     */
    public Movie getMovie() {
        return movie;
    }
}

package org.dima.commands;

import org.dima.movies.Color;
import org.dima.movies.Movie;

import java.io.Serializable;

/**
 * Класс для сериализации в поток команды replaceifgreater
 */
public class ReplaceIfGreaterCommand extends MovieCommand {
    private final String key;
    private final Movie movie;

    /**
     * Конструктор - создание нового объекта с определенными значениями
     * @param key ключ для сравнения
     * @param movie объект фильма
     */
    public ReplaceIfGreaterCommand(String key, Movie movie) {
        this.key = key;
        this.movie = movie;
    }

    /**
     * Функция получения значения поля key
     * @return возвращает значение поля key
     */
    public String getKey() {
        return key;
    }

    /**
     * Функция получения значения поля movie
     * @return возвращает значение поля movie
     */
    public Movie getMovie() {
        return movie;
    }
}

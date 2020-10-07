package org.dima.commands;

import org.dima.movies.Color;
import org.dima.movies.MovieGenre;

import java.io.Serializable;

/**
 * Класс для сериализации в поток команды PrintFieldAscendingGenre
 */
public class PrintFieldAscendingGenreCommand extends MovieCommand {
    private final MovieGenre genre;

    /**
     * Конструктор - создание нового объекта с определенными значениями
     * @param genre жанр для сравнения
     */
    public PrintFieldAscendingGenreCommand(MovieGenre genre) {
        this.genre = genre;
    }

    /**
     * Функция получения значения поля genre
     * @return возвращает значение поля genre
     */
    public MovieGenre getGenre() {
        return genre;
    }
}

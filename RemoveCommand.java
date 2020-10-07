package org.dima.commands;

import org.dima.movies.Color;

import java.io.Serializable;

/**
 * Класс для сериализации в поток команды Remove
 */
public class RemoveCommand extends MovieCommand {
    private final String key;

    /**
     * Конструктор - создание нового объекта с определенными значениями
     * @param key ключ для сравнения
     */
    public RemoveCommand(String key) {
        this.key = key;
    }

    /**
     * Функция получения значения поля key
     * @return возвращает значение поля key
     */
    public String getKey() {
        return key;
    }
}

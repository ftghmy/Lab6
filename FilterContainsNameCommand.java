package org.dima.commands;

import org.dima.movies.Color;

import java.io.Serializable;

/**
 * Класс для сериализации в поток команды FilterContainsName
 */
public class FilterContainsNameCommand extends MovieCommand {
    private final String key;

    /**
     * Конструктор - создание нового объекта с определенными значениями
     * @param key ключ для сортировки
     */
    public FilterContainsNameCommand(String key) {
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

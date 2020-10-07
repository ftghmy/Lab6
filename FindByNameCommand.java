package org.dima.commands;

import java.io.Serializable;

/**
 * Класс для сериализации в поток команды FindByName
 */
public class FindByNameCommand extends MovieCommand {
    private final String key;

    /**
     * Конструктор - создание нового объекта с определенными значениями
     * @param key ключ для поиска
     */
    public FindByNameCommand(String key) {
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

package org.dima.commands;

import java.io.Serializable;

/**
 * Класс для сериализации в поток команды FindById
 */
public class FindByIdCommand extends MovieCommand {
    private final Long id;

    /**
     * Конструктор - создание нового объекта с определенными значениями
     * @param id индентификатор для сравнения
     */
    public FindByIdCommand(Long id) {
        this.id = id;
    }

    /**
     * Функция получения значения поля id
     * @return возвращает значение поля id
     */
    public Long getId() {
        return id;
    }
}

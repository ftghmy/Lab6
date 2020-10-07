package org.dima.commands;

import java.io.Serializable;

/**
 * Класс для сериализации в поток ответа от сервера с возвращаемым объектом
 */
public class CommandResultWithObject extends CommandResult implements Serializable {
    private Serializable object = null;

    /**
     * Конструктор - создание нового объекта с определенными значениями
     * @param object ответ который привязывается к результату
     */
    public CommandResultWithObject(Serializable object) {
        super(Type.SUCCESS, null);
        this.object  = object;
    }

    /**
     * Конструктор - создание нового объекта с определенными значениями
     * @param code тип результата
     * @param error строка с сообщением об ошибке в случае её существования
     */
    public CommandResultWithObject(Type code, String error) {
        super(code, error);
        this.object  = null;
    }

    /**
     * Функция получения значения поля object
     * @return возвращает значение поля object
     */
    public Serializable getObject() {
        return object;
    }
}

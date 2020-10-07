package org.dima.commands;

import org.dima.server.DbServer;
import org.dima.server.DbWorker;

import java.io.Serializable;

/**
 * Класс для сериализации в поток ответа от сервера без возвращаемого объекта
 */
public class CommandResult implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        SUCCESS,
        WARNING,
        ERROR
    }

    private final Type type;
    private final String error;

    /**
     * Конструктор - создание нового объекта
     */
    public CommandResult() {
        this(Type.SUCCESS, null);
    }

    /**
     * Конструктор - создание нового объекта с определенными значениями
     * @param code тип результата
     * @param error сообщение об ошибке в случае её существования
     */
    public CommandResult(Type code, String error) {
        this.type = code;
        this.error = error;
        if(code == Type.WARNING) {
            DbWorker.logger.warn(error);
        } else if(code == Type.ERROR) {
            DbWorker.logger.error(error);
        }
    }

    /**
     * Функция получения значения поля type
     * @return возвращает значение поля type
     */
    public Type getType() {
        return type;
    }

    /**
     * Функция получения значения поля error
     * @return возвращает значение поля error
     */
    public String getError() {
        return error;
    }
}

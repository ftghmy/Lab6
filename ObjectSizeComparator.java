package org.dima.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Класс для сравнения объектов коллекции по размеру
 */
public class ObjectSizeComparator implements Comparator<Serializable> {
    /**
     * Функция сравнения объектов по размеру
     * @param o1 объект №1
     * @param o2 объект №2
     * @return результат сравнения
     */
    @Override
    public int compare(Serializable o1, Serializable o2) {
        Integer o1_size =  getObjectSize(o1);
        Integer o2_size =  getObjectSize(o2);
        return o1_size.compareTo(o2_size);
    }

    /**
     * Функция нахождения размера объекта
     * @param obj объект
     * @return размер объекта
     */
    public static int getObjectSize(Serializable obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            return baos.size();
        } catch (IOException e) {
            return 0;
        }
    }
}

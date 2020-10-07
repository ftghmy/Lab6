package org.dima.movies;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Scanner;

/**
 * класс расположения
 */
public class Location  implements CanValidate, Serializable {
    private int x;
    private long y;
    private String name; //Строка не может быть пустой, Поле не может быть null

    /**
     *получает координату х
     * @return возвращает координату
     */
    public int getX() {
        return x;
    }

    /**
     *устанавливает координату х
     * @param x устанавливаемое значение
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     *получает координату у
     * @return возвращает координату
     */
    public long getY() {
        return y;
    }

    /**
     * устанавливает координату х
     * @param y устанавливаемое значение
     */
    public void setY(long y) {
        this.y = y;
    }

    /**
     * получает название места
     * @return возвращает полученное значение
     */
    public String getName() {
        return name;
    }

    /**
     * устанавливает название места
     * @param name устанавливаемое значение
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * считывание XML файла
     * @param scanner файл источника данных
     * @return возвращает экземпляр класса
     * @throws XmlParseException Неверный формат данных
     */
    public static Location fromXml(Scanner scanner) throws XmlParseException {
        Location location = new Location();
        if(scanner.findWithinHorizon("<X>", 0) == null){
            throw new XmlParseException();
        }
        scanner.useDelimiter("</X>");
        if (scanner.hasNextInt()) {
            location.setX(scanner.nextInt());
        } else {
            throw  new XmlParseException();
        }
        scanner.findWithinHorizon("<Y>", 0);
        scanner.useDelimiter("</Y>");
        if (scanner.hasNextLong()) {
            location.setY(scanner.nextLong());
        } else {
            throw  new XmlParseException();
        }
        if(scanner.findWithinHorizon("<NAME>", 0) == null) {
            throw new XmlParseException();
        }
        scanner.useDelimiter("</NAME>");
        if (scanner.hasNext()) {
            location.setName(scanner.next());
        } else {
            throw new XmlParseException();
        }

        return location;
    }

    /**
     * преобразование к строке
     * @return преобразованную строку
     */
    @Override
    public String toString() {
        return "Location{" +
                "x=" + x +
                ", y=" + y +
                ", name='" + name + '\'' +
                '}';
    }

    /**
     * вывод в поток в виде XML
     * @param writer поток вывода
     * @param shift сдвиг для форматирования файла
     */
    public void toXml(PrintWriter writer, String shift) {
        writer.printf(shift + "<X>%d</X>\n", x);
        writer.printf(shift + "<Y>%d</Y>\n", y);
        writer.printf(shift + "<NAME>%s</NAME>\n",name);
    }

    /**
     * ппроверка на правильность вводимых данных
     * @return True-поля заполнены верно,False-поля заполнены неверно
     */
    @Override
    public boolean validate() {
        return !(name == null || name.isEmpty());
    }
}

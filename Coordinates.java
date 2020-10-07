package org.dima.movies;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Scanner;

/**
 * класс координат
 */
public class Coordinates implements CanValidate, Serializable {
    private double x; //Максимальное значение поля: 266
    private int y;

    /**
     *получает координату х
     * @return возвращает координату
     */
    public double getX() {
        return x;
    }

    /**
     *устанавливает координату х
     * @param x устанавливаемое значение
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     *получает координату у
     * @return возвращает координату
     */
    public int getY() {
        return y;
    }

    /**
     * устанавливает координату х
     * @param y устанавливаемое значение
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * считывание XML файла
     * @param scanner файл источника данных
     * @return возвращает экземпляр класса
     * @throws XmlParseException Неверный формат данных
     */
    public static Coordinates fromXml(Scanner scanner) throws XmlParseException {
        Coordinates coordinates = new Coordinates();
        if(scanner.findWithinHorizon("<X>", 0) == null){
            throw new XmlParseException();
        }
        scanner.useDelimiter("</X>");
        if (scanner.hasNextDouble()) {
            coordinates.setX(scanner.nextDouble());
        } else {
            throw  new XmlParseException();
        }
        scanner.findWithinHorizon("<Y>", 0);
        scanner.useDelimiter("</Y>");
        if (scanner.hasNextInt()) {
            coordinates.setY(scanner.nextInt());
        } else {
            throw  new XmlParseException();
        }
        return coordinates;
    }

    /**
     * ппроверка на правильность вводимых данных
     * @return True-поля заполнены верно,False-поля заполнены неверно
     */
    @Override
    public boolean validate() {
        return x <= 266;
    }

    /**
     * вывод в поток в виде XML
     * @param writer поток вывода
     * @param shift сдвиг для форматирования файла
     */
    public void toXml(PrintWriter writer, String shift) {
        writer.printf(shift + "<X>%f</X>\n", x);
        writer.printf(shift + "<Y>%d</Y>\n", y);
    }

    /**
     * преобразование к строке
     * @return преобразованную строку
     */
    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

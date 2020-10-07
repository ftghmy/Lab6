package org.dima.movies;

import java.io.PrintWriter;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Scanner;

/**
 * класс описания человека
 */
public class Person  implements CanValidate, Serializable {
    private String name; //Поле не может быть null, Строка не может быть пустой
    private ZonedDateTime birthday; //Поле может быть null
    private String passportID; //Длина строки не должна быть больше 39, Поле не может быть null
    private Color hairColor; //Поле может быть null
    private Location location; //Поле не может быть null

    /**
     *получает имя
     * @return возвращает получаемое имя
     */
    public String getName() {
        return name;
    }

    /**
     *устанавливает имя
     * @param name устанавливаемое имя
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *получает день рождения
     * @return возвращает получаемый день рождения
     */
    public ZonedDateTime getBirthday() {
        return birthday;
    }

    /**
     *устанавливает день рождения
     * @param birthday устанавливаемый день рождения
     */
    public void setBirthday(ZonedDateTime birthday) {
        this.birthday = birthday;
    }

    /**
     *получает номер паспорта
     * @return возвращает получаемый номер паспорта
     */
    public String getPassportID() {
        return passportID;
    }

    /**
     устанавливает номер паспорта
     * @param passportID устанавливаемый номер паспорта
     */
    public void setPassportID(String passportID) {
        this.passportID = passportID;
    }

    /**
     получает цвет волос
     * @return возвращает получаемый цвет волос
     */
    public Color getHairColor() {
        return hairColor;
    }

    /**
     *устанавливает цвет волос
     * @param hairColor устанавливаемый цвет волос
     */
    public void setHairColor(Color hairColor) {
        this.hairColor = hairColor;
    }

    /**
     *получает позицию
     * @return возвращает получаемую позицию
     */
    public Location getLocation() {
        return location;
    }

    /**
     * устанавливает положение
     * @param location устанавливает соответствующую позицию
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * считывание XML файла
     * @param scanner файл источника данных
     * @return возвращает экземпляр класса
     * @throws XmlParseException неверный формат данных
     */
    public static Person fromXml(Scanner scanner) throws XmlParseException {
        Person person = new Person();
        if (scanner.findWithinHorizon("<ID>", 0) != null) {
            scanner.useDelimiter("</ID>");
            if (scanner.hasNext()) {
                person.setPassportID(scanner.next());
            } else {
                throw new XmlParseException();
            }
        } else {
            throw new XmlParseException();
        }
        //Read name
        if (scanner.findWithinHorizon("<NAME>", 0) == null) {
            throw new XmlParseException();
        }
        scanner.useDelimiter("</NAME>");
        if (scanner.hasNext()) {
            person.setName(scanner.next());
        } else {
            throw new XmlParseException();
        }
        if (scanner.findWithinHorizon("<LOCATION>", 0) == null) {
            throw new XmlParseException();
        }
        scanner.useDelimiter("</LOCATION>");
        if (scanner.hasNext()) {
            person.location = Location.fromXml(scanner);
        }
        if (scanner.findWithinHorizon("<BIRTHDAY>", 0) == null) {
            throw new XmlParseException();
        }
        scanner.useDelimiter("</BIRTHDAY>");
        if (scanner.hasNext()) {
            ZonedDateTime date = ZonedDateTime.parse(scanner.next());
            person.setBirthday(date);
        } else {
            throw new XmlParseException();
        }
        if (scanner.findWithinHorizon("<COLOR>", 0) != null) {
            scanner.useDelimiter("</COLOR>");
            if (scanner.hasNext()) {
                person.setHairColor(Color.valueOf(scanner.next()));
            } else {
                throw new XmlParseException();
            }
        } else {
            throw new XmlParseException();
        }
        return person;
    }

    /**
     * вывод в поток в виде XML
     * @param writer поток вывода
     * @param shift сдвиг для форматирования файла
     */
    public void toXml(PrintWriter writer, String shift) {
        writer.printf(shift + "<ID>%s</ID>\n", passportID);
        writer.printf(shift + "<NAME>%s</NAME>\n", name);
        writer.println(shift + "<LOCATION>") ;
        location.toXml(writer, shift + "\t");
        writer.println(shift + "</LOCATION>");
        writer.printf(shift + "<BIRTHDAY>%s</BIRTHDAY>\n",birthday);
        writer.printf(shift + "<COLOR>%s</COLOR>\n",hairColor);
    }

    /**
     * преобразование к строке
     * @return возвращает получившуюся строку
     */
    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", birthday=" + birthday +
                ", passportID='" + passportID + '\'' +
                ", hairColor=" + hairColor +
                ", location=" + location +
                '}';
    }

    /**
     *  Проверка на правильность заполнения класса Person
     * @return True-поля заполнены верно,False-поля заполнены неверно
     */
    @Override
    public boolean validate() {
        if(name == null || name.isEmpty()) {
            return false;
        }
        if(birthday == null) {
            return false;
        }
        if(passportID == null || passportID.length() > 39) {
            return false;
        }
        if(hairColor == null) {
            return false;
        }
        if(location == null || !location.validate()) {
            return false;
        }
        return true;
    }
}

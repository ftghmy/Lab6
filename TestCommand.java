package org.dima.commands;

import org.dima.movies.Color;

import java.io.Serializable;

public class TestCommand  extends MovieCommand {
    private final String test;
    private final Color color;

    public TestCommand(String test, Color color) {
        this.test = test;
        this.color = color;
    }

    public String getTest() {
        return test;
    }

    public Color getColor() {
        return color;
    }

}

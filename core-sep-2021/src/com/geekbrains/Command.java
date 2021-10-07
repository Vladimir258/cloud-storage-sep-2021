package com.geekbrains;

import java.io.Serializable;

public class Command implements Serializable {

    CommandType type;

    // Конструктор
    public CommandType getType() {
        return type;
    }
}

package com.geekbrains;

public enum CommandType {
    FILE_MESSAGE,   // Сам файл
    FILE_REQUEST,   // Запросить файл с именем
    LIST_REQUEST,   // Запросить список файлов на сервере
    LIST_RESPONSE,  // Список файлов на сервере
    PATH_REQUEST,   // Запросить директорию нахождения клиента
    PATH_RESPONSE   // Ответ в какой директории находится клиент
}

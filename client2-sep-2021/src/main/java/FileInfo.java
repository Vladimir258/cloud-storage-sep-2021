

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FileInfo {
    // Enum указывает нам имеем мы дело с файлом или директорией
    public enum FileType {
        FILE("F"), DIRECTORY("D");

        private String name; // Сокращенные имена (те что в "")

        public String getName() {
            return name;
        }

        FileType(String name) {
            this.name = name;
        }
    }

    private String filename; // Имя файла
    private FileType type; // Тип файла
    private long size; // Размер файла
    private LocalDateTime lastModified; // Последняя модификация файла

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    // Сбор данных о файле
    public FileInfo(Path path) {
        try {
            this.filename = path.getFileName().toString();
            this.size = Files.size(path); // Files.size может сгенерировать исключение, try catch для него
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE; // Определяем на что мы смотрим, файл или директория
            if(this.type == FileType.DIRECTORY) {
                this.size = -1L;
            }
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3));
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info from path");
        }
    }
}



import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;





public class PanelController implements Initializable {

    @FXML
    TextField pathField;
    @FXML
    TableView<FileInfo> filesTable;
    @FXML
    ComboBox<String> diskBox;

    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

    @Override
    public void initialize(URL location, ResourceBundle resource) {

        // Создаем столбец Тип в таблицу
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>("Тип"); // <Что хранится, в что преобразовываем>
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName())); // Формируем столбец
        fileTypeColumn.setPrefWidth(40); // Длина столбца 24 пикселя

        // Создаем столбец Имя в таблицу
        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Имя");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumn.setPrefWidth(240);

        // Создаем столбец Размер в таблицу
        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize())); // Вытаскиваем размер файла
        // Форматирование столбца с размером файла
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if(item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if(item == -1L) {
                            text = "Folder";
                        }
                        setText(text);
                    }
                }
            };
        });
        fileSizeColumn.setPrefWidth(120);

        // Создадим свой формат даты, для удобного отображения в столбце - Дата изменения
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Создаем столбец Дата изменения в таблицу
        TableColumn<FileInfo, String> fileDataColumn = new TableColumn<>("Дата изменения");
        fileDataColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileNameColumn.setPrefWidth(120);


        // Добавляем столбцы в таблицу
        filesTable.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDataColumn);
        filesTable.getSortOrder().add(fileTypeColumn); // Сортировка по умолчанию = по первому столбцу

        // Списки дисков
        diskBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) { // Получить список дисков
            diskBox.getItems().add(p.toString()); // В выпадающий список добавить ссылки на диски
        }
        diskBox.getSelectionModel().select(0); // по умолчанию выбираем первый

        // Переход в папки
        filesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() == 2) {
                    // pathField.getText()) - путь к корню каталога + resolve(filesTable.getSelectionModel().getSelectedItem().getFilename() - то по чему кликнули
                    Path path = Paths.get(pathField.getText()).resolve(filesTable.getSelectionModel().getSelectedItem().getFilename());
                    // Если path - директория, то переходим туда
                    if(Files.isDirectory(path)) {
                        updateList(path);
                    }
                }
            }
        });


        // Указываем где собирать файлы
        updateList(Paths.get("." + "/disk")); // Path.get - способ задания путей
    }

    // Метод собирающий список файлов по указанному пути
    public void updateList(Path path) {
        try {
            // 00.33.00 описание этой тряпки, надо бы в комменты
            pathField.setText(path.normalize().toAbsolutePath().toString());
            filesTable.getItems().clear(); // Предварительная чистка списка файлов
            //  getItems - ссылка на список файлов, addAll - хотим добавить коллекцию файлов, Files.list(path) - возвращает поток путей, по указанному пути
            // map(FileInfo::new) - преобразовываем то что получили из Files.list(path), к FileInfo (map - преобразование файлов потока)
            // collect(Collectors.toList()) - собираем все в лист и отдаем в таблицу
            filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            filesTable.sort(); // Сортируем табличку по умолчанию(по первому столбцу)
        } catch (IOException e) {
            // Предупреждение что не удалось обновить таблицу файлов
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине, не удалось обновить список файлов.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    // Логика кнопки Вверх
    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if(upperPath != null) {
            updateList(upperPath);
        }
    }

    // Лоигка, что при выборе из списка дисков в ComboBox осуществляется переход, на выбранный диск.
    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource(); // Получаем ссылку на элемент
        // element - здесь comboBox, getSelectionModel() - информация об выделенных элементах в нем, getSelectedItem() - выбор выделенного для перехода
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    // Возвращаем имя выбранного файла
    public String getSelectedFilename() {
        if(!filesTable.isFocused()) {
            return null;
        }
        return filesTable.getSelectionModel().getSelectedItem().getFilename();
    }

    // Возвращаем адрес папки на которую указали
    public String getCurrentPath() {
        return pathField.getText();
    }

    // Выбрана ли панель
    public boolean getSelectedPanel() {
        if(!filesTable.isFocused()) {
            return false;
        }
        return true;
    }

    // Подключение к серверу
    public void serverConnect() {
        try {

          //  fillFilesInCurrentDir();
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            updateList(Paths.get("C:\\projectJ\\cloud\\cloud-storage-sep-2021\\server-sep-2021\\root"));
            Thread daemon = new Thread(() -> {
                try {
                    while (true) {
//                        Command msg = (Command) is.readObject();
//                        // TODO Разработка системы команд
//                        switch (msg.getType()) {
//                            case LIST_REQUEST:
//                                break;
//                            case LIST_RESPONSE:
//                                break;
//                            case FILE_REQUEST:
//                                break;
//                            case FILE_MESSAGE:
//                                break;
//                            case PATH_REQUEST:
//                                break;
//                            case PATH_RESPONSE:
//                                break;
//                            default:
//                                break;
//                        }
                    }
                } catch (Exception e) {
                    //log.error("exception while read from input stream");
                   // System.out.println("Не могу прочитать с сервера");
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Не могу прочитать с сервера", ButtonType.OK);
                    alert.showAndWait();
                }
            });
            daemon.setDaemon(true);
            daemon.start();
        } catch (IOException ioException) {
           // log.error("e=", ioException);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка подключения", ButtonType.OK);
            alert.showAndWait();
        }
    }

}

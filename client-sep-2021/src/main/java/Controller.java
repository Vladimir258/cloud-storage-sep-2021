import com.geekbrains.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


@Slf4j
public class Controller implements Initializable {

    private static final String ROOT_DIR = "client-sep-2021/root";
    private static byte[] buffer = new byte[1024];

    public ListView<String> listView;
    public ListView<String> serverView;

    public TextField clientPath;
    public TextField serverPath;

    private Path currentDir;

    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            currentDir = Paths.get("client-sep-2021", "root");
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            updateListView();
            directoryNavigator();

            Thread daemon = new Thread(() -> {
                try {
                    while (true) {
                        Command msg = (Command) is.readObject();

                        switch (msg.getType()) {
                            case LIST_RESPONSE:
                                ListResponse response = (ListResponse) msg;
                                List<String> names = response.getNames();
                                updateServerView(names);
                                break;
                            case FILE_MESSAGE:
                                FileMessage message = (FileMessage) msg;
                                Files.write(currentDir.resolve(message.getName()), message.getBytes());
                                updateListView();
                                break;
                            case PATH_RESPONSE:
                                PathResponse pathResponse = (PathResponse) msg;
                                String path = pathResponse.getPath();
                                Platform.runLater(() -> serverPath.setText(path));
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    log.error("exception while read from input stream");
                }
            });
            daemon.setDaemon(true);
            daemon.start();
        } catch (IOException ioException) {
            log.error("e=", ioException);
        }
    }

    public void updateListView() throws IOException {
        clientPath.setText(currentDir.toString());
        List<String> names = Files.list(currentDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        Platform.runLater(() ->{
            listView.getItems().clear();
            listView.getItems().addAll(names);
        });
    }

    private void directoryNavigator() {
        listView.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2) {
                String item = listView.getSelectionModel().getSelectedItem();
                Path newPath = currentDir.resolve(item);
                if(Files.isDirectory(newPath)) {
                    currentDir = newPath;
                    try{
                        updateListView();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });

        serverView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = serverView.getSelectionModel().getSelectedItem();
                try {
                    os.writeObject(new PathInRequest(item));
                    os.flush();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    public void btnUpload(ActionEvent actionEvent) throws IOException {
        String fileName = listView.getSelectionModel().getSelectedItem();
        FileMessage message = new FileMessage(currentDir.resolve(fileName));
        os.writeObject(message);
        os.flush();
    }

    public void btnDownLoad(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(fileName));
        os.flush();
    }

    public void btnClientPathUp (ActionEvent actionEvent) throws IOException{
        currentDir = currentDir.getParent();
        clientPath.setText(currentDir.toString());
        updateListView();
    }

    public void btnServerPathUp(ActionEvent actionEvent) throws IOException{
        os.writeObject(new PathUpRequest());
        os.flush();
    }

    public void updateServerView(List<String> names) {
        Platform.runLater(() -> {
            serverView.getItems().clear();
            serverView.getItems().addAll(names);
        });
    }
}

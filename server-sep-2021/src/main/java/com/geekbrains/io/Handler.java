package com.geekbrains.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Handler implements Runnable {

    public static final int BUFFER_SIZE = 256;
    private static final String ROOT_DIR = "server-sep-2021/root";
    private static byte[] buffer = new byte[BUFFER_SIZE];
    private final Socket socket;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        try (DataOutputStream os = new DataOutputStream(socket.getOutputStream());
             DataInputStream is = new DataInputStream(socket.getInputStream())
        ) {
            while (true) {
                String fileName = is.readUTF(); // Получаем имя файла
                log.debug("Received fileName: {}", fileName);
                long size = is.readLong();  // Получаем размер файла
                log.debug("File size: {}", size);
                int read;
                try(OutputStream fos = Files.newOutputStream(Paths.get(ROOT_DIR, fileName))) {
                    for (int i = 0; i < (size + BUFFER_SIZE - 1 )/ BUFFER_SIZE; i++) {
                        read = is.read(buffer);
                        fos.write(buffer, 0, read);
                        //  is.flush(); - вызовется сам при закрытии ресурса
                    }
                } catch (Exception e) {
                    log.error("problem with file system");
                }
            }
        } catch (Exception e) {
           log.error("stacktrace: ", e);
        }
    }
}

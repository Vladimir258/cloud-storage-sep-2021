package com.geekbrains.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Server {
    private ServerSocketChannel serverChannel; // Трансфер сервера
    private Selector selector;                 // Для классификации событий
    private ByteBuffer buffer; // Буфер для чтения

    public Server() throws IOException {
        buffer = ByteBuffer.allocate(256);
        serverChannel = ServerSocketChannel.open(); // Открыли канал
        selector = Selector.open();
        serverChannel.bind(new InetSocketAddress(8189));        // Выделили порт 8189
        serverChannel.configureBlocking(false);                      // Асинхронный не блокирующий режим
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);    // Регистрируем селектор на операции OP_ACCEPT

        while (serverChannel.isOpen()) {
            selector.select();                                  // Отбирает события
            Set<SelectionKey> keys =  selector.selectedKeys();  // Коллекция выбранных событий
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if(key.isAcceptable()) {
                    handleAccept(key);
                }
                if(key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        buffer.clear();
        int read = 0;
        StringBuilder msg = new StringBuilder();
        while (true) {

            if(read == -1) {
                System.out.println("not msg -1");
                channel.close();
                return;
            }
            read = channel.read(buffer);
            if(read == 0) {
                System.out.println("not msg 0");
                break;
            }
                System.out.println("not msg OK");
                buffer.flip();
                while (buffer.hasRemaining()) {
                    msg.append((char) buffer.get());
                }
                buffer.clear();

        }
        String message = msg.toString();
        switch (message){
            case "l": // aka ls
                Path path = Paths.get("server-sep-2021","root");
                DirectoryStream<Path> filesList = Files.newDirectoryStream(path); // try
                for(Path p: filesList) {
                    channel.write(ByteBuffer.wrap((p.toString().substring(20,p.toString().length())+"\n").getBytes(StandardCharsets.UTF_8)));
                }
                break;
            case "c": // aka cat
                Path pathFile = Paths.get("server-sep-2021","root","one.txt");
                List<String> fileContent = Files.readAllLines(pathFile);
                for(String s: fileContent) {
                    channel.write(ByteBuffer.wrap((s + "\n").getBytes(StandardCharsets.UTF_8)));
                }
            default:
                channel.write(ByteBuffer.wrap(("[" + LocalDateTime.now() + "] " + message).getBytes(StandardCharsets.UTF_8)));
                break;
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);

    }

    public static void main(String[] args) throws IOException {
        new Server();
    }

}

package com.geekbrains.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Iterator;
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
        channel.write(ByteBuffer.wrap(("[" + LocalDateTime.now() + "] " + message).getBytes(StandardCharsets.UTF_8)));
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

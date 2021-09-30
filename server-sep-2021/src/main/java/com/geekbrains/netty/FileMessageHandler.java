package com.geekbrains.netty;

import com.geekbrains.Command;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileMessageHandler extends SimpleChannelInboundHandler<Command> {

    private static final Path ROOT = Paths.get("server-sep-2021","root");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command cmd) throws Exception {
        // TODO Разработка системы команд (разные типы команд которые отправляем)
//        Files.write(
//                ROOT.resolve(fileMessage.getName()),
//                fileMessage.getBytes()
//        );
//        ctx.writeAndFlush("OK");

        switch (cmd.getType()) {
            case LIST_REQUEST:
                break;
            case LIST_RESPONSE:
                break;
            case FILE_REQUEST:
                break;
            case FILE_MESSAGE:
                break;
            case PATH_REQUEST:
                break;
            case PATH_RESPONSE:
                break;
            default:
                break;
        }
    }
}

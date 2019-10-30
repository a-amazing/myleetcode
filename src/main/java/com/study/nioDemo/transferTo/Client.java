package com.study.nioDemo.transferTo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author:wangyi
 * @Date:2019/10/29
 */
public class Client {

    SocketChannel channel;

    public Client(int port) throws IOException {
        this.channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress("localhost", port));
//        channel.configureBlocking(false);
//        channel.
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client(8888);
        ByteBuffer buffer = ByteBuffer.allocate(128);
        buffer.clear();
        buffer.put("Hello,it's client!".getBytes(StandardCharsets.UTF_8.name()));
        buffer.flip();
        while(!client.channel.finishConnect()) {
            while (buffer.hasRemaining()) {
                client.channel.write(buffer);
            }
//            client.channel.finishConnect();
        }
    }
}

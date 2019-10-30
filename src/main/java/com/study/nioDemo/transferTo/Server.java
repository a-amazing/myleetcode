package com.study.nioDemo.transferTo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author:wangyi
 * @Date:2019/10/29
 */
public class Server {
    private static Server server = new Server();
    private ServerSocketChannel serverSocketChannel;
    private ServerSocket serverSocket;
    private Selector linkSelector;
    private Selector connSelector;
    private ExecutorService pool;

    public static Server getServer() {
        return server;
    }

    private Server() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            InetSocketAddress address = new InetSocketAddress("localhost", 8888);
            serverSocket = serverSocketChannel.socket();
            serverSocket.bind(address);
            linkSelector = Selector.open();
            connSelector = Selector.open();
            serverSocketChannel.register(linkSelector, SelectionKey.OP_ACCEPT);
            pool = Executors.newFixedThreadPool(10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int num = 0;
                    try {
                        num = linkSelector.select();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (num > 0) {
                        Iterator<SelectionKey> keyIterator = linkSelector.selectedKeys().iterator();
                        while (keyIterator.hasNext()) {
                            SelectionKey key = keyIterator.next();
                            if (SelectionKey.OP_ACCEPT == (key.readyOps() & SelectionKey.OP_ACCEPT) && key.isAcceptable()) {
                                // accept
                                SocketChannel accept = null;
                                try {
                                    accept = ((ServerSocketChannel) key.channel()).accept();
                                    System.out.println("有新连接建立!");
                                    accept.configureBlocking(false);
                                    accept.register(connSelector, SelectionKey.OP_READ);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            // 其它条件
                            keyIterator.remove();
                        }
                    } else {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, "acceptServer").start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int num = 0;
                    try {
                        num = connSelector.select();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (num <= 0) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    Iterator<SelectionKey> keyIterator = connSelector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isReadable()) {
                            // accept
                            SocketChannel accept = (SocketChannel) key.channel();
                            System.out.println("有新的消息待读取!");
                            pool.execute(new Task(accept));
                        }
                        // 其它条件
                        keyIterator.remove();
                    }
                }
            }
        }, "readServer").start();
    }

    class Task implements Runnable {
        private SocketChannel socketChannel;

        public Task(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        @Override
        public void run() {
            ByteBuffer buffer = ByteBuffer.allocate(2048);
            int read = 0;
            while (true) {
                try {
                    if (((read = socketChannel.read(buffer)) == -1)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                buffer.flip();
                byte[] dst = new byte[buffer.limit()];
                try {
                    System.out.println(Thread.currentThread().getName() + new String(dst, StandardCharsets.UTF_8.name()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Server server = Server.getServer();
        try {
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
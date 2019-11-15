package com.study.tomcat.nio2.Nio2Demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.Selector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author:wangyi
 * @Date:2019/11/14
 */
public class Nio2Server {
    private AsynchronousServerSocketChannel assc;
    private  ExecutorService pool;
    private Selector selector;

    public Nio2Server() throws IOException {
        pool = Executors.newCachedThreadPool();
        selector = Selector.open();
    }


    void listen() throws IOException {
        ExecutorService es = Executors.newCachedThreadPool();
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withCachedThreadPool(es,5);
        AsynchronousServerSocketChannel channel = AsynchronousServerSocketChannel.open(group);
        channel.bind(new InetSocketAddress(8080));
        channel.accept(this,new AcceptHandler());
    }

    public ExecutorService getPool() {
        return pool;
    }

    public void setPool(ExecutorService pool) {
        this.pool = pool;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public AsynchronousServerSocketChannel getAssc() {
        return assc;
    }

    public void setAssc(AsynchronousServerSocketChannel assc) {
        this.assc = assc;
    }
}

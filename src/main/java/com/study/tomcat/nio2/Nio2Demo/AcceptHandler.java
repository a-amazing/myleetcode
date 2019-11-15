package com.study.tomcat.nio2.Nio2Demo;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @author:wangyi
 * @Date:2019/11/14
 */
public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel,Nio2Server> {

    @Override
    public void completed(AsynchronousSocketChannel result, Nio2Server attachment) {
        attachment.getAssc().accept(attachment,this);
    }

    @Override
    public void failed(Throwable exc, Nio2Server attachment) {

    }
}

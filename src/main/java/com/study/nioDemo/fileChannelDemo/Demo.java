package com.study.nioDemo.fileChannelDemo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author:wangyi
 * @Date:2019/10/30
 */
public class Demo {
    public static void main(String[] args) throws IOException {
        //获取对象
        FileChannel channel;
        //FileInputStream fileInputStream = new FileInputStream("a.txt");
        //channel = fileInputStream.getChannel();
        RandomAccessFile file = new RandomAccessFile("C:\\Users\\wbkf5\\Desktop\\test\\testFile.txt", "r");
        channel = file.getChannel();


        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.read(buffer);
        buffer.flip();
        byte[] bytes = new byte[1024];
        int index = 0;
        while (buffer.hasRemaining()) {
            bytes[index] = buffer.get();
            index++;
        }
        System.out.println(new String(bytes));

        buffer.clear();
        buffer.put("new txt".getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }

        channel.close();
    }
}

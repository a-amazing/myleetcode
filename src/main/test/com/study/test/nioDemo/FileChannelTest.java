package com.study.test.nioDemo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author:wangyi
 * @Date:2019/10/28
 */
public class FileChannelTest {

    public static void main(String[] args) throws IOException {
        RandomAccessFile file = new RandomAccessFile("C:/Users/wbkf5/Desktop/test/testFile.txt", "rw");
        FileChannel channel = file.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(24);
        int read = 0;
        byte[] dst = new byte[24];
        ArrayList<Byte> bytes = new ArrayList<>();
        while ((read = channel.read(buffer)) != -1) {
            buffer.flip();
//            for (int i = buffer.position(), j = buffer.limit(); i < j; i++) {
//
//            }
            int op = buffer.position();
            int ed = buffer.limit();
            buffer.get(dst, op, ed);
            for (int i = 0; i < ed; i++) {
                bytes.add(dst[i]);
            }
            buffer.clear();
        }
        Byte[] array = bytes.toArray(new Byte[0]);
        byte[] byteArr = new byte[array.length];
        for (int i = 0, len = array.length; i < len; i++) {
            byteArr[i] = array[i];
        }
        String str = new String(byteArr);
        System.out.println(str);
    }
}

package com.study.netty.buffer.byteBuffer;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author:wangyi
 * @Date:2019/11/13
 */
public class ByteBufferDemo {
    public static void main(String[] args) {
        byte[] bytes = {1,2,3,4,5};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        ByteBuffer wrapoffset = ByteBuffer.wrap(bytes,2,2);
        bytes[0] = 117;
        if(buffer.position() != 0){
            buffer.flip();
        }
        System.out.println(buffer.get());

//        buffer.clear();
//        byte temp = 12;
//        buffer.put(0,temp);
//        System.out.println(bytes[0]);

//        System.out.println(Arrays.toString(bytes));

        System.out.println(wrapoffset.get(0));
    }
}

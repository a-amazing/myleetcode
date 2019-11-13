package com.study.netty.socketReUse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

/**
 * @author:wangyi
 * @Date:2019/11/13
 */
public class SocketReUseDemo {
    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    createConn();
                }
            }).start();
        }
    }

    public static void createConn(){
        URL url = null;
        try {
            url = new URL("http://114.114.114.114");
            URLConnection conn = url.openConnection();
            conn.connect();
            TimeUnit.MINUTES.sleep(1);
            conn.setConnectTimeout(1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

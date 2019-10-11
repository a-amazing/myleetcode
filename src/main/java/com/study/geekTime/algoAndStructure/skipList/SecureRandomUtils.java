package com.study.geekTime.algoAndStructure.skipList;

import java.security.SecureRandom;

/**
 * @author:wangyi
 * @Date:2019/10/11
 */
public class SecureRandomUtils {
    private static final SecureRandom random = new SecureRandom();

    public static int getNextInt(int bound) {
        return random.nextInt(bound);
    }
}

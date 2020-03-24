package com.study.newInstanceDemo;

/**
 * @author wangyi
 * @date 2020/03/24
 */
public class Father {
    private int fi = 10;
    private int fj;
    private static int fstatic = 5;

    static {
        System.out.println("fstatic = " + fstatic);
        System.out.println("father static invoke");
        fstatic += 5;
    }

    {
        fj = 20;
        System.out.println("father invoke!");
    }

    public Father() {
        System.out.println("fi = " + fi);
        System.out.println("fj = " + fj);
        System.out.println("fstatic 2 = " + fstatic);
    }

}

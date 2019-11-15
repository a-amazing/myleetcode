package java.com.study.test.normalTest;

/**
 * @author:wangyi
 * @Date:2019/8/19
 */
public class TryCatchFinallyTest {


    @SuppressWarnings("finally")
    public static final String test1() {
        String t = "";

        try {
            t = t + "try";
            Integer.parseInt(null);
            return t;
        } catch (Exception e) {
            t = t + " catch";
            Integer.parseInt(null);
            return t;
        } finally {
            t = t + " finally";
            return t;
        }
    }

    @SuppressWarnings("finally")
    public static final String test2() {
        String t = "";

        try {
            t = t + "try";
            Integer.parseInt(null);
            return t;
        } catch (Exception e) {
            t = t + " catch";
            return t;
        } finally {
            t = t + " finally";
            return t;
        }
    }

    @SuppressWarnings("finally")
    public static final String test3() {
        String t = "";

        try {
            t = t + "try";
            return t;
        } catch (Exception e) {
            t = t + " catch";
            return t;
        } finally {
            t = t + " finally";
            return t;
        }
    }

    @SuppressWarnings("finally")
    public static final String test4() {
        String t = "";

        try {
            t = t + "try";
            return t;
        } catch (Exception e) {
            t = t + " catch";
            return t;
        } finally {
            t = t + " finally";
        }
    }

    public static final String test5() {
        String t = "";

        try {
            t = t + "try";
        } catch (Exception e) {
            t = t + " catch";
            return t;
        } finally {
            t = t + " finally";
        }
        return t;
    }

    public static void main(String[] args) {
        //System.out.println(TryCatchFinally.test());
        System.out.println(TryCatchFinallyTest.test1());
        System.out.println(TryCatchFinallyTest.test2());//1、2说明只要try有异常，一定会执行catch，如果finally有返回,一定从finally返回
        System.out.println(TryCatchFinallyTest.test3());//3说明只要try没有异常，一定不执行catch
        System.out.println(TryCatchFinallyTest.test4());//4说明只要try没有异常，在try里返回数据且finally处没有返回，则finally没有改变try返回的数据
        System.out.println(TryCatchFinallyTest.test5());//5说明只要try没有异常，在try里不返回数据且finally处没有返回，则finally改变了try里面的数据
    }


}

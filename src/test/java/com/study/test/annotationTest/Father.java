package java.com.study.test.annotationTest;

/**
 * @author:wangyi
 * @Date:2019/9/17
 */
public class Father {

    @MyAnno("father anno")
    public void test(){
        System.out.println("father");
    }

    @MyAnno("father anno 2")
    public void test2(){
        System.out.println("father test2");
    }
}

package java.com.study.test.normalTest;

/**
 * @author:wangyi
 * @Date:2019/8/20
 */
public class SystemTest {
    public static void main(String[] args) {
        String env = System.getenv("CATALINA_HOME_DAF");
        System.out.println(env);

        String catalina_home_daf = System.getProperty("CATALINA_HOME_DAF");
        System.out.println(catalina_home_daf);
    }
}

package java.com.study.test.springTest;

import org.junit.Test;
import org.springframework.beans.BeanUtils;

/**
 * @author:wangyi
 * @Date:2019/9/24
 */
public class BeanUtilsTest {

    @Test
    public void testCopy() {
        TestClass1 a = new TestClass1();
        a.setAge(10);
        a.setName("wangyi");
        TestClass1.Inner inner = new TestClass1.Inner();
        inner.setInnerName("wangyi inner");
        a.setInner(inner);

        TestClass2 b = new TestClass2();
        TestClass2.Inner inner2 = new TestClass2.Inner();
        b.setInner(inner2);
        BeanUtils.copyProperties(a, b);
        BeanUtils.copyProperties(a.getInner(),b.getInner());

        System.out.println(b.getName());
        System.out.println(b.getAge());
        System.out.println(b.getInner().getInnerName());
    }
}

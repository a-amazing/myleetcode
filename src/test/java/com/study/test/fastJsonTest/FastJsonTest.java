package java.com.study.test.fastJsonTest;

import com.alibaba.fastjson.JSON;

import java.util.Map;

/**
 * @author:wangyi
 * @Date:2019/9/6
 */
public class FastJsonTest {
    public static void main(String[] args) {
        FundVo fundVo = new FundVo();
        fundVo.setFundCode("J04700");
        fundVo.setFundName("明月明月");
        fundVo.setMinimun("1000.00");
        String string = JSON.toJSONString(fundVo);
        Map<String,Object> map = JSON.parseObject(string,Map.class);

        System.out.println("complete!");
    }
}

package java.com.study.test.enumTest;

/**
 * @author:wangyi
 * @Date:2019/9/4
 */
public class EnumTest {

    public static void main(String[] args) {
        if(WeekDay.Mon.getDayValue() >= 1 && WeekDay.Mon.getDayValue() <= 5){
            System.out.println("It's weekdays!");
        }else{
            System.out.println("It's weekend!");
        }
        System.out.println(WeekDay.Mon.name());
    }

    enum WeekDay{

        Mon(1),Tues(2),Wed(3),Thur(4),Fri(5),Sat(6),Sun(6);
        private final int dayValue;

        private WeekDay(int dayValue){
            this.dayValue = dayValue;
        }

        public int getDayValue() {
            return dayValue;
        }
    }
}

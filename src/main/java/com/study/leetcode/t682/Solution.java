package com.study.leetcode.t682;

import java.util.Stack;

/**
 * 你现在是棒球比赛记录员。
 * 给定一个字符串列表，每个字符串可以是以下四种类型之一：
 * 1.整数（一轮的得分）：直接表示您在本轮中获得的积分数。
 * 2. "+"（一轮的得分）：表示本轮获得的得分是前两轮有效 回合得分的总和。
 * 3. "D"（一轮的得分）：表示本轮获得的得分是前一轮有效 回合得分的两倍。
 * 4. "C"（一个操作，这不是一个回合的分数）：表示您获得的最后一个有效 回合的分数是无效的，应该被移除。
 *
 * 每一轮的操作都是永久性的，可能会对前一轮和后一轮产生影响。
 * 你需要返回你在所有回合中得分的总和。
 *
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/baseball-game
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 */
public class Solution {

    Stack<String> stack = new Stack<>();
    Stack<String> reverse = new Stack<>();

    public int calPoints(String[] ops) {
        for (String op : ops) {
            if("C".equals(op)){
                if (stack.isEmpty()){
                    continue;
                }
                stack.pop();
            }else {
                stack.push(op);
            }
        }
        while(!stack.isEmpty()){
            reverse.push(stack.pop());
        }
        int first = 0;
        int second = 0;
        int sum = 0;
        int third = 0;
        if (reverse.isEmpty()){
            return 0;
        }else if(reverse.size() == 1){
            return Integer.parseInt(reverse.pop());
        }else if(reverse.size() == 2){
            first = Integer.parseInt(reverse.pop());
            String secondStr = reverse.pop();
            if("D".equals(secondStr)){
                second = first * 2;
            }else{
                second = Integer.parseInt(secondStr);
            }
            return first + second;
        }else{
            first = Integer.parseInt(reverse.pop());
            String secondStr = reverse.pop();
            if("D".equals(secondStr)){
                second = first * 2;
            }else{
                second = Integer.parseInt(secondStr);
            }
            sum = first + second;
            while(!reverse.isEmpty()){
                String pop = reverse.pop();
                third = calc(pop,first,second);
                sum+=third;
                first = second;
                second = third;
            }
            return sum;
        }
    }

    private int calc(String pop, int first, int second) {
        if("+".equals(pop)){
            return first + second;
        }
        if("D".equals(pop)){
            return 2 * second;
        }
        return Integer.parseInt(pop);
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        String[] arr = {"5","2","C","D","+"};
        solution.calPoints(arr);
    }
}

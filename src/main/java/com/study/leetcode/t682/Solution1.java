package com.study.leetcode.t682;

import java.util.Stack;

public class Solution1 {

    Stack<Integer> stack = new Stack<>();

    public int calPoints(String[] ops) {
        for (String op : ops) {
            switch (op) {
                case "D":
                    stack.push(stack.peek() * 2);
                    break;
                case "C":
                    stack.pop();
                    break;
                case "+":
                    Integer pop = stack.pop();
                    Integer push = pop + stack.peek();
                    stack.push(pop);
                    stack.push(push);
                    break;
                default:
                    stack.push(Integer.parseInt(op));
            }
        }
        int sum = 0;
        while(!stack.isEmpty()){
            sum += stack.pop();
        }
        return sum;
    }
}

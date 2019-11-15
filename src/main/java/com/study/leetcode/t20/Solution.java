package com.study.leetcode.t20;

public class Solution {
    public boolean isValid(String s) {
        if(s.equals("")){
            return true;
        }
        if(s.charAt(0) ==  '}' || s.charAt(0) == ']' || s.charAt(0) == ')'){
            return false;
        }
        char[] chars = s.toCharArray();
        Stack stack = new Stack(s.length());
        for (char aChar : chars) {
            if (aChar == '{' || aChar == '[' || aChar == '(') {
                stack.push(aChar);
            }
            if (aChar == '}') {
                if('{' != stack.pop()){
                    return false;
                }
            }
            if (aChar == ']') {
                if('[' != stack.pop()){
                    return false;
                }
            }
            if (aChar == ')') {
                if('(' != stack.pop()){
                    return false;
                }
            }
        }
        if (stack.getCount() == 0) {
            return true;
        }
        return false;
    }

    class Stack {
        char[] arr;
        int count;
        int size;

        Stack(int size) {
            arr = new char[size];
            this.size = size;
            this.count = 0;
        }

        public boolean push(char c) {
            if (count == size) {
                return false;
            }
            arr[count] = c;
            count++;
            return true;
        }

        public int getCount() {
            return count;
        }

        public char pop() {
            if (count == 0) {
                return ' ';
            }
            char c = arr[count - 1];
            count--;
            return c;
        }

        public char leak() {
            if (count == 0) {
                return ' ';
            }
            char c = arr[count - 1];
            return c;
        }
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        solution.isValid("(])");
    }
}

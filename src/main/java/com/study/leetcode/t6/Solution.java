package com.study.leetcode.t6;

/**
 * @author:wangyi
 * @Date:2020/8/12
 */
public class Solution {
    public String convert(String s, int numRows) {
        if (numRows == 1) {
            return s;
        }
        char[] chars = s.toCharArray();
        StringBuilder[] strs = new StringBuilder[numRows];
        for (int i = 0; i < numRows; i++) {
            strs[i] = new StringBuilder();
        }

        //这题最主要的逻辑就在这段内容
        int period = numRows * 2 - 2;
        for (int i = 0, len = chars.length; i < len; i++) {
            int mod = i % period;
            if (mod < numRows) {
                strs[mod].append(chars[i]);
            } else {
                strs[period - mod].append(chars[i]);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (StringBuilder asb : strs) {
            sb.append(asb);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String abc = new Solution().convert("ABC", 2);
        System.out.println(abc);
    }
}

package com.study.leetcode.t415;

/**
 * @author:wangyi
 * @Date:2020/8/13
 */
public class Solution {
    public String add(String num1, String num2) {
        char[] chars1 = num1.toCharArray();
        char[] chars2 = num2.toCharArray();
        int len1 = chars1.length;
        int len2 = chars2.length;
        int[] nums1 = new int[len1];
        int[] nums2 = new int[len2];

        for (int i = 0; i < len1; i++) {
            nums1[i] = chars1[i] - '0';
        }
        for (int i = 0; i < len2; i++) {
            nums2[i] = chars2[i] - '0';
        }

        int addResult = 0;
        int lift = 0;
        int i = 0;
        int j = 0;
        StringBuilder sb = new StringBuilder();
        for (i = len1 - 1, j = len2 - 1; i > -1 && j > -1; i--, j--) {
            addResult = nums1[i] + nums2[j] + lift;
            lift = addResult / 10;
            sb.append(addResult % 10);
        }

        if (i <= -1) {
            for (; j > -1; j--) {
                addResult = nums2[j] + lift;
                lift = addResult / 10;
                sb.append(addResult % 10);
            }
        } else if (j <= -1) {
            for (; i > -1; i--) {
                addResult = nums1[i] + lift;
                lift = addResult / 10;
                sb.append(addResult % 10);
            }
        }
        if (lift > 0) {
            sb.append(lift);
        }

        return sb.reverse().toString();
    }
}

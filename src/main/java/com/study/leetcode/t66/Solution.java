package com.study.leetcode.t66;

import java.util.Arrays;

/**
 * @author:wangyi
 * @Date:2020/8/7
 */
class Solution {
    public int[] plusOne(int[] digits) {
        int len = digits.length;
        int[] temp = new int[len];
        int index = len - 1;
        int carryFlag = 0;
        if (digits[index] == 9) {
            temp[index] = 0;
            carryFlag = 1;
        } else {
            temp[index] = digits[index] + 1;
            carryFlag = 0;
        }

        for (int i = index - 1; i >= 0; i--) {
            if (digits[i] == 9 && carryFlag == 1) {
                temp[i] = 0;
                carryFlag = 1;
            } else if (carryFlag == 1) {
                temp[i] = digits[i] + 1;
                carryFlag = 0;
            } else {
                temp[i] = digits[i];
                carryFlag = 0;
            }
        }

        if (carryFlag == 1) {
            int[] temp2 = new int[len + 1];
            System.arraycopy(temp, 0, temp2, 1, len);
            temp2[0] = 1;
            temp = temp2;
        }

        return temp;
    }

    public static void main(String[] args) {
        int[] ints = new Solution().plusOne(new int[]{1, 2, 3});
    }
}

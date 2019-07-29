package com.study.leetcode.t496;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author:wangyi
 * @Date:2019/7/29
 */
public class Solution {

    public int[] nextGreaterElement(int[] nums1, int[] nums2) {
        Stack<Integer> stack = new Stack<Integer>();
        HashMap<Integer, Integer> hasMap = new HashMap<Integer, Integer>();

        int[] result = new int[nums1.length];

        for (int num : nums2) {
            while (!stack.isEmpty() && stack.peek() < num) {
                hasMap.put(stack.pop(), num);
            }
            stack.push(num);
        }

        for (int i = 0; i < nums1.length; i++) result[i] = hasMap.getOrDefault(nums1[i], -1);

        return result;
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        int[] arr = {1,9,5,7,2,6,4,8,3};
        int[] arr2 = {1,3,5,7};
        int[] ints = solution.nextGreaterElement(arr2, arr);
        System.out.println(Arrays.toString(ints));
    }
}

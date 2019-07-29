package com.study.leetcode.t496;

import java.util.Stack;

/**
 *
 * 给定两个没有重复元素的数组 nums1 和 nums2 ，其中nums1 是 nums2 的子集。找到 nums1 中每个元素在 nums2 中的下一个比其大的值。
 * nums1 中数字 x 的下一个更大元素是指 x 在 nums2 中对应位置的右边的第一个比 x 大的元素。如果不存在，对应位置输出-1。
 *
 * @author:wangyi
 * @Date:2019/7/29
 */
public class Solution1 {

    Stack<Integer> stack = new Stack<>();

    /**
     * 最简单的做法,遍历nums1之后再遍历nums2
     * @param nums1
     * @param nums2
     * @return
     */
    public int[] nextGreaterElement(int[] nums1, int[] nums2) {
        for (int r = nums2.length-1; r >= 0; r--) {
            stack.push(nums2[r]);
        }
        int[] result = new int[nums1.length];
        Stack<Integer> temp = null;
        for (int i = 0; i < nums1.length; i++) {
            temp = (Stack<Integer>) stack.clone();
            result[i] = checkNextGreaterElement(temp,nums1[i]);
        }
        return result;
    }

    private int checkNextGreaterElement(Stack<Integer> temp, int i) {
        int result = -1;
        int flag = 0;
        while(!temp.isEmpty()){
            Integer pop = temp.pop();
            if(flag == 0 && pop != i){
                continue;
            }else if(flag == 0 && pop == i){
                flag = 1;
            }
            if(flag == 1){
                if(pop > i){
                    return pop;
                }
            }
        }
        return result;
    }
}

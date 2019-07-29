package com.study.leetcode.t496;

import java.util.Stack;

/**
 * 给定两个没有重复元素的数组 nums1 和 nums2 ，其中nums1 是 nums2 的子集。找到 nums1 中每个元素在 nums2 中的下一个比其大的值。
 * nums1 中数字 x 的下一个更大元素是指 x 在 nums2 中对应位置的右边的第一个比 x 大的元素。如果不存在，对应位置输出-1。
 *
 * @author:wangyi
 * @Date:2019/7/29
 */

/**
 * 学习了单调栈的使用,与解题无关
 */
public class Solution2 {

    Stack<Integer> monotoneStack = new Stack<>();

    public int[] nextGreaterElement(int[] nums1, int[] nums2) {
        for (int i : nums2) {
            monotoneStack.push(i);
        }
        int[] result = new int[nums1.length];
        for (int i = 0; i < nums1.length; i++) {
            Stack<Integer> clone = (Stack<Integer>) monotoneStack.clone();
            result[i] = checkNextGreaterNum(clone,nums1[i]);
        }
        return result;

    }

    private int checkNextGreaterNum(Stack<Integer> clone, int i) {
        int result = -1;
        while(!clone.isEmpty()){
            Integer pop = clone.pop();
            if(pop > i ){
                return pop;
            }
        }
        return result;
    }

    private void push(int x){
        if(monotoneStack.isEmpty()){
            monotoneStack.push(x);
            return;
        }
        Stack<Integer> temp = new Stack<>();
        while(!monotoneStack.isEmpty() && monotoneStack.peek() < x){
            temp.push(monotoneStack.pop());
        }
        monotoneStack.push(x);
        while(!temp.isEmpty()){
            monotoneStack.push(temp.pop());
        }
    }
}

package com.study.leetcode.t70;

import java.util.HashMap;
import java.util.Map;

/**
 * @author:wangyi
 * @Date:2019/7/31
 */
public class Solution {
    Map<Integer, Integer> map = new HashMap<>();

    /**
     * 假设你正在爬楼梯。需要 n 阶你才能到达楼顶。
     * 每次你可以爬 1 或 2 个台阶。你有多少种不同的方法可以爬到楼顶呢？
     * 注意：给定 n 是一个正整数。
     * <p>
     * 这是一个递归调用,所以要考虑怎么把之前计算的结果存入到缓存中
     *
     * @param n
     * @return
     */
    public int climbStairs(int n) {
        int[] cache = new int[n + 1];
        return climb(n, cache);
    }

    private int climb(int n, int[] cache) {
        int result = 0;
        if (n == 1) {
            result = 1;
            cache[n] = 1;
        } else if (n == 2) {
            result = 2;
            cache[n] = 2;
        } else {
            result = cache[n];
            if (result == 0) {
                result = climb(n - 1, cache) + climb(n - 2, cache);
            }
            cache[n] =result;
        }
        return result;
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        int i = solution.climbStairs(10);
        System.out.println(i);
    }
}

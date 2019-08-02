package com.study.leetcode.t1051;

import java.util.Arrays;

/**
 * @author:wangyi
 * @Date:2019/8/1
 */
public class Solution {
    public int heightChecker(int[] heights) {
        int[] sort = Arrays.copyOf(heights, heights.length);
        Arrays.sort(sort);
        int count = 0;
        for (int i = 0; i < sort.length; i++) {
            if (sort[i] == heights[i]) {
                continue;
            }
            count++;
        }
        return count;
    }
}

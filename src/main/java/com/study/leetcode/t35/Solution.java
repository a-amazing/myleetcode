package com.study.leetcode.t35;

/**
 * @author:wangyi
 * @Date:2020/5/19
 */
class Solution {
    public int searchInsert(int[] nums, int target) {
        //二分查找?
        int len = nums.length;
        int left = 0, right = len - 1; // 注意
        while(left <= right) { // 注意
            int mid = (left + right) / 2; // 注意
            if(nums[mid] == target) { // 注意
                // 相关逻辑
                return mid;
            } else if(nums[mid] < target) {
                left = mid + 1; // 注意
            } else {
                right = mid - 1; // 注意
            }
        }
        if(nums[left] > target){
            if(left - 1 > -1){
                return left -1;
            }else{
                return 0;
            }
        }else if(nums[right] < target){
            if(right + 1 < len){
                return right  + 1;
            }else{
                return len -1;
            }
        }
        return 0;
    }
}

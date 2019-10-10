package com.study.leetcode.t33;

/**
 * @author:wangyi
 * @Date:2019/10/10
 */
public class Solution2 {
    /**
     * 另外还有一种解题思路,将数组二分后,判断每段首尾是否大小顺序一致,如果一致且target目标值在范围内,直接对该段数组进行二分查找即可
     * 如果target在不一致的那段数组里,对那段数组进行递归即可
     */
    public int search(int[] nums, int target) {
        if (nums == null || nums.length == 0) return -1;
        int low = 0;
        int high = nums.length - 1;
        if (nums[low] <= nums[high]) {
            return normalBSearch(nums, target, low, high);
        } else {
            return bsearch(nums, target, low, high);
        }
    }

    public int bsearch(int[] nums, int target, int low, int high) {
        int mid;
        if (low <= high) {
            mid = low + ((high - low) >> 1);
            if (nums[low] <= nums[mid]) {
                if (target >= nums[low] && target <= nums[mid]) {
                    if (low <= mid)
                        return normalBSearch(nums, target, low, mid);
                } else {
                    if (mid <= high)
                        if (high - mid == 1) {
                            if (target == nums[high]) return high;
                            if (target == nums[mid]) return mid;
                            return -1;
                        } else
                            return bsearch(nums, target, mid, high);

                }
            } else if (nums[mid] <= nums[high]) {
                if (target >= nums[mid] && target <= nums[high]) {
                    if (mid <= high)
                        return normalBSearch(nums, target, mid, high);
                } else {
                    if (low <= mid)
                        if(mid - low == 1){
                            if (target == nums[low]) return low;
                            if (target == nums[mid]) return mid;
                            return -1;
                        }
                        return bsearch(nums, target, low, mid);
                }
            }
        }
        return -1;
    }

    public int normalBSearch(int[] nums, int target, int low, int high) {
        int mid;
        while (low <= high) {
            mid = low + ((high - low) >> 1);
            if (nums[mid] == target) {
                return mid;
            } else if (nums[mid] > target) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        int[] arr = {4, 5, 6, 7, 0, 1, 2};
        Solution2 solution = new Solution2();
        int search = solution.search(arr, 3);
        System.out.println(search);
    }
}

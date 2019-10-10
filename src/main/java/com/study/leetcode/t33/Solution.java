package com.study.leetcode.t33;

/**
 * @author:wangyi
 * @Date:2019/10/10
 */
public class Solution {
    /**
     * 假设按照升序排序的数组在预先未知的某个点上进行了旋转。
     * ( 例如，数组 [0,1,2,4,5,6,7] 可能变为 [4,5,6,7,0,1,2] )。
     * 搜索一个给定的目标值，如果数组中存在这个目标值，则返回它的索引，否则返回 -1 。
     * 你可以假设数组中不存在重复的元素。
     * 你的算法时间复杂度必须是 O(log n) 级别。
     * 来源：力扣（LeetCode）
     * 链接：https://leetcode-cn.com/problems/search-in-rotated-sorted-array
     * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
     *
     * @param nums
     * @param target
     * @return
     */
    public static int search(int[] nums, int target) {
        /**
         * 如果这是一个有序的数组,可以很方便的通过二分查找的方式查找对应的元素是否存在
         * 所有,难点在于如何找到旋转点,将数组重新转化为有序数组
         * 或者说,在数组的两段区间内分别做二分查找
         */
        if (nums == null || nums.length == 0) {
            return -1;
        }
        //1.找到旋转点
        int low = 0;
        int high = nums.length - 1;
        int mid;
        int lowValue = nums[low];
        int highValue = nums[high];
        int reversePoint = -1;
        //发生了旋转
        if (lowValue > highValue) {
            while (low <= high) {
                mid = low + ((high - low) >> 1);
                if (nums[mid] >= lowValue) {
                    if (nums[mid + 1] <= highValue) {
                        reversePoint = mid;
                        break;
                    } else {
                        low = mid + 1;
                    }
                } else {
                    high = mid - 1;
                }
            }
        }

        if (reversePoint == -1) {
            low = 0;
            high = nums.length - 1;
        } else if (target > highValue) {
            low = 0;
            high = reversePoint;
        } else {
            low = reversePoint + 1;
            high = nums.length - 1;
        }
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
        int[] arr = {3, 1};
        System.out.println(search(arr, 0));
    }

    /**
     * 另外还有一种解题思路,将数组二分后,判断每段首尾是否大小顺序一致,如果一致且target目标值在范围内,直接对该段数组进行二分查找即可
     * 如果target在不一致的那段数组里,对那段数组进行递归即可
     */
}

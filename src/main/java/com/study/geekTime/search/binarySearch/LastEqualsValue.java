package com.study.geekTime.search.binarySearch;

/**
 * @author:wangyi
 * @Date:2019/10/10
 */
public class LastEqualsValue {

    public static int bsearch(int[] arr, int n, int value) {
        n = arr.length;
        int low = 0;
        int high = n - 1;
        int mid;

        while (low <= high) {
            mid = low + (high - low) / 2;
            if (arr[mid] > value) {
                high = mid - 1;
            } else if (arr[mid] < value) {
                low = mid + 1 ;
            } else {
                /**
                 * 如果a[mid]这个元素已经是数组中的最后一个元素了，那它肯定是我们要找的；如果a[mid]的后一个元素a[mid+1]不等于value，那也说明a[mid]就是我们要找的最后一个值等于给定值的元
                 * 素。
                 */
                if ((mid == n - 1) || arr[mid + 1] != value) return mid;
                else low = mid + 1;
            }
        }
        return -1;//元素不存在
    }
}

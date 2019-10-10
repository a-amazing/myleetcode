package com.study.geekTime.search.binarySearch;

/**
 * @author:wangyi
 * @Date:2019/10/10
 */
public class LastLessThanOrEquals {

    public static int bsearch(int[] arr, int n, int value) {
        n = arr.length;
        int low = 0;
        int high = n - 1;
        int mid;

        while (low <= high) {
            mid = low + ((high - low) >> 1);
            if (arr[mid] > value) {
                high = mid - 1;
            } else {
                if ((mid == n - 1) || arr[mid + 1] > value) {
                    return mid;
                } else {
                    low = mid + 1;
                }
            }
        }
        return -1;
    }
}

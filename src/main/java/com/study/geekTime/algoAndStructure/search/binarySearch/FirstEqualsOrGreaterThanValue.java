package com.study.geekTime.algoAndStructure.search.binarySearch;

/**
 * @author:wangyi
 * @Date:2019/10/10
 * @Description:第一个大于或者等于给定值的元素位置
 */
public class FirstEqualsOrGreaterThanValue {

    public static int bsearch(int[] arr, int n, int value) {
        n = arr.length;
        int low = 0;
        int high = n - 1;
        int mid;

        while(low <= high){
            mid = low + (high - low) /2;
            if(arr[mid] < value){
                low = mid + 1;
            }else {
                if (mid == 0 || arr[mid - 1] < value) return mid;
                else high = mid - 1;
            }
        }
        return -1;
    }

}

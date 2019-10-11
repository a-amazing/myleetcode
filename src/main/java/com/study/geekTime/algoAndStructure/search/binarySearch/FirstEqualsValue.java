package com.study.geekTime.algoAndStructure.search.binarySearch;

/**
 * @author:wangyi
 * @Date:2019/10/10
 */
public class FirstEqualsValue {

    public int firstEqualsValue(int[] arr, int low, int high, int value) {
        return 0;
    }

    public int bsearch(int[] a, int n, int value) {
        int low = 0;
        int high = n - 1;
        while (low <= high) {
            int mid = low + ((high - low) >> 1);
            if (a[mid] >= value) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        if (low < n && a[low] == value) return low;
        else return -1;
    }

    public int bsearch2(int[] a, int n, int value) {
        int low = 0;
        int high = n - 1;
        while (low <= high) {
            int mid = low + ((high - low) >> 1);
            if (a[mid] > value) {
                high = mid - 1;
            } else if (a[mid] < value) {
                low = mid + 1;
            //下面显示的其实是a[mid] == value时,再判断是否存在mid前一位是否等于value
            } else {
                //等于0说明没有前一位元素,!=value说明当前就是第一个等于value的元素
                /**
                 * 如果mid等于0，那这个元素已经是数组的第一个元素，那它肯定是我们要找的；如果mid不等于0，但a[mid]的前一个元素a[mid-1]不等于value，那也说明a[mid]就是我们要找的第一个值等于给
                 * 定值的元素。
                 */
                if ((mid == 0) || (a[mid - 1] != value)) return mid;
                else high = mid - 1;
            }
        }
        return -1;
    }
}

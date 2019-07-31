package com.study.sort.selectionSort;

import java.util.Arrays;

/**
 * @author:wangyi
 * @Date:2019/7/31
 */
public class SelectionSort {

    /**
     * 从小到大排序 选择排序
     *
     * @param arr
     * @return
     */
    public static int[] sort(int[] arr) {
        if (arr.length <= 1) {
            return arr;
        }
        for (int i = 0; i < arr.length - 1; i++) {
            //找到未排序部分的最小值
            int index = i;
            int min = arr[i];
            for (int j = i; j < arr.length; j++) {
                if (arr[j] < min) {
                    min = arr[j];
                    index = j;
                }
            }
            if (index == i) {
                continue;
            } else {
                arr[index] = arr[i];
                arr[i] = min;
            }
        }
        return arr;
    }

    public static void main(String[] args) {
        int[] arr = {1,5,3,9,6,8,4,2,2,2,2};

        arr = sort(arr);
        System.out.println(Arrays.toString(arr));
    }
}

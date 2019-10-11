package com.study.geekTime.algoAndStructure.sort.insertionSort;

import java.util.Arrays;

/**
 * @author:wangyi
 * @Date:2019/7/31
 */
public class InsertionSort {

    /**
     * 从小到大对数组进行排序
     *
     * @param arr
     * @return
     */
    public static int[] sort(int[] arr) {
        if (arr.length <= 1) {
            return arr;
        }
        //从第二个元素开始,插入到已经排序完成部分
        for (int i = 1; i < arr.length; i++) {
            for (int j = 0; j < i; j++) {
                //如果第i个元素小于第j个元素,就将第i个元素插入在j位置
                if (arr[i] < arr[j]) {
                    int temp = arr[i];
                    //j+1-i的元素等于其-1,每个元素后移一位
                    for (int k = i; k > j; k--) {
                        arr[k] = arr[k-1];
                    }
                    arr[j] = temp;
                    break;
                }
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

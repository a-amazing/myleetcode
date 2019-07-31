package com.study.sort.bubbleSort;

import java.util.Arrays;

/**
 * @author:wangyi
 * @Date:2019/7/31
 */
public class BubbleSort {

    /**
     * 冒泡排序arr数组(从小到大,从小开始冒泡到大)
     *
     * @param arr
     */
    public static int[] sort(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            int count = 0;
            for (int j = i + 1; j < arr.length; j++) {
                if(arr[j-1] > arr[j]){
                    int temp = arr[j];
                    arr[j] = arr[j-1];
                    arr[j-1] = temp;
                    count++;
                }
            }
            if(count == 0){
                break;
            }
        }
        return arr;
    }

    public static void main(String[] args) {
//        int[] arr = {1,3,5,2,6,9,7};
        int[] arr = {1, 2, 3, 5, 6, 7, 9};
        int[] sort = sort(arr);
        System.out.println(Arrays.toString(sort));
    }
}

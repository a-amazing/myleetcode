package com.study.sort.mergeSort;

import java.util.Arrays;

/**
 * @author:wangyi
 * @Date:2019/8/5
 */
public class MergeSort {

    /**
     * 通过分治思想,进行从小到大排序
     *
     * @param arr
     * @return
     */
    public static int[] sort(int[] arr) {
        if (arr == null || arr.length == 1) {
            return arr;
        } else if (arr.length == 2) {
            if (arr[0] > arr[1]) {
                int temp = arr[0];
                arr[0] = arr[1];
                arr[1] = temp;
            }
            return arr;
        }
        int len = arr.length;
        int half = len / 2;
        int[] result = new int[len];
        int[] result1 = new int[half];
        int[] result2 = new int[half];
        int[] left = new int[1];
        if (len % 2 == 0) {
            for (int i = 0; i < half; i++) {
                result1[i] = arr[i];
            }
            for (int i = half, j = 0; j < half; i++, j++) {
                result2[j] = arr[i];
            }
            result1 = sort(result1);
            result2 = sort(result2);
            result = merge(result1, result2);
        } else {
            for (int i = 0; i < half; i++) {
                result1[i] = arr[i];
            }
            for (int i = half + 1, j = 0; j < half; i++, j++) {
                result2[j] = arr[i];
            }
            left[0] = arr[half];
            result1 = sort(result1);
            result2 = sort(result2);
            result = merge(merge(result1, left), result2);
        }
        return result;
    }

    /**
     * 合并两个有序数组成为一个有序数组
     * @param arr1
     * @param arr2
     * @return
     */
    private static int[] merge(int[] arr1, int[] arr2) {
        int[] result = new int[arr1.length + arr2.length];
        int i = 0;
        int j = 0;
        int k = 0;
        while (i < arr1.length || j < arr2.length) {
            if (i < arr1.length && j < arr2.length) {
                if (arr1[i] <= arr2[j]) {
                    result[k] = arr1[i];
                    i++;
                } else {
                    result[k] = arr2[j];
                    j++;
                }
            } else if (i == arr1.length) {
                result[k] = arr2[j];
                j++;
            } else {
                result[k] = arr1[i];
                i++;
            }
            k++;
        }
        return result;
    }

    public static void main(String[] args) {
        int[] arr = {1, 5, 3, 9, 6, 8, 4, 2, 2, 2, 2};

        arr = sort(arr);
        System.out.println(Arrays.toString(arr));
    }
}

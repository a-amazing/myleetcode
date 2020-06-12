package com.study.leetcode.t443;

/**
 * @author:wangyi
 * @Date:2020/5/19
 */
class Solution {
    public static int compress(char[] chars) {
        int len = chars.length;
        int i = 0;
        int idx = 0;
        String count;
        for(int j = 0;j <= len;j++){
            if(j < len){
                if(chars[i] == chars[j]){
                    continue;
                }else{
                    if((j - i) == 1){
                        chars[idx] = chars[i];
                        idx++;
                        i = j;
                    }else{
                        count = String.valueOf(j-i);
                        chars[idx] = chars[i];
                        idx ++;
                        for(int x = 0;x < count.length();x++){
                            chars[idx] = count.charAt(x);
                            idx++;
                        }
                        i = j;
                    }
                }
            }else{
                if((j - i) == 1){
                    chars[idx] = chars[i];
                    idx++;
                    i = j;
                }else{
                    count = String.valueOf(j-i);
                    chars[idx] = chars[i];
                    idx ++;
                    for(int x = 0;x < count.length();x++){
                        chars[idx] = count.charAt(x);
                        idx++;
                    }
                    i = j;
                }
            }

        }
        return idx;
    }
}

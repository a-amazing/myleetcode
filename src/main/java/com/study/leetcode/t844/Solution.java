package com.study.leetcode.t844;

import java.util.Stack;

/**
 * 给定 S 和 T 两个字符串，当它们分别被输入到空白的文本编辑器后，判断二者是否相等，并返回结果。 # 代表退格字符。
 */
public class Solution {
    public boolean backspaceCompare(String S, String T) {
        Stack<Character> sStack = new Stack<>();
        Stack<Character> tStack = new Stack<>();
        char[] sChars = S.toCharArray();
        char[] tChars = T.toCharArray();
        for (char sChar : sChars) {
            if (sChar != '#'){
                sStack.push(sChar);
            }else{
                if(sStack.isEmpty()){
                    continue;
                }
                sStack.pop();
            }
        }
        for (char tChar : tChars) {
            if (tChar != '#'){
                tStack.push(tChar);
            }else{
                if(tStack.isEmpty()){
                    continue;
                }
                tStack.pop();
            }
        }
        /**
         * 增加关于栈大小的比较,在栈大小不一致时,避免遍历栈
         */
        if(sStack.size() != tStack.size()){
            return false;
        }
        char sTemp;
        char tTemp;
        while(!sStack.isEmpty() && !tStack.isEmpty()){
            sTemp = sStack.pop();
            tTemp = tStack.pop();
            if(sTemp != tTemp){
                return false;
            }
        }
        if(sStack.isEmpty() && tStack.isEmpty()){
            return true;
        }
        return  false;
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        solution.backspaceCompare("ab#c", "ad#c");
    }
}

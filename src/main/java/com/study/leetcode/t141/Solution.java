package com.study.leetcode.t141;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wangyi
 * @date 2019/07/24
 */
public class Solution {
    public boolean hasCycle(ListNode head) {
        Set<ListNode> set = new HashSet<>();
        while (head != null){
            set.add(head);
            head = head.next;
            if (set.contains(head)){
                return true;
            }
        }
        return false;
    }

}

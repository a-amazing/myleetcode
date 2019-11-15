package com.study.leetcode.t141;

/**
 * @author wangyi
 * @date 2019/07/24
 */
public class Solution1 {
    /**
     * 尝试使用快慢指针的方式进行测试
     * @param head
     * @return
     */
    public boolean hasCycle(ListNode head) {
        if(head == null || head.next == null){
            return false;
        }
        ListNode slow = head.next;
        ListNode fast = head.next.next;
        try {
            while (slow != null && fast != null) {
                if (slow == fast){
                    return true;
                }
                slow = slow.next;
                fast = fast.next.next;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }
}

package com.study.leetcode.t876;

public class Solution {
    //快慢双遍历,快的一次两个节点,慢的一次一个节点,
    public ListNode middleNode(ListNode head) {
        ListNode fast = head;
        ListNode slow = head;
        while(fast.next != null && fast.next.next != null & slow.next !=null){
            fast = fast.next.next;
            slow = slow.next;
        }
        if(fast.next == null){
            return slow;
        }else{
            return slow.next;
        }
    }
}

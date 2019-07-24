package com.study.leetcode.t206;

public class Solution {
    public ListNode reverseList(ListNode head) {
        if(head == null || head.next == null){
            return head;
        }
        return reverse(head,head.next);
    }

    public ListNode reverse(ListNode current,ListNode next){
        ListNode temp = next;
        while(next.next != null){
            temp = next.next;
            next.next = current;
            temp = reverse(next,temp);
        }

        return temp;
    }
}
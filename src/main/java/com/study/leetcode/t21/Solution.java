package com.study.leetcode.t21;

public class Solution {
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        if(l1 == null && l2 == null){
            return null;
        }
        ListNode head = null;
        head = lowerNode(l1, l2);
        if (head == l1) {
            l1 = l1.next;
        }
        if (head == l2) {
            l2 = l2.next;
        }
        ListNode temp = head;
        ListNode next = null;
        while (l1 != null || l2 != null) {
            next = lowerNode(l1, l2);
            temp.next = next;
            temp = next;
            if (temp == l1) {
                l1 = l1.next;
            } else {
                l2 = l2.next;
            }
        }
        return head;
    }

    ListNode lowerNode(ListNode n1, ListNode n2) {
        if (n1 == null && n2 == null) {
            return null;
        } else if (n1 == null) {
            return n2;
        } else if (n2 == null) {
            return n1;
        }
        int v1 = n1.val;
        int v2 = n2.val;
        if (v1 <= v2) {
            return n1;
        } else {
            return n2;
        }
    }
}

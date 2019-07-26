package com.study.leetcode.t19;

public class Solution {

    public ListNode removeNthFromEnd(ListNode head, int n) {
        int count = count(head);
        int index = count - n;
        if (index == 0) {
            head = head.next;
            return head;
        }
        ListNode temp = null;
        ListNode prev = null;
        ListNode next = null;
        ListNode remove = null;
        if (count == 0 || count == 1) {
            return null;
        } else if (count == 2) {
            if (n == 1) {
                head.next = null;
                return head;
            } else if (n == 2) {
                head = head.next;
                return head;
            }
        }

        for (int i = 0; i < index + 2; i++) {
            if (i == 0) {
                temp = head;
            } else {
                temp = temp.next;
            }
            if (i == index - 1) {
                prev = temp;
            }
            if (i == index) {
                remove = temp;
            }
            if (i == index + 1) {
                next = temp;
            }
        }
        prev.next = next;
        return head;
    }

    int count(ListNode head) {
        int count = 0;
        while (head != null) {
            count++;
            head = head.next;
        }
        return count;
    }
}

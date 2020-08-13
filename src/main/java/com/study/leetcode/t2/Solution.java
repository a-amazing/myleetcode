package com.study.leetcode.t2;

/**
 * @author:wangyi
 * @Date:2020/8/13
 */
public class Solution {

    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode head = new ListNode(0);
        int lift = 0;
        ListNode temp;
        ListNode curr = head;
        int result = 0;
        while (l1 != null && l2 != null) {
            result = l1.val + l2.val + lift;
            lift = result / 10;
            temp = new ListNode(result % 10);
            curr.next = temp;
            curr = temp;
            l1 = l1.next;
            l2 = l2.next;
        }
        while (l1 != null) {
            result = l1.val + lift;
            lift = result / 10;
            temp = new ListNode(result % 10);
            curr.next = temp;
            curr = temp;
            l1 = l1.next;
        }
        while (l2 != null) {
            result = l2.val + lift;
            lift = result / 10;
            temp = new ListNode(result % 10);
            curr.next = temp;
            curr = temp;
            l2 = l2.next;
        }

        if (lift > 0) {
            temp = new ListNode(lift);
            curr.next = temp;
        }
        return head.next;
    }

    class ListNode {
        int val;
        ListNode next;

        ListNode(int x) {
            val = x;
        }
    }
}

package com.study.leetcode.t19;

/**
 * @author:wangyi
 * @Date:2020/5/18
 */
public class Solution2 {
    public static ListNode removeNthFromEnd(ListNode head, int n) {
        //一趟扫描删除,需要多指针同时移动
        ListNode prev = head;
        ListNode end = prev;
        while(n >= 0){
            end = end.next;
            n--;
        }

        while(end != null){
            end = end.next;
            prev = prev.next;
        }

        prev.next = prev.next.next;

        return head;
    }

    static class ListNode {
        int val;
        ListNode next;
        ListNode(int x) { val = x; }
    }

    public static void main(String[] args){
        ListNode a = new ListNode(1);
        ListNode b = new ListNode(2);
        ListNode c = new ListNode(3);
        ListNode d = new ListNode(4);
        ListNode e = new ListNode(5);

        a.next = b;
        b.next = c;
        c.next = d;
        d.next = e;

        removeNthFromEnd(a,2);
    }
}

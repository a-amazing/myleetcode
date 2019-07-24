package com.study.leetcode.t206;

/**
 * @author wangyi
 * @date 2019/07/24
 */
public class Solution1 {

    /**
     * 尝试使用递归的方法
     * 思路解析
     * 1->2->3->4
     * 1->2->3<-4
     * 1->2<-3<-4
     * 1<-2<-3<-4
     * @param head
     * @return
     */
    public ListNode reverseList(ListNode head) {
        if(head == null || head.next ==null){
            return head;
        }
        ListNode temp = reverseList(head.next);
        head.next.next = head;
        head.next = null;
        return temp;
    }

    public static void main(String[] args) {
        Solution1 solution1 = new Solution1();
        ListNode head = new ListNode(1);
        head.next = new ListNode(2);
        head.next.next = new ListNode(3);
        solution1.printList(head);
        ListNode newHead = solution1.reverseList(head);
        solution1.printList(newHead);
    }

    public void printList(ListNode head){
        if(head == null){
            System.out.println("[]");
        }
        StringBuilder sb = new StringBuilder("[");
        do{
            sb.append(head.val).append(",");
            head =head.next;
        }while (head != null);
        String s = sb.toString();
        s = s.substring(0,s.length()-1)+"]";
        System.out.println(s);
    }
}

package com.study.geekTime.algoAndStructure.skipList;

import java.security.SecureRandom;

/**
 * @author:wangyi
 * @Date:2019/10/11
 */
public class SkipList {
    Node head;
    int size;
    Node[] indexs = new Node[32];

    public Node getHead() {
        return this.head;
    }

    public int size() {
        return this.size;
    }

    public void insert(int val) {
        if (head == null) {
            Node node = new Node();
            node.setNext(null);
            node.setValue(val);
        } else {
            Node node = getFirstLessThanVal(val);
            Node next = node.getNext();
            Node newNode = new Node();
            newNode.setValue(val);
            newNode.setNext(next);
            node.setNext(newNode);

        }
        size++;
        int k = SecureRandomUtils.getNextInt(32);
        int log = (int) Math.log(size());
        if (k > log) {
            k = log;
        }

        Node tempNode = null;
        for (int i = (k - 1); i >= 0; i--) {
            Node idxHead = indexs[i];//最上层索引,方便查找val的位置

        }
    }

    private Node getFirstLessThanVal(int val) {
        Node temp = null;
        Node head = getHead();
        for (int i = indexs.length - 1; i > -1; i--) {
            if (indexs[i] != null) {
                temp = indexs[i];
                break;
            }
        }

        //node.getDown() != null 说明这是索引节点,非正链节点
        while (temp != null && temp.getDown() != null) {
            
            temp = temp.getNext();
        }

    }

    class Node {
        Node next;
        int val;
        Node down;

        public Node getDown() {
            return down;
        }

        public void setDown(Node down) {
            this.down = down;
        }

        public void setValue(int value) {
            this.val = value;
        }

        public int getValue() {
            return val;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public Node getNext() {
            return this.next;
        }

    }
}

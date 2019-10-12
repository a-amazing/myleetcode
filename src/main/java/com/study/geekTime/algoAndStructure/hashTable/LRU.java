package com.study.geekTime.algoAndStructure.hashTable;

import java.util.Objects;

/**
 * @author:wangyi
 * @Date:2019/10/12
 */
public class LRU {

    private Node[] table;
    private int size;
    private int maxSize;
    private Node head;
    private Node tail;

    public LRU(int maxSize) {
        this.table = new Node[32];
        this.size = 0;
        this.head = null;
        this.tail = head;
        this.maxSize = maxSize;
    }

    /**
     * 通过散列表，我们可以很快地在缓存中找到一个数据。
     * 当找到数据之后，我们还需要将它移动到双向链表的尾部。
     *
     * @param val
     * @return
     */
    public Node find(int val) {
        Node node = new Node();
        node.setValue(val);
        int hashCode = node.hashCode();
        Node cacheNode = findNodeByHashCode(hashCode, val);
        if (cacheNode != null) {
            Node prev = cacheNode.prev;
            Node next = cacheNode.next;
            prev.next = next;
            next.prev = prev;
            this.tail.next = cacheNode;
            cacheNode.prev = this.tail;
            this.tail = cacheNode;
        }
        return cacheNode;
    }

    /**
     * 我们需要找到数据所在的结点，然后将结点删除。
     * 借助散列表，我们可以在O(1)时间复杂度里找到要删除的结点。
     * 因为我们的链表是双向链表，双向链表可以通过前驱指针O(1)时间复杂度获取前驱结点，
     * 所以在双向链表中，删除结点只需要O(1)的时间复杂度。
     *
     * @param val
     */
    public void delete(int val) {
        Node node = new Node();
        node.setValue(val);
        int hashCode = node.hashCode();
        Node cacheNode = findNodeByHashCode(hashCode, val);
        if (cacheNode != null) {
            Node prev = cacheNode.prev;
            Node next = cacheNode.next;
            prev.next = next;
            next.prev = prev;
        }
    }

    private Node findNodeByHashCode(int hashCode, int val) {
        int bucket = hashCode & 32;
        Node temp = table[bucket];
        while (temp != null) {
            if (temp.getValue() == val) {
                return temp;
            } else {
                temp = temp.getHnext();
            }
        }
        return null;
    }

    /**
     * 添加数据到缓存稍微有点麻烦，我们需要先看这个数据是否已经在缓存中。
     * 如果已经在其中，需要将其移动到双向链表的尾部；
     * 如果不在其中，还要看缓存有没有满。
     * 如果满了，则将双向链表头部的结点删除，然后再将数据放到链表的尾部；
     * 如果没有满，就直接将数据放到链表的尾部。
     *
     * @param val
     */
    public void insert(int val) {
        Node node = new Node();
        node.setValue(val);

    }


    class Node {
        private Node next;
        private Node prev;
        private Node hnext;
        private int value;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return value == node.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        public Node() {
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public Node getPrev() {
            return prev;
        }

        public void setPrev(Node prev) {
            this.prev = prev;
        }

        public Node getHnext() {
            return hnext;
        }

        public void setHnext(Node hnext) {
            this.hnext = hnext;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}

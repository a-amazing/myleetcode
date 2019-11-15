package com.study.geekTime.algoAndStructure.skipList;

/**
 * @author:wangyi
 * @Date:2019/10/11
 */
public class SkipList {
    Node head;
    int size;
    Node[] indexs = new Node[32];//0-31 分别存储了1-32级索引的首节点

    public Node getHead() {
        return this.head;
    }

    public int size() {
        return this.size;
    }

    public void insert(int val) {
        //-1插入末尾节点 0插入普通中间节点 1插入首节点
        //去除-1判断条件,末尾节点不需要进行特殊判断
        int insertHeadNode = 0;
        if (head == null) {
            Node node = new Node();
            node.setNext(null);
            node.setValue(val);
            insertHeadNode = 1;
        } else {
            Node node = getFirstLessThanVal(val);
            //没有比该节点小的节点,在头部插入该节点
            if (node == null) {
                Node head = getHead();
                Node newHead = new Node();
                newHead.setValue(val);
                newHead.setNext(head);
                this.head = newHead;
                insertHeadNode = 1;
            } else {
                Node next = node.getNext();
                Node newNode = new Node();
                newNode.setValue(val);
                newNode.setNext(next);
                node.setNext(newNode);
            }
        }
        size++;
        int k = SecureRandomUtils.getNextInt(32);
        int log = (int) Math.log(size());
        int last = -1;
        for (int i = 31; i >= 0; i--) {
            if (indexs[i] != null) {
                last = i;
                break;
            }
        }
        if (k > log) {
            k = log;
        }

        Node tempNode = null;
        for (int i = (k - 1); i >= 0; i--) {
            Node idxHead = indexs[i];//最上层索引,方便查找val的位置

        }

        //插入头部节点,所有已存在的索引必须增加头节点
        if (insertHeadNode == 1) {

        } else {

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
            Node next;
            if (temp.getValue() <= val) {
                //索引节点的末尾
                next = temp.getNext();
                if (next == null) {
                    temp = temp.getDown();
                    //还有后置索引节点
                } else {
                    //值在temp和next之间
                    if (next.getValue() >= val) {
                        temp = temp.getDown();
                    } else {
                        //在下一个索引区间
                        temp = next;
                    }
                }
            }
        }

        //单链节点
        if (temp != null && temp.getDown() == null) {
            head = temp;
        }

        while (head != null) {
            if (head.getValue() <= val) {
                //已经是最后一个节点
                if (head.getNext() == null) {
                    return null;
                } else {
                    if (head.getNext().getValue() >= val) {
                        return head;
                    } else {
                        head = head.next;
                    }
                }
            }
        }
        return head;
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

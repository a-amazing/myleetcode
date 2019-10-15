package com.study.geekTime.algoAndStructure.binaryTree.binarySearchTree;

/**
 * @author:wangyi
 * @Date:2019/10/15
 */
public class BinarySearchTree {
    private Node root;

    public Node getRoot() {
        return root;
    }

    public void insert(int val) {
        if (root == null) {
            Node node = new Node();
            node.setValue(val);
            root = node;
        } else {
            insert(root, val);
        }
    }

    private void insert(Node temp, int val) {
        if(temp.getValue() > val){
            if(temp.getLeft() == null){
                Node node = new Node();
                node.setValue(val);
                temp.setLeft(node);
            }else{
                insert(temp.getLeft(),val);
            }
        }else if(temp.getValue() < val){
            if(temp.getRight() == null){
                Node node = new Node();
                node.setValue(val);
                temp.setRight(node);
            }else{
                insert(temp.getRight(),val);
            }
        }
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public Node quickSearch(int val) {
        return quickSearch(root, val);
    }

    public Node quickSearch(Node temp, int val) {
        if (temp == null) return null;
        if (temp.getValue() == val) {
            return temp;
        } else if (temp.getValue() < val) {
            return quickSearch(temp.getRight(), val);
        } else {
            return quickSearch(temp.getLeft(), val);
        }
    }

    static class Node {
        private Node left;
        private Node right;
        private int value;

        public Node getLeft() {
            return left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        Node node1 = new Node();
        node1.setValue(2);
        Node node2 = new Node();
        node2.setValue(0);
        Node node3 = new Node();
        node3.setValue(-1);
        Node node4 = new Node();
        node4.setValue(1);
        Node node5 = new Node();
        node5.setValue(3);
        Node node6 = new Node();
        node6.setValue(4);

        BinarySearchTree tree = new BinarySearchTree();
        node1.setLeft(node2);
        node1.setRight(node5);
        node2.setLeft(node3);
        node2.setRight(node4);
        node5.setRight(node6);
        tree.setRoot(node1);

        Node node = tree.quickSearch(1);
        tree.insert(10);
        System.out.println("end!");
    }
}

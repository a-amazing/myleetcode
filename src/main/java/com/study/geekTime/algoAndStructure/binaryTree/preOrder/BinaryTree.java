package com.study.geekTime.algoAndStructure.binaryTree.preOrder;

/**
 * @author:wangyi
 * @Date:2019/10/15 前序遍历:先打印本身,再打印左子树,再打印右子树
 * 中序遍历:先打印左子树,再打印本身,再打印右子树(大小有序?)
 * 后序遍历:先打印左子树,再打印右子树,再打印本身
 */
public class BinaryTree {
    private Node root;

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    /**
     * 前序遍历
     * @param temp
     */
    public void preOrder(Node temp) {
        if (temp == null) return;
        System.out.println(temp.getValue());
        preOrder(temp.getLeft());
        preOrder(temp.getRight());
    }

    /**
     * 中序遍历
     * @param temp
     */
    public void midOrder(Node temp) {
        if (temp == null) return;
        midOrder(temp.getLeft());
        System.out.println(temp.getValue());
        midOrder(temp.getRight());
    }

    /**
     * 后序遍历
     * @param temp
     */
    public void postOrder(Node temp) {
        if (temp == null) return;
        postOrder(temp.getLeft());
        postOrder(temp.getRight());
        System.out.println(temp.getValue());
    }

    /**
     * 按层遍历
     * 层次遍历需要借助队列这样一个辅助数据结构。
     * （其实也可以不用，这样就要自己手动去处理节点的关系，代码不太好理解，好处就是空间复杂度是o(1)。
     * 不过用队列比较好理解，缺点就是空间复杂度是o(n)）。
     * 根节点先入队列，然后队列不空，取出对头元素，如果左孩子存在就入列队，否则什么也不做，右孩子同理。
     * 直到队列为空，则表示树层次遍历结束。树的层次遍历，其实也是一个广度优先的遍历算法。
     * @param temp
     */
    public void levelOrder(Node temp) {
        //todo
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

        BinaryTree tree = new BinaryTree();
        node1.setLeft(node2);
        node1.setRight(node5);
        node2.setLeft(node3);
        node2.setRight(node4);
        node5.setRight(node6);
        tree.setRoot(node1);

        //tree.preOrder(tree.getRoot());
        //tree.midOrder(tree.getRoot());
        tree.postOrder(tree.getRoot());
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
}

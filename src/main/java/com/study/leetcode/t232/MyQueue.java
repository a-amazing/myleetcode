package com.study.leetcode.t232;

import java.util.Stack;

/**
 * 使用栈实现队列的下列操作：
 *
 * push(x) -- 将一个元素放入队列的尾部。
 * pop() -- 从队列首部移除元素。
 * peek() -- 返回队列首部的元素。
 * empty() -- 返回队列是否为空。
 * 你只能使用标准的栈操作 -- 也就是只有 push to top, peek/pop from top, size, 和 is empty 操作是合法的。
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/implement-queue-using-stacks
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 * @author wangyi
 * @date 2019/07/28
 */
public class MyQueue {

    Stack<Integer> stack;
    Stack<Integer> temp;
    /** Initialize your data structure here. */
    public MyQueue() {
        stack = new Stack<Integer>();
        temp = new Stack<Integer>();
    }

    /** Push element x to the back of queue. */
    public void push(int x) {
        if(stack.isEmpty()){
            stack.push(x);
            return;
        }
        reverseAndCopy(stack,temp);
        temp.push(x);
        reverseAndCopy(temp,stack);
    }

    private void reverseAndCopy(Stack<Integer> from, Stack<Integer> to) {
        while(!from.isEmpty()){
            to.push(from.pop());
        }
    }

    /** Removes the element from in front of queue and returns that element. */
    public int pop() {
        if (stack.isEmpty()){
            return 0;
        }
        return stack.pop();
    }

    /** Get the front element. */
    public int peek() {
        if (stack.isEmpty()){
            return 0;
        }
        return stack.peek();
    }

    /** Returns whether the queue is empty. */
    public boolean empty() {
        return stack.isEmpty();
    }
}

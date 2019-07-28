package com.study.leetcode.t155;

import java.util.Stack;

/**
 * 设计一个支持 push，pop，top 操作，并能在常数时间内检索到最小元素的栈。
 *
 * @author wangyi
 * @date 2019/07/28
 */
public class MinStack {

    Stack<Integer> stack;
    Stack<Integer> min_Stack;

    /**
     * initialize your data structure here.
     */
    public MinStack() {
        this.stack = new Stack();
        this.min_Stack = new Stack();
    }

    /**
     * 将元素 x 推入栈中。
     *
     * @param x
     */
    public void push(int x) {
        this.stack.push(x);
        if (this.min_Stack.empty() || x <= this.min_Stack.peek()) {
            this.min_Stack.push(x);
        }
    }

    /**
     * 删除栈顶的元素。
     */
    public void pop() {
        if (!this.stack.empty()) {
            int pop = this.stack.pop();
            if (this.min_Stack.peek() == pop) {
                this.min_Stack.pop();
            }
        }
    }

    /**
     * 获取栈顶元素。
     *
     * @return
     */
    public int top() {
        return this.stack.peek();
    }

    /**
     * 检索栈中的最小元素。
     *
     * @return
     */
    public int getMin() {
        if (!this.min_Stack.empty())
            return this.min_Stack.peek();
        else
            return 0;
    }
}

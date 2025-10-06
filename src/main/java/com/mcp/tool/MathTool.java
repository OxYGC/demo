package com.mcp.tool;

import org.springframework.ai.tool.annotation.Tool;

/*
 * 四则运算
 */
public class MathTool {

    /**
     * @param a
     * @param b
     * @return a+b
     */
    @Tool(description = "两个数  字相加")
    public int addNumbers(int a, int b) {
        return a + b;
    }

    /**
     * @param a
     * @param b
     * @return a-b
     */
    @Tool(description = "两个数字相减")
    public int subNumbers(int a, int b) {
        return a - b;
    }

    /**
     * @param a
     * @param b
     * @return a*b
     */
    @Tool(description = "两个数字相乘")
    public int multiplyNumbers(int a, int b) {
        return a * b;
    }

    /**
     * @param a
     * @param b
     * @return a/b
     */
    @Tool(description = "两个数字相除")
    public double divideNumbers(int a, int b) {
        return (double) a / b;
    }
}



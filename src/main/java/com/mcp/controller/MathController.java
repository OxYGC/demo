package com.mcp.controller;

import com.mcp.tool.MathTool;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对外暴露的 Math 工具接口。
 * 
 * 基于现有的 MathTool 方法提供简单的 HTTP 访问入口：
 * - GET /api/math/add?a=1&b=2
 * - GET /api/math/sub?a=3&b=1
 * - GET /api/math/multiply?a=2&b=4
 * - GET /api/math/divide?a=8&b=2
 */
@RestController
@RequestMapping("/api/math")
public class MathController {

    // 为了最小化改动，这里直接实例化 MathTool；
    // 保持与 McpServerApplication 中的工具注册逻辑相互独立。
    private final MathTool mathTool = new MathTool();

    @GetMapping("/add")
    public int add(@RequestParam int a, @RequestParam int b) {
        return mathTool.addNumbers(a, b);
    }

    @GetMapping("/sub")
    public int sub(@RequestParam int a, @RequestParam int b) {
        return mathTool.subNumbers(a, b);
    }

    @GetMapping("/multiply")
    public int multiply(@RequestParam int a, @RequestParam int b) {
        return mathTool.multiplyNumbers(a, b);
    }

    @GetMapping("/divide")
    public ResponseEntity<?> divide(@RequestParam int a, @RequestParam int b) {
        if (b == 0) {
            return ResponseEntity.badRequest().body("参数 b 不能为 0（除数为 0）");
        }
        return ResponseEntity.ok(mathTool.divideNumbers(a, b));
    }
}

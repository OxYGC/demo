package com.mcp;

import com.mcp.tool.MathTool;
import com.mcp.tool.WeatherTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    /**
     *  注入
     * @return
     */
    @Bean
    public ToolCallbackProvider mathTool(){
        return MethodToolCallbackProvider.builder().toolObjects(new MathTool()).build();
    }


    /**
     *  注入
     * @return
     */
    @Bean
    public ToolCallbackProvider weatherTool(){
        return MethodToolCallbackProvider.builder().toolObjects(new WeatherTool()).build();
    }


}

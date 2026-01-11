package com.demo26.service;// DynamicThreadPoolTaskExecutor.java
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class DynamicThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    public void setCorePoolSize(int corePoolSize) {
        super.setCorePoolSize(corePoolSize);
        // JDK 的 ThreadPoolExecutor 允许运行时调整 corePoolSize
    }

    public void setMaxPoolSize(int maxPoolSize) {
        super.setMaxPoolSize(maxPoolSize);
    }

    public void setQueueCapacity(int capacity) {
        // ⚠️ 注意：BlockingQueue 容量不能直接改！需替换队列（见下文）
        throw new UnsupportedOperationException("Queue capacity cannot be changed dynamically");
    }

    // 获取当前状态（用于监控）
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return super.getThreadPoolExecutor();
    }
}
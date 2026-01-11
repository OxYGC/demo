package com.demo26.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TaskService {
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    private TaskStatusService taskStatusService;


    @PostConstruct
    public void init() {
        taskStatusService.setTaskExecutor(taskExecutor);
    }

    public String executeTasks(int threadCount, int taskCount) {
        String taskId = "task_" + System.currentTimeMillis();
        taskStatusService.startNewTask(taskId, taskCount);

        // 动态调整线程池（仅演示，生产慎用）
        if (taskExecutor.getCorePoolSize() < threadCount) {
            taskExecutor.setCorePoolSize(threadCount);
            taskExecutor.setMaxPoolSize(Math.max(threadCount, taskExecutor.getMaxPoolSize()));
        }

        for (int i = 0; i < taskCount; i++) {
            final int taskIdFinal = i;
            CompletableFuture.runAsync(() -> {
                // 标记任务开始
                taskStatusService.onTaskStart();
                try {
                    simulateTask(taskIdFinal);
                } finally {
                    taskStatusService.onTaskEnd();   // ✅ 标记任务结束
                }
            }, taskExecutor);
        }

        return taskId;
    }

    private void simulateTask(int taskId) {
        try {
            int delay = ThreadLocalRandom.current().nextInt(1000, 3000); // 1~3秒
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
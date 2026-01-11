// src/main/java/com/example/multithreaddemo/service/TaskStatusService.java
package com.demo26.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
// TaskStatusService.java（更新版）

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskStatusService {
    // 当前任务 ID
    private volatile String currentTaskId = null;

    // 计数器
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final AtomicInteger activeThreads = new AtomicInteger(0); // 当前活跃线程数

    // 时间戳
    private volatile Instant startTime = null;
    private volatile Instant lastUpdateTime = null;

    // 速率计算（滑动窗口）
    private final AtomicLong lastCompletedCount = new AtomicLong(0);
    private volatile double currentThroughput = 0.0; // tasks/sec

    // 总任务数
    private volatile int totalTasks = 0;

    // 线程池引用（用于获取队列/拒绝等信息）
    private ThreadPoolTaskExecutor taskExecutor;

    // 状态锁（简单同步）
    private final Object lock = new Object();


    public synchronized void startNewTask(String taskId, int total) {
        this.currentTaskId = taskId;
        this.totalTasks = total;
        this.completedTasks.set(0);
        this.activeThreads.set(0);
        this.startTime = Instant.now();
        this.lastUpdateTime = Instant.now();
        this.lastCompletedCount.set(0);
        this.currentThroughput = 0.0;
    }


    public void incrementCompleted() {
        completedTasks.incrementAndGet();
    }

    public TaskProgress getProgress(String taskId) {
        if (currentTaskId == null || !currentTaskId.equals(taskId)) {
            return new TaskProgress(0, 0, false); // 无任务或已过期
        }
        return new TaskProgress(totalTasks, completedTasks.get(), completedTasks.get() >= totalTasks);
    }

    public static class TaskProgress {
        public final int total;
        public final int completed;
        public final boolean finished;

        public TaskProgress(int total, int completed, boolean finished) {
            this.total = total;
            this.completed = completed;
            this.finished = finished;
        }
    }


    public void setTaskExecutor(ThreadPoolTaskExecutor executor) {
        this.taskExecutor = executor;
    }


    public void onTaskStart() {
        activeThreads.incrementAndGet();
    }

    public void onTaskEnd() {
        int completed = completedTasks.incrementAndGet();
        activeThreads.decrementAndGet();

        // 每 200ms 更新一次吞吐量（避免频繁计算）
        Instant now = Instant.now();
        if (java.time.Duration.between(lastUpdateTime, now).toMillis() >= 200) {
            long elapsedMs = java.time.Duration.between(lastUpdateTime, now).toMillis();
            if (elapsedMs > 0) {
                long delta = completed - lastCompletedCount.get();
                currentThroughput = (delta * 1000.0) / elapsedMs; // tasks per second
                lastCompletedCount.set(completed);
                lastUpdateTime = now;
            }
        }
    }

    public TaskMetrics getMetrics(String taskId) {
        if (currentTaskId == null || !currentTaskId.equals(taskId)) {
            return new TaskMetrics();
        }

        long elapsedMs = startTime != null ? java.time.Duration.between(startTime, Instant.now()).toMillis() : 0;
        int completed = completedTasks.get();
        int active = activeThreads.get();

        // 线程池指标（如果可用）
        int queueSize = 0;
        int poolSize = 0;
        int maxPoolSize = 0;
        long rejected = 0;

        if (taskExecutor != null) {
            queueSize = taskExecutor.getThreadPoolExecutor().getQueue().size();
            poolSize = taskExecutor.getThreadPoolExecutor().getPoolSize();
            maxPoolSize = taskExecutor.getThreadPoolExecutor().getMaximumPoolSize();
            rejected = taskExecutor.getThreadPoolExecutor().getRejectedExecutionHandler() instanceof java.util.concurrent.ThreadPoolExecutor.AbortPolicy
                    ? 0 : 0; // 简化：实际拒绝数需自定义统计
        }

        return new TaskMetrics(
                totalTasks,
                completed,
                active,
                elapsedMs,
                currentThroughput,
                poolSize,
                maxPoolSize,
                queueSize
        );
    }

    public static class TaskMetrics {
        public final int total;
        public final int completed;
        public final int activeThreads;
        public final long elapsedTimeMs;
        public final double throughput; // tasks/sec
        public final int poolSize;
        public final int maxPoolSize;
        public final int queueSize;

        public TaskMetrics() {
            this(0, 0, 0, 0, 0.0, 0, 0, 0);
        }

        public TaskMetrics(int total, int completed, int activeThreads, long elapsedTimeMs,
                           double throughput, int poolSize, int maxPoolSize, int queueSize) {
            this.total = total;
            this.completed = completed;
            this.activeThreads = activeThreads;
            this.elapsedTimeMs = elapsedTimeMs;
            this.throughput = Math.round(throughput * 100.0) / 100.0; // 保留两位小数
            this.poolSize = poolSize;
            this.maxPoolSize = maxPoolSize;
            this.queueSize = queueSize;
        }
    }

}
package com.demo26.controller;


import com.demo26.service.TaskService;
import com.demo26.service.TaskStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class TaskController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskStatusService taskStatusService;

    @GetMapping("/")
    public String index(Model model) {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int recommended = Math.max(2, cpuCores * 2);
//        int recommended = getRecommendedThreadCount();
        model.addAttribute("cpuCores", cpuCores);
        model.addAttribute("recommendedThreadCount", recommended);
        model.addAttribute("threadCount", recommended);
        model.addAttribute("taskCount", 20);
        return "index";
    }

    @GetMapping("/metrics")
    @ResponseBody
    public TaskStatusService.TaskMetrics getMetrics(@RequestParam String taskId) {
        return taskStatusService.getMetrics(taskId);
    }


    @PostMapping("/start")
    @ResponseBody
    public Map<String, Object> startTasks(
            @RequestParam(defaultValue = "5") int threadCount,
            @RequestParam(defaultValue = "20") int taskCount) {

        String taskId = taskService.executeTasks(threadCount, taskCount);

        return Map.of("success", true,
                "message", "任务已启动",
                "taskId", taskId,
                "total", taskCount);
    }

    @GetMapping("/progress")
    @ResponseBody
    public TaskStatusService.TaskProgress getProgress(@RequestParam String taskId) {
        return taskStatusService.getProgress(taskId);
    }



    private int getRecommendedThreadCount() {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        // 假设是 IO 密集型任务，保守估计：核心数 * 2
        return Math.max(2, cpuCores * 2);
    }






}

package com.student.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thymeleaf 页面控制器
 * 用于渲染系统门户首页
 *
 * @author Student System
 */
@Controller
public class IndexController {

    /**
     * 系统门户首页
     * 使用 Thymeleaf 模板引擎渲染
     */
    @GetMapping("/")
    public String index(Model model) {
        // 传递数据到模板
        model.addAttribute("systemName", "学生信息管理系统");
        model.addAttribute("systemVersion", "v1.0.0");
        model.addAttribute("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("description", "基于Spring Boot + Vue3的高性能学生信息管理系统");
        
        // 返回模板名称（对应 templates/index.html）
        return "index";
    }

    /**
     * 系统公告页面
     */
    @GetMapping("/notice")
    public String notice(Model model) {
        model.addAttribute("title", "系统公告");
        model.addAttribute("notices", new String[]{
            "系统已完成权限控制功能升级",
            "新增基于角色的访问控制（RBAC）",
            "支持动态权限管理",
            "优化了成绩排行榜性能"
        });
        return "notice";
    }

    /**
     * 关于页面
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "关于系统");
        model.addAttribute("features", new String[]{
            "JWT + Redis 用户认证",
            "RBAC 权限控制模型",
            "Redis ZSet 成绩排名",
            "Kafka 异步日志处理",
            "AOP 接口统计监控",
            "Docker 容器化部署"
        });
        return "about";
    }

}

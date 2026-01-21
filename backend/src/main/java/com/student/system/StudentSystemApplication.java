package com.student.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 学生信息管理系统 - 应用启动类
 *
 * @author Student System
 * @since 2024-01-21
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class StudentSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentSystemApplication.class, args);
        System.out.println("""

                ========================================
                学生信息管理系统启动成功！
                API 地址: http://localhost:8088/api
                Swagger 文档: http://localhost:8088/api/doc.html
                ========================================
                """);
    }

}

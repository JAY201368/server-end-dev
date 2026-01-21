package com.student.system.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 配置类
 * 配置 Kafka Topics
 *
 * @author Student System
 * @since 2024
 */
@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topics.operation-log:operation-log-topic}")
    private String operationLogTopic;

    /**
     * 创建系统日志主题
     * Topic: system-log-topic
     * 分区数: 3
     * 副本数: 1 (单机环境)
     */
    @Bean
    public NewTopic systemLogTopic() {
        return TopicBuilder.name("system-log-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * 创建操作日志主题（备用）
     */
    @Bean
    public NewTopic operationLogTopic() {
        return TopicBuilder.name(operationLogTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

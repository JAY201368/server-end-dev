package com.student.system.kafka;

import com.alibaba.fastjson2.JSON;
import com.student.system.dto.SystemLogMessage;
import com.student.system.entity.SysLog;
import com.student.system.service.SysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 系统日志 Kafka 消费者
 * 监听 system-log-topic，将日志写入 MySQL
 *
 * @author Student System
 * @since 2024
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemLogConsumer {

    private final SysLogService sysLogService;

    /**
     * 监听系统日志主题
     * 消费消息并写入数据库
     *
     * @param message 日志消息（JSON格式）
     * @param partition 分区
     * @param offset 偏移量
     * @param ack 手动确认对象
     */
    @KafkaListener(
            topics = "system-log-topic",
            groupId = "student-system-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeSystemLog(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("收到Kafka消息 - Topic: system-log-topic, Partition: {}, Offset: {}", partition, offset);

        try {
            // 解析JSON消息
            SystemLogMessage logMessage = JSON.parseObject(message, SystemLogMessage.class);

            // 转换为实体类
            SysLog sysLog = new SysLog();
            BeanUtils.copyProperties(logMessage, sysLog);
            sysLog.setResult("SUCCESS");

            // 保存到数据库
            boolean success = sysLogService.save(sysLog);

            if (success) {
                log.info("系统日志已保存到数据库 - 操作类型: {}, 操作人: {}, 目标ID: {}",
                        sysLog.getOperationType(), sysLog.getOperator(), sysLog.getTargetId());

                // 手动确认消息
                if (ack != null) {
                    ack.acknowledge();
                }
            } else {
                log.error("保存系统日志失败 - Message: {}", message);
            }

        } catch (Exception e) {
            log.error("处理Kafka消息异常 - Partition: {}, Offset: {}, Message: {}", partition, offset, message, e);
            // 这里可以选择是否确认消息，或者将失败的消息发送到死信队列
            // 为了避免阻塞消费，这里选择确认消息
            if (ack != null) {
                ack.acknowledge();
            }
        }
    }
}

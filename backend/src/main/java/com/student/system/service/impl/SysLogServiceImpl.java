package com.student.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.system.entity.SysLog;
import com.student.system.mapper.SysLogMapper;
import com.student.system.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 系统日志 Service 实现类
 *
 * @author Student System
 * @since 2024
 */
@Slf4j
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {
}

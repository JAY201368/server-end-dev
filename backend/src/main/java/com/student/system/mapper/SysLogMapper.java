package com.student.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.system.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统日志 Mapper 接口
 *
 * @author Student System
 * @since 2024
 */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {
}

package com.student.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.system.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生表 Mapper 接口
 * 继承MyBatis-Plus的BaseMapper，自动提供基础CRUD方法
 *
 * @author Student System
 * @since 2024
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    // BaseMapper已提供以下方法，无需手动编写：
    // - insert: 插入一条记录
    // - deleteById: 根据ID删除
    // - updateById: 根据ID更新
    // - selectById: 根据ID查询
    // - selectList: 查询列表
    // - selectPage: 分页查询
    // 等等...

    // 如需自定义SQL，可在此添加方法，并在对应的XML文件中实现
}

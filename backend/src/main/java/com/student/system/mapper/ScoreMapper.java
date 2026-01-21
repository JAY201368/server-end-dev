package com.student.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.system.entity.Score;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 成绩表 Mapper 接口
 * 继承MyBatis-Plus的BaseMapper，自动提供基础CRUD方法
 *
 * @author Student System
 * @since 2024
 */
@Mapper
public interface ScoreMapper extends BaseMapper<Score> {

    // BaseMapper已提供基础CRUD方法

    /**
     * 查询指定学期的所有成绩（用于排名计算）
     *
     * @param semester 学期
     * @return 成绩列表
     */
    @Select("SELECT * FROM score WHERE semester = #{semester} ORDER BY score DESC")
    List<Score> listBySemester(@Param("semester") String semester);

    /**
     * 查询学生在某个课程的成绩
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param semester 学期
     * @return 成绩对象
     */
    @Select("SELECT * FROM score WHERE student_id = #{studentId} AND course_id = #{courseId} AND semester = #{semester}")
    Score getByStudentAndCourse(@Param("studentId") Long studentId,
                                @Param("courseId") Long courseId,
                                @Param("semester") String semester);
}

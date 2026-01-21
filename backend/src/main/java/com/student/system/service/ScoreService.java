package com.student.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.system.entity.Score;
import com.student.system.vo.ScoreRankVO;

import java.util.List;

/**
 * 成绩服务接口
 * 继承MyBatis-Plus的IService接口，提供基础CRUD服务
 * 集成Redis ZSet实现成绩排名功能
 *
 * @author Student System
 * @since 2024
 */
public interface ScoreService extends IService<Score> {

    /**
     * 录入成绩
     * 保存到MySQL的同时，使用Redis ZSet存储成绩用于排名
     *
     * @param score 成绩对象
     * @return 是否成功
     */
    boolean saveScore(Score score);

    /**
     * 更新成绩
     * 更新MySQL的同时，同步更新Redis ZSet中的排名数据
     *
     * @param score 成绩对象
     * @return 是否成功
     */
    boolean updateScore(Score score);

    /**
     * 删除成绩
     * 删除MySQL记录的同时，从Redis ZSet中移除
     *
     * @param id 成绩ID
     * @return 是否成功
     */
    boolean deleteScore(Long id);

    /**
     * 获取成绩排行榜（前N名）
     * 直接从Redis ZSet中获取，性能高
     *
     * @param semester 学期
     * @param topN 前N名
     * @return 排行榜列表
     */
    List<ScoreRankVO> getTopRanking(String semester, int topN);

    /**
     * 获取学生的成绩排名
     *
     * @param studentId 学生ID
     * @param semester 学期
     * @return 排名（从1开始）
     */
    Long getStudentRank(Long studentId, String semester);

    /**
     * 查询学生在某个课程的成绩
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param semester 学期
     * @return 成绩对象
     */
    Score getByStudentAndCourse(Long studentId, Long courseId, String semester);

    /**
     * 同步某个学期的所有成绩到Redis
     * 用于初始化或数据修复
     *
     * @param semester 学期
     */
    void syncScoresToRedis(String semester);
}

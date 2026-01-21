package com.student.system.controller;

import com.student.system.common.Result;
import com.student.system.dto.ScoreDTO;
import com.student.system.entity.Score;
import com.student.system.service.ScoreService;
import com.student.system.vo.ScoreRankVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成绩管理控制器
 * 核心功能：
 * 1. 成绩录入（保存到MySQL + Redis ZSet）
 * 2. 成绩排行榜（从Redis ZSet快速查询）
 * 3. 学生排名查询
 *
 * @author Student System
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    /**
     * 录入成绩
     * 业务逻辑：
     * 1. 数据校验
     * 2. 保存到MySQL数据库
     * 3. 同步到Redis ZSet（用于排名）
     *
     * @param scoreDTO 成绩数据传输对象
     * @return 响应结果
     */
    @PostMapping("/save")
    public Result<String> saveScore(@Valid @RequestBody ScoreDTO scoreDTO) {
        log.info("开始录入成绩 - 学生ID: {}, 课程ID: {}, 分数: {}",
                scoreDTO.getStudentId(), scoreDTO.getCourseId(), scoreDTO.getScore());

        try {
            // 检查是否已存在该学生该课程该学期的成绩
            Score existingScore = scoreService.getByStudentAndCourse(
                    scoreDTO.getStudentId(),
                    scoreDTO.getCourseId(),
                    scoreDTO.getSemester()
            );

            if (existingScore != null) {
                return Result.error("该学生在该课程该学期的成绩已存在，请使用更新接口");
            }

            // DTO转Entity
            Score score = new Score();
            BeanUtils.copyProperties(scoreDTO, score);

            // 调用Service保存成绩（同时写入MySQL和Redis）
            boolean success = scoreService.saveScore(score);

            if (success) {
                return Result.success("成绩录入成功");
            } else {
                return Result.error("成绩录入失败");
            }

        } catch (Exception e) {
            log.error("录入成绩异常", e);
            return Result.error("录入成绩失败: " + e.getMessage());
        }
    }

    /**
     * 更新成绩
     * 同步更新MySQL和Redis ZSet
     *
     * @param scoreDTO 成绩数据传输对象
     * @return 响应结果
     */
    @PutMapping("/update")
    public Result<String> updateScore(@Valid @RequestBody ScoreDTO scoreDTO) {
        log.info("开始更新成绩 - ID: {}, 新分数: {}", scoreDTO.getId(), scoreDTO.getScore());

        try {
            if (scoreDTO.getId() == null) {
                return Result.error("成绩ID不能为空");
            }

            // 检查成绩是否存在
            Score existingScore = scoreService.getById(scoreDTO.getId());
            if (existingScore == null) {
                return Result.error("成绩不存在");
            }

            // 更新数据
            Score score = new Score();
            BeanUtils.copyProperties(scoreDTO, score);

            // 调用Service更新成绩（同时更新MySQL和Redis）
            boolean success = scoreService.updateScore(score);

            if (success) {
                return Result.success("成绩更新成功");
            } else {
                return Result.error("成绩更新失败");
            }

        } catch (Exception e) {
            log.error("更新成绩异常", e);
            return Result.error("更新成绩失败: " + e.getMessage());
        }
    }

    /**
     * 删除成绩
     * 同时从MySQL和Redis ZSet中删除
     *
     * @param id 成绩ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteScore(@PathVariable Long id) {
        log.info("开始删除成绩 - ID: {}", id);

        try {
            boolean success = scoreService.deleteScore(id);

            if (success) {
                return Result.success("成绩删除成功");
            } else {
                return Result.error("成绩删除失败");
            }

        } catch (Exception e) {
            log.error("删除成绩异常", e);
            return Result.error("删除成绩失败: " + e.getMessage());
        }
    }

    /**
     * 获取成绩排行榜（前10名）
     * 核心技术点：直接从Redis ZSet中获取，性能极高
     * ZSet特性：
     * - 自动按score排序
     * - reverseRange获取高分到低分
     * - 时间复杂度：O(log(N)+M)，N是集合大小，M是返回元素数
     *
     * @param semester 学期（例如: 2024-1）
     * @return 排行榜数据
     */
    @GetMapping("/ranking")
    public Result<List<ScoreRankVO>> getRanking(
            @RequestParam String semester,
            @RequestParam(defaultValue = "10") Integer topN) {

        log.info("查询成绩排行榜 - 学期: {}, Top{}", semester, topN);

        try {
            // 从Redis ZSet获取排行榜
            List<ScoreRankVO> rankingList = scoreService.getTopRanking(semester, topN);

            return Result.success("查询成功", rankingList);

        } catch (Exception e) {
            log.error("查询排行榜异常", e);
            return Result.error("查询排行榜失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生排名
     *
     * @param studentId 学生ID
     * @param semester 学期
     * @return 排名（从1开始）
     */
    @GetMapping("/rank")
    public Result<Long> getStudentRank(
            @RequestParam Long studentId,
            @RequestParam String semester) {

        log.info("查询学生排名 - 学生ID: {}, 学期: {}", studentId, semester);

        try {
            Long rank = scoreService.getStudentRank(studentId, semester);

            if (rank == null) {
                return Result.error("该学生在该学期暂无排名");
            }

            return Result.success("查询成功", rank);

        } catch (Exception e) {
            log.error("查询学生排名异常", e);
            return Result.error("查询学生排名失败: " + e.getMessage());
        }
    }

    /**
     * 查询成绩详情
     *
     * @param id 成绩ID
     * @return 成绩详情
     */
    @GetMapping("/{id}")
    public Result<Score> getScore(@PathVariable Long id) {
        log.info("查询成绩详情 - ID: {}", id);

        try {
            Score score = scoreService.getById(id);

            if (score == null) {
                return Result.error("成绩不存在");
            }

            return Result.success("查询成功", score);

        } catch (Exception e) {
            log.error("查询成绩详情异常", e);
            return Result.error("查询成绩详情失败: " + e.getMessage());
        }
    }

    /**
     * 同步成绩到Redis
     * 用于数据初始化或修复
     *
     * @param semester 学期
     * @return 响应结果
     */
    @PostMapping("/sync")
    public Result<String> syncScoresToRedis(@RequestParam String semester) {
        log.info("开始同步成绩到Redis - 学期: {}", semester);

        try {
            scoreService.syncScoresToRedis(semester);
            return Result.success("同步成功");

        } catch (Exception e) {
            log.error("同步成绩到Redis异常", e);
            return Result.error("同步失败: " + e.getMessage());
        }
    }
}

package com.student.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.system.entity.Score;
import com.student.system.entity.Student;
import com.student.system.mapper.ScoreMapper;
import com.student.system.mapper.StudentMapper;
import com.student.system.service.ScoreService;
import com.student.system.vo.ScoreRankVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 成绩服务实现类
 * 核心功能：使用Redis ZSet实现成绩排名
 * 技术要点：
 * 1. 录入成绩时同时写入MySQL和Redis ZSet
 * 2. Redis ZSet的score存储成绩分数，member存储学生ID
 * 3. ZSet自动按score降序排列，实现快速排名查询
 *
 * @author Student System
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {

    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis Key前缀，从配置文件读取
     */
    @Value("${app.redis.key-prefix.score-rank:score:rank:}")
    private String scoreRankPrefix;

    /**
     * 构建Redis ZSet的Key
     * 格式: score:rank:{semester}
     *
     * @param semester 学期
     * @return Redis Key
     */
    private String buildRankKey(String semester) {
        return scoreRankPrefix + semester;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveScore(Score score) {
        try {
            // 1. 保存到MySQL数据库
            boolean saved = this.save(score);
            if (!saved) {
                log.error("保存成绩到数据库失败: {}", score);
                return false;
            }

            // 2. 同步到Redis ZSet进行排名
            // ZSet的score使用成绩分数，member使用学生ID
            String rankKey = buildRankKey(score.getSemester());
            redisTemplate.opsForZSet().add(
                    rankKey,
                    score.getStudentId().toString(),
                    score.getScore().doubleValue()
            );

            log.info("成绩录入成功 - 学生ID: {}, 课程ID: {}, 分数: {}, 已同步到Redis排行榜",
                    score.getStudentId(), score.getCourseId(), score.getScore());

            return true;
        } catch (Exception e) {
            log.error("录入成绩失败", e);
            throw new RuntimeException("录入成绩失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateScore(Score score) {
        try {
            // 1. 更新MySQL数据库
            boolean updated = this.updateById(score);
            if (!updated) {
                log.error("更新成绩到数据库失败: {}", score);
                return false;
            }

            // 2. 同步更新Redis ZSet
            // 直接add会覆盖原有的score值
            String rankKey = buildRankKey(score.getSemester());
            redisTemplate.opsForZSet().add(
                    rankKey,
                    score.getStudentId().toString(),
                    score.getScore().doubleValue()
            );

            log.info("成绩更新成功 - 学生ID: {}, 课程ID: {}, 新分数: {}",
                    score.getStudentId(), score.getCourseId(), score.getScore());

            return true;
        } catch (Exception e) {
            log.error("更新成绩失败", e);
            throw new RuntimeException("更新成绩失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteScore(Long id) {
        try {
            // 1. 先查询成绩信息（用于从Redis删除）
            Score score = this.getById(id);
            if (score == null) {
                log.warn("成绩不存在: {}", id);
                return false;
            }

            // 2. 从MySQL删除
            boolean deleted = this.removeById(id);
            if (!deleted) {
                log.error("从数据库删除成绩失败: {}", id);
                return false;
            }

            // 3. 从Redis ZSet中移除
            String rankKey = buildRankKey(score.getSemester());
            redisTemplate.opsForZSet().remove(rankKey, score.getStudentId().toString());

            log.info("成绩删除成功 - ID: {}, 学生ID: {}", id, score.getStudentId());

            return true;
        } catch (Exception e) {
            log.error("删除成绩失败", e);
            throw new RuntimeException("删除成绩失败: " + e.getMessage());
        }
    }

    @Override
    public List<ScoreRankVO> getTopRanking(String semester, int topN) {
        try {
            String rankKey = buildRankKey(semester);

            // 使用Redis ZSet的reverseRangeWithScores获取排名前N的数据
            // reverseRange表示从高到低排序（成绩高的在前）
            Set<ZSetOperations.TypedTuple<Object>> topScores =
                    redisTemplate.opsForZSet().reverseRangeWithScores(rankKey, 0, topN - 1);

            if (topScores == null || topScores.isEmpty()) {
                log.info("Redis中暂无排名数据，学期: {}", semester);
                return new ArrayList<>();
            }

            // 构建返回结果
            List<ScoreRankVO> rankList = new ArrayList<>();
            long rank = 1;
            for (ZSetOperations.TypedTuple<Object> tuple : topScores) {
                String studentIdStr = (String) tuple.getValue();
                Double scoreValue = tuple.getScore();

                if (studentIdStr == null || scoreValue == null) {
                    continue;
                }

                Long studentId = Long.parseLong(studentIdStr);

                // 查询学生信息
                Student student = studentMapper.selectById(studentId);
                if (student == null) {
                    continue;
                }

                // 构建排名VO
                ScoreRankVO rankVO = new ScoreRankVO();
                rankVO.setRank(rank++);
                rankVO.setStudentId(studentId);
                rankVO.setStudentNo(student.getStudentNo());
                rankVO.setStudentName(student.getName());
                rankVO.setScore(BigDecimal.valueOf(scoreValue));
                rankVO.setSemester(semester);
                rankVO.setAvatar(student.getAvatar());

                rankList.add(rankVO);
            }

            log.info("获取排行榜成功 - 学期: {}, Top{}: {} 人", semester, topN, rankList.size());

            return rankList;
        } catch (Exception e) {
            log.error("获取排行榜失败", e);
            throw new RuntimeException("获取排行榜失败: " + e.getMessage());
        }
    }

    @Override
    public Long getStudentRank(Long studentId, String semester) {
        try {
            String rankKey = buildRankKey(semester);

            // reverseRank返回从高到低的排名（0开始），所以需要+1
            Long rank = redisTemplate.opsForZSet().reverseRank(rankKey, studentId.toString());

            if (rank == null) {
                log.warn("学生排名不存在 - 学生ID: {}, 学期: {}", studentId, semester);
                return null;
            }

            // 排名从1开始，所以+1
            return rank + 1;
        } catch (Exception e) {
            log.error("获取学生排名失败", e);
            throw new RuntimeException("获取学生排名失败: " + e.getMessage());
        }
    }

    @Override
    public Score getByStudentAndCourse(Long studentId, Long courseId, String semester) {
        return scoreMapper.getByStudentAndCourse(studentId, courseId, semester);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncScoresToRedis(String semester) {
        try {
            log.info("开始同步成绩到Redis - 学期: {}", semester);

            // 1. 查询该学期所有成绩
            List<Score> scores = scoreMapper.listBySemester(semester);

            if (scores.isEmpty()) {
                log.warn("该学期暂无成绩数据: {}", semester);
                return;
            }

            // 2. 批量写入Redis ZSet
            String rankKey = buildRankKey(semester);

            // 先清空原有数据
            redisTemplate.delete(rankKey);

            // 批量添加到ZSet
            for (Score score : scores) {
                redisTemplate.opsForZSet().add(
                        rankKey,
                        score.getStudentId().toString(),
                        score.getScore().doubleValue()
                );
            }

            log.info("成绩同步完成 - 学期: {}, 共同步 {} 条记录", semester, scores.size());

        } catch (Exception e) {
            log.error("同步成绩到Redis失败", e);
            throw new RuntimeException("同步成绩到Redis失败: " + e.getMessage());
        }
    }
}

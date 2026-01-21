package com.student.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 成绩排名视图对象
 * 用于返回排行榜数据
 *
 * @author Student System
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreRankVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 排名（从1开始）
     */
    private Long rank;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 成绩分数
     */
    private BigDecimal score;

    /**
     * 学期
     */
    private String semester;

    /**
     * 头像URL
     */
    private String avatar;
}

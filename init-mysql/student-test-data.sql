-- 插入测试学生数据
-- 用于测试成绩录入和排行榜功能

USE student_sys;

-- 插入测试学生（10个学生）
INSERT INTO `student` (`student_no`, `name`, `gender`, `birth_date`, `phone`, `email`, `avatar`, `enrollment_date`, `status`) VALUES
('2024001', '张三', 1, '2005-03-15', '13800138001', 'zhangsan@student.com', 'https://i.pravatar.cc/150?img=1', '2024-09-01', 1),
('2024002', '李四', 1, '2005-05-20', '13800138002', 'lisi@student.com', 'https://i.pravatar.cc/150?img=2', '2024-09-01', 1),
('2024003', '王五', 0, '2005-07-10', '13800138003', 'wangwu@student.com', 'https://i.pravatar.cc/150?img=3', '2024-09-01', 1),
('2024004', '赵六', 1, '2005-02-28', '13800138004', 'zhaoliu@student.com', 'https://i.pravatar.cc/150?img=4', '2024-09-01', 1),
('2024005', '孙七', 0, '2005-11-12', '13800138005', 'sunqi@student.com', 'https://i.pravatar.cc/150?img=5', '2024-09-01', 1),
('2024006', '周八', 1, '2005-04-18', '13800138006', 'zhouba@student.com', 'https://i.pravatar.cc/150?img=6', '2024-09-01', 1),
('2024007', '吴九', 0, '2005-08-25', '13800138007', 'wujiu@student.com', 'https://i.pravatar.cc/150?img=7', '2024-09-01', 1),
('2024008', '郑十', 1, '2005-06-30', '13800138008', 'zhengshi@student.com', 'https://i.pravatar.cc/150?img=8', '2024-09-01', 1),
('2024009', '钱十一', 0, '2005-09-05', '13800138009', 'qianshiyi@student.com', 'https://i.pravatar.cc/150?img=9', '2024-09-01', 1),
('2024010', '陈十二', 1, '2005-01-22', '13800138010', 'chenshier@student.com', 'https://i.pravatar.cc/150?img=10', '2024-09-01', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 插入测试成绩（2024-1学期）
-- 为前10名学生插入计算机基础课程的成绩
INSERT INTO `score` (`student_id`, `course_id`, `score`, `semester`, `exam_date`) VALUES
(1, 1, 95.5, '2024-1', '2024-12-20'),
(2, 1, 88.0, '2024-1', '2024-12-20'),
(3, 1, 92.5, '2024-1', '2024-12-20'),
(4, 1, 87.0, '2024-1', '2024-12-20'),
(5, 1, 91.0, '2024-1', '2024-12-20'),
(6, 1, 85.5, '2024-1', '2024-12-20'),
(7, 1, 93.0, '2024-1', '2024-12-20'),
(8, 1, 89.5, '2024-1', '2024-12-20'),
(9, 1, 90.0, '2024-1', '2024-12-20'),
(10, 1, 86.0, '2024-1', '2024-12-20')
ON DUPLICATE KEY UPDATE score=VALUES(score);

SELECT '测试学生和成绩数据插入完成！' AS message;
SELECT '已插入10个学生和10条成绩记录' AS info;
SELECT '请运行以下命令同步成绩到Redis：' AS reminder;
SELECT 'POST http://localhost:8088/api/score/sync?semester=2024-1' AS sync_command;

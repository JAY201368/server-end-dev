#!/bin/bash
# Docker环境重启和数据初始化脚本

echo "=========================================="
echo "学生信息管理系统 - Docker环境重启"
echo "=========================================="

# 1. 停止并删除所有容器
echo ""
echo "[1/5] 停止并删除现有容器..."
docker-compose down -v

# 2. 清理Docker卷（可选，如果需要完全重置数据）
echo ""
echo "[2/5] 清理Docker卷..."
docker volume prune -f

# 3. 重新构建并启动容器
echo ""
echo "[3/5] 启动Docker容器..."
docker-compose up -d

# 4. 等待MySQL容器启动完成
echo ""
echo "[4/5] 等待MySQL容器启动..."
echo "等待30秒以确保MySQL完全启动..."
sleep 30

# 5. 检查容器状态
echo ""
echo "[5/5] 检查容器状态..."
docker-compose ps

echo ""
echo "=========================================="
echo "Docker环境启动完成！"
echo "=========================================="
echo ""
echo "服务访问地址："
echo "  - MySQL:     localhost:3307"
echo "  - Redis:     localhost:6379"
echo "  - Kafka:     localhost:9092"
echo "  - Kafka UI:  http://localhost:8080"
echo ""
echo "数据库信息："
echo "  - 数据库名: student_sys"
echo "  - 用户名:   student_user"
echo "  - 密码:     student_pass"
echo ""
echo "默认管理员账号："
echo "  - 用户名: admin"
echo "  - 密码:   admin123"
echo ""
echo "=========================================="
echo "提示：SQL初始化文件会自动执行"
echo "=========================================="

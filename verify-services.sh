#!/bin/bash

echo "===================================="
echo "学生信息管理系统 - 服务验证脚本"
echo "===================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}错误: Docker 未运行，请先启动 Docker${NC}"
    exit 1
fi

echo -e "\n${YELLOW}==== 1. 验证 MySQL (端口 3307) ====${NC}"
if docker exec student_sys_mysql mysql -uroot -proot123456 -e "SELECT 'MySQL OK' as status;" 2>/dev/null; then
    echo -e "${GREEN}✓ MySQL 运行正常 (宿主机端口: 3307)${NC}"
    docker exec student_sys_mysql mysql -uroot -proot123456 -e "SHOW DATABASES;" 2>/dev/null | grep student_sys > /dev/null
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ 数据库 student_sys 已创建${NC}"
    fi
    # 测试从宿主机连接
    if command -v mysql > /dev/null 2>&1; then
        if mysql -h127.0.0.1 -P3307 -uroot -proot123456 -e "SELECT 1" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ 宿主机可通过 3307 端口连接${NC}"
        else
            echo -e "${YELLOW}○ 宿主机连接测试失败，但容器内 MySQL 正常${NC}"
        fi
    fi
else
    echo -e "${RED}✗ MySQL 连接失败${NC}"
fi

echo -e "\n${YELLOW}==== 2. 验证 Redis ====${NC}"
if docker exec student_sys_redis redis-cli -a redis123456 ping 2>/dev/null | grep PONG > /dev/null; then
    echo -e "${GREEN}✓ Redis 运行正常${NC}"
else
    echo -e "${RED}✗ Redis 连接失败${NC}"
fi

echo -e "\n${YELLOW}==== 3. 验证 Zookeeper ====${NC}"
if docker exec student_sys_zookeeper nc -z localhost 2181 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Zookeeper 运行正常${NC}"
else
    echo -e "${RED}✗ Zookeeper 连接失败${NC}"
fi

echo -e "\n${YELLOW}==== 4. 验证 Kafka ====${NC}"
if docker exec student_sys_kafka kafka-broker-api-versions --bootstrap-server localhost:9093 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Kafka 运行正常${NC}"
else
    echo -e "${RED}✗ Kafka 连接失败${NC}"
fi

echo -e "\n${YELLOW}==== 5. 所有服务健康状态 ====${NC}"
for service in student_sys_mysql student_sys_redis student_sys_zookeeper student_sys_kafka; do
    status=$(docker inspect $service --format='{{.State.Health.Status}}' 2>/dev/null || echo "no_healthcheck")
    if [ "$status" = "healthy" ]; then
        echo -e "${GREEN}✓ $service: $status${NC}"
    elif [ "$status" = "no_healthcheck" ]; then
        echo -e "${YELLOW}○ $service: $status${NC}"
    else
        echo -e "${RED}✗ $service: $status${NC}"
    fi
done

echo -e "\n${YELLOW}==== 6. Kafka UI ====${NC}"
if curl -s http://localhost:8080 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Kafka UI 可访问: http://localhost:8080${NC}"
else
    echo -e "${YELLOW}○ Kafka UI 可能还在启动中，请稍后访问: http://localhost:8080${NC}"
fi

echo -e "\n${YELLOW}==== 7. 容器运行状态 ====${NC}"
docker-compose ps

echo -e "\n${YELLOW}==== 8. 端口映射信息 ====${NC}"
echo -e "MySQL: localhost:3307 -> 容器:3306 ${GREEN}(避免与本机 3306 冲突)${NC}"
echo -e "Redis: localhost:6379 -> 容器:6379"
echo -e "Kafka: localhost:9092 -> 容器:9093 (内部通信)"
echo -e "Kafka UI: http://localhost:8080"

echo -e "\n${GREEN}===================================="
echo "验证完成！"
echo "===================================="
echo -e "${NC}"

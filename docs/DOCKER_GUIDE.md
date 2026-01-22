# Docker 环境启动与验证指南

## 一、启动所有服务

### 1. 启动容器
在项目根目录下执行：

```bash
docker-compose up -d
```

参数说明：
- `-d`: 后台运行模式

### 2. 查看容器状态
```bash
docker-compose ps
```

### 3. 查看容器日志
```bash
# 查看所有容器日志
docker-compose logs -f

# 查看特定容器日志
docker-compose logs -f mysql
docker-compose logs -f redis
docker-compose logs -f kafka
docker-compose logs -f zookeeper
```

---

## 二、验证各服务是否启动成功

### 1. MySQL 验证

#### 方法一：使用 docker exec
```bash
docker exec -it student_sys_mysql mysql -uroot -proot123456 -e "SHOW DATABASES;"
```

预期输出应包含 `student_sys` 数据库。

#### 方法二：验证表结构
```bash
docker exec -it student_sys_mysql mysql -uroot -proot123456 student_sys -e "SHOW TABLES;"
```

预期输出应显示所有初始化的表。

#### 方法三：本地客户端连接
```bash
mysql -h127.0.0.1 -P3307 -uroot -proot123456
```

连接信息：
- Host: localhost
- Port: 3307 (避免与本机 MySQL 的 3306 端口冲突)
- Root密码: root123456
- 数据库: student_sys
- 普通用户: student_user / student_pass

---

### 2. Redis 验证

#### 方法一：使用 redis-cli
```bash
docker exec -it student_sys_redis redis-cli -a redis123456 ping
```

预期输出：`PONG`

#### 方法二：测试读写
```bash
docker exec -it student_sys_redis redis-cli -a redis123456 <<EOF
SET test_key "hello"
GET test_key
DEL test_key
EOF
```

#### 方法三：本地客户端连接
```bash
redis-cli -h 127.0.0.1 -p 6379 -a redis123456
```

连接信息：
- Host: localhost
- Port: 6379
- 密码: redis123456

---

### 3. Zookeeper 验证

```bash
docker exec -it student_sys_zookeeper nc -z localhost 2181 && echo "Zookeeper is running"
```

预期输出：`Zookeeper is running`

或查看状态：
```bash
docker exec -it student_sys_zookeeper zkServer.sh status
```

---

### 4. Kafka 验证

#### 方法一：查看 Kafka 版本
```bash
docker exec -it student_sys_kafka kafka-broker-api-versions --bootstrap-server localhost:9093
```

#### 方法二：创建测试主题
```bash
docker exec -it student_sys_kafka kafka-topics --create \
  --bootstrap-server localhost:9093 \
  --topic test-topic \
  --partitions 1 \
  --replication-factor 1
```

#### 方法三：列出所有主题
```bash
docker exec -it student_sys_kafka kafka-topics --list --bootstrap-server localhost:9093
```

#### 方法四：测试生产和消费消息

**生产消息：**
```bash
docker exec -it student_sys_kafka kafka-console-producer \
  --bootstrap-server localhost:9093 \
  --topic test-topic
```
输入一些消息后按 Ctrl+C 退出。

**消费消息：**
```bash
docker exec -it student_sys_kafka kafka-console-consumer \
  --bootstrap-server localhost:9093 \
  --topic test-topic \
  --from-beginning
```

---

### 5. Kafka UI 验证

打开浏览器访问：
```
http://localhost:8080
```

应该能看到 Kafka UI 管理界面，可以查看：
- Brokers 状态
- Topics 列表
- Consumer Groups
- 消息内容

---

## 三、常用操作命令

### 停止所有服务
```bash
docker-compose down
```

### 停止并删除所有数据卷（慎用）
```bash
docker-compose down -v
```

### 重启特定服务
```bash
docker-compose restart mysql
docker-compose restart redis
docker-compose restart kafka
```

### 查看服务资源使用情况
```bash
docker stats student_sys_mysql student_sys_redis student_sys_kafka student_sys_zookeeper
```

### 进入容器内部
```bash
# 进入 MySQL 容器
docker exec -it student_sys_mysql bash

# 进入 Redis 容器
docker exec -it student_sys_redis sh

# 进入 Kafka 容器
docker exec -it student_sys_kafka bash
```

---

## 四、健康检查

所有服务都配置了健康检查，可以通过以下命令查看健康状态：

```bash
docker inspect student_sys_mysql --format='{{.State.Health.Status}}'
docker inspect student_sys_redis --format='{{.State.Health.Status}}'
docker inspect student_sys_zookeeper --format='{{.State.Health.Status}}'
docker inspect student_sys_kafka --format='{{.State.Health.Status}}'
```

预期输出：`healthy`

---

## 五、故障排查

### 1. 容器启动失败
```bash
# 查看容器日志
docker-compose logs [service_name]

# 查看容器详细信息
docker inspect [container_name]
```

### 2. 端口冲突
如果端口被占用，可以在 `docker-compose.yml` 中修改端口映射。

### 3. Kafka 无法连接
确保 Zookeeper 已经启动并健康：
```bash
docker-compose ps zookeeper
docker-compose logs zookeeper
```

### 4. 数据持久化
所有数据都持久化在 Docker Volumes 中：
```bash
# 查看所有卷
docker volume ls | grep project

# 查看卷详情
docker volume inspect project_mysql_data
```

---

## 六、快速验证脚本

创建一个验证脚本，一键检查所有服务：

```bash
#!/bin/bash

echo "==== 验证 MySQL ===="
docker exec -it student_sys_mysql mysql -uroot -proot123456 -e "SELECT 'MySQL OK' as status;"

echo -e "\n==== 验证 Redis ===="
docker exec -it student_sys_redis redis-cli -a redis123456 ping

echo -e "\n==== 验证 Zookeeper ===="
docker exec -it student_sys_zookeeper nc -z localhost 2181 && echo "Zookeeper OK"

echo -e "\n==== 验证 Kafka ===="
docker exec -it student_sys_kafka kafka-broker-api-versions --bootstrap-server localhost:9093 2>&1 | head -n 1

echo -e "\n==== 所有服务健康状态 ===="
for service in student_sys_mysql student_sys_redis student_sys_zookeeper student_sys_kafka; do
  status=$(docker inspect $service --format='{{.State.Health.Status}}' 2>/dev/null || echo "no healthcheck")
  echo "$service: $status"
done

echo -e "\n==== Kafka UI ===="
echo "请访问: http://localhost:8080"
```

将以上脚本保存为 `verify-services.sh`，赋予执行权限后运行：
```bash
chmod +x verify-services.sh
./verify-services.sh
```

---

## 七、服务连接信息总结

| 服务 | 容器名 | 宿主机端口 | 容器内端口 | 用户名 | 密码 |
|------|--------|-----------|-----------|--------|------|
| MySQL | student_sys_mysql | 3307 | 3306 | root | root123456 |
| MySQL | student_sys_mysql | 3307 | 3306 | student_user | student_pass |
| Redis | student_sys_redis | 6379 | 6379 | - | redis123456 |
| Zookeeper | student_sys_zookeeper | 2181 | 2181 | - | - |
| Kafka | student_sys_kafka | 9092 (外部) | 9093 (内部) | - | - |
| Kafka UI | student_sys_kafka_ui | 8080 | 8080 | - | - |

**注意**: MySQL 使用 3307 端口是为了避免与本机 MySQL 的 3306 端口冲突。

### 应用程序配置示例

**Spring Boot application.yml:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/student_sys?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: student_user
    password: student_pass

  redis:
    host: localhost
    port: 6379
    password: redis123456

  kafka:
    bootstrap-servers: localhost:9092
```

**容器间通信（后端在 Docker 中运行时）:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/student_sys?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai

  redis:
    host: redis
    port: 6379

  kafka:
    bootstrap-servers: kafka:9093
```

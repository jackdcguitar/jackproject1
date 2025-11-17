# 🚀 快速开始指南

本指南提供最快的方式启动微服务订票平台，包括本地开发环境和生产环境部署。

> **详细文档**: 如需完整配置说明，请参考 [DEPLOYMENT.md](DEPLOYMENT.md) 和 [ROCKY_LINUX_DEPLOYMENT.md](ROCKY_LINUX_DEPLOYMENT.md)

---

## 📋 目录

- [方式一：本地开发环境（Docker Compose）](#方式一本地开发环境docker-compose)
- [方式二：生产环境（K8s + Rocky Linux VM）](#方式二生产环境k8s--rocky-linux-vm)
- [验证部署](#验证部署)
- [常用操作](#常用操作)
- [故障排查](#故障排查)

---

## 方式一：本地开发环境（Docker Compose）

**适用场景**: 快速开发和测试，单机环境

### 前置要求

```bash
# 检查环境
java -version   # 需要 JDK 17+
mvn -version    # 需要 Maven 3.8+
docker --version
docker-compose --version
node -v         # 需要 Node.js 18+
```

### 1️⃣ 启动基础设施（1分钟）

```bash
cd ticket-system/docker

# 启动 MySQL, MongoDB, Redis, Nacos
docker-compose up -d

# 查看服务状态
docker-compose ps
```

**等待服务就绪**（约30秒）:
```bash
# 检查 Nacos（约20秒启动）
curl http://localhost:8848/nacos/

# 检查 MySQL
docker exec -it ticket-mysql mysql -uroot -pticket123 -e "SELECT 1"

# 检查 MongoDB
docker exec -it ticket-mongodb mongosh --eval "db.runCommand({ping:1})"

# 检查 Redis
docker exec -it ticket-redis redis-cli ping
```

### 2️⃣ 初始化数据库（30秒）

```bash
cd ticket-system

# 创建数据库和表
docker exec -i ticket-mysql mysql -uroot -pticket123 < docs/sql/init-mysql.sql

# 初始化 MongoDB 集合
docker exec -i ticket-mongodb mongosh ticket_db < docs/sql/init-mongodb.js
```

### 3️⃣ 构建并启动后端服务（3分钟）

```bash
# 构建项目（首次需要下载依赖，约2-3分钟）
cd ticket-system
mvn clean install -DskipTests

# 启动所有服务（使用 screen 或多个终端）
# 终端1: 网关服务
cd gateway-service && mvn spring-boot:run

# 终端2: 认证服务
cd auth-service && mvn spring-boot:run

# 终端3: 节目服务（MongoDB）
cd event-service && mvn spring-boot:run

# 终端4: 座位服务
cd seat-service && mvn spring-boot:run

# 终端5: 订单服务
cd order-service && mvn spring-boot:run

# 终端6: 支付服务
cd payment-service && mvn spring-boot:run

# 终端7: 日志服务
cd log-service && mvn spring-boot:run
```

**或使用一键启动脚本**:
```bash
# 使用 Docker 容器方式启动（推荐）
./scripts/start-all-services.sh

# 查看服务状态
./scripts/check-services.sh
```

### 4️⃣ 启动前端（1分钟）

```bash
cd ticket-system/frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

### 5️⃣ 访问应用

- **前端**: http://localhost:5173
- **API网关**: http://localhost:8080
- **Nacos控制台**: http://localhost:8848/nacos （nacos/nacos）
- **Swagger文档**: http://localhost:8081/swagger-ui.html

### 🎉 完成！开始使用

默认账号:
- 管理员: `admin` / `admin123`
- 普通用户: `user1` / `user123`

---

## 方式二：生产环境（K8s + Rocky Linux VM）

**适用场景**: 生产部署，完整的 CI/CD 流程

### 架构概览

```
┌─────────────────────────────────────────┐
│  Windows/macOS 主机（192.168.1.50）     │
│  - Docker Desktop + K8s                 │
│  - MySQL, MongoDB, Redis, Nacos         │
│  - 所有微服务 Pods                       │
└─────────────────────────────────────────┘
               ↕
┌─────────────────────────────────────────┐
│  Rocky Linux 9.6 VM (192.168.1.100)    │
│  - Jenkins (CI/CD)                      │
│  - Nexus (Maven + Docker Registry)      │
└─────────────────────────────────────────┘
```

### 前置要求

**主机（Windows/macOS）**:
- Docker Desktop 已安装并启用 Kubernetes
- kubectl 命令可用
- 能够访问 VM: `ping 192.168.1.100`

**虚拟机（Rocky Linux 9.6）**:
- 静态 IP: 192.168.1.100
- 至少 4GB 内存，2核 CPU
- 50GB 磁盘空间

---

### 第一步：配置 Rocky Linux VM（15分钟）

#### 1.1 一键自动部署（推荐）

```bash
# SSH 登录到 Rocky Linux VM
ssh your-user@192.168.1.100

# 下载并运行自动部署脚本
curl -o deploy-rocky.sh https://your-git-repo/scripts/deploy-rocky.sh
chmod +x deploy-rocky.sh
sudo ./deploy-rocky.sh
```

脚本会自动完成:
- ✅ Docker 安装
- ✅ Nexus 部署（端口 8081, 8082）
- ✅ Jenkins 部署（端口 8080）
- ✅ 防火墙配置
- ✅ SELinux 配置

**等待约10分钟完成部署**

#### 1.2 手动部署（如果自动脚本失败）

参考完整指南: [ROCKY_LINUX_DEPLOYMENT.md](ROCKY_LINUX_DEPLOYMENT.md)

```bash
# 1. 安装 Docker
sudo dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo dnf install -y docker-ce docker-ce-cli containerd.io
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# 2. 部署 Nexus
sudo mkdir -p /opt/nexus-data
sudo chown -R 200:200 /opt/nexus-data
docker run -d --name nexus --restart always \
  -p 8081:8081 -p 8082:8082 \
  -v /opt/nexus-data:/nexus-data \
  sonatype/nexus3:latest

# 3. 部署 Jenkins
sudo mkdir -p /opt/jenkins_home
sudo chown -R 1000:1000 /opt/jenkins_home
docker run -d --name jenkins --restart always \
  -p 8080:8080 -p 50000:50000 \
  -v /opt/jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -u root \
  jenkins/jenkins:lts-jdk17

# 4. 配置防火墙
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=8081/tcp
sudo firewall-cmd --permanent --add-port=8082/tcp
sudo firewall-cmd --reload

# 5. 设置 SELinux（开发环境）
sudo setenforce 0
```

#### 1.3 配置 Nexus（5分钟）

```bash
# 等待 Nexus 启动（约2分钟）
until curl -s http://192.168.1.100:8081 > /dev/null; do
  echo "等待 Nexus 启动..."
  sleep 5
done

# 获取初始密码
docker exec nexus cat /nexus-data/admin.password
```

1. 访问: http://192.168.1.100:8081
2. 登录: `admin` / `<初始密码>`
3. 修改密码为: `nexus123`
4. 创建 Docker Registry（HTTP端口8082）:
   - Repository → Create repository → docker (hosted)
   - Name: `docker-hosted`
   - HTTP: `8082`
   - Enable Docker V1 API: ✅
   - Blob store: `default`
5. 创建 Maven Repository:
   - Name: `maven-releases`
   - Version policy: Release

#### 1.4 配置 Jenkins（5分钟）

```bash
# 获取 Jenkins 初始密码
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

1. 访问: http://192.168.1.100:8080
2. 使用初始密码登录
3. 安装推荐插件
4. 创建管理员账户: `admin` / `admin123`
5. 安装额外插件:
   - Kubernetes CLI Plugin
   - Pipeline Plugin
   - Git Plugin
   - Docker Pipeline Plugin

---

### 第二步：配置主机 K8s（10分钟）

#### 2.1 配置 Docker Registry 访问

**macOS**:
```bash
# 编辑 Docker Desktop 设置
# Preferences → Docker Engine → 添加:
{
  "insecure-registries": ["192.168.1.100:8082"]
}

# 重启 Docker Desktop
```

**Windows**:
```powershell
# 右键 Docker Desktop 图标 → Settings → Docker Engine
# 添加:
{
  "insecure-registries": ["192.168.1.100:8082"]
}

# Apply & Restart
```

**测试连接**:
```bash
# 登录 Nexus Docker Registry
docker login 192.168.1.100:8082
# Username: admin
# Password: nexus123

# 测试推送
docker pull alpine:latest
docker tag alpine:latest 192.168.1.100:8082/alpine:test
docker push 192.168.1.100:8082/alpine:test
```

#### 2.2 创建 Kubernetes Secret

```bash
# 创建 namespace
kubectl create namespace ticket-system

# 创建 Docker Registry Secret
kubectl create secret docker-registry nexus-registry \
  --docker-server=192.168.1.100:8082 \
  --docker-username=admin \
  --docker-password=nexus123 \
  --docker-email=admin@example.com \
  -n ticket-system

# 验证
kubectl get secret nexus-registry -n ticket-system
```

---

### 第三步：部署基础设施到 K8s（5分钟）

```bash
cd ticket-system/k8s

# 部署所有基础设施
kubectl apply -f namespace.yaml
kubectl apply -f mysql-deployment.yaml
kubectl apply -f mongodb-deployment.yaml
kubectl apply -f redis-deployment.yaml
kubectl apply -f nacos-deployment.yaml

# 等待所有 Pod 运行
kubectl get pods -n ticket-system -w

# 预期输出（所有 Pod 都是 Running）:
# ticket-mysql-0       1/1     Running   0          2m
# ticket-mongodb-0     1/1     Running   0          2m
# ticket-redis-0       1/1     Running   0          2m
# ticket-nacos-0       1/1     Running   0          2m
```

**初始化数据库**:
```bash
# MySQL
kubectl exec -it ticket-mysql-0 -n ticket-system -- \
  mysql -uroot -pticket123 < ../docs/sql/init-mysql.sql

# MongoDB
kubectl exec -it ticket-mongodb-0 -n ticket-system -- \
  mongosh ticket_db < ../docs/sql/init-mongodb.js
```

---

### 第四步：配置 Jenkins Pipeline（5分钟）

#### 4.1 配置 Jenkins 凭据

访问: http://192.168.1.100:8080

**添加 Git 凭据**:
- Manage Jenkins → Credentials → Global → Add Credentials
- Kind: Username with password
- ID: `git-credentials`
- Username: 你的 Git 用户名
- Password: 你的 Git 密码/Token

**添加 Kubernetes 配置**:
```bash
# 在主机上获取 kubeconfig
cat ~/.kube/config

# 或生成服务账户 token
kubectl create serviceaccount jenkins -n ticket-system
kubectl create rolebinding jenkins-admin \
  --clusterrole=admin \
  --serviceaccount=ticket-system:jenkins \
  -n ticket-system
kubectl create token jenkins -n ticket-system
```

- Manage Jenkins → Credentials → Add Credentials
- Kind: Secret text
- ID: `k8s-token`
- Secret: `<上面的token>`

**添加 Nexus 凭据**:
- Kind: Username with password
- ID: `nexus-credentials`
- Username: `admin`
- Password: `nexus123`

#### 4.2 创建 Pipeline 任务

1. 点击 "New Item"
2. 名称: `ticket-system-pipeline`
3. 类型: Pipeline
4. Pipeline:
   - Definition: Pipeline script from SCM
   - SCM: Git
   - Repository URL: `https://your-git-repo/ticket-system.git`
   - Credentials: `git-credentials`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
5. 保存

---

### 第五步：构建和部署（10分钟）

#### 5.1 触发 Jenkins 构建

```bash
# 方式1: 通过 Jenkins UI
# 访问 http://192.168.1.100:8080/job/ticket-system-pipeline/
# 点击 "Build Now"

# 方式2: 通过命令行（需要安装 jenkins-cli）
java -jar jenkins-cli.jar -s http://192.168.1.100:8080/ \
  -auth admin:admin123 \
  build ticket-system-pipeline
```

**Pipeline 步骤**（约10分钟）:
1. ✅ Checkout 代码
2. ✅ Maven 构建（每个服务约1-2分钟）
3. ✅ Docker 镜像构建（并行）
4. ✅ 推送到 Nexus Registry
5. ✅ 部署到 K8s
6. ✅ 健康检查

#### 5.2 手动构建（如果不使用 Jenkins）

```bash
cd ticket-system

# 1. 构建所有服务
mvn clean package -DskipTests

# 2. 构建 Docker 镜像并推送
services=("gateway" "auth" "event" "seat" "order" "payment" "log")
for service in "${services[@]}"; do
  docker build -t 192.168.1.100:8082/ticket-${service}:latest \
    -f docker/Dockerfile.backend \
    --build-arg SERVICE_NAME=${service}-service \
    .
  docker push 192.168.1.100:8082/ticket-${service}:latest
done

# 3. 构建前端镜像
docker build -t 192.168.1.100:8082/ticket-frontend:latest \
  -f docker/Dockerfile.frontend \
  ./frontend
docker push 192.168.1.100:8082/ticket-frontend:latest

# 4. 部署到 K8s
kubectl apply -f k8s/gateway-deployment.yaml
kubectl apply -f k8s/auth-service-deployment.yaml
kubectl apply -f k8s/event-service-deployment.yaml
kubectl apply -f k8s/seat-service-deployment.yaml
kubectl apply -f k8s/order-service-deployment.yaml
kubectl apply -f k8s/payment-service-deployment.yaml
kubectl apply -f k8s/log-service-deployment.yaml
kubectl apply -f k8s/frontend-deployment.yaml

# 5. 部署 Ingress
kubectl apply -f k8s/ingress.yaml
```

---

### 第六步：访问应用

#### 6.1 配置本地 hosts

```bash
# macOS/Linux
sudo vim /etc/hosts

# Windows: C:\Windows\System32\drivers\etc\hosts
# 添加以下行:
192.168.1.50  ticket.example.com
192.168.1.50  api.ticket.example.com
```

#### 6.2 获取服务地址

```bash
# 查看所有服务
kubectl get svc -n ticket-system

# 获取 Gateway LoadBalancer IP（Docker Desktop 使用 localhost）
kubectl get svc ticket-gateway -n ticket-system

# 获取 Ingress
kubectl get ingress -n ticket-system
```

#### 6.3 访问应用

- **前端**: http://ticket.example.com 或 http://localhost:30080
- **API网关**: http://api.ticket.example.com 或 http://localhost:30081
- **Nacos控制台**: http://localhost:30848/nacos

---

## ✅ 验证部署

### 1. 检查 Pod 状态

```bash
# 查看所有 Pod
kubectl get pods -n ticket-system

# 预期输出（所有 Pod 都是 Running）:
NAME                              READY   STATUS    RESTARTS   AGE
ticket-gateway-xxxxxxxxxx-xxxxx   1/1     Running   0          5m
ticket-auth-xxxxxxxxxx-xxxxx      1/1     Running   0          5m
ticket-event-xxxxxxxxxx-xxxxx     1/1     Running   0          5m
ticket-seat-xxxxxxxxxx-xxxxx      1/1     Running   0          5m
ticket-order-xxxxxxxxxx-xxxxx     1/1     Running   0          5m
ticket-payment-xxxxxxxxxx-xxxxx   1/1     Running   0          5m
ticket-log-xxxxxxxxxx-xxxxx       1/1     Running   0          5m
ticket-frontend-xxxxxxxxxx-xxxxx  1/1     Running   0          5m

# 查看 Pod 日志
kubectl logs -f <pod-name> -n ticket-system
```

### 2. 检查服务注册

访问 Nacos 控制台: http://localhost:30848/nacos

应该看到所有服务已注册:
- ✅ ticket-gateway
- ✅ ticket-auth-service
- ✅ ticket-event-service
- ✅ ticket-seat-service
- ✅ ticket-order-service
- ✅ ticket-payment-service
- ✅ ticket-log-service

### 3. API 测试

```bash
# 健康检查
curl http://localhost:30081/actuator/health

# 用户注册
curl -X POST http://localhost:30081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123",
    "email": "test@example.com"
  }'

# 用户登录
curl -X POST http://localhost:30081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123"
  }'

# 获取节目列表（MongoDB）
TOKEN="<上面获取的JWT token>"
curl -X GET http://localhost:30081/event/list \
  -H "Authorization: Bearer $TOKEN"
```

### 4. 数据库验证

```bash
# MySQL
kubectl exec -it ticket-mysql-0 -n ticket-system -- \
  mysql -uroot -pticket123 -e "USE ticket_db; SHOW TABLES; SELECT COUNT(*) FROM t_user;"

# MongoDB
kubectl exec -it ticket-mongodb-0 -n ticket-system -- \
  mongosh ticket_db --eval "db.events.find().pretty()"

# Redis
kubectl exec -it ticket-redis-0 -n ticket-system -- \
  redis-cli KEYS "*"
```

### 5. 前端验证

1. 访问 http://localhost:30080
2. 注册新用户
3. 登录系统
4. 浏览节目列表
5. 选择座位并下单
6. 模拟支付

---

## 🔧 常用操作

### 重启服务

```bash
# 重启单个服务
kubectl rollout restart deployment/ticket-gateway -n ticket-system

# 重启所有服务
kubectl rollout restart deployment -n ticket-system
```

### 扩缩容

```bash
# 扩展 Gateway 副本数
kubectl scale deployment ticket-gateway --replicas=3 -n ticket-system

# 查看 HPA 状态
kubectl get hpa -n ticket-system
```

### 查看日志

```bash
# 实时查看日志
kubectl logs -f <pod-name> -n ticket-system

# 查看最近100行
kubectl logs --tail=100 <pod-name> -n ticket-system

# 查看所有 Gateway Pod 日志
kubectl logs -l app=ticket-gateway -n ticket-system --all-containers=true
```

### 进入 Pod 调试

```bash
# 进入 Pod shell
kubectl exec -it <pod-name> -n ticket-system -- /bin/sh

# 在 Pod 中执行命令
kubectl exec <pod-name> -n ticket-system -- curl http://localhost:8081/actuator/health
```

### 更新配置

```bash
# 更新 ConfigMap
kubectl edit configmap ticket-config -n ticket-system

# 重启服务以应用新配置
kubectl rollout restart deployment -n ticket-system
```

### 数据备份

```bash
# MySQL 备份
kubectl exec ticket-mysql-0 -n ticket-system -- \
  mysqldump -uroot -pticket123 --all-databases > backup-mysql-$(date +%Y%m%d).sql

# MongoDB 备份
kubectl exec ticket-mongodb-0 -n ticket-system -- \
  mongodump --out=/tmp/backup --db=ticket_db

# 拷贝备份到本地
kubectl cp ticket-mongodb-0:/tmp/backup ./backup-mongodb-$(date +%Y%m%d) -n ticket-system
```

---

## 🐛 故障排查

### 问题 1: Pod 无法启动

```bash
# 查看 Pod 详情
kubectl describe pod <pod-name> -n ticket-system

# 常见原因:
# 1. 镜像拉取失败 - 检查 Nexus Registry 配置
# 2. 资源不足 - 检查 CPU/Memory limits
# 3. 配置错误 - 检查 ConfigMap 和 Secret
```

**解决方案**:
```bash
# 检查镜像是否存在
docker images | grep 192.168.1.100:8082

# 检查 Secret
kubectl get secret nexus-registry -n ticket-system -o yaml

# 检查节点资源
kubectl top nodes
```

### 问题 2: 服务无法注册到 Nacos

```bash
# 检查 Nacos 是否运行
kubectl get pods -l app=ticket-nacos -n ticket-system

# 检查服务日志
kubectl logs <service-pod> -n ticket-system | grep nacos

# 检查网络连接
kubectl exec <service-pod> -n ticket-system -- \
  curl http://ticket-nacos:8848/nacos/v1/ns/instance/list?serviceName=ticket-gateway
```

**解决方案**:
```yaml
# 检查 application.yml 配置
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ticket-nacos:8848  # 使用 K8s Service 名称
```

### 问题 3: 数据库连接失败

```bash
# 测试 MySQL 连接
kubectl exec <service-pod> -n ticket-system -- \
  mysql -h ticket-mysql -uroot -pticket123 -e "SELECT 1"

# 测试 MongoDB 连接
kubectl exec <service-pod> -n ticket-system -- \
  mongosh mongodb://ticket-mongodb:27017/ticket_db --eval "db.runCommand({ping:1})"
```

**解决方案**:
```bash
# 检查 Service DNS 解析
kubectl exec <service-pod> -n ticket-system -- nslookup ticket-mysql

# 检查数据库 Pod 状态
kubectl get pods -l app=ticket-mysql -n ticket-system
kubectl logs ticket-mysql-0 -n ticket-system
```

### 问题 4: Redis 连接失败

```bash
# 测试 Redis 连接
kubectl exec <service-pod> -n ticket-system -- \
  redis-cli -h ticket-redis ping

# 检查 Redis 密码
kubectl get secret ticket-redis-secret -n ticket-system -o yaml
```

### 问题 5: Ingress 无法访问

```bash
# 检查 Ingress 配置
kubectl describe ingress ticket-ingress -n ticket-system

# 检查 Ingress Controller
kubectl get pods -n ingress-nginx

# Docker Desktop 可能需要手动端口转发
kubectl port-forward svc/ticket-gateway 8080:8080 -n ticket-system
```

### 问题 6: Jenkins 构建失败

**常见错误**:
```
Error: Cannot connect to Docker daemon
```

**解决方案**:
```bash
# 检查 Docker socket 挂载
docker exec jenkins ls -la /var/run/docker.sock

# 重新启动 Jenkins
docker restart jenkins

# 给 Jenkins 用户 Docker 权限
docker exec -u root jenkins usermod -aG docker jenkins
docker restart jenkins
```

### 问题 7: Nexus 推送失败

```
Error: http: server gave HTTP response to HTTPS client
```

**解决方案**:
```bash
# 确保添加了 insecure-registries
docker info | grep "Insecure Registries"

# 应显示: 192.168.1.100:8082
```

### 查看完整日志

```bash
# 服务日志（最近1小时）
kubectl logs --since=1h <pod-name> -n ticket-system

# 事件日志
kubectl get events -n ticket-system --sort-by='.lastTimestamp'

# 系统日志
journalctl -u docker -n 100
```

---

## 📚 下一步

### 开发相关
- 查看 [API.md](API.md) 了解所有 API 接口
- 查看 [ARCHITECTURE.md](ARCHITECTURE.md) 了解系统架构
- 查看 [ANTI_OVERSELL_SOLUTIONS.md](ANTI_OVERSELL_SOLUTIONS.md) 了解防超卖方案

### 部署相关
- 查看 [DEPLOYMENT.md](DEPLOYMENT.md) 了解详细部署配置
- 查看 [ROCKY_LINUX_DEPLOYMENT.md](ROCKY_LINUX_DEPLOYMENT.md) 了解 Rocky Linux 专用配置

### 监控和运维
- 配置 Prometheus + Grafana 监控
- 配置日志收集（ELK Stack）
- 配置链路追踪（Zipkin/Jaeger）
- 配置告警通知（钉钉/邮件）

---

## ❓ 获取帮助

### 常用命令速查

```bash
# 查看所有资源
kubectl get all -n ticket-system

# 查看配置
kubectl get configmap,secret -n ticket-system

# 查看持久卷
kubectl get pv,pvc -n ticket-system

# 查看网络
kubectl get ingress,svc -n ticket-system

# 导出资源配置
kubectl get deployment ticket-gateway -n ticket-system -o yaml > gateway-backup.yaml
```

### 联系方式

- **项目仓库**: https://github.com/yourusername/ticket-system
- **问题反馈**: https://github.com/yourusername/ticket-system/issues
- **文档中心**: https://docs.yoursite.com/ticket-system

---

## 🎯 检查清单

部署完成后，请确认以下项目:

### 基础设施
- [ ] MySQL Pod 运行正常，可以连接
- [ ] MongoDB Pod 运行正常，可以连接
- [ ] Redis Pod 运行正常，可以连接
- [ ] Nacos Pod 运行正常，控制台可访问

### 微服务
- [ ] Gateway 服务已注册到 Nacos
- [ ] Auth 服务已注册到 Nacos
- [ ] Event 服务已注册到 Nacos（使用 MongoDB）
- [ ] Seat 服务已注册到 Nacos
- [ ] Order 服务已注册到 Nacos
- [ ] Payment 服务已注册到 Nacos
- [ ] Log 服务已注册到 Nacos（使用 MongoDB）

### 功能验证
- [ ] 用户可以注册和登录
- [ ] 可以获取 JWT Token
- [ ] 可以浏览节目列表（MongoDB 查询）
- [ ] 可以查看座位信息
- [ ] 可以锁定座位（Redis 防超卖）
- [ ] 可以创建订单
- [ ] 可以完成支付

### DevOps
- [ ] Jenkins Pipeline 可以成功构建
- [ ] Docker 镜像可以推送到 Nexus
- [ ] K8s 可以从 Nexus 拉取镜像
- [ ] HPA 自动扩缩容正常
- [ ] Ingress 路由配置正确

---

**恭喜！你已经成功部署了微服务订票平台！** 🎉

如有任何问题，请参考详细文档或提交 Issue。

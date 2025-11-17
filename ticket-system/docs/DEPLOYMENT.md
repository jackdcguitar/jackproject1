# 部署文档 - Docker Desktop K8s + 本地虚拟机

## 环境说明

本文档针对以下环境：
- **Kubernetes**: Docker Desktop 内置 K8s 功能
- **Jenkins**: 本地虚拟机部署
- **Nexus**: 本地虚拟机部署
- **开发机**: Windows/macOS + Docker Desktop

---

## 目录

1. [环境准备](#1-环境准备)
2. [Docker Desktop K8s配置](#2-docker-desktop-k8s配置)
3. [本地虚拟机配置](#3-本地虚拟机配置)
4. [MySQL和MongoDB部署](#4-mysql和mongodb部署)
5. [微服务部署](#5-微服务部署)
6. [Jenkins CI/CD配置](#6-jenkins-cicd配置)
7. [验证和测试](#7-验证和测试)
8. [故障排查](#8-故障排查)

---

## 1. 环境准备

### 1.1 软件版本要求

| 软件 | 版本 | 说明 |
|------|------|------|
| Docker Desktop | 4.20+ | 包含K8s 1.27+ |
| JDK | 17 | 用于Maven构建 |
| Maven | 3.8+ | 项目构建工具 |
| Node.js | 18+ | 前端构建 |
| kubectl | 1.27+ | K8s命令行工具 |
| 虚拟机 | - | Jenkins + Nexus |

### 1.2 检查环境

```bash
# 检查Docker版本
docker version

# 检查Docker Desktop K8s状态
kubectl version --client
kubectl cluster-info

# 检查Maven
mvn -version

# 检查JDK
java -version
```

---

## 2. Docker Desktop K8s配置

### 2.1 启用Kubernetes

#### Windows
1. 打开 Docker Desktop
2. 点击 **Settings** (齿轮图标)
3. 选择 **Kubernetes** 选项卡
4. 勾选 **Enable Kubernetes**
5. 点击 **Apply & Restart**
6. 等待约2-5分钟（首次启动会下载镜像）

#### macOS
1. 打开 Docker Desktop
2. 点击菜单栏 **Docker Desktop** → **Preferences**
3. 选择 **Kubernetes**
4. 勾选 **Enable Kubernetes**
5. 点击 **Apply & Restart**

### 2.2 验证K8s安装

```bash
# 查看集群信息
kubectl cluster-info

# 预期输出：
# Kubernetes control plane is running at https://kubernetes.docker.internal:6443
# CoreDNS is running at https://kubernetes.docker.internal:6443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy

# 查看节点
kubectl get nodes

# 预期输出：
# NAME             STATUS   ROLES           AGE   VERSION
# docker-desktop   Ready    control-plane   5m    v1.27.2
```

### 2.3 配置kubectl上下文

```bash
# 查看所有上下文
kubectl config get-contexts

# 切换到Docker Desktop上下文
kubectl config use-context docker-desktop

# 验证当前上下文
kubectl config current-context
# 输出: docker-desktop
```

### 2.4 配置K8s Dashboard（可选）

```bash
# 部署Dashboard
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml

# 创建管理员用户
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  name: admin-user
  namespace: kubernetes-dashboard
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: admin-user
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: admin-user
  namespace: kubernetes-dashboard
EOF

# 获取访问Token
kubectl -n kubernetes-dashboard create token admin-user

# 启动代理
kubectl proxy

# 访问Dashboard
# http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
```

---

## 3. 本地虚拟机配置

### 3.1 虚拟机网络配置

假设虚拟机配置：
- **IP地址**: 192.168.1.100
- **操作系统**: Ubuntu 22.04 LTS
- **配置**: 4核CPU, 8GB内存, 100GB磁盘

#### 确保主机可以访问虚拟机

```bash
# 在主机上测试虚拟机连通性
ping 192.168.1.100

# 测试SSH连接
ssh user@192.168.1.100
```

### 3.2 虚拟机基础环境安装

```bash
# SSH登录虚拟机
ssh user@192.168.1.100

# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version

# 重新登录使用户组生效
exit
ssh user@192.168.1.100
```

### 3.3 部署Nexus

```bash
# 创建Nexus目录
mkdir -p ~/nexus-data
sudo chown -R 200 ~/nexus-data

# 启动Nexus容器
docker run -d \
  --name nexus \
  --restart always \
  -p 8081:8081 \
  -p 8082:8082 \
  -v ~/nexus-data:/nexus-data \
  sonatype/nexus3:latest

# 查看日志（等待启动完成，约2-3分钟）
docker logs -f nexus

# 等待看到 "Started Sonatype Nexus" 字样

# 获取初始管理员密码
docker exec nexus cat /nexus-data/admin.password
# 复制输出的密码
```

#### 配置Nexus

1. **访问Nexus**: http://192.168.1.100:8081
2. **登录**: 用户名 `admin`, 密码为上面获取的初始密码
3. **设置新密码**: 按照向导设置新密码（例如：admin123）
4. **启用匿名访问**: 选择 "Enable anonymous access"

#### 创建Docker Registry

1. 点击顶部 **齿轮图标** (Server administration)
2. 左侧菜单选择 **Repositories**
3. 点击 **Create repository**
4. 选择 **docker (hosted)**
5. 配置：
   - **Name**: docker-hosted
   - **HTTP**: 8082
   - **Allow anonymous docker pull**: 勾选
   - **Enable Docker V1 API**: 勾选
6. 点击 **Create repository**

#### 创建Maven Repository

1. **Repositories** → **Create repository**
2. 选择 **maven2 (hosted)**
3. 配置：
   - **Name**: maven-releases
   - **Version policy**: Release
   - **Deployment policy**: Allow redeploy
4. 点击 **Create repository**

#### 配置Docker客户端信任Nexus

在主机上配置（Windows）：
```json
# 编辑 Docker Desktop Settings → Docker Engine
# 添加以下配置：
{
  "insecure-registries": ["192.168.1.100:8082"]
}
```

在主机上配置（macOS/Linux）：
```bash
# 编辑 /etc/docker/daemon.json
sudo tee /etc/docker/daemon.json <<EOF
{
  "insecure-registries": ["192.168.1.100:8082"]
}
EOF

# 重启Docker Desktop
```

#### 测试Nexus Docker Registry

```bash
# 登录Nexus Docker Registry
docker login 192.168.1.100:8082
# Username: admin
# Password: admin123

# 拉取测试镜像
docker pull nginx:alpine

# 标记镜像
docker tag nginx:alpine 192.168.1.100:8082/nginx:alpine

# 推送到Nexus
docker push 192.168.1.100:8082/nginx:alpine

# 删除本地镜像
docker rmi nginx:alpine 192.168.1.100:8082/nginx:alpine

# 从Nexus拉取
docker pull 192.168.1.100:8082/nginx:alpine

# 成功则表示Nexus配置正确
```

### 3.4 部署Jenkins

```bash
# 创建Jenkins目录
mkdir -p ~/jenkins_home
sudo chown -R 1000:1000 ~/jenkins_home

# 启动Jenkins容器
docker run -d \
  --name jenkins \
  --restart always \
  -p 8080:8080 \
  -p 50000:50000 \
  -v ~/jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk17

# 查看日志
docker logs -f jenkins

# 等待看到初始管理员密码
# *************************************************************
# Jenkins initial setup is required. An admin user has been created and a password generated.
# Please use the following password to proceed to installation:
#
# a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
#
# *************************************************************

# 或者直接获取密码
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

#### 配置Jenkins

1. **访问Jenkins**: http://192.168.1.100:8080
2. **输入初始密码**: 粘贴上面获取的密码
3. **安装插件**: 选择 "Install suggested plugins"
4. **创建管理员用户**:
   - Username: admin
   - Password: admin123
   - Full name: Admin
   - Email: admin@example.com
5. **Jenkins URL**: http://192.168.1.100:8080/

#### 安装必要插件

1. **Manage Jenkins** → **Manage Plugins**
2. 点击 **Available plugins** 标签
3. 搜索并安装以下插件：
   - **Kubernetes**
   - **Docker Pipeline**
   - **Nexus Artifact Uploader**
   - **Git**
   - **Maven Integration**
   - **Pipeline**
   - **Blue Ocean** (可选，更好的界面)

4. 安装完成后重启Jenkins

#### 配置Maven

1. **Manage Jenkins** → **Global Tool Configuration**
2. 找到 **Maven** 部分
3. 点击 **Add Maven**
4. 配置：
   - **Name**: Maven-3.8
   - **Install automatically**: 勾选
   - **Version**: 3.8.8
5. 点击 **Save**

#### 配置Docker

1. **Manage Jenkins** → **Global Tool Configuration**
2. 找到 **Docker** 部分
3. 点击 **Add Docker**
4. 配置：
   - **Name**: docker
   - **Install automatically**: 勾选
   - **Docker version**: latest
5. 点击 **Save**

#### 配置Nexus凭证

1. **Manage Jenkins** → **Manage Credentials**
2. 点击 **(global)** → **Add Credentials**
3. 配置：
   - **Kind**: Username with password
   - **Username**: admin
   - **Password**: admin123
   - **ID**: nexus-credentials
   - **Description**: Nexus Repository Credentials
4. 点击 **Create**

#### 配置Kubernetes凭证

由于使用Docker Desktop的K8s，需要配置kubeconfig：

```bash
# 在主机上获取kubeconfig
kubectl config view --raw > kubeconfig.yaml

# 复制kubeconfig内容
cat kubeconfig.yaml
```

在Jenkins中添加凭证：
1. **Manage Jenkins** → **Manage Credentials**
2. 点击 **(global)** → **Add Credentials**
3. 配置：
   - **Kind**: Secret file
   - **File**: 上传kubeconfig.yaml
   - **ID**: kubernetes-credentials
   - **Description**: Docker Desktop Kubernetes Config
4. 点击 **Create**

#### 配置Kubernetes插件

1. **Manage Jenkins** → **Configure System**
2. 找到 **Cloud** 部分
3. 点击 **Add a new cloud** → **Kubernetes**
4. 配置：
   - **Name**: docker-desktop-k8s
   - **Kubernetes URL**: https://kubernetes.docker.internal:6443
   - **Kubernetes Namespace**: ticket-system
   - **Credentials**: 选择上面创建的 kubernetes-credentials
   - **Jenkins URL**: http://192.168.1.100:8080
5. 点击 **Test Connection**（应该显示 "Connected to Kubernetes..."）
6. 点击 **Save**

---

## 4. MySQL和MongoDB部署

### 4.1 创建命名空间

```bash
# 创建项目命名空间
kubectl apply -f k8s/namespace.yaml

# 验证
kubectl get namespace ticket-system
```

### 4.2 部署MySQL

```bash
# 部署MySQL
kubectl apply -f k8s/mysql-deployment.yaml

# 查看部署状态
kubectl get pods -n ticket-system -l app=mysql

# 等待Pod运行
kubectl wait --for=condition=ready pod -l app=mysql -n ticket-system --timeout=300s

# 查看日志
kubectl logs -n ticket-system -l app=mysql

# 测试MySQL连接
kubectl run mysql-client --rm -it --image=mysql:8.0 -n ticket-system -- \
  mysql -h mysql -uroot -proot

# 在MySQL客户端中测试
mysql> SHOW DATABASES;
mysql> CREATE DATABASE ticket_auth;
mysql> CREATE DATABASE ticket_order;
mysql> exit
```

#### 从主机访问MySQL（可选）

```bash
# 端口转发
kubectl port-forward -n ticket-system svc/mysql 3306:3306

# 在另一个终端使用MySQL客户端连接
mysql -h 127.0.0.1 -P 3306 -uroot -proot

# 或使用MySQL Workbench连接
# Host: 127.0.0.1
# Port: 3306
# Username: root
# Password: root
```

### 4.3 部署MongoDB

```bash
# 部署MongoDB
kubectl apply -f k8s/mongodb-deployment.yaml

# 查看部署状态
kubectl get pods -n ticket-system -l app=mongodb

# 等待Pod运行
kubectl wait --for=condition=ready pod -l app=mongodb -n ticket-system --timeout=300s

# 测试MongoDB连接
kubectl run mongo-client --rm -it --image=mongo:5.0 -n ticket-system -- \
  mongosh mongodb://admin:admin123@mongodb:27017

# 在MongoDB Shell中测试
> use ticket_system
> db.test.insertOne({name: "test"})
> db.test.find()
> exit
```

#### 从主机访问MongoDB（可选）

```bash
# 端口转发
kubectl port-forward -n ticket-system svc/mongodb 27017:27017

# 使用mongosh连接
mongosh mongodb://admin:admin123@localhost:27017

# 或使用MongoDB Compass
# Connection String: mongodb://admin:admin123@localhost:27017
```

### 4.4 部署Redis

```bash
# 创建Redis配置
cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  namespace: ticket-system
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7.0-alpine
        ports:
        - containerPort: 6379
        command: ["redis-server", "--appendonly", "yes"]
---
apiVersion: v1
kind: Service
metadata:
  name: redis
  namespace: ticket-system
spec:
  selector:
    app: redis
  ports:
  - port: 6379
    targetPort: 6379
  type: ClusterIP
EOF

# 验证Redis部署
kubectl get pods -n ticket-system -l app=redis

# 测试Redis连接
kubectl run redis-client --rm -it --image=redis:7.0-alpine -n ticket-system -- redis-cli -h redis ping
# 应输出: PONG
```

### 4.5 部署Nacos

```bash
# 创建Nacos配置
cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nacos
  namespace: ticket-system
spec:
  replicas: 1
  selector:
    matchLabels:
      app: nacos
  template:
    metadata:
      labels:
        app: nacos
    spec:
      containers:
      - name: nacos
        image: nacos/nacos-server:v2.2.0
        env:
        - name: MODE
          value: "standalone"
        - name: PREFER_HOST_MODE
          value: "hostname"
        ports:
        - containerPort: 8848
        - containerPort: 9848
        - containerPort: 9849
---
apiVersion: v1
kind: Service
metadata:
  name: nacos
  namespace: ticket-system
spec:
  selector:
    app: nacos
  ports:
  - name: http
    port: 8848
    targetPort: 8848
  - name: grpc
    port: 9848
    targetPort: 9848
  - name: grpc-2
    port: 9849
    targetPort: 9849
  type: ClusterIP
EOF

# 等待Nacos启动（约1分钟）
kubectl wait --for=condition=ready pod -l app=nacos -n ticket-system --timeout=300s

# 访问Nacos（端口转发）
kubectl port-forward -n ticket-system svc/nacos 8848:8848

# 打开浏览器访问
# http://localhost:8848/nacos
# 默认用户名/密码: nacos/nacos
```

### 4.6 验证基础设施

```bash
# 查看所有Pod
kubectl get pods -n ticket-system

# 预期输出：
# NAME                      READY   STATUS    RESTARTS   AGE
# mysql-0                   1/1     Running   0          5m
# mongodb-0                 1/1     Running   0          5m
# redis-xxx                 1/1     Running   0          3m
# nacos-xxx                 1/1     Running   0          2m

# 查看所有Service
kubectl get svc -n ticket-system

# 预期输出：
# NAME      TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)                     AGE
# mysql     ClusterIP   None            <none>        3306/TCP                    5m
# mongodb   ClusterIP   None            <none>        27017/TCP                   5m
# redis     ClusterIP   10.96.1.100     <none>        6379/TCP                    3m
# nacos     ClusterIP   10.96.1.101     <none>        8848/TCP,9848/TCP,9849/TCP  2m
```

---

## 5. 微服务部署

### 5.1 构建Docker镜像

在项目根目录执行：

```bash
# 进入项目目录
cd ticket-system

# 方式1: 使用Maven构建所有服务
mvn clean package -DskipTests

# 方式2: 使用Docker Compose构建（推荐）
# 修改docker-compose.yml中的镜像地址为Nexus地址
```

#### 修改docker-compose.yml镜像地址

```yaml
# 编辑docker-compose.yml
# 将所有镜像构建配置修改为推送到Nexus

services:
  gateway-service:
    build:
      context: ./gateway-service
      dockerfile: ../docker/Dockerfile.backend
    image: 192.168.1.100:8082/ticket-system/gateway-service:latest
    # ...

  auth-service:
    build:
      context: ./auth-service
      dockerfile: ../docker/Dockerfile.backend
    image: 192.168.1.100:8082/ticket-system/auth-service:latest
    # ...
```

#### 构建并推送镜像

```bash
# 登录Nexus Docker Registry
docker login 192.168.1.100:8082
# Username: admin
# Password: admin123

# 构建所有服务镜像
docker-compose build

# 推送到Nexus
docker-compose push

# 验证镜像已上传
# 访问 http://192.168.1.100:8081
# 进入 Browse → Browse docker-hosted
# 应该看到所有服务的镜像
```

### 5.2 创建Kubernetes配置

#### 创建Gateway部署配置

```bash
# 修改k8s/gateway-deployment.yaml中的镜像地址
sed -i 's|nexus.example.com|192.168.1.100:8082|g' k8s/gateway-deployment.yaml
```

#### 创建ConfigMap（统一配置）

```bash
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: ticket-system
data:
  NACOS_SERVER_ADDR: "nacos:8848"
  MYSQL_HOST: "mysql"
  MYSQL_PORT: "3306"
  MYSQL_DATABASE: "ticket_system"
  MONGODB_HOST: "mongodb"
  MONGODB_PORT: "27017"
  MONGODB_DATABASE: "ticket_system"
  REDIS_HOST: "redis"
  REDIS_PORT: "6379"
  TZ: "Asia/Shanghai"
EOF
```

#### 创建Secret（敏感信息）

```bash
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Secret
metadata:
  name: app-secret
  namespace: ticket-system
type: Opaque
stringData:
  MYSQL_USER: "root"
  MYSQL_PASSWORD: "root"
  MONGODB_USERNAME: "admin"
  MONGODB_PASSWORD: "admin123"
  JWT_SECRET: "ticket-booking-system-secret-key-2024-minimum-256-bits"
EOF
```

### 5.3 部署微服务

#### 创建服务部署模板

创建 `k8s/auth-service-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
  namespace: ticket-system
spec:
  replicas: 2
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: 192.168.1.100:8082/ticket-system/auth-service:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8081
        envFrom:
        - configMapRef:
            name: app-config
        - secretRef:
            name: app-secret
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        resources:
          requests:
            cpu: "500m"
            memory: "512Mi"
          limits:
            cpu: "1000m"
            memory: "1Gi"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: auth-service
  namespace: ticket-system
spec:
  selector:
    app: auth-service
  ports:
  - port: 8081
    targetPort: 8081
  type: ClusterIP
```

#### 部署所有服务

```bash
# 部署Gateway
kubectl apply -f k8s/gateway-deployment.yaml

# 部署Auth Service
kubectl apply -f k8s/auth-service-deployment.yaml

# 部署Event Service（类似配置）
# 部署Seat Service
# 部署Order Service
# 部署Payment Service

# 查看部署状态
kubectl get deployments -n ticket-system

# 查看Pod状态
kubectl get pods -n ticket-system

# 查看服务
kubectl get svc -n ticket-system
```

### 5.4 配置Ingress

```bash
# 部署Nginx Ingress Controller（Docker Desktop自带）
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml

# 等待Ingress Controller就绪
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s

# 应用项目的Ingress配置
kubectl apply -f k8s/ingress.yaml

# 查看Ingress
kubectl get ingress -n ticket-system
```

#### 配置本地hosts

编辑hosts文件：

**Windows**: `C:\Windows\System32\drivers\etc\hosts`
**macOS/Linux**: `/etc/hosts`

添加：
```
127.0.0.1 ticket.example.com
127.0.0.1 api.ticket.example.com
```

#### 测试访问

```bash
# 测试网关
curl http://api.ticket.example.com/actuator/health

# 测试认证服务
curl http://api.ticket.example.com/auth/login
```

---

## 6. Jenkins CI/CD配置

### 6.1 配置Jenkins Pipeline项目

1. **访问Jenkins**: http://192.168.1.100:8080
2. 点击 **New Item**
3. 输入项目名称: `ticket-system`
4. 选择 **Pipeline**
5. 点击 **OK**

### 6.2 配置Git仓库

在项目配置页面：

1. **General** 部分：
   - 勾选 **GitHub project**
   - **Project url**: 你的Git仓库URL

2. **Build Triggers** 部分：
   - 勾选 **GitHub hook trigger for GITScm polling**
   - 或勾选 **Poll SCM**: `H/5 * * * *`（每5分钟检查一次）

3. **Pipeline** 部分：
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: 你的Git仓库URL
   - **Credentials**: 添加Git凭证
   - **Branch Specifier**: */claude/microservices-ticket-system-*
   - **Script Path**: Jenkinsfile

### 6.3 修改Jenkinsfile

编辑 `Jenkinsfile`，修改以下配置：

```groovy
environment {
    // 修改Nexus地址
    NEXUS_URL = '192.168.1.100:8082'
    NEXUS_REPO = 'ticket-system'

    // 修改Kubernetes配置
    K8S_NAMESPACE = 'ticket-system'

    // 其他配置...
}

// 修改Docker构建函数
def buildAndPushDocker(String service) {
    script {
        echo "Building ${service}..."

        dir(service) {
            sh """
                # 构建镜像
                docker build -t ${NEXUS_URL}/${NEXUS_REPO}/${service}:${BUILD_TAG} \
                -f ../docker/Dockerfile.backend .

                # 标记latest
                docker tag ${NEXUS_URL}/${NEXUS_REPO}/${service}:${BUILD_TAG} \
                ${NEXUS_URL}/${NEXUS_REPO}/${service}:latest

                # 登录Nexus
                docker login ${NEXUS_URL} -u admin -p admin123

                # 推送镜像
                docker push ${NEXUS_URL}/${NEXUS_REPO}/${service}:${BUILD_TAG}
                docker push ${NEXUS_URL}/${NEXUS_REPO}/${service}:latest
            """
        }
    }
}
```

### 6.4 配置kubectl访问

由于Jenkins在虚拟机上，需要让Jenkins能访问主机的Docker Desktop K8s：

#### 方案1: 使用kubeconfig（推荐）

在主机上：
```bash
# 导出kubeconfig
kubectl config view --raw > kubeconfig.yaml

# 修改server地址为主机IP（假设主机IP为192.168.1.50）
# 编辑kubeconfig.yaml，将server改为:
# server: https://192.168.1.50:6443
```

将修改后的kubeconfig上传到Jenkins：
1. **Manage Jenkins** → **Manage Credentials**
2. 添加 **Secret file** 类型凭证
3. 上传修改后的kubeconfig.yaml

#### 方案2: 暴露K8s API（不安全，仅开发环境）

在主机Docker Desktop中暴露K8s API：

```bash
# 创建代理服务
kubectl proxy --address='0.0.0.0' --port=8001 --accept-hosts='.*' &
```

修改Jenkinsfile中的kubectl命令：
```groovy
sh """
    kubectl --server=http://192.168.1.50:8001 \
    apply -f k8s/
"""
```

### 6.5 测试Pipeline

1. 在Jenkins项目页面点击 **Build Now**
2. 查看 **Console Output**
3. 观察各个阶段的执行情况

预期流程：
```
✓ 环境准备 (Checkout代码)
✓ 代码质量检查 (可跳过SonarQube)
✓ 单元测试
✓ Maven构建
✓ Docker构建 & 推送
  ✓ Gateway Service
  ✓ Auth Service
  ✓ Event Service
  ...
✓ 部署到Kubernetes
✓ 健康检查
```

---

## 7. 验证和测试

### 7.1 验证所有Pod运行

```bash
# 查看所有Pod
kubectl get pods -n ticket-system

# 预期所有Pod都是Running状态
# NAME                              READY   STATUS    RESTARTS   AGE
# gateway-service-xxx-yyy           1/1     Running   0          2m
# auth-service-xxx-yyy              1/1     Running   0          2m
# event-service-xxx-yyy             1/1     Running   0          2m
# mysql-0                           1/1     Running   0          10m
# mongodb-0                         1/1     Running   0          10m
# redis-xxx                         1/1     Running   0          8m
# nacos-xxx                         1/1     Running   0          7m

# 查看Pod日志
kubectl logs -n ticket-system -l app=gateway-service --tail=50

# 查看Pod详情
kubectl describe pod -n ticket-system gateway-service-xxx-yyy
```

### 7.2 测试API端点

#### 测试网关健康检查

```bash
# 方式1: 通过Ingress
curl http://api.ticket.example.com/actuator/health

# 方式2: 通过端口转发
kubectl port-forward -n ticket-system svc/gateway-service 8080:8080
curl http://localhost:8080/actuator/health
```

#### 测试用户注册

```bash
curl -X POST http://api.ticket.example.com/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com",
    "phone": "13800138000"
  }'
```

#### 测试用户登录

```bash
# 登录获取Token
TOKEN=$(curl -X POST http://api.ticket.example.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}' \
  | jq -r '.data.token')

echo "Token: $TOKEN"
```

#### 测试节目查询

```bash
# 查询节目列表（无需Token）
curl http://api.ticket.example.com/event/list

# 查询节目详情
curl http://api.ticket.example.com/event/{eventId}
```

### 7.3 测试Nacos服务注册

```bash
# 端口转发Nacos
kubectl port-forward -n ticket-system svc/nacos 8848:8848

# 访问Nacos控制台
open http://localhost:8848/nacos
# 用户名/密码: nacos/nacos

# 查看服务列表
# 应该看到: gateway-service, auth-service, event-service 等
```

### 7.4 测试数据库连接

#### 测试MySQL

```bash
# 端口转发MySQL
kubectl port-forward -n ticket-system svc/mysql 3306:3306

# 使用MySQL客户端连接
mysql -h 127.0.0.1 -P 3306 -uroot -proot

# 查看数据库
mysql> SHOW DATABASES;
mysql> USE ticket_system;
mysql> SHOW TABLES;
mysql> SELECT * FROM t_user;
```

#### 测试MongoDB

```bash
# 端口转发MongoDB
kubectl port-forward -n ticket-system svc/mongodb 27017:27017

# 使用mongosh连接
mongosh mongodb://admin:admin123@localhost:27017

# 查看数据库
> show dbs
> use ticket_system
> show collections
> db.events.find()
```

---

## 8. 故障排查

### 8.1 Pod无法启动

#### 问题1: ImagePullBackOff

```bash
# 查看Pod详情
kubectl describe pod -n ticket-system <pod-name>

# 常见原因：
# 1. 镜像地址错误
# 2. Nexus Registry不可达
# 3. 未登录Nexus Registry

# 解决方案：
# 创建Docker Registry Secret
kubectl create secret docker-registry nexus-secret \
  --docker-server=192.168.1.100:8082 \
  --docker-username=admin \
  --docker-password=admin123 \
  -n ticket-system

# 在Deployment中添加imagePullSecrets
# spec:
#   imagePullSecrets:
#   - name: nexus-secret
```

#### 问题2: CrashLoopBackOff

```bash
# 查看Pod日志
kubectl logs -n ticket-system <pod-name>

# 常见原因：
# 1. 应用启动失败
# 2. 连接不上数据库
# 3. 端口冲突
# 4. 配置错误

# 查看最近的日志
kubectl logs -n ticket-system <pod-name> --tail=100

# 查看上一次容器的日志
kubectl logs -n ticket-system <pod-name> --previous
```

#### 问题3: Pending状态

```bash
# 查看Pod事件
kubectl describe pod -n ticket-system <pod-name>

# 常见原因：
# 1. 资源不足（CPU/内存）
# 2. PVC未绑定
# 3. 节点不可调度

# 查看节点资源
kubectl describe nodes

# 减少资源请求
# 编辑Deployment，降低resources.requests值
```

### 8.2 服务无法访问

#### 问题1: Service ClusterIP无法访问

```bash
# 检查Service
kubectl get svc -n ticket-system

# 检查Endpoints
kubectl get endpoints -n ticket-system

# 测试Service连通性
kubectl run test-pod --rm -it --image=curlimages/curl -n ticket-system -- sh
# 在Pod中执行：
curl http://gateway-service:8080/actuator/health
```

#### 问题2: Ingress无法访问

```bash
# 检查Ingress
kubectl get ingress -n ticket-system
kubectl describe ingress -n ticket-system ticket-ingress

# 检查Ingress Controller
kubectl get pods -n ingress-nginx
kubectl logs -n ingress-nginx <ingress-controller-pod>

# 检查hosts配置
ping api.ticket.example.com

# 检查Ingress规则
kubectl get ingress -n ticket-system -o yaml
```

### 8.3 数据库连接问题

#### MySQL连接超时

```bash
# 检查MySQL Pod
kubectl get pods -n ticket-system -l app=mysql

# 检查MySQL日志
kubectl logs -n ticket-system mysql-0

# 测试MySQL连接
kubectl run mysql-client --rm -it --image=mysql:8.0 -n ticket-system -- \
  mysql -h mysql -uroot -proot -e "SELECT 1"

# 检查MySQL Service
kubectl get svc -n ticket-system mysql
kubectl describe svc -n ticket-system mysql
```

#### MongoDB连接失败

```bash
# 检查MongoDB Pod
kubectl get pods -n ticket-system -l app=mongodb

# 检查MongoDB日志
kubectl logs -n ticket-system mongodb-0

# 测试MongoDB连接
kubectl run mongo-client --rm -it --image=mongo:5.0 -n ticket-system -- \
  mongosh mongodb://admin:admin123@mongodb:27017 --eval "db.adminCommand('ping')"
```

### 8.4 Jenkins构建失败

#### Maven构建失败

```bash
# 在Jenkins Console Output中查看详细错误

# 常见问题：
# 1. 依赖下载失败 -> 配置Maven镜像
# 2. 编译错误 -> 检查代码
# 3. 测试失败 -> 跳过测试 mvn package -DskipTests
```

#### Docker构建失败

```bash
# 常见问题：
# 1. Docker daemon连接失败
# 解决：确保Jenkins容器可以访问Docker socket

# 2. Nexus推送失败
# 解决：检查Nexus凭证，确保已登录

# 3. 网络问题
# 解决：检查虚拟机网络配置
```

#### Kubernetes部署失败

```bash
# 常见问题：
# 1. kubectl无法连接K8s API
# 解决：检查kubeconfig配置

# 2. 权限不足
# 解决：确保kubeconfig有足够权限

# 3. Namespace不存在
# 解决：先创建namespace
kubectl create namespace ticket-system
```

### 8.5 常用调试命令

```bash
# 进入Pod调试
kubectl exec -it -n ticket-system <pod-name> -- /bin/sh

# 查看Pod环境变量
kubectl exec -n ticket-system <pod-name> -- env

# 查看Pod内网络
kubectl exec -n ticket-system <pod-name> -- ping mysql

# 查看Pod资源使用
kubectl top pods -n ticket-system

# 查看节点资源使用
kubectl top nodes

# 查看集群事件
kubectl get events -n ticket-system --sort-by='.lastTimestamp'

# 导出资源配置
kubectl get deployment -n ticket-system gateway-service -o yaml > gateway.yaml

# 强制删除Pod
kubectl delete pod -n ticket-system <pod-name> --force --grace-period=0

# 扩缩容
kubectl scale deployment -n ticket-system gateway-service --replicas=3

# 查看日志（实时）
kubectl logs -f -n ticket-system <pod-name>

# 查看多个Pod日志
kubectl logs -n ticket-system -l app=gateway-service --tail=50 --all-containers=true
```

---

## 9. 性能优化建议

### 9.1 Docker Desktop资源配置

1. 打开Docker Desktop **Settings**
2. 选择 **Resources**
3. 建议配置：
   - **CPUs**: 4-6核
   - **Memory**: 8-12GB
   - **Swap**: 2GB
   - **Disk image size**: 100GB

### 9.2 Kubernetes资源优化

```yaml
# 合理设置resources
resources:
  requests:
    cpu: "200m"      # 最小CPU
    memory: "256Mi"  # 最小内存
  limits:
    cpu: "500m"      # 最大CPU
    memory: "512Mi"  # 最大内存
```

### 9.3 镜像优化

```bash
# 使用多阶段构建减小镜像大小
# 已在Dockerfile.backend中实现

# 使用.dockerignore排除不必要文件
cat > .dockerignore <<EOF
target/
.git/
.idea/
*.md
EOF
```

---

## 10. 备份和恢复

### 10.1 备份MySQL数据

```bash
# 导出所有数据库
kubectl exec -n ticket-system mysql-0 -- \
  mysqldump -uroot -proot --all-databases > mysql-backup.sql

# 备份到本地
kubectl cp ticket-system/mysql-0:/mysql-backup.sql ./mysql-backup.sql
```

### 10.2 备份MongoDB数据

```bash
# 导出数据库
kubectl exec -n ticket-system mongodb-0 -- \
  mongodump --uri="mongodb://admin:admin123@localhost:27017" --out=/tmp/backup

# 复制到本地
kubectl cp ticket-system/mongodb-0:/tmp/backup ./mongodb-backup
```

### 10.3 恢复数据

```bash
# 恢复MySQL
kubectl cp ./mysql-backup.sql ticket-system/mysql-0:/tmp/
kubectl exec -n ticket-system mysql-0 -- \
  mysql -uroot -proot < /tmp/mysql-backup.sql

# 恢复MongoDB
kubectl cp ./mongodb-backup ticket-system/mongodb-0:/tmp/
kubectl exec -n ticket-system mongodb-0 -- \
  mongorestore --uri="mongodb://admin:admin123@localhost:27017" /tmp/backup
```

---

## 总结

本文档详细介绍了如何在 **Docker Desktop K8s + 本地虚拟机** 环境下部署微服务订票平台。

### 关键点回顾

✅ **Docker Desktop K8s** - 启用并配置
✅ **虚拟机环境** - Jenkins + Nexus部署
✅ **MySQL + MongoDB** - StatefulSet部署
✅ **微服务部署** - Deployment + Service + Ingress
✅ **CI/CD流水线** - Jenkins自动化部署
✅ **故障排查** - 常见问题解决方案

### 下一步

1. 完善其他微服务代码
2. 添加监控（Prometheus + Grafana）
3. 配置日志收集（ELK）
4. 实施备份策略
5. 生产环境优化

如有问题，请参考本文档的故障排查部分。

# Rocky Linux 9.6 虚拟机部署完整指南

## 📋 文档说明

本文档专门针对 **Rocky Linux 9.6 虚拟机环境**，提供从零开始的完整部署步骤。

**环境架构：**
- **主机**: Windows/macOS + Docker Desktop K8s
- **虚拟机**: Rocky Linux 9.6 (Jenkins + Nexus)
- **网络**: 桥接模式或NAT模式

---

## 目录

1. [虚拟机基础配置](#1-虚拟机基础配置)
2. [安装Docker](#2-安装docker)
3. [部署Nexus](#3-部署nexus)
4. [部署Jenkins](#4-部署jenkins)
5. [配置防火墙](#5-配置防火墙)
6. [配置SELinux](#6-配置selinux)
7. [测试连接](#7-测试连接)
8. [主机端配置](#8-主机端配置)
9. [完整部署流程](#9-完整部署流程)
10. [故障排查](#10-故障排查)

---

## 1. 虚拟机基础配置

### 1.1 系统要求

| 项目 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 2核 | 4核 |
| 内存 | 4GB | 8GB |
| 硬盘 | 50GB | 100GB |
| 网络 | NAT/桥接 | 桥接模式 |

### 1.2 检查系统信息

```bash
# 查看系统版本
cat /etc/rocky-release
# 输出应为: Rocky Linux release 9.6 (Blue Onyx)

# 查看内核版本
uname -r

# 查看IP地址
ip addr show

# 查看主机名
hostname

# 查看资源
free -h
df -h
```

### 1.3 设置主机名（可选）

```bash
# 设置主机名为 ticket-vm
sudo hostnamectl set-hostname ticket-vm

# 验证
hostname
```

### 1.4 更新系统

```bash
# 更新所有包到最新版本
sudo dnf update -y

# 安装常用工具
sudo dnf install -y \
    wget \
    curl \
    vim \
    git \
    net-tools \
    bind-utils \
    telnet \
    nc

# 重启系统（推荐）
sudo reboot
```

### 1.5 配置静态IP（重要！）

**方式1: 使用nmtui（图形化工具）**

```bash
# 启动网络配置工具
sudo nmtui

# 按照以下步骤：
# 1. 选择 "Edit a connection"
# 2. 选择你的网络接口（通常是 ens33 或 eth0）
# 3. 选择 "IPv4 CONFIGURATION" -> Manual
# 4. 设置：
#    - Address: 192.168.1.100/24
#    - Gateway: 192.168.1.1
#    - DNS servers: 8.8.8.8
# 5. 保存并退出

# 重启网络服务
sudo systemctl restart NetworkManager

# 验证IP
ip addr show
```

**方式2: 手动编辑配置文件**

```bash
# 查找网络接口名称
ip link show

# 假设接口名为 ens33
sudo vim /etc/NetworkManager/system-connections/ens33.nmconnection

# 添加或修改以下内容：
```

```ini
[connection]
id=ens33
type=ethernet
interface-name=ens33

[ipv4]
method=manual
address1=192.168.1.100/24,192.168.1.1
dns=8.8.8.8;8.8.4.4;

[ipv6]
method=auto
```

```bash
# 设置权限
sudo chmod 600 /etc/NetworkManager/system-connections/ens33.nmconnection

# 重新加载配置
sudo nmcli connection reload
sudo nmcli connection up ens33

# 验证
ip addr show ens33
ping -c 3 8.8.8.8
```

### 1.6 配置主机名解析

```bash
# 编辑 /etc/hosts
sudo vim /etc/hosts

# 添加以下内容：
```

```
192.168.1.100   ticket-vm
192.168.1.50    ticket-host     # 主机IP（根据实际情况修改）
```

---

## 2. 安装Docker

### 2.1 卸载旧版本Docker（如果存在）

```bash
sudo dnf remove -y \
    docker \
    docker-client \
    docker-client-latest \
    docker-common \
    docker-latest \
    docker-latest-logrotate \
    docker-logrotate \
    docker-engine \
    podman \
    runc
```

### 2.2 安装Docker

**方式1: 使用官方仓库（推荐）**

```bash
# 1. 安装必要的依赖
sudo dnf install -y dnf-plugins-core

# 2. 添加Docker官方仓库
sudo dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# 3. 安装Docker Engine
sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 4. 启动Docker服务
sudo systemctl start docker
sudo systemctl enable docker

# 5. 验证安装
sudo docker --version
sudo docker run hello-world
```

**方式2: 使用安装脚本（快速）**

```bash
# 下载并执行Docker安装脚本
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 启动Docker
sudo systemctl start docker
sudo systemctl enable docker

# 清理安装脚本
rm get-docker.sh
```

### 2.3 配置Docker

```bash
# 1. 创建docker用户组（如果不存在）
sudo groupadd docker 2>/dev/null || true

# 2. 将当前用户添加到docker组
sudo usermod -aG docker $USER

# 3. 重新登录使组设置生效
# 退出并重新SSH登录，或执行：
newgrp docker

# 4. 测试无需sudo运行docker
docker ps
```

### 2.4 配置Docker镜像加速（可选，提升速度）

```bash
# 创建配置目录
sudo mkdir -p /etc/docker

# 配置镜像加速器（使用阿里云或其他国内镜像）
sudo tee /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://mirror.ccs.tencentyun.com"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  },
  "storage-driver": "overlay2"
}
EOF

# 重启Docker使配置生效
sudo systemctl daemon-reload
sudo systemctl restart docker

# 验证配置
docker info | grep -A 10 "Registry Mirrors"
```

### 2.5 安装Docker Compose

```bash
# Docker Compose已作为插件安装，验证：
docker compose version

# 如果没有安装，手动安装：
DOCKER_COMPOSE_VERSION="v2.24.0"
sudo curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" \
    -o /usr/local/bin/docker-compose

sudo chmod +x /usr/local/bin/docker-compose

# 验证
docker-compose --version
```

---

## 3. 部署Nexus

### 3.1 创建Nexus目录

```bash
# 创建数据目录
sudo mkdir -p /opt/nexus-data

# 设置权限（Nexus容器使用UID 200）
sudo chown -R 200:200 /opt/nexus-data
```

### 3.2 启动Nexus容器

```bash
# 启动Nexus
docker run -d \
  --name nexus \
  --restart always \
  -p 8081:8081 \
  -p 8082:8082 \
  -v /opt/nexus-data:/nexus-data \
  sonatype/nexus3:latest

# 查看容器状态
docker ps | grep nexus

# 查看日志（等待启动完成，约2-3分钟）
docker logs -f nexus

# 等待看到以下日志表示启动完成：
# -------------------------------------------------
# Started Sonatype Nexus OSS
# -------------------------------------------------
```

### 3.3 获取初始管理员密码

```bash
# 等待约2-3分钟后执行
docker exec nexus cat /nexus-data/admin.password

# 记录这个密码，例如：a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6
```

### 3.4 首次登录配置Nexus

1. **访问Nexus**
   ```
   浏览器打开: http://192.168.1.100:8081
   ```

2. **登录**
   - 点击右上角 "Sign in"
   - Username: `admin`
   - Password: 上一步获取的密码

3. **设置新密码**
   - 按照向导设置新密码（建议：`admin123`）
   - 记录新密码

4. **启用匿名访问**
   - 在向导中选择 "Enable anonymous access"
   - 点击 "Next" → "Finish"

### 3.5 创建Docker Registry

1. **进入Repository管理**
   - 点击顶部 **齿轮图标** (Administration)
   - 左侧菜单选择 **Repository** → **Repositories**

2. **创建Docker (hosted) Repository**
   - 点击 **Create repository**
   - 选择 **docker (hosted)**
   - 配置：
     ```
     Name: docker-hosted
     HTTP: 8082
     Enable Docker V1 API: ✓ 勾选
     Allow anonymous docker pull: ✓ 勾选
     ```
   - 点击 **Create repository**

3. **创建Maven Repository**
   - 点击 **Create repository**
   - 选择 **maven2 (hosted)**
   - 配置：
     ```
     Name: maven-releases
     Version policy: Release
     Deployment policy: Allow redeploy
     ```
   - 点击 **Create repository**

### 3.6 配置Docker客户端信任Nexus

```bash
# 配置Docker信任Nexus的insecure registry
sudo tee /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn"
  ],
  "insecure-registries": [
    "192.168.1.100:8082"
  ],
  "log-opts": {
    "max-size": "100m"
  }
}
EOF

# 重启Docker
sudo systemctl daemon-reload
sudo systemctl restart docker

# 验证配置
docker info | grep -A 2 "Insecure Registries"
```

### 3.7 测试Nexus Docker Registry

```bash
# 1. 登录Nexus Docker Registry
docker login 192.168.1.100:8082
# Username: admin
# Password: admin123

# 2. 拉取测试镜像
docker pull nginx:alpine

# 3. 标记镜像
docker tag nginx:alpine 192.168.1.100:8082/nginx:alpine

# 4. 推送到Nexus
docker push 192.168.1.100:8082/nginx:alpine

# 5. 删除本地镜像
docker rmi nginx:alpine 192.168.1.100:8082/nginx:alpine

# 6. 从Nexus拉取
docker pull 192.168.1.100:8082/nginx:alpine

# 成功则表示Nexus配置正确 ✓
```

---

## 4. 部署Jenkins

### 4.1 创建Jenkins目录

```bash
# 创建Jenkins home目录
sudo mkdir -p /opt/jenkins_home

# 设置权限（Jenkins容器使用UID 1000）
sudo chown -R 1000:1000 /opt/jenkins_home
```

### 4.2 启动Jenkins容器

```bash
# 启动Jenkins
docker run -d \
  --name jenkins \
  --restart always \
  -p 8080:8080 \
  -p 50000:50000 \
  -v /opt/jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -u root \
  jenkins/jenkins:lts-jdk17

# 查看容器状态
docker ps | grep jenkins

# 查看日志（等待启动完成，约1-2分钟）
docker logs -f jenkins

# 等待看到以下内容：
# *************************************************************
# Jenkins initial setup is required. An admin user has been created and
# a password generated.
# Please use the following password to proceed to installation:
#
# a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
#
# *************************************************************
```

### 4.3 获取初始管理员密码

```bash
# 从日志中复制密码，或执行：
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# 记录这个密码
```

### 4.4 首次登录配置Jenkins

1. **访问Jenkins**
   ```
   浏览器打开: http://192.168.1.100:8080
   ```

2. **输入初始密码**
   - 粘贴上一步获取的密码
   - 点击 "Continue"

3. **安装插件**
   - 选择 **"Install suggested plugins"**（推荐）
   - 等待插件安装完成（约5-10分钟）

4. **创建管理员用户**
   ```
   Username: admin
   Password: admin123
   Confirm password: admin123
   Full name: Admin
   E-mail address: admin@example.com
   ```
   - 点击 "Save and Continue"

5. **Jenkins URL配置**
   ```
   Jenkins URL: http://192.168.1.100:8080/
   ```
   - 点击 "Save and Finish"
   - 点击 "Start using Jenkins"

### 4.5 安装必要的插件

1. **进入插件管理**
   - 点击 **Manage Jenkins** → **Manage Plugins**

2. **安装以下插件**
   - 点击 **Available plugins** 标签
   - 搜索并勾选以下插件：
     ```
     ✓ Docker Pipeline
     ✓ Kubernetes
     ✓ Git
     ✓ Maven Integration
     ✓ Pipeline
     ✓ Nexus Artifact Uploader
     ✓ Blue Ocean (可选，更好的UI)
     ```
   - 点击 **Install without restart**

3. **等待安装完成**
   - 勾选 "Restart Jenkins when installation is complete and no jobs are running"
   - 等待Jenkins重启（约1分钟）

### 4.6 配置Jenkins工具

**配置Maven**

1. **Manage Jenkins** → **Global Tool Configuration**
2. 找到 **Maven** 部分
3. 点击 **Add Maven**
4. 配置：
   ```
   Name: Maven-3.9
   Install automatically: ✓ 勾选
   Version: 3.9.6
   ```
5. 点击 **Save**

**配置Docker**

Jenkins容器已经可以访问Docker（通过volume挂载），无需额外配置。

### 4.7 配置Jenkins凭证

**添加Nexus凭证**

1. **Manage Jenkins** → **Manage Credentials**
2. 点击 **(global)** → **Add Credentials**
3. 配置：
   ```
   Kind: Username with password
   Scope: Global
   Username: admin
   Password: admin123
   ID: nexus-credentials
   Description: Nexus Repository Credentials
   ```
4. 点击 **Create**

**添加Git凭证（如果需要）**

1. 重复上述步骤
2. 配置：
   ```
   Kind: Username with password
   Scope: Global
   Username: <your-git-username>
   Password: <your-git-token>
   ID: git-credentials
   Description: Git Repository Credentials
   ```

---

## 5. 配置防火墙

### 5.1 检查防火墙状态

```bash
# 查看防火墙状态
sudo firewall-cmd --state

# 查看已开放的端口
sudo firewall-cmd --list-all
```

### 5.2 开放必要的端口

```bash
# 开放Jenkins端口
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=50000/tcp

# 开放Nexus端口
sudo firewall-cmd --permanent --add-port=8081/tcp
sudo firewall-cmd --permanent --add-port=8082/tcp

# 开放Docker Registry端口（如果Nexus使用其他端口）
# sudo firewall-cmd --permanent --add-port=5000/tcp

# 重新加载防火墙规则
sudo firewall-cmd --reload

# 验证端口已开放
sudo firewall-cmd --list-ports
```

### 5.3 配置防火墙区域（可选）

```bash
# 查看当前区域
sudo firewall-cmd --get-active-zones

# 将网络接口添加到public区域
sudo firewall-cmd --permanent --zone=public --add-interface=ens33

# 重新加载
sudo firewall-cmd --reload
```

### 5.4 测试端口连通性

**在虚拟机上测试**

```bash
# 测试Jenkins
curl http://localhost:8080

# 测试Nexus
curl http://localhost:8081
```

**在主机上测试**

```bash
# 测试Jenkins（在主机上执行）
curl http://192.168.1.100:8080

# 测试Nexus
curl http://192.168.1.100:8081
```

---

## 6. 配置SELinux

### 6.1 检查SELinux状态

```bash
# 查看SELinux状态
getenforce
# 输出: Enforcing (启用) 或 Permissive (宽松) 或 Disabled (禁用)

# 查看详细状态
sestatus
```

### 6.2 临时设置为Permissive模式（推荐用于测试）

```bash
# 临时设置为宽松模式（重启后失效）
sudo setenforce 0

# 验证
getenforce
# 输出: Permissive
```

### 6.3 永久配置SELinux（可选）

**方式1: 保持Enforcing模式，配置规则（推荐生产环境）**

```bash
# 允许Docker访问
sudo setsebool -P container_manage_cgroup on

# 允许Jenkins访问Docker socket
sudo chcon -Rt svirt_sandbox_file_t /var/run/docker.sock

# 允许容器绑定特权端口
sudo setsebool -P nis_enabled on
```

**方式2: 永久禁用SELinux（简单但不推荐）**

```bash
# 编辑配置文件
sudo vim /etc/selinux/config

# 修改为：
SELINUX=permissive
# 或
SELINUX=disabled

# 保存后重启系统
sudo reboot
```

### 6.4 验证配置

```bash
# 重启Docker和Jenkins
docker restart nexus jenkins

# 查看容器状态
docker ps

# 测试访问
curl http://localhost:8080
curl http://localhost:8081
```

---

## 7. 测试连接

### 7.1 虚拟机内部测试

```bash
# 测试Jenkins
curl -I http://localhost:8080
# 应返回: HTTP/1.1 200 OK 或 403 Forbidden

# 测试Nexus
curl -I http://localhost:8081
# 应返回: HTTP/1.1 200 OK

# 测试Docker Registry
curl http://localhost:8082/v2/_catalog
# 应返回: {"repositories":[...]}

# 测试Docker
docker ps
docker images

# 测试网络连通性
ping -c 3 8.8.8.8
ping -c 3 192.168.1.1
```

### 7.2 主机到虚拟机测试

**在您的主机（Windows/macOS）上执行：**

```bash
# 测试网络连通性
ping 192.168.1.100

# 测试Jenkins端口
telnet 192.168.1.100 8080
# 或使用nc（netcat）
nc -zv 192.168.1.100 8080

# 测试Nexus端口
nc -zv 192.168.1.100 8081
nc -zv 192.168.1.100 8082

# 浏览器测试
# 打开浏览器访问：
http://192.168.1.100:8080  # Jenkins
http://192.168.1.100:8081  # Nexus
```

### 7.3 Docker Registry测试

**在主机上配置Docker信任Nexus**

**Windows (Docker Desktop):**

1. 打开 Docker Desktop
2. Settings → Docker Engine
3. 添加配置：
   ```json
   {
     "insecure-registries": ["192.168.1.100:8082"]
   }
   ```
4. 点击 "Apply & Restart"

**macOS (Docker Desktop):**

1. Docker Desktop → Preferences → Docker Engine
2. 添加相同配置
3. Apply & Restart

**Linux主机:**

```bash
sudo vim /etc/docker/daemon.json

# 添加：
{
  "insecure-registries": ["192.168.1.100:8082"]
}

sudo systemctl restart docker
```

**测试推送和拉取**

```bash
# 在主机上执行：

# 1. 登录
docker login 192.168.1.100:8082
# Username: admin
# Password: admin123

# 2. 测试推送
docker pull busybox
docker tag busybox 192.168.1.100:8082/busybox:test
docker push 192.168.1.100:8082/busybox:test

# 3. 测试拉取
docker rmi busybox 192.168.1.100:8082/busybox:test
docker pull 192.168.1.100:8082/busybox:test

# 成功则配置正确 ✓
```

---

## 8. 主机端配置

### 8.1 配置hosts文件

**Windows:**

```powershell
# 以管理员身份运行记事本
notepad C:\Windows\System32\drivers\etc\hosts

# 添加：
192.168.1.100   ticket-vm jenkins.local nexus.local
```

**macOS/Linux:**

```bash
sudo vim /etc/hosts

# 添加：
192.168.1.100   ticket-vm jenkins.local nexus.local
```

### 8.2 测试访问

```bash
# 测试域名解析
ping ticket-vm
ping jenkins.local

# 浏览器访问
http://jenkins.local:8080
http://nexus.local:8081
```

### 8.3 配置kubectl访问Docker Desktop K8s

**导出kubeconfig**

```bash
# 在主机上执行
kubectl config view --raw > ~/kubeconfig-for-jenkins.yaml

# 查看当前context
kubectl config current-context
# 应输出: docker-desktop
```

**上传kubeconfig到Jenkins**

1. 访问 Jenkins: http://192.168.1.100:8080
2. **Manage Jenkins** → **Manage Credentials**
3. 点击 **(global)** → **Add Credentials**
4. 配置：
   ```
   Kind: Secret file
   File: 上传 kubeconfig-for-jenkins.yaml
   ID: kubernetes-config
   Description: Docker Desktop Kubernetes Config
   ```
5. 点击 **Create**

---

## 9. 完整部署流程

### 9.1 快速部署脚本

创建一个自动化部署脚本：

```bash
# 在虚拟机上创建脚本
vim ~/deploy-all.sh
```

```bash
#!/bin/bash

set -e

echo "=========================================="
echo "微服务订票平台 - Rocky Linux 9.6 自动部署"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查是否为root或有sudo权限
if [ "$EUID" -ne 0 ] && ! sudo -v; then
    echo -e "${RED}错误: 需要root权限或sudo权限${NC}"
    exit 1
fi

echo -e "\n${YELLOW}[1/10] 更新系统...${NC}"
sudo dnf update -y

echo -e "\n${YELLOW}[2/10] 安装必要工具...${NC}"
sudo dnf install -y wget curl vim git net-tools bind-utils

echo -e "\n${YELLOW}[3/10] 检查Docker...${NC}"
if ! command -v docker &> /dev/null; then
    echo "Docker未安装，正在安装..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    rm get-docker.sh
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -aG docker $USER
else
    echo -e "${GREEN}Docker已安装${NC}"
fi

echo -e "\n${YELLOW}[4/10] 配置防火墙...${NC}"
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=8081/tcp
sudo firewall-cmd --permanent --add-port=8082/tcp
sudo firewall-cmd --permanent --add-port=50000/tcp
sudo firewall-cmd --reload

echo -e "\n${YELLOW}[5/10] 配置SELinux...${NC}"
sudo setenforce 0

echo -e "\n${YELLOW}[6/10] 创建数据目录...${NC}"
sudo mkdir -p /opt/nexus-data /opt/jenkins_home
sudo chown -R 200:200 /opt/nexus-data
sudo chown -R 1000:1000 /opt/jenkins_home

echo -e "\n${YELLOW}[7/10] 启动Nexus...${NC}"
if [ "$(docker ps -q -f name=nexus)" ]; then
    echo "Nexus已运行"
else
    docker run -d \
      --name nexus \
      --restart always \
      -p 8081:8081 \
      -p 8082:8082 \
      -v /opt/nexus-data:/nexus-data \
      sonatype/nexus3:latest
    echo "等待Nexus启动（约2分钟）..."
    sleep 120
fi

echo -e "\n${YELLOW}[8/10] 启动Jenkins...${NC}"
if [ "$(docker ps -q -f name=jenkins)" ]; then
    echo "Jenkins已运行"
else
    docker run -d \
      --name jenkins \
      --restart always \
      -p 8080:8080 \
      -p 50000:50000 \
      -v /opt/jenkins_home:/var/jenkins_home \
      -v /var/run/docker.sock:/var/run/docker.sock \
      -u root \
      jenkins/jenkins:lts-jdk17
    echo "等待Jenkins启动（约1分钟）..."
    sleep 60
fi

echo -e "\n${YELLOW}[9/10] 配置Docker信任Nexus...${NC}"
sudo tee /etc/docker/daemon.json > /dev/null <<EOF
{
  "insecure-registries": ["192.168.1.100:8082"]
}
EOF
sudo systemctl restart docker
docker restart nexus jenkins

echo -e "\n${YELLOW}[10/10] 获取初始密码...${NC}"
echo -e "\n${GREEN}=========================================="
echo "部署完成！"
echo "==========================================${NC}"

echo -e "\n${YELLOW}访问信息：${NC}"
echo "Nexus:   http://$(hostname -I | awk '{print $1}'):8081"
echo "Jenkins: http://$(hostname -I | awk '{print $1}'):8080"

echo -e "\n${YELLOW}初始密码：${NC}"
echo -n "Nexus初始密码: "
docker exec nexus cat /nexus-data/admin.password 2>/dev/null || echo "尚未生成，请稍后执行: docker exec nexus cat /nexus-data/admin.password"

echo -n "Jenkins初始密码: "
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || echo "尚未生成，请稍后执行: docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword"

echo -e "\n${GREEN}请重新登录以使Docker组生效：${NC}"
echo "  exit"
echo "  ssh user@$(hostname -I | awk '{print $1}')"

echo -e "\n${YELLOW}后续步骤：${NC}"
echo "1. 访问Nexus并完成初始配置"
echo "2. 访问Jenkins并安装插件"
echo "3. 查看完整文档: docs/ROCKY_LINUX_DEPLOYMENT.md"
```

```bash
# 设置执行权限
chmod +x ~/deploy-all.sh

# 执行脚本
./deploy-all.sh
```

### 9.2 验证部署

```bash
# 检查容器运行状态
docker ps

# 应该看到：
# CONTAINER ID   IMAGE                    STATUS          PORTS
# xxxxxxxxxxxx   jenkins/jenkins:lts-jdk17   Up 5 minutes    0.0.0.0:8080->8080/tcp, 0.0.0.0:50000->50000/tcp
# xxxxxxxxxxxx   sonatype/nexus3:latest      Up 5 minutes    0.0.0.0:8081->8081/tcp, 0.0.0.0:8082->8082/tcp

# 检查端口监听
sudo netstat -tlnp | grep -E '8080|8081|8082'

# 测试访问
curl -I http://localhost:8080  # Jenkins
curl -I http://localhost:8081  # Nexus
```

---

## 10. 故障排查

### 10.1 Docker相关问题

**问题1: Docker服务无法启动**

```bash
# 检查状态
sudo systemctl status docker

# 查看日志
sudo journalctl -xeu docker

# 重启Docker
sudo systemctl restart docker

# 检查SELinux
getenforce
sudo setenforce 0
```

**问题2: 容器无法启动**

```bash
# 查看容器日志
docker logs jenkins
docker logs nexus

# 检查磁盘空间
df -h

# 检查内存
free -h

# 重建容器
docker stop jenkins
docker rm jenkins
# 重新运行启动命令
```

### 10.2 网络连接问题

**问题1: 主机无法访问虚拟机**

```bash
# 在虚拟机上检查IP
ip addr show

# 检查防火墙
sudo firewall-cmd --list-all

# 测试端口
sudo netstat -tlnp | grep 8080

# 检查SELinux
getenforce

# 临时关闭防火墙测试
sudo systemctl stop firewalld

# 在主机上ping虚拟机
ping 192.168.1.100

# 在主机上telnet端口
telnet 192.168.1.100 8080
```

**问题2: 虚拟机无法访问外网**

```bash
# 检查网关
ip route show

# 检查DNS
cat /etc/resolv.conf

# 测试DNS
nslookup google.com

# 测试网络
ping -c 3 8.8.8.8
ping -c 3 www.google.com

# 重启网络
sudo systemctl restart NetworkManager
```

### 10.3 Nexus问题

**问题1: Nexus启动慢或卡住**

```bash
# 检查内存（Nexus需要至少2GB内存）
free -h

# 查看日志
docker logs nexus | tail -100

# 重启Nexus
docker restart nexus

# 等待2-3分钟
sleep 180

# 检查状态
curl http://localhost:8081
```

**问题2: 无法推送镜像到Nexus**

```bash
# 检查Docker配置
cat /etc/docker/daemon.json

# 检查是否登录
docker login 192.168.1.100:8082

# 测试连接
curl http://192.168.1.100:8082/v2/_catalog

# 检查防火墙
sudo firewall-cmd --list-ports | grep 8082
```

### 10.4 Jenkins问题

**问题1: Jenkins插件安装失败**

```bash
# 检查网络
docker exec jenkins curl -I https://updates.jenkins.io

# 查看日志
docker logs jenkins | grep -i error

# 进入容器
docker exec -it jenkins bash

# 手动下载插件
cd /var/jenkins_home/plugins
wget https://updates.jenkins.io/download/plugins/...
```

**问题2: Jenkins无法访问Docker**

```bash
# 检查Docker socket权限
ls -l /var/run/docker.sock

# 重启Jenkins容器
docker restart jenkins

# 检查Jenkins容器内Docker
docker exec jenkins docker ps
```

### 10.5 常用命令

```bash
# 查看所有容器
docker ps -a

# 查看容器资源使用
docker stats

# 查看系统日志
sudo journalctl -xe

# 查看防火墙日志
sudo journalctl -u firewalld

# 重启所有服务
docker restart nexus jenkins

# 清理Docker资源
docker system prune -af

# 查看磁盘使用
du -sh /opt/nexus-data
du -sh /opt/jenkins_home
```

---

## 11. 性能优化

### 11.1 系统优化

```bash
# 增加文件描述符限制
sudo vim /etc/security/limits.conf

# 添加：
* soft nofile 65536
* hard nofile 65536

# 优化网络参数
sudo vim /etc/sysctl.conf

# 添加：
net.core.somaxconn = 1024
net.ipv4.tcp_max_syn_backlog = 2048
vm.swappiness = 10

# 应用配置
sudo sysctl -p
```

### 11.2 Docker优化

```bash
# 限制容器资源
docker update --memory=4g --cpus=2 jenkins
docker update --memory=4g --cpus=2 nexus

# 清理未使用的镜像和容器
docker system prune -af

# 查看Docker磁盘使用
docker system df
```

### 11.3 Nexus优化

```bash
# 增加Nexus内存
docker stop nexus
docker rm nexus

docker run -d \
  --name nexus \
  --restart always \
  -p 8081:8081 \
  -p 8082:8082 \
  -e INSTALL4J_ADD_VM_PARAMS="-Xms2g -Xmx4g -XX:MaxDirectMemorySize=4g" \
  -v /opt/nexus-data:/nexus-data \
  sonatype/nexus3:latest
```

---

## 12. 备份与恢复

### 12.1 备份数据

```bash
# 创建备份脚本
vim ~/backup.sh
```

```bash
#!/bin/bash

BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# 停止容器
docker stop jenkins nexus

# 备份Jenkins
tar czf $BACKUP_DIR/jenkins_${DATE}.tar.gz /opt/jenkins_home

# 备份Nexus
tar czf $BACKUP_DIR/nexus_${DATE}.tar.gz /opt/nexus-data

# 启动容器
docker start jenkins nexus

echo "备份完成: $BACKUP_DIR"
ls -lh $BACKUP_DIR
```

```bash
chmod +x ~/backup.sh
./backup.sh
```

### 12.2 恢复数据

```bash
# 停止容器
docker stop jenkins nexus

# 恢复Jenkins
tar xzf /opt/backups/jenkins_20240620_100000.tar.gz -C /

# 恢复Nexus
tar xzf /opt/backups/nexus_20240620_100000.tar.gz -C /

# 启动容器
docker start jenkins nexus
```

---

## 13. 总结

### ✅ 完成清单

- [x] Rocky Linux 9.6 系统配置
- [x] Docker安装和配置
- [x] Nexus部署和配置
- [x] Jenkins部署和配置
- [x] 防火墙配置
- [x] SELinux配置
- [x] 网络连通性测试
- [x] Docker Registry配置

### 📝 关键信息

| 服务 | 地址 | 端口 | 默认账号 |
|------|------|------|---------|
| Jenkins | http://192.168.1.100:8080 | 8080 | admin/admin123 |
| Nexus | http://192.168.1.100:8081 | 8081 | admin/admin123 |
| Docker Registry | 192.168.1.100:8082 | 8082 | admin/admin123 |

### 🚀 下一步

1. **完成Jenkins配置**
   - 安装必要插件
   - 配置Maven和Docker
   - 添加凭证

2. **完成Nexus配置**
   - 创建Docker和Maven仓库
   - 配置权限

3. **开始部署项目**
   - 参考主部署文档: `DEPLOYMENT.md`
   - 构建和推送Docker镜像
   - 部署到Kubernetes

---

## 附录

### A. 完整的环境变量

```bash
# 在 ~/.bashrc 中添加
export JENKINS_URL=http://192.168.1.100:8080
export NEXUS_URL=http://192.168.1.100:8081
export DOCKER_REGISTRY=192.168.1.100:8082

# 应用配置
source ~/.bashrc
```

### B. 有用的别名

```bash
# 添加到 ~/.bashrc
alias dps='docker ps'
alias dlogs='docker logs -f'
alias dstop='docker stop'
alias dstart='docker start'
alias drestart='docker restart'

alias jenkins-logs='docker logs -f jenkins'
alias nexus-logs='docker logs -f nexus'
alias jenkins-restart='docker restart jenkins'
alias nexus-restart='docker restart nexus'
```

### C. 参考链接

- Rocky Linux文档: https://docs.rockylinux.org/
- Docker文档: https://docs.docker.com/
- Jenkins文档: https://www.jenkins.io/doc/
- Nexus文档: https://help.sonatype.com/repomanager3

---

**部署愉快！如有问题，请参考故障排查章节。** 🚀

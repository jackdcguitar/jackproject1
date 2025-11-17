# 虛擬機部署指南（Rocky Linux 9.6）

本文檔詳細說明如何在 Rocky Linux 9.6 虛擬機上配置和部署商城系統的基礎設施服務。

## 目錄

1. [系統要求](#系統要求)
2. [虛擬機安裝](#虛擬機安裝)
3. [系統初始化配置](#系統初始化配置)
4. [網絡配置](#網絡配置)
5. [安裝和配置 MySQL](#安裝和配置-mysql)
6. [安裝和配置 Redis](#安裝和配置-redis)
7. [安裝和配置 Nginx](#安裝和配置-nginx)
8. [防火牆配置](#防火牆配置)
9. [安全加固](#安全加固)
10. [監控和維護](#監控和維護)
11. [故障排除](#故障排除)

## 系統要求

### 硬件要求

- **CPU**: 最少 4 核心（推薦 8 核心）
- **內存**: 最少 8GB（推薦 16GB）
- **磁盤**: 最少 100GB（推薦 200GB+）
- **網絡**: 千兆網卡

### 軟件要求

- **操作系統**: Rocky Linux 9.6
- **虛擬化平台**: VMware、VirtualBox、KVM、Hyper-V 等
- **網絡**: 與 Docker Desktop 主機在同一網絡或可路由

## 虛擬機安裝

### 下載 Rocky Linux 9.6

訪問官方網站下載 ISO：
```
https://rockylinux.org/download
```

選擇：
- **Rocky Linux 9.6**
- **x86_64**
- **Minimal ISO** 或 **DVD ISO**

### 創建虛擬機

#### VMware Workstation/Fusion

1. 打開 VMware，點擊 **創建新虛擬機**
2. 選擇 **典型（推薦）**
3. 選擇 **稍後安裝操作系統**
4. 選擇 **Linux** → **Red Hat Enterprise Linux 9 64-bit**
5. 設置虛擬機名稱和位置
6. 設置磁盤大小：**100GB**（推薦 200GB）
7. 點擊 **自定義硬件**：
   - **內存**: 8GB（8192 MB）
   - **處理器**: 4 核
   - **網絡適配器**: 橋接模式（Bridged）
   - **CD/DVD**: 掛載 Rocky Linux ISO
8. 完成創建

#### VirtualBox

1. 打開 VirtualBox，點擊 **新建**
2. 名稱：`Rocky-Linux-Mall`
3. 類型：**Linux**
4. 版本：**Red Hat (64-bit)**
5. 內存：**8192 MB**
6. 硬盤：**創建虛擬硬盤** → **VDI** → **動態分配** → **100 GB**
7. 在設置中：
   - **系統** → **處理器**：4 核
   - **網絡** → **網卡 1**：橋接網卡
   - **存儲** → **控制器 IDE**：添加光盤，選擇 Rocky Linux ISO
8. 啟動虛擬機

### 安裝 Rocky Linux

1. **啟動虛擬機**，選擇 **Install Rocky Linux 9.6**

2. **選擇語言**：
   - 建議選擇 **English (United States)**
   - 或選擇 **中文（繁體）** 如果需要

3. **安裝摘要配置**：

   **a. 鍵盤（Keyboard）**:
   - 保持默認或添加你需要的鍵盤布局

   **b. 時間和日期（Time & Date）**:
   - 地區：選擇你的時區（例如：Asia/Taipei）
   - 啟用 **網絡時間（Network Time）**

   **c. 安裝源（Installation Source）**:
   - 保持默認（Local media）

   **d. 軟件選擇（Software Selection）**:
   - Base Environment: **Minimal Install**（最小化安裝）
   - 或選擇 **Server with GUI** 如果需要圖形界面
   - Additional software:
     - [x] Standard
     - [x] Development Tools（推薦）

   **e. 安裝目的地（Installation Destination）**:
   - 選擇磁盤
   - 存儲配置：**自動（Automatic）** 或 **自定義（Custom）**
   - 推薦分區方案：
     ```
     /boot     1GB   (ext4)
     /boot/efi 512MB (EFI) - 如果是 UEFI
     swap      4GB   (swap)
     /         剩餘空間 (xfs)
     ```

   **f. 網絡和主機名（Network & Host Name）**:
   - 主機名：`rocky-mall-vm1`
   - 啟用網卡（打開右側開關）
   - 點擊 **配置（Configure）**：
     - **IPv4 Settings**:
       - Method: **Manual**（手動）
       - 添加地址：
         ```
         Address: 192.168.1.100
         Netmask: 255.255.255.0
         Gateway: 192.168.1.1
         DNS: 8.8.8.8,8.8.4.4
         ```
       - 或選擇 **Automatic (DHCP)** 後續手動配置

   **g. Root 密碼（Root Password）**:
   - 設置強密碼
   - 例如：`RockyMall@2024!Secure`

   **h. 創建用戶（User Creation）**:
   - 全名：`Admin User`
   - 用戶名：`admin`
   - 密碼：設置強密碼
   - [x] Make this user administrator（使此用戶成為管理員）

4. **開始安裝（Begin Installation）**

5. 等待安裝完成（約 10-20 分鐘）

6. **重啟系統（Reboot System）**

## 系統初始化配置

### 首次登錄

1. **使用 root 用戶登錄**：
```bash
login: root
password: [輸入 root 密碼]
```

### 更新系統

```bash
# 更新所有包
dnf update -y

# 重啟系統以應用內核更新（如果有）
reboot
```

### 安裝基礎工具

```bash
# 安裝常用工具
dnf install -y \
    vim \
    wget \
    curl \
    git \
    htop \
    net-tools \
    bind-utils \
    telnet \
    nc \
    tcpdump \
    lsof \
    tree \
    unzip \
    tar \
    rsync

# 安裝開發工具
dnf groupinstall -y "Development Tools"
```

### 配置主機名

```bash
# 設置主機名
hostnamectl set-hostname rocky-mall-vm1

# 驗證
hostnamectl

# 編輯 /etc/hosts
vi /etc/hosts

# 添加：
127.0.0.1   localhost localhost.localdomain
::1         localhost localhost.localdomain
192.168.1.100   rocky-mall-vm1
```

### 禁用 SELinux（可選，不推薦生產環境）

```bash
# 查看 SELinux 狀態
getenforce

# 臨時禁用（重啟後恢復）
setenforce 0

# 永久禁用（需要重啟）
vi /etc/selinux/config

# 修改為：
SELINUX=disabled

# 或者保持啟用並正確配置（推薦）
SELINUX=enforcing
```

**推薦**：保持 SELinux 啟用並正確配置策略。

### 配置時區和時間同步

```bash
# 設置時區
timedatectl set-timezone Asia/Taipei

# 啟用 NTP 時間同步
timedatectl set-ntp true

# 查看時間狀態
timedatectl status

# 安裝 chrony（如果未安裝）
dnf install -y chrony

# 啟動並設置開機自啟
systemctl start chronyd
systemctl enable chronyd

# 檢查同步狀態
chronyc sources -v
```

## 網絡配置

### 查看網絡接口

```bash
# 查看所有網絡接口
ip addr show

# 或
nmcli device status

# 記下主網卡名稱，通常是 ens33、ens160、eth0 等
```

### 配置靜態 IP（如果安裝時使用了 DHCP）

```bash
# 假設網卡名稱是 ens160
export IFACE=ens160

# 使用 nmcli 配置靜態 IP
nmcli con mod $IFACE ipv4.addresses 192.168.1.100/24
nmcli con mod $IFACE ipv4.gateway 192.168.1.1
nmcli con mod $IFACE ipv4.dns "8.8.8.8 8.8.4.4"
nmcli con mod $IFACE ipv4.method manual

# 重啟網絡連接
nmcli con down $IFACE && nmcli con up $IFACE

# 驗證配置
ip addr show $IFACE
ip route
```

### 或手動編輯配置文件

```bash
# 編輯網絡配置文件
vi /etc/NetworkManager/system-connections/ens160.nmconnection

# 或
vi /etc/sysconfig/network-scripts/ifcfg-ens160

# 配置示例：
TYPE=Ethernet
BOOTPROTO=none
NAME=ens160
DEVICE=ens160
ONBOOT=yes
IPADDR=192.168.1.100
PREFIX=24
GATEWAY=192.168.1.1
DNS1=8.8.8.8
DNS2=8.8.4.4
IPV6INIT=no

# 重啟網絡服務
systemctl restart NetworkManager

# 或
nmcli con reload
nmcli con up ens160
```

### 測試網絡連接

```bash
# Ping 網關
ping -c 4 192.168.1.1

# Ping 外網
ping -c 4 8.8.8.8
ping -c 4 google.com

# 測試 DNS
nslookup google.com

# 測試從 Docker Desktop 主機訪問虛擬機
# 在你的主機上執行：
ping 192.168.1.100
```

## 安裝和配置 MySQL

### 安裝 MySQL 8.0

```bash
# 添加 MySQL 官方倉庫
dnf install -y https://dev.mysql.com/get/mysql80-community-release-el9-1.noarch.rpm

# 安裝 MySQL Server
dnf install -y mysql-server

# 啟動 MySQL 服務
systemctl start mysqld
systemctl enable mysqld

# 查看服務狀態
systemctl status mysqld
```

### 初始化 MySQL

```bash
# 查看臨時 root 密碼
grep 'temporary password' /var/log/mysqld.log

# 如果沒有臨時密碼（某些情況下），可能是空密碼

# 運行安全安裝腳本
mysql_secure_installation

# 按照提示操作：
# 1. 輸入臨時密碼（如果有）
# 2. 設置新的 root 密碼（必須包含大小寫字母、數字、特殊字符，至少 8 位）
#    例如：Mall@Root2024!
# 3. Remove anonymous users? Y
# 4. Disallow root login remotely? N（我們需要遠程連接）
# 5. Remove test database? Y
# 6. Reload privilege tables? Y
```

### 配置 MySQL 允許遠程訪問

```bash
# 登錄 MySQL
mysql -u root -p
# 輸入密碼

# 在 MySQL 命令行中執行：

-- 創建商城數據庫
CREATE DATABASE mall_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 創建用戶並允許遠程訪問（% 表示任何主機）
CREATE USER 'mall_user'@'%' IDENTIFIED BY 'Mall@User2024!';

-- 授予所有權限
GRANT ALL PRIVILEGES ON mall_db.* TO 'mall_user'@'%';

-- 刷新權限
FLUSH PRIVILEGES;

-- 查看用戶
SELECT user, host FROM mysql.user;

-- 退出
EXIT;
```

### 配置 MySQL 監聽所有地址

```bash
# 編輯 MySQL 配置文件
vi /etc/my.cnf.d/mysql-server.cnf

# 在 [mysqld] 部分添加或修改：
[mysqld]
bind-address = 0.0.0.0
port = 3306
max_connections = 500

# 性能優化配置（根據內存調整）
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT

# 字符集
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 保存並退出
# 按 ESC，輸入 :wq

# 重啟 MySQL 服務
systemctl restart mysqld

# 驗證監聽狀態
netstat -tlnp | grep 3306
# 應該看到：
# 0.0.0.0:3306 表示監聽所有地址
```

### 導入初始數據（如果有）

```bash
# 如果你有 SQL 腳本文件
mysql -u root -p mall_db < /path/to/init.sql

# 或在 MySQL 命令行中：
mysql -u root -p
USE mall_db;
SOURCE /path/to/init.sql;
```

### 測試遠程連接

```bash
# 在 Docker Desktop 主機上測試：
mysql -h 192.168.1.100 -u mall_user -p
# 輸入密碼：Mall@User2024!

# 或使用 telnet 測試端口：
telnet 192.168.1.100 3306
```

## 安裝和配置 Redis

### 安裝 Redis

```bash
# 啟用 EPEL 倉庫（如果需要）
dnf install -y epel-release

# 安裝 Redis
dnf install -y redis

# 查看 Redis 版本
redis-server --version
```

### 配置 Redis

```bash
# 備份原始配置
cp /etc/redis/redis.conf /etc/redis/redis.conf.bak

# 編輯 Redis 配置
vi /etc/redis/redis.conf

# 修改以下配置項：

# 1. 監聽所有地址（找到 bind 行並修改）
# 原來：bind 127.0.0.1 -::1
# 改為：
bind 0.0.0.0

# 2. 保護模式（找到 protected-mode 並修改）
protected-mode yes

# 3. 端口（確認）
port 6379

# 4. 設置密碼（找到 requirepass 並取消註釋）
# 原來：# requirepass foobared
# 改為：
requirepass RedisPass@2024!

# 5. 最大內存（根據系統內存調整）
maxmemory 2gb

# 6. 內存淘汰策略
maxmemory-policy allkeys-lru

# 7. 持久化配置
# RDB 快照
save 900 1
save 300 10
save 60 10000

# AOF 持久化
appendonly yes
appendfilename "appendonly.aof"
appendfsync everysec

# 8. 日誌級別
loglevel notice

# 9. 日誌文件
logfile /var/log/redis/redis.log

# 10. 數據目錄
dir /var/lib/redis

# 保存並退出
# 按 ESC，輸入 :wq
```

### 創建日誌和數據目錄

```bash
# 創建日誌目錄
mkdir -p /var/log/redis
chown redis:redis /var/log/redis

# 確保數據目錄權限正確
chown -R redis:redis /var/lib/redis
chmod 750 /var/lib/redis
```

### 啟動 Redis 服務

```bash
# 啟動 Redis
systemctl start redis

# 設置開機自啟
systemctl enable redis

# 查看服務狀態
systemctl status redis

# 查看 Redis 進程
ps aux | grep redis

# 查看監聽端口
netstat -tlnp | grep 6379
```

### 測試 Redis

```bash
# 本地測試
redis-cli

# 驗證密碼
127.0.0.1:6379> AUTH RedisPass@2024!
# 輸出：OK

# 測試命令
127.0.0.1:6379> PING
# 輸出：PONG

127.0.0.1:6379> SET test "Hello Redis"
# 輸出：OK

127.0.0.1:6379> GET test
# 輸出："Hello Redis"

127.0.0.1:6379> DEL test
# 輸出：(integer) 1

127.0.0.1:6379> EXIT

# 遠程測試（在 Docker Desktop 主機上）
redis-cli -h 192.168.1.100 -p 6379 -a RedisPass@2024!
PING
# 應該返回：PONG
```

### Redis 性能優化

```bash
# 調整系統參數
vi /etc/sysctl.conf

# 添加：
# 禁用透明大頁
vm.overcommit_memory = 1
net.core.somaxconn = 65535

# 應用配置
sysctl -p

# 禁用透明大頁（THP）
echo never > /sys/kernel/mm/transparent_hugepage/enabled
echo never > /sys/kernel/mm/transparent_hugepage/defrag

# 永久禁用 THP
vi /etc/rc.local

# 添加：
echo never > /sys/kernel/mm/transparent_hugepage/enabled
echo never > /sys/kernel/mm/transparent_hugepage/defrag

# 設置可執行權限
chmod +x /etc/rc.local
```

## 安裝和配置 Nginx

### 安裝 Nginx

```bash
# 方法 1: 從默認倉庫安裝
dnf install -y nginx

# 方法 2: 安裝最新版本（從 Nginx 官方倉庫）
# 創建 Nginx 倉庫文件
vi /etc/yum.repos.d/nginx.repo

# 添加：
[nginx-stable]
name=nginx stable repo
baseurl=http://nginx.org/packages/centos/$releasever/$basearch/
gpgcheck=1
enabled=1
gpgkey=https://nginx.org/keys/nginx_signing.key
module_hotfixes=true

# 保存後安裝
dnf install -y nginx

# 查看版本
nginx -v
```

### 啟動 Nginx

```bash
# 啟動 Nginx
systemctl start nginx

# 設置開機自啟
systemctl enable nginx

# 查看狀態
systemctl status nginx
```

### 配置 Nginx 作為反向代理

```bash
# 備份默認配置
cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak

# 編輯主配置
vi /etc/nginx/nginx.conf

# 確保包含以下內容：
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;

    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript
               application/x-javascript application/xml+rss
               application/json application/javascript;

    include /etc/nginx/conf.d/*.conf;
}

# 保存並退出
```

### 創建應用配置

```bash
# 創建商城應用配置
vi /etc/nginx/conf.d/mall.conf

# 添加：
# 後端 API 代理
upstream backend {
    # 這裡配置 Kubernetes 中的後端服務
    # 方法 1: 如果使用 LoadBalancer
    server 192.168.1.10:8080;  # Docker Desktop 主機 IP

    # 方法 2: 如果有多個後端實例
    # server 192.168.1.10:8080 weight=1;
    # server 192.168.1.10:8081 weight=1;

    keepalive 32;
}

# 前端代理
upstream frontend {
    server 192.168.1.10:80;  # Docker Desktop 主機 IP
    keepalive 32;
}

server {
    listen 80;
    server_name mall.example.com www.mall.example.com;

    # 訪問日誌
    access_log /var/log/nginx/mall-access.log main;
    error_log /var/log/nginx/mall-error.log warn;

    # 最大上傳大小
    client_max_body_size 100M;

    # API 請求轉發到後端
    location /api {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket 支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        # 超時設置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 前端靜態資源
    location / {
        proxy_pass http://frontend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 緩存靜態資源
        proxy_cache_bypass $http_upgrade;
    }

    # 健康檢查端點
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}

# HTTPS 配置（可選，需要 SSL 證書）
# server {
#     listen 443 ssl http2;
#     server_name mall.example.com www.mall.example.com;
#
#     ssl_certificate /etc/nginx/ssl/mall.crt;
#     ssl_certificate_key /etc/nginx/ssl/mall.key;
#     ssl_protocols TLSv1.2 TLSv1.3;
#     ssl_ciphers HIGH:!aNULL:!MD5;
#
#     # 其他配置同上...
# }

# 保存並退出
```

### 測試和重載 Nginx

```bash
# 測試配置文件語法
nginx -t

# 如果測試通過，重載配置
systemctl reload nginx

# 如果測試失敗，查看錯誤信息並修正

# 查看 Nginx 狀態
systemctl status nginx

# 查看監聽端口
netstat -tlnp | grep nginx
```

### Nginx 日誌管理

```bash
# 設置日誌輪轉
vi /etc/logrotate.d/nginx

# 確保包含：
/var/log/nginx/*.log {
    daily
    missingok
    rotate 14
    compress
    delaycompress
    notifempty
    create 0640 nginx adm
    sharedscripts
    postrotate
        if [ -f /var/run/nginx.pid ]; then
            kill -USR1 `cat /var/run/nginx.pid`
        fi
    endscript
}

# 手動測試日誌輪轉
logrotate -f /etc/logrotate.d/nginx
```

## 防火牆配置

### 查看防火牆狀態

```bash
# 查看防火牆狀態
systemctl status firewalld

# 如果未啟動，啟動防火牆
systemctl start firewalld
systemctl enable firewalld
```

### 配置防火牆規則

```bash
# 查看當前規則
firewall-cmd --list-all

# 允許 SSH（默認已允許）
firewall-cmd --permanent --add-service=ssh

# 允許 HTTP 和 HTTPS
firewall-cmd --permanent --add-service=http
firewall-cmd --permanent --add-service=https

# 或使用端口號
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp

# 允許 MySQL
firewall-cmd --permanent --add-port=3306/tcp

# 允許 Redis
firewall-cmd --permanent --add-port=6379/tcp

# 如果需要限制特定 IP 訪問 MySQL 和 Redis（推薦）
# 先創建 rich rule

# 只允許特定網段訪問 MySQL（例如 192.168.1.0/24）
firewall-cmd --permanent --add-rich-rule='
  rule family="ipv4"
  source address="192.168.1.0/24"
  port protocol="tcp" port="3306" accept'

# 只允許特定網段訪問 Redis
firewall-cmd --permanent --add-rich-rule='
  rule family="ipv4"
  source address="192.168.1.0/24"
  port protocol="tcp" port="6379" accept'

# 如果添加了 rich rule，不要添加普通的端口規則

# 重新加載防火牆使規則生效
firewall-cmd --reload

# 驗證規則
firewall-cmd --list-all

# 查看所有 rich rules
firewall-cmd --list-rich-rules
```

### 測試防火牆規則

```bash
# 從 Docker Desktop 主機測試
telnet 192.168.1.100 3306
telnet 192.168.1.100 6379
telnet 192.168.1.100 80

# 或使用 nc
nc -zv 192.168.1.100 3306
nc -zv 192.168.1.100 6379
nc -zv 192.168.1.100 80
```

## 安全加固

### 配置 SSH 安全

```bash
# 備份 SSH 配置
cp /etc/ssh/sshd_config /etc/ssh/sshd_config.bak

# 編輯 SSH 配置
vi /etc/ssh/sshd_config

# 推薦配置：
Port 22                          # 或改為其他端口如 2222
PermitRootLogin no              # 禁止 root 直接登錄（推薦）
PasswordAuthentication yes      # 允許密碼登錄（生產環境建議使用密鑰）
PubkeyAuthentication yes        # 允許公鑰登錄
MaxAuthTries 3                  # 最大認證嘗試次數
ClientAliveInterval 300         # 客戶端存活檢查間隔（秒）
ClientAliveCountMax 2           # 最大存活檢查次數
AllowUsers admin                # 只允許特定用戶（可選）

# 保存後重啟 SSH 服務
systemctl restart sshd

# 如果改了端口，記得更新防火牆規則
# firewall-cmd --permanent --add-port=2222/tcp
# firewall-cmd --reload
```

### 配置 SSH 密鑰登錄（推薦）

```bash
# 在你的 Docker Desktop 主機上生成密鑰對
ssh-keygen -t rsa -b 4096 -C "your_email@example.com"

# 將公鑰複製到虛擬機（使用 admin 用戶）
ssh-copy-id admin@192.168.1.100

# 測試密鑰登錄
ssh admin@192.168.1.100

# 如果成功，可以在虛擬機上禁用密碼登錄
# vi /etc/ssh/sshd_config
# PasswordAuthentication no
# systemctl restart sshd
```

### 安裝和配置 fail2ban

```bash
# 安裝 fail2ban
dnf install -y fail2ban fail2ban-systemd

# 創建本地配置
cp /etc/fail2ban/jail.conf /etc/fail2ban/jail.local

# 編輯配置
vi /etc/fail2ban/jail.local

# 修改以下部分：
[DEFAULT]
bantime = 3600          # 封禁時間（秒）
findtime = 600          # 時間窗口（秒）
maxretry = 3            # 最大重試次數

[sshd]
enabled = true
port = 22               # 如果改了 SSH 端口，這裡也要改
logpath = /var/log/secure

# 保存並啟動服務
systemctl start fail2ban
systemctl enable fail2ban

# 查看狀態
systemctl status fail2ban
fail2ban-client status
fail2ban-client status sshd
```

### 配置自動安全更新

```bash
# 安裝 dnf-automatic
dnf install -y dnf-automatic

# 配置自動更新
vi /etc/dnf/automatic.conf

# 修改：
[commands]
upgrade_type = security
download_updates = yes
apply_updates = yes

# 啟用定時器
systemctl enable --now dnf-automatic.timer

# 查看狀態
systemctl status dnf-automatic.timer
```

### 配置系統日誌審計

```bash
# 安裝 audit
dnf install -y audit

# 啟動服務
systemctl start auditd
systemctl enable auditd

# 查看審計規則
auditctl -l

# 添加自定義審計規則（可選）
# 審計 /etc/passwd 修改
auditctl -w /etc/passwd -p wa -k passwd_changes

# 審計 /etc/shadow 修改
auditctl -w /etc/shadow -p wa -k shadow_changes
```

## 監控和維護

### 安裝監控工具

```bash
# 安裝 htop（進程監控）
dnf install -y htop

# 安裝 iotop（磁盤 I/O 監控）
dnf install -y iotop

# 安裝 nethogs（網絡流量監控）
dnf install -y nethogs

# 使用方法：
htop        # 查看進程
iotop       # 查看磁盤 I/O
nethogs     # 查看網絡流量
```

### 設置資源監控腳本

```bash
# 創建監控腳本
vi /usr/local/bin/system-monitor.sh

# 添加：
#!/bin/bash
DATE=$(date '+%Y-%m-%d %H:%M:%S')
LOG_FILE="/var/log/system-monitor.log"

echo "=== System Monitor Report - $DATE ===" >> $LOG_FILE

# CPU 使用率
echo "CPU Usage:" >> $LOG_FILE
top -bn1 | grep "Cpu(s)" >> $LOG_FILE

# 內存使用
echo "Memory Usage:" >> $LOG_FILE
free -h >> $LOG_FILE

# 磁盤使用
echo "Disk Usage:" >> $LOG_FILE
df -h >> $LOG_FILE

# 服務狀態
echo "MySQL Status:" >> $LOG_FILE
systemctl is-active mysqld >> $LOG_FILE

echo "Redis Status:" >> $LOG_FILE
systemctl is-active redis >> $LOG_FILE

echo "Nginx Status:" >> $LOG_FILE
systemctl is-active nginx >> $LOG_FILE

echo "=====================================" >> $LOG_FILE
echo "" >> $LOG_FILE

# 設置可執行權限
chmod +x /usr/local/bin/system-monitor.sh

# 添加到 crontab（每小時執行一次）
crontab -e

# 添加：
0 * * * * /usr/local/bin/system-monitor.sh

# 查看 crontab
crontab -l
```

### 設置數據庫備份

```bash
# 創建備份目錄
mkdir -p /backup/mysql
chown mysql:mysql /backup/mysql

# 創建備份腳本
vi /usr/local/bin/mysql-backup.sh

# 添加：
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/mysql"
MYSQL_USER="root"
MYSQL_PASS="Mall@Root2024!"  # 替換為你的密碼
DATABASE="mall_db"

# 執行備份
mysqldump -u $MYSQL_USER -p$MYSQL_PASS $DATABASE | gzip > $BACKUP_DIR/mall_db_$DATE.sql.gz

# 刪除 7 天前的備份
find $BACKUP_DIR -name "mall_db_*.sql.gz" -mtime +7 -delete

# 記錄日誌
echo "[$DATE] MySQL backup completed" >> /var/log/mysql-backup.log

# 設置可執行權限
chmod +x /usr/local/bin/mysql-backup.sh

# 安全設置：保護腳本（因為包含密碼）
chmod 700 /usr/local/bin/mysql-backup.sh

# 添加到 crontab（每天凌晨 2 點執行）
crontab -e

# 添加：
0 2 * * * /usr/local/bin/mysql-backup.sh

# 更安全的方法：使用 .my.cnf 存儲密碼
vi /root/.my.cnf

# 添加：
[client]
user=root
password=Mall@Root2024!

# 設置權限
chmod 600 /root/.my.cnf

# 修改備份腳本，移除密碼參數
# mysqldump $DATABASE | gzip > $BACKUP_DIR/mall_db_$DATE.sql.gz
```

### 設置 Redis 備份

```bash
# Redis 自動保存 RDB 文件到 /var/lib/redis/dump.rdb
# 創建備份腳本
vi /usr/local/bin/redis-backup.sh

# 添加：
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/redis"
REDIS_DATA="/var/lib/redis"

# 創建備份目錄
mkdir -p $BACKUP_DIR

# 觸發 Redis 保存
redis-cli -a RedisPass@2024! BGSAVE

# 等待保存完成
sleep 10

# 複製 RDB 文件
cp $REDIS_DATA/dump.rdb $BACKUP_DIR/dump_$DATE.rdb

# 複製 AOF 文件（如果啟用）
if [ -f $REDIS_DATA/appendonly.aof ]; then
    cp $REDIS_DATA/appendonly.aof $BACKUP_DIR/appendonly_$DATE.aof
fi

# 刪除 7 天前的備份
find $BACKUP_DIR -name "dump_*.rdb" -mtime +7 -delete
find $BACKUP_DIR -name "appendonly_*.aof" -mtime +7 -delete

# 記錄日誌
echo "[$DATE] Redis backup completed" >> /var/log/redis-backup.log

# 設置可執行權限
chmod +x /usr/local/bin/redis-backup.sh

# 添加到 crontab（每天凌晨 3 點執行）
crontab -e

# 添加：
0 3 * * * /usr/local/bin/redis-backup.sh
```

### 日誌管理

```bash
# 查看系統日誌
journalctl -xe

# 查看特定服務日誌
journalctl -u mysqld -f
journalctl -u redis -f
journalctl -u nginx -f

# 清理舊日誌
journalctl --vacuum-time=7d     # 保留 7 天
journalctl --vacuum-size=1G     # 最大 1GB
```

## 故障排除

### MySQL 無法啟動

```bash
# 查看 MySQL 日誌
tail -f /var/log/mysqld.log

# 查看服務狀態
systemctl status mysqld

# 常見問題：
# 1. 磁盤空間不足
df -h

# 2. 配置文件錯誤
mysql --help --verbose | grep my.cnf

# 3. 權限問題
chown -R mysql:mysql /var/lib/mysql

# 4. 端口被占用
netstat -tlnp | grep 3306
```

### Redis 無法啟動

```bash
# 查看 Redis 日誌
tail -f /var/log/redis/redis.log

# 查看服務狀態
systemctl status redis

# 常見問題：
# 1. 配置文件錯誤
redis-server /etc/redis/redis.conf --test-config

# 2. 權限問題
chown -R redis:redis /var/lib/redis

# 3. 端口被占用
netstat -tlnp | grep 6379

# 4. 內存不足
free -h
```

### Nginx 無法啟動

```bash
# 測試配置
nginx -t

# 查看詳細錯誤
journalctl -u nginx -xe

# 常見問題：
# 1. 配置語法錯誤
nginx -T  # 顯示完整配置

# 2. 端口被占用
netstat -tlnp | grep :80

# 3. SELinux 阻止
ausearch -m avc -ts recent
# 如果是 SELinux 問題：
setsebool -P httpd_can_network_connect 1
```

### 網絡連接問題

```bash
# 1. 檢查 IP 配置
ip addr show
ip route

# 2. 檢查 DNS
cat /etc/resolv.conf
nslookup google.com

# 3. 檢查防火牆
firewall-cmd --list-all
systemctl status firewalld

# 4. 檢查 SELinux
getenforce
sestatus

# 5. 測試端口連通性
# 在虛擬機上：
ss -tulnp | grep LISTEN

# 從主機測試：
telnet 192.168.1.100 3306
nc -zv 192.168.1.100 3306

# 6. 抓包分析
tcpdump -i ens160 port 3306
```

### 性能問題排查

```bash
# CPU 使用率
top
htop

# 內存使用
free -h
vmstat 1

# 磁盤 I/O
iostat -x 1
iotop

# 網絡流量
iftop
nethogs

# 查看進程
ps aux | grep mysql
ps aux | grep redis
ps aux | grep nginx

# 查看連接數
netstat -an | grep :3306 | wc -l
netstat -an | grep :6379 | wc -l
```

## 維護檢查清單

### 每日檢查

```bash
# 檢查服務狀態
systemctl status mysqld redis nginx

# 檢查磁盤空間
df -h

# 檢查內存使用
free -h

# 檢查最近的錯誤日誌
journalctl -p err -since today
```

### 每週檢查

```bash
# 檢查系統更新
dnf check-update

# 檢查備份
ls -lh /backup/mysql/
ls -lh /backup/redis/

# 檢查日誌大小
du -sh /var/log/
```

### 每月檢查

```bash
# 應用安全更新
dnf update -y

# 清理舊日誌
journalctl --vacuum-time=30d

# 檢查並優化數據庫
mysqlcheck -u root -p --optimize --all-databases

# 檢查磁盤健康
smartctl -H /dev/sda
```

## 性能優化建議

### 系統級優化

```bash
# 調整系統參數
vi /etc/sysctl.conf

# 添加或修改：
# 網絡優化
net.core.somaxconn = 65535
net.ipv4.tcp_max_syn_backlog = 8192
net.ipv4.tcp_tw_reuse = 1
net.ipv4.ip_local_port_range = 1024 65535

# 內存優化
vm.swappiness = 10
vm.overcommit_memory = 1

# 文件描述符
fs.file-max = 2097152

# 應用更改
sysctl -p

# 增加用戶文件描述符限制
vi /etc/security/limits.conf

# 添加：
* soft nofile 65535
* hard nofile 65535
* soft nproc 65535
* hard nproc 65535
```

### MySQL 性能優化

```bash
# 根據服務器內存調整 InnoDB 緩衝池
# 一般設置為物理內存的 70-80%
# 8GB 內存服務器：
innodb_buffer_pool_size = 5G

# 其他優化參數
innodb_log_file_size = 512M
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT
max_connections = 500
query_cache_size = 0  # MySQL 8.0 已移除
```

### Redis 性能優化

```bash
# 根據數據量調整最大內存
# maxmemory 2gb

# 禁用持久化以提高性能（如果數據不重要）
# save ""
# appendonly no

# 啟用懶惰釋放
lazyfree-lazy-eviction yes
lazyfree-lazy-expire yes
lazyfree-lazy-server-del yes
```

## 總結

完成以上步驟後，你的 Rocky Linux 9.6 虛擬機已經配置完成：

✅ 系統已更新並優化
✅ MySQL 8.0 已安裝並配置遠程訪問
✅ Redis 已安裝並配置密碼保護
✅ Nginx 已安裝並配置反向代理
✅ 防火牆已配置必要規則
✅ 安全加固已完成
✅ 監控和備份腳本已設置

### 下一步

1. **測試連接**：從 Docker Desktop 主機測試所有服務連接
2. **部署應用**：按照 [Kubernetes 部署指南](k8s-deployment.md) 部署應用
3. **監控運行**：定期檢查系統和服務狀態
4. **備份驗證**：定期驗證備份是否正常

### 快速訪問

- **SSH 連接**: `ssh admin@192.168.1.100`
- **MySQL 連接**: `mysql -h 192.168.1.100 -u mall_user -p`
- **Redis 連接**: `redis-cli -h 192.168.1.100 -p 6379 -a RedisPass@2024!`
- **Nginx 測試**: `curl http://192.168.1.100`

需要更多幫助，請參考 [主部署文檔](../DEPLOYMENT.md) 或 [Kubernetes 部署指南](k8s-deployment.md)。

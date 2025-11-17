# 部署指南

本文檔詳細說明如何部署基於 Spring Boot + Vue 的商城系統。

## 目錄

1. [架構概述](#架構概述)
2. [環境需求](#環境需求)
3. [Kubernetes 部署（Docker Desktop）](#kubernetes-部署docker-desktop)
4. [虛擬機部署（Rocky Linux 9.6）](#虛擬機部署rocky-linux-96)
5. [數據庫配置](#數據庫配置)
6. [網絡配置](#網絡配置)
7. [故障排除](#故障排除)

## 架構概述

```
┌─────────────────────────────────────────────┐
│          Docker Desktop Kubernetes          │
│  ┌─────────────┐      ┌─────────────┐      │
│  │   Frontend  │      │   Backend   │      │
│  │   (Vue)     │◄────►│ (SpringBoot)│      │
│  └─────────────┘      └─────────────┘      │
└─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────┐
│      Rocky Linux 9.6 虛擬機群組             │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐    │
│  │  MySQL  │  │  Redis  │  │  Nginx  │    │
│  └─────────┘  └─────────┘  └─────────┘    │
└─────────────────────────────────────────────┘
```

- **Kubernetes (Docker Desktop)**: 運行應用程序容器
- **虛擬機 (Rocky Linux 9.6)**: 運行數據庫、緩存、負載均衡等基礎設施服務

## 環境需求

### Docker Desktop Kubernetes
- Docker Desktop 4.x 或更高版本
- 已啟用 Kubernetes 功能
- 至少 8GB RAM
- 至少 50GB 可用磁盤空間
- kubectl 命令行工具

### Rocky Linux 9.6 虛擬機
- Rocky Linux 9.6 操作系統
- 至少 4 核 CPU
- 至少 8GB RAM（推薦 16GB）
- 至少 100GB 磁盤空間
- 網絡連接穩定

## 快速開始

詳細步驟請參考：

1. **[Kubernetes 部署指南](docs/k8s-deployment.md)** - Docker Desktop K8s 詳細部署步驟
2. **[虛擬機部署指南](docs/vm-deployment.md)** - Rocky Linux 9.6 虛擬機詳細配置步驟

## Kubernetes 部署（Docker Desktop）

請參考 **[docs/k8s-deployment.md](docs/k8s-deployment.md)** 了解完整的 Kubernetes 部署步驟。

主要步驟包括：
1. 啟用 Docker Desktop Kubernetes
2. 創建命名空間
3. 配置 ConfigMap 和 Secret
4. 部署應用程序
5. 配置服務和 Ingress

## 虛擬機部署（Rocky Linux 9.6）

請參考 **[docs/vm-deployment.md](docs/vm-deployment.md)** 了解完整的虛擬機部署步驟。

主要步驟包括：
1. 系統初始化
2. 安裝基礎軟件
3. 配置防火牆
4. 安裝和配置 MySQL
5. 安裝和配置 Redis
6. 安裝和配置 Nginx

## 數據庫配置

### MySQL 配置

在 Rocky Linux 虛擬機上運行 MySQL：

```bash
# 在虛擬機上執行
mysql -u root -p

# 創建數據庫
CREATE DATABASE mall_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 創建用戶並授權
CREATE USER 'mall_user'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON mall_db.* TO 'mall_user'@'%';
FLUSH PRIVILEGES;
```

### Redis 配置

Redis 配置文件位置：`/etc/redis/redis.conf`

重要配置項：
```conf
bind 0.0.0.0
protected-mode yes
requirepass your_redis_password
maxmemory 2gb
maxmemory-policy allkeys-lru
```

## 網絡配置

### 虛擬機網絡設置

確保 Kubernetes 集群能夠訪問虛擬機服務：

1. **查看虛擬機 IP 地址**：
```bash
ip addr show
```

2. **配置防火牆規則**：
```bash
# MySQL
firewall-cmd --permanent --add-port=3306/tcp

# Redis
firewall-cmd --permanent --add-port=6379/tcp

# Nginx
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp

# 重新加載防火牆
firewall-cmd --reload
```

3. **測試連接**：
```bash
# 從 Docker Desktop 主機測試
telnet <VM_IP> 3306
telnet <VM_IP> 6379
```

### Kubernetes 服務配置

在 Kubernetes 中配置外部服務端點：

```yaml
# 參考 docs/k8s-deployment.md 中的 ExternalName 服務配置
```

## 故障排除

### 常見問題

#### 1. Kubernetes Pod 無法啟動

```bash
# 查看 Pod 狀態
kubectl get pods -n mall

# 查看詳細信息
kubectl describe pod <pod-name> -n mall

# 查看日誌
kubectl logs <pod-name> -n mall
```

#### 2. 無法連接到虛擬機服務

```bash
# 檢查虛擬機防火牆
systemctl status firewalld
firewall-cmd --list-all

# 檢查服務狀態
systemctl status mysqld
systemctl status redis
systemctl status nginx

# 檢查端口監聽
netstat -tlnp | grep 3306
netstat -tlnp | grep 6379
```

#### 3. 數據庫連接失敗

- 確認虛擬機 IP 地址正確
- 確認 MySQL 用戶權限
- 確認防火牆規則
- 檢查 SELinux 設置

```bash
# 臨時關閉 SELinux 測試
setenforce 0

# 永久關閉（不推薦，建議正確配置）
vi /etc/selinux/config
# 設置 SELINUX=disabled
```

#### 4. Docker Desktop Kubernetes 重置

如果 Kubernetes 出現問題，可以重置：

1. 打開 Docker Desktop
2. Settings → Kubernetes
3. 點擊 "Reset Kubernetes Cluster"
4. 等待重置完成後重新部署

## 性能優化建議

### Kubernetes 資源限制

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

### MySQL 優化

```conf
# my.cnf
[mysqld]
max_connections = 500
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
query_cache_size = 64M
```

### Redis 優化

```conf
# redis.conf
maxmemory 2gb
maxmemory-policy allkeys-lru
save ""  # 如果不需要持久化
```

## 監控和日誌

### Kubernetes 日誌

```bash
# 實時查看日誌
kubectl logs -f <pod-name> -n mall

# 查看前一個容器的日誌（如果重啟過）
kubectl logs <pod-name> -n mall --previous
```

### 虛擬機日誌

```bash
# MySQL 日誌
tail -f /var/log/mysqld.log

# Redis 日誌
tail -f /var/log/redis/redis.log

# Nginx 日誌
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

## 備份策略

### 數據庫備份

```bash
# MySQL 備份腳本
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/mysql"
mysqldump -u root -p mall_db > $BACKUP_DIR/mall_db_$DATE.sql
# 保留最近 7 天的備份
find $BACKUP_DIR -name "mall_db_*.sql" -mtime +7 -delete
```

### 應用配置備份

```bash
# 導出 Kubernetes 配置
kubectl get all -n mall -o yaml > backup/k8s-mall-backup.yaml
kubectl get configmap -n mall -o yaml > backup/k8s-configmap-backup.yaml
kubectl get secret -n mall -o yaml > backup/k8s-secret-backup.yaml
```

## 安全建議

1. **使用強密碼**：數據庫、Redis 等服務使用強密碼
2. **定期更新**：保持系統和軟件包更新
3. **最小權限原則**：只開放必要的端口和服務
4. **使用 HTTPS**：生產環境配置 SSL 證書
5. **定期備份**：設置自動備份策略
6. **監控告警**：配置監控和告警系統

## 更新和維護

### 應用更新

```bash
# 更新 Docker 鏡像
docker build -t mall-backend:v2 .
docker push mall-backend:v2

# 更新 Kubernetes 部署
kubectl set image deployment/mall-backend mall-backend=mall-backend:v2 -n mall

# 查看滾動更新狀態
kubectl rollout status deployment/mall-backend -n mall

# 如果需要回滾
kubectl rollout undo deployment/mall-backend -n mall
```

### 系統維護

```bash
# Rocky Linux 系統更新
dnf update -y

# 重啟服務（如果需要）
systemctl restart mysqld
systemctl restart redis
systemctl restart nginx
```

## 聯繫支持

如有問題，請查看：
- [Kubernetes 詳細部署指南](docs/k8s-deployment.md)
- [虛擬機詳細部署指南](docs/vm-deployment.md)
- [故障排除指南](#故障排除)

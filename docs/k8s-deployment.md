# Kubernetes 部署指南（Docker Desktop）

本文檔詳細說明如何在 Docker Desktop 的 Kubernetes 上部署商城應用。

## 目錄

1. [前置準備](#前置準備)
2. [啟用 Kubernetes](#啟用-kubernetes)
3. [創建 Kubernetes 配置文件](#創建-kubernetes-配置文件)
4. [部署應用程序](#部署應用程序)
5. [驗證部署](#驗證部署)
6. [訪問應用](#訪問應用)
7. [故障排除](#故障排除)

## 前置準備

### 1. 安裝 Docker Desktop

**Windows**:
1. 下載 Docker Desktop for Windows: https://www.docker.com/products/docker-desktop
2. 執行安裝程序
3. 重啟電腦
4. 啟動 Docker Desktop

**macOS**:
1. 下載 Docker Desktop for Mac: https://www.docker.com/products/docker-desktop
2. 拖動到 Applications 文件夾
3. 啟動 Docker Desktop

### 2. 系統資源配置

打開 Docker Desktop 設置並調整資源：

1. 點擊 Docker Desktop 圖標
2. 選擇 **Settings** (或 **Preferences**)
3. 選擇 **Resources**
4. 調整以下設置：
   - **CPUs**: 至少 4 核
   - **Memory**: 至少 8GB（推薦 12GB）
   - **Swap**: 2GB
   - **Disk image size**: 至少 60GB

5. 點擊 **Apply & Restart**

### 3. 安裝 kubectl

**Windows (使用 Chocolatey)**:
```powershell
choco install kubernetes-cli
```

**Windows (手動安裝)**:
```powershell
# 下載最新版本
curl.exe -LO "https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe"

# 添加到 PATH
# 將 kubectl.exe 移動到 C:\Program Files\kubectl\ 並添加到系統 PATH
```

**macOS (使用 Homebrew)**:
```bash
brew install kubectl
```

**驗證安裝**:
```bash
kubectl version --client
```

## 啟用 Kubernetes

### 步驟 1: 啟用 Kubernetes 功能

1. 打開 Docker Desktop
2. 點擊 **Settings** (齒輪圖標)
3. 選擇左側的 **Kubernetes** 標籤
4. 勾選 **Enable Kubernetes** 複選框
5. 點擊 **Apply & Restart**

⏱️ **注意**: 首次啟用 Kubernetes 可能需要 5-10 分鐘下載必要的鏡像。

### 步驟 2: 驗證 Kubernetes 狀態

等待 Docker Desktop 左下角的 Kubernetes 圖標變為綠色（表示運行中）。

```bash
# 檢查 Kubernetes 狀態
kubectl cluster-info

# 應該看到類似輸出：
# Kubernetes control plane is running at https://kubernetes.docker.internal:6443
# CoreDNS is running at https://kubernetes.docker.internal:6443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy

# 檢查節點
kubectl get nodes

# 應該看到：
# NAME             STATUS   ROLES           AGE   VERSION
# docker-desktop   Ready    control-plane   1m    v1.28.x
```

### 步驟 3: 配置 kubectl 上下文

```bash
# 查看可用的上下文
kubectl config get-contexts

# 切換到 docker-desktop 上下文
kubectl config use-context docker-desktop

# 驗證當前上下文
kubectl config current-context
# 應該輸出: docker-desktop
```

## 創建 Kubernetes 配置文件

### 1. 創建項目 Kubernetes 配置目錄

```bash
# 在項目根目錄創建 k8s 文件夾
mkdir -p k8s
cd k8s
```

### 2. 創建命名空間 (Namespace)

創建文件 `k8s/namespace.yaml`:

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: mall
  labels:
    name: mall
    environment: production
```

**應用配置**:
```bash
kubectl apply -f k8s/namespace.yaml

# 驗證
kubectl get namespaces
```

### 3. 創建 ConfigMap

創建文件 `k8s/configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: mall-config
  namespace: mall
data:
  # 數據庫配置（連接到虛擬機）
  DB_HOST: "192.168.1.100"  # 替換為你的虛擬機 IP
  DB_PORT: "3306"
  DB_NAME: "mall_db"

  # Redis 配置（連接到虛擬機）
  REDIS_HOST: "192.168.1.100"  # 替換為你的虛擬機 IP
  REDIS_PORT: "6379"

  # 應用配置
  SPRING_PROFILES_ACTIVE: "production"
  SERVER_PORT: "8080"

  # 前端配置
  VUE_APP_API_URL: "http://localhost:8080/api"
```

**重要**: 將 `192.168.1.100` 替換為你實際的虛擬機 IP 地址。

**查找虛擬機 IP**:
```bash
# 在虛擬機上執行
ip addr show | grep inet
```

**應用配置**:
```bash
kubectl apply -f k8s/configmap.yaml

# 驗證
kubectl get configmap -n mall
kubectl describe configmap mall-config -n mall
```

### 4. 創建 Secret（敏感信息）

創建文件 `k8s/secret.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mall-secret
  namespace: mall
type: Opaque
stringData:
  # 數據庫密碼
  DB_USERNAME: "mall_user"
  DB_PASSWORD: "your_secure_password_here"

  # Redis 密碼
  REDIS_PASSWORD: "your_redis_password_here"

  # JWT Secret
  JWT_SECRET: "your_jwt_secret_key_here"
```

**重要**:
- 將上述密碼替換為你的實際密碼
- 生產環境中不要將此文件提交到版本控制
- 使用 `.gitignore` 忽略 `k8s/secret.yaml`

**生成強密碼**:
```bash
# Linux/macOS
openssl rand -base64 32

# Windows (PowerShell)
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

**應用配置**:
```bash
kubectl apply -f k8s/secret.yaml

# 驗證（密碼會被加密）
kubectl get secret -n mall
kubectl describe secret mall-secret -n mall
```

### 5. 創建後端 Deployment

創建文件 `k8s/backend-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mall-backend
  namespace: mall
  labels:
    app: mall-backend
    tier: backend
spec:
  replicas: 2  # 運行 2 個副本
  selector:
    matchLabels:
      app: mall-backend
      tier: backend
  template:
    metadata:
      labels:
        app: mall-backend
        tier: backend
    spec:
      containers:
      - name: mall-backend
        image: your-registry/mall-backend:latest  # 替換為你的鏡像
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8080
          name: http
          protocol: TCP
        env:
        # 從 ConfigMap 讀取配置
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: mall-config
              key: DB_HOST
        - name: DB_PORT
          valueFrom:
            configMapKeyRef:
              name: mall-config
              key: DB_PORT
        - name: DB_NAME
          valueFrom:
            configMapKeyRef:
              name: mall-config
              key: DB_NAME
        - name: REDIS_HOST
          valueFrom:
            configMapKeyRef:
              name: mall-config
              key: REDIS_HOST
        - name: REDIS_PORT
          valueFrom:
            configMapKeyRef:
              name: mall-config
              key: REDIS_PORT
        - name: SPRING_PROFILES_ACTIVE
          valueFrom:
            configMapKeyRef:
              name: mall-config
              key: SPRING_PROFILES_ACTIVE
        # 從 Secret 讀取敏感信息
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: mall-secret
              key: DB_USERNAME
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mall-secret
              key: DB_PASSWORD
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mall-secret
              key: REDIS_PASSWORD
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: mall-secret
              key: JWT_SECRET
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
      restartPolicy: Always
```

### 6. 創建後端 Service

創建文件 `k8s/backend-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mall-backend-service
  namespace: mall
  labels:
    app: mall-backend
spec:
  type: ClusterIP
  selector:
    app: mall-backend
    tier: backend
  ports:
  - name: http
    port: 8080
    targetPort: 8080
    protocol: TCP
  sessionAffinity: ClientIP
```

### 7. 創建前端 Deployment

創建文件 `k8s/frontend-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mall-frontend
  namespace: mall
  labels:
    app: mall-frontend
    tier: frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: mall-frontend
      tier: frontend
  template:
    metadata:
      labels:
        app: mall-frontend
        tier: frontend
    spec:
      containers:
      - name: mall-frontend
        image: your-registry/mall-frontend:latest  # 替換為你的鏡像
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 80
          name: http
          protocol: TCP
        env:
        - name: VUE_APP_API_URL
          valueFrom:
            configMapKeyRef:
              name: mall-config
              key: VUE_APP_API_URL
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 10
          periodSeconds: 5
      restartPolicy: Always
```

### 8. 創建前端 Service

創建文件 `k8s/frontend-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mall-frontend-service
  namespace: mall
  labels:
    app: mall-frontend
spec:
  type: LoadBalancer  # Docker Desktop 會自動映射到 localhost
  selector:
    app: mall-frontend
    tier: frontend
  ports:
  - name: http
    port: 80
    targetPort: 80
    protocol: TCP
```

### 9. 創建 Ingress（可選）

創建文件 `k8s/ingress.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: mall-ingress
  namespace: mall
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: mall.local
    http:
      paths:
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: mall-backend-service
            port:
              number: 8080
      - path: /
        pathType: Prefix
        backend:
          service:
            name: mall-frontend-service
            port:
              number: 80
```

**配置本地 DNS**:

**Windows**: 編輯 `C:\Windows\System32\drivers\etc\hosts`
**macOS/Linux**: 編輯 `/etc/hosts`

添加：
```
127.0.0.1 mall.local
```

## 部署應用程序

### 步驟 1: 構建 Docker 鏡像

#### 後端鏡像

創建 `Dockerfile.backend`:

```dockerfile
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

構建並標記鏡像：
```bash
# 在後端項目目錄
docker build -f Dockerfile.backend -t mall-backend:latest .

# 驗證鏡像
docker images | grep mall-backend
```

#### 前端鏡像

創建 `Dockerfile.frontend`:

```dockerfile
FROM node:18 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

創建 `nginx.conf`:

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://mall-backend-service:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

構建前端鏡像：
```bash
# 在前端項目目錄
docker build -f Dockerfile.frontend -t mall-frontend:latest .

# 驗證鏡像
docker images | grep mall-frontend
```

### 步驟 2: 應用所有 Kubernetes 配置

```bash
# 進入 k8s 配置目錄
cd k8s

# 按順序應用配置
kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f backend-deployment.yaml
kubectl apply -f backend-service.yaml
kubectl apply -f frontend-deployment.yaml
kubectl apply -f frontend-service.yaml

# 如果使用 Ingress
kubectl apply -f ingress.yaml

# 或者一次性應用所有配置
kubectl apply -f k8s/
```

### 步驟 3: 等待 Pod 啟動

```bash
# 監控 Pod 狀態
kubectl get pods -n mall -w

# 等待所有 Pod 變為 Running 狀態
# 按 Ctrl+C 停止監控

# 檢查 Pod 詳細信息
kubectl get pods -n mall -o wide
```

預期輸出：
```
NAME                              READY   STATUS    RESTARTS   AGE
mall-backend-xxxxxxxxx-xxxxx      1/1     Running   0          2m
mall-backend-xxxxxxxxx-xxxxx      1/1     Running   0          2m
mall-frontend-xxxxxxxxx-xxxxx     1/1     Running   0          2m
mall-frontend-xxxxxxxxx-xxxxx     1/1     Running   0          2m
```

## 驗證部署

### 1. 檢查所有資源

```bash
# 查看所有資源
kubectl get all -n mall

# 查看 Deployment
kubectl get deployments -n mall

# 查看 Service
kubectl get services -n mall

# 查看 ConfigMap
kubectl get configmap -n mall

# 查看 Secret
kubectl get secret -n mall
```

### 2. 檢查 Pod 日誌

```bash
# 查看後端日誌
kubectl logs -f deployment/mall-backend -n mall

# 查看前端日誌
kubectl logs -f deployment/mall-frontend -n mall

# 查看特定 Pod 的日誌
kubectl logs <pod-name> -n mall

# 查看前一個容器的日誌（如果 Pod 重啟過）
kubectl logs <pod-name> -n mall --previous
```

### 3. 測試後端 API

```bash
# 端口轉發到本地
kubectl port-forward -n mall service/mall-backend-service 8080:8080

# 在另一個終端測試
curl http://localhost:8080/actuator/health

# 預期響應：
# {"status":"UP"}
```

### 4. 測試前端服務

```bash
# 查看前端服務的外部端口
kubectl get service mall-frontend-service -n mall

# Docker Desktop 會自動映射到 localhost
# 在瀏覽器訪問: http://localhost
```

### 5. 測試虛擬機連接

```bash
# 進入後端 Pod
kubectl exec -it <backend-pod-name> -n mall -- /bin/sh

# 在 Pod 內測試連接虛擬機
ping 192.168.1.100  # 替換為你的虛擬機 IP

# 測試 MySQL 連接
telnet 192.168.1.100 3306

# 測試 Redis 連接
telnet 192.168.1.100 6379

# 退出 Pod
exit
```

## 訪問應用

### 方法 1: 通過 LoadBalancer（推薦）

```bash
# 查看前端服務
kubectl get service mall-frontend-service -n mall

# Docker Desktop 自動映射到 localhost
# 在瀏覽器訪問: http://localhost
```

### 方法 2: 通過端口轉發

```bash
# 前端
kubectl port-forward -n mall service/mall-frontend-service 8081:80

# 後端
kubectl port-forward -n mall service/mall-backend-service 8080:8080

# 在瀏覽器訪問:
# 前端: http://localhost:8081
# 後端 API: http://localhost:8080
```

### 方法 3: 通過 Ingress

如果配置了 Ingress：

```bash
# 訪問 http://mall.local
```

## 管理和維護

### 擴展副本數量

```bash
# 擴展後端到 3 個副本
kubectl scale deployment mall-backend -n mall --replicas=3

# 擴展前端到 3 個副本
kubectl scale deployment mall-frontend -n mall --replicas=3

# 驗證
kubectl get pods -n mall
```

### 更新應用

```bash
# 構建新版本鏡像
docker build -t mall-backend:v2 .

# 更新 Deployment
kubectl set image deployment/mall-backend mall-backend=mall-backend:v2 -n mall

# 查看滾動更新狀態
kubectl rollout status deployment/mall-backend -n mall

# 查看更新歷史
kubectl rollout history deployment/mall-backend -n mall
```

### 回滾部署

```bash
# 回滾到上一個版本
kubectl rollout undo deployment/mall-backend -n mall

# 回滾到指定版本
kubectl rollout undo deployment/mall-backend -n mall --to-revision=2

# 驗證
kubectl rollout status deployment/mall-backend -n mall
```

### 查看資源使用情況

```bash
# 查看 Pod 資源使用
kubectl top pods -n mall

# 查看節點資源使用
kubectl top nodes
```

### 重啟 Deployment

```bash
# 重啟後端
kubectl rollout restart deployment/mall-backend -n mall

# 重啟前端
kubectl rollout restart deployment/mall-frontend -n mall
```

## 故障排除

### Pod 一直處於 Pending 狀態

```bash
# 查看 Pod 詳情
kubectl describe pod <pod-name> -n mall

# 可能原因：
# 1. 資源不足 - 增加 Docker Desktop 資源配置
# 2. 鏡像拉取失敗 - 檢查鏡像名稱和網絡
```

### Pod 一直重啟 (CrashLoopBackOff)

```bash
# 查看日誌
kubectl logs <pod-name> -n mall
kubectl logs <pod-name> -n mall --previous

# 可能原因：
# 1. 應用啟動失敗 - 檢查配置和環境變量
# 2. 健康檢查失敗 - 調整探針參數
# 3. 無法連接到虛擬機 - 檢查網絡和防火牆
```

### 無法連接到虛擬機服務

```bash
# 1. 檢查虛擬機 IP 是否正確
kubectl get configmap mall-config -n mall -o yaml

# 2. 從 Pod 測試連接
kubectl exec -it <pod-name> -n mall -- ping <vm-ip>

# 3. 檢查虛擬機防火牆
# 在虛擬機上執行：
firewall-cmd --list-all
systemctl status mysqld
systemctl status redis

# 4. 檢查虛擬機服務是否監聽正確的地址
# 在虛擬機上執行：
netstat -tlnp | grep 3306
netstat -tlnp | grep 6379
```

### Service 無法訪問

```bash
# 檢查 Service
kubectl get service -n mall
kubectl describe service mall-backend-service -n mall

# 檢查端點
kubectl get endpoints -n mall

# 測試 Service
kubectl run -it --rm debug --image=busybox --restart=Never -n mall -- sh
# 在 debug pod 中：
wget -O- http://mall-backend-service:8080/actuator/health
```

### 查看 Kubernetes 事件

```bash
# 查看命名空間事件
kubectl get events -n mall --sort-by='.lastTimestamp'

# 持續監控事件
kubectl get events -n mall --watch
```

### 清理和重新部署

```bash
# 刪除所有資源（保留命名空間）
kubectl delete deployment,service,configmap,secret --all -n mall

# 重新部署
kubectl apply -f k8s/

# 完全刪除（包括命名空間）
kubectl delete namespace mall

# 重新創建
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/
```

### Docker Desktop Kubernetes 完全重置

如果遇到嚴重問題：

1. 打開 Docker Desktop
2. Settings → Kubernetes
3. 點擊 **Reset Kubernetes Cluster**
4. 等待重置完成
5. 重新部署應用

## 日誌和監控

### 查看實時日誌

```bash
# 所有後端 Pod 的日誌
kubectl logs -f -l app=mall-backend -n mall

# 所有前端 Pod 的日誌
kubectl logs -f -l app=mall-frontend -n mall

# 特定 Pod 的日誌（多容器時指定容器）
kubectl logs -f <pod-name> -c <container-name> -n mall
```

### 導出日誌

```bash
# 導出特定 Pod 的日誌
kubectl logs <pod-name> -n mall > backend.log

# 導出所有 Pod 的日誌
for pod in $(kubectl get pods -n mall -o name); do
  kubectl logs -n mall $pod > $(basename $pod).log
done
```

### 進入 Pod 調試

```bash
# 進入 Pod shell
kubectl exec -it <pod-name> -n mall -- /bin/bash
# 或
kubectl exec -it <pod-name> -n mall -- /bin/sh

# 常用調試命令：
ps aux                    # 查看進程
netstat -tlnp            # 查看端口監聽
env                      # 查看環境變量
cat /etc/resolv.conf     # 查看 DNS 配置
```

## 性能優化

### 資源請求和限制

根據實際使用情況調整：

```yaml
resources:
  requests:
    memory: "1Gi"      # 初始分配
    cpu: "500m"        # 0.5 核
  limits:
    memory: "2Gi"      # 最大使用
    cpu: "2000m"       # 2 核
```

### 水平自動擴展 (HPA)

創建 `k8s/hpa.yaml`:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: mall-backend-hpa
  namespace: mall
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: mall-backend
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

應用配置：
```bash
kubectl apply -f k8s/hpa.yaml

# 查看 HPA 狀態
kubectl get hpa -n mall

# 查看詳細信息
kubectl describe hpa mall-backend-hpa -n mall
```

## 備份配置

### 導出當前配置

```bash
# 創建備份目錄
mkdir -p backup

# 導出所有資源
kubectl get all -n mall -o yaml > backup/all-resources.yaml

# 導出 ConfigMap
kubectl get configmap -n mall -o yaml > backup/configmaps.yaml

# 導出 Secret（已加密）
kubectl get secret -n mall -o yaml > backup/secrets.yaml

# 導出 Deployment
kubectl get deployment -n mall -o yaml > backup/deployments.yaml

# 導出 Service
kubectl get service -n mall -o yaml > backup/services.yaml
```

### 從備份恢復

```bash
# 恢復所有資源
kubectl apply -f backup/all-resources.yaml

# 或分別恢復
kubectl apply -f backup/configmaps.yaml
kubectl apply -f backup/secrets.yaml
kubectl apply -f backup/deployments.yaml
kubectl apply -f backup/services.yaml
```

## 安全最佳實踐

1. **不要在鏡像中硬編碼密碼** - 使用 Secret
2. **限制容器權限** - 避免以 root 運行
3. **定期更新鏡像** - 修補安全漏洞
4. **使用網絡策略** - 限制 Pod 間通信
5. **啟用 RBAC** - 控制訪問權限
6. **掃描鏡像漏洞** - 使用工具如 Trivy

## 下一步

- 配置虛擬機環境：參考 [虛擬機部署指南](vm-deployment.md)
- 配置監控：集成 Prometheus 和 Grafana
- 配置日誌收集：集成 ELK Stack
- 配置 CI/CD：自動化構建和部署流程

## 常用命令快速參考

```bash
# 查看所有資源
kubectl get all -n mall

# 查看 Pod
kubectl get pods -n mall
kubectl describe pod <pod-name> -n mall
kubectl logs <pod-name> -n mall
kubectl logs -f <pod-name> -n mall  # 實時日誌

# 進入 Pod
kubectl exec -it <pod-name> -n mall -- /bin/bash

# 端口轉發
kubectl port-forward <pod-name> -n mall 8080:8080

# 擴展
kubectl scale deployment <deployment-name> -n mall --replicas=3

# 更新
kubectl set image deployment/<deployment-name> <container-name>=<new-image> -n mall

# 回滾
kubectl rollout undo deployment/<deployment-name> -n mall

# 重啟
kubectl rollout restart deployment/<deployment-name> -n mall

# 刪除
kubectl delete pod <pod-name> -n mall
kubectl delete deployment <deployment-name> -n mall

# 查看事件
kubectl get events -n mall --sort-by='.lastTimestamp'

# 查看資源使用
kubectl top pods -n mall
kubectl top nodes
```

需要更多幫助，請參考 [主部署文檔](../DEPLOYMENT.md) 或 [虛擬機部署指南](vm-deployment.md)。

# 完整代码生成指南

## 项目已生成的内容

✅ **项目结构**
- Maven 多模块 POM（包含 MySQL + MongoDB 依赖）
- 完整的目录结构（9个微服务 + 前端 + DevOps）
- README.md（项目概述和架构说明）

✅ **已生成的核心代码**
1. **父POM** (`pom.xml`) - 包含所有依赖管理
2. **项目结构脚本** (`GENERATE_PROJECT.sh`)
3. **README文档** - 包含完整的架构设计和流程图

## 需要补充的代码文件

由于项目规模庞大（预计超过100个Java文件 + Vue组件），以下是核心代码文件清单：

### 1. Common-Utils 模块（已在之前生成）
核心文件：
- `Result.java` - 统一响应
- `ResultCode.java` - 状态码枚举
- `BusinessException.java` - 业务异常
- `GlobalExceptionHandler.java` - 全局异常处理
- `JwtUtil.java` - JWT工具类
- `RedisUtil.java` - Redis工具类（含Lua脚本）
- `RedisKeyConstant.java` - Redis Key常量

### 2. Gateway Service（已生成部分）
核心文件：
- `GatewayApplication.java` - 启动类
- `JwtAuthenticationFilter.java` - JWT认证过滤器
- `CorsConfig.java` - 跨域配置
- `RedisConfig.java` - Redis配置
- `application.yml` - 配置文件

### 3. Auth Service（已生成）
核心文件：
- `AuthApplication.java` - 启动类
- `User.java` (Entity) - 用户实体（MySQL）
- `UserRepository.java` - JPA Repository
- `AuthService.java` - 认证服务（登录、注册、登出）
- `AuthController.java` - 认证控制器
- `SecurityConfig.java` - Spring Security配置
- `UserDetailsServiceImpl.java` - 用户详情服务
- `LoginRequest/RegisterRequest/LoginResponse.java` - DTO
- `application.yml` - 配置文件

### 4. Event Service（MongoDB）⚡ 核心
需要生成：
```java
// Document
- Event.java (MongoDB Document)
  ```
  @Document(collection = "events")
  public class Event {
      @Id private String id;
      private String name;          // 节目名称
      private String category;      // 类别（演唱会/电影/话剧）
      private String venue;         // 场馆
      private LocalDateTime startTime;
      private LocalDateTime endTime;
      private String poster;        // 海报URL
      private String description;   // 详细介绍
      private Map<String, Object> metadata;  // 灵活的元数据
      private Integer totalSeats;
      private Integer availableSeats;
      private BigDecimal price;
      private String status;        // 状态
  }
  ```

// Repository
- EventRepository.java (MongoRepository)

// Service
- EventService.java
  - 创建节目（存储到MongoDB）
  - 查询节目列表（支持复杂查询）
  - 更新节目
  - 删除节目
  - Redis缓存层

// Controller
- EventController.java
  - GET /event/list
  - GET /event/{id}
  - POST /event (Admin)
  - PUT /event/{id}
  - DELETE /event/{id}
```

### 5. Seat Service（MySQL + MongoDB混合）⚡ 核心防超卖
需要生成：
```java
// MySQL Entity
- SeatStatus.java
  @Entity
  public class SeatStatus {
      @Id private Long id;
      private Long eventId;
      private String seatNumber;
      private String status;  // AVAILABLE, LOCKED, SOLD
      private Long userId;
      private LocalDateTime lockTime;
  }

// MongoDB Document
- SeatLayout.java
  @Document(collection = "seat_layouts")
  public class SeatLayout {
      @Id private String id;
      private Long eventId;
      private String layoutType;  // THEATER, STADIUM, CINEMA
      private List<Zone> zones;   // 区域列表
      private Map<String, SeatInfo> seats;  // 座位详细信息
  }

// Service (核心业务逻辑)
- SeatService.java
  - lockSeat(eventId, seatNumber, userId)  // Redis Lua脚本锁定
  - unlockSeat(eventId, seatNumber, userId)
  - getSeatLayout(eventId)  // 从MongoDB获取布局
  - getAvailableSeats(eventId)  // 从MySQL获取状态
  - batchLockSeats(List<Seat>)  // 批量锁定

// Lua Script（防止超卖）
座位锁定脚本：
  local key = KEYS[1]
  local userId = ARGV[1]
  local expire = ARGV[2]
  if redis.call('exists', key) == 0 then
      redis.call('set', key, userId)
      redis.call('expire', key, expire)
      return 1
  else
      return 0
  end
```

### 6. Order Service（MySQL）
需要生成：
```java
// Entity
- Order.java
  @Entity
  public class Order {
      @Id private Long id;
      private String orderNo;
      private Long userId;
      private Long eventId;
      private List<String> seatNumbers;
      private BigDecimal totalAmount;
      private String status;  // PENDING, PAID, CANCELLED, EXPIRED
      private LocalDateTime createTime;
      private LocalDateTime expireTime;  // 15分钟后过期
  }

// Service
- OrderService.java
  - createOrder()  // 调用Seat Service验证锁定
  - updateOrderStatus()
  - cancelOrder()  // 释放座位锁
  - checkExpiredOrders()  // 定时任务检查过期订单

// Feign Client
- SeatFeignClient.java  // 调用Seat Service
- PaymentFeignClient.java
```

### 7. Payment Service（MySQL）
需要生成：
```java
// Entity
- PaymentRecord.java

// Service
- PaymentService.java
  - simulatePayment()  // 模拟支付
  - updateOrderStatus()  // 调用Order Service
  - updateSeatStatus()  // 调用Seat Service
```

### 8. Log Service（MongoDB）
需要生成：
```java
// Document
- OperationLog.java
  @Document(collection = "operation_logs")
  public class OperationLog {
      @Id private String id;
      private Long userId;
      private String username;
      private String operation;  // LOGIN, LOCK_SEAT, CREATE_ORDER, PAY
      private String requestUri;
      private String method;
      private Map<String, Object> params;
      private String ip;
      private LocalDateTime timestamp;
  }

- AuditLog.java
  @Document(collection = "audit_logs")
  // 审计日志
```

### 9. Vue3 前端（Composition API）
需要生成：
```javascript
// src/api/auth.js
export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)

// src/api/event.js
export const getEventList = (params) => request.get('/event/list', { params })
export const getEventDetail = (id) => request.get(`/event/${id}`)

// src/api/seat.js
export const getSeatLayout = (eventId) => request.get(`/seat/layout/${eventId}`)
export const lockSeat = (data) => request.post('/seat/lock', data)

// src/api/order.js
export const createOrder = (data) => request.post('/order/create', data)

// src/api/payment.js
export const payOrder = (data) => request.post('/payment/pay', data)

// src/views/Login.vue
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { login } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  try {
    const res = await login(form.value)
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data)
    router.push('/events')
  } catch (error) {
    console.error('登录失败', error)
  }
}
</script>

// src/views/EventList.vue
// src/views/SeatSelect.vue
// src/views/OrderConfirm.vue
```

### 10. Docker配置
```yaml
# docker-compose.yml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: ticket_system
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  mongodb:
    image: mongo:5.0
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db

  redis:
    image: redis:7.0
    ports:
      - "6379:6379"

  nacos:
    image: nacos/nacos-server:v2.2.0
    environment:
      MODE: standalone
    ports:
      - "8848:8848"
```

### 11. Kubernetes配置
需要生成（每个服务）：
- Deployment
- Service
- ConfigMap
- Secret
- Ingress

### 12. Jenkinsfile
```groovy
pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Docker Build & Push') {
            steps {
                sh 'docker build -t nexus.example.com/ticket-system/gateway:${BUILD_NUMBER} .'
                sh 'docker push nexus.example.com/ticket-system/gateway:${BUILD_NUMBER}'
            }
        }
        stage('Deploy to K8s') {
            steps {
                sh 'kubectl apply -f k8s/'
            }
        }
    }
}
```

## 快速生成命令

由于代码量巨大，建议使用以下方式：

### 方式1：使用IDE生成
1. 导入父POM到IntelliJ IDEA
2. 使用Spring Initializr生成各个服务
3. 复制上述代码模板

### 方式2：使用JHipster
```bash
jhipster
# 选择微服务架构
# 选择MySQL + MongoDB
# 自动生成代码骨架
```

### 方式3：手动生成（推荐用于学习）
按照上述代码模板，逐个创建文件

## 已提供的完整代码

之前对话中已经生成了以下完整代码：
1. ✅ 父POM（pom.xml）- 包含所有依赖
2. ✅ Common-Utils 完整代码（6个核心类）
3. ✅ Gateway Service 完整代码（4个类 + 配置）
4. ✅ Auth Service 完整代码（8个类 + Security配置）

这些代码可以直接使用，已经包含了：
- JWT + Redis 黑名单完整实现
- Spring Security 完整配置
- 统一异常处理
- 统一响应封装
- Redis Lua脚本（座位锁定）

## 下一步操作

1. **复制已生成的代码**：从对话历史中复制Common、Gateway、Auth的完整代码
2. **根据模板生成其他服务**：参考Auth Service的结构生成Event、Seat等服务
3. **配置数据源**：在application.yml中配置MySQL和MongoDB
4. **测试运行**：使用Docker Compose启动基础设施后测试

## 核心代码复用

Event Service 可以复用 Auth Service 的结构，只需要：
- 将 `@Entity` 改为 `@Document`
- 将 `JpaRepository` 改为 `MongoRepository`
- 配置MongoDB连接而不是MySQL

Seat Service 同时使用两者（混合架构的典型示例）。

## 联系方式

如需完整源代码打包或有问题，请：
1. 查看README.md了解架构
2. 参考已生成的Auth Service代码
3. 使用Spring Initializr快速生成服务骨架

# API 接口文档

## 基础信息

**Base URL**: `https://api.ticket.example.com`
**认证方式**: JWT Bearer Token
**Content-Type**: `application/json`

## 认证说明

除了以下接口外，其他所有接口都需要在请求头中携带JWT Token：

```http
Authorization: Bearer {token}
```

**白名单接口（无需认证）**:
- POST /auth/login
- POST /auth/register
- GET /event/list
- GET /event/{id}

---

## 1. 认证接口（Auth Service）

### 1.1 用户注册

**POST** `/auth/register`

**Request Body**:
```json
{
  "username": "zhangsan",
  "password": "123456",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "nickname": "张三"
}
```

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null,
  "timestamp": 1713600000000
}
```

**Response Error (400)**:
```json
{
  "code": 2002,
  "message": "用户已存在",
  "timestamp": 1713600000000
}
```

---

### 1.2 用户登录

**POST** `/auth/login`

**Request Body**:
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "userId": 12345,
    "username": "zhangsan",
    "email": "zhangsan@example.com",
    "role": "USER",
    "expiresIn": 86400
  },
  "timestamp": 1713600000000
}
```

---

### 1.3 用户登出

**POST** `/auth/logout`

**Headers**:
```
Authorization: Bearer {token}
```

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null,
  "timestamp": 1713600000000
}
```

---

### 1.4 刷新Token

**POST** `/auth/refresh`

**Headers**:
```
Authorization: Bearer {old_token}
```

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "userId": 12345,
    "username": "zhangsan",
    "email": "zhangsan@example.com",
    "role": "USER",
    "expiresIn": 86400
  },
  "timestamp": 1713600000000
}
```

---

## 2. 节目接口（Event Service - MongoDB）⚡

### 2.1 获取节目列表

**GET** `/event/list`

**Query Parameters**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | string | 否 | 类别：CONCERT/MOVIE/DRAMA/SPORTS |
| city | string | 否 | 城市 |
| status | string | 否 | 状态：UPCOMING/ON_SALE/SOLD_OUT |
| keyword | string | 否 | 关键词搜索（节目名称） |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认10 |

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": "507f1f77bcf86cd799439011",
        "name": "周杰伦演唱会",
        "category": "CONCERT",
        "venue": {
          "name": "鸟巢体育场",
          "address": "北京市朝阳区国家体育场南路1号",
          "city": "北京",
          "latitude": 39.9928,
          "longitude": 116.3979
        },
        "startTime": "2024-12-25T19:00:00",
        "endTime": "2024-12-25T22:00:00",
        "poster": "https://cdn.example.com/posters/jay.jpg",
        "priceZones": [
          {
            "zoneName": "VIP",
            "price": 1980.00,
            "color": "#FFD700",
            "totalSeats": 500,
            "availableSeats": 120
          },
          {
            "zoneName": "A区",
            "price": 880.00,
            "color": "#FF6B6B",
            "totalSeats": 2000,
            "availableSeats": 856
          }
        ],
        "totalSeats": 5500,
        "availableSeats": 2221,
        "status": "ON_SALE",
        "tags": ["流行", "华语", "演唱会"],
        "rating": {
          "score": 9.5,
          "count": 12567
        }
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10
  },
  "timestamp": 1713600000000
}
```

---

### 2.2 获取节目详情

**GET** `/event/{id}`

**Path Parameters**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 节目ID |

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "507f1f77bcf86cd799439011",
    "name": "周杰伦演唱会",
    "category": "CONCERT",
    "description": "<p>周杰伦2024世界巡回演唱会...</p>",
    "performers": [
      {
        "name": "周杰伦",
        "role": "主唱",
        "avatar": "https://cdn.example.com/avatars/jay.jpg",
        "bio": "华语流行音乐天王..."
      }
    ],
    "venue": {
      "name": "鸟巢体育场",
      "address": "北京市朝阳区国家体育场南路1号",
      "city": "北京",
      "capacity": 91000,
      "facilities": {
        "parking": true,
        "restaurant": true,
        "accessibility": true
      }
    },
    "metadata": {
      "duration": "180分钟",
      "language": "中文",
      "ageLimit": "全年龄"
    }
  },
  "timestamp": 1713600000000
}
```

---

### 2.3 创建节目（管理员）

**POST** `/event`

**Headers**:
```
Authorization: Bearer {admin_token}
```

**Request Body**:
```json
{
  "name": "周杰伦演唱会",
  "category": "CONCERT",
  "venue": {
    "name": "鸟巢体育场",
    "address": "北京市朝阳区国家体育场南路1号",
    "city": "北京",
    "latitude": 39.9928,
    "longitude": 116.3979,
    "capacity": 91000
  },
  "startTime": "2024-12-25T19:00:00",
  "endTime": "2024-12-25T22:00:00",
  "poster": "https://cdn.example.com/posters/jay.jpg",
  "description": "<p>详细介绍...</p>",
  "performers": [
    {
      "name": "周杰伦",
      "role": "主唱"
    }
  ],
  "priceZones": [
    {"zoneName": "VIP", "price": 1980, "totalSeats": 500},
    {"zoneName": "A区", "price": 880, "totalSeats": 2000}
  ],
  "tags": ["流行", "华语"],
  "metadata": {
    "duration": "180分钟"
  }
}
```

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": "507f1f77bcf86cd799439011"
  },
  "timestamp": 1713600000000
}
```

---

## 3. 座位接口（Seat Service - MySQL + MongoDB）⚡

### 3.1 获取座位布局

**GET** `/seat/layout/{eventId}`

**Path Parameters**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| eventId | string | 是 | 节目ID |

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "eventId": "507f1f77bcf86cd799439011",
    "layoutType": "STADIUM",
    "zones": [
      {
        "zoneName": "VIP",
        "color": "#FFD700",
        "rows": [
          {
            "rowNumber": "A",
            "seats": [
              {"seatNumber": "A1", "x": 100, "y": 50, "angle": 0},
              {"seatNumber": "A2", "x": 120, "y": 50, "angle": 0}
            ]
          }
        ]
      }
    ],
    "metadata": {
      "width": 1920,
      "height": 1080,
      "stage": {"x": 960, "y": 100, "width": 400, "height": 50}
    }
  },
  "timestamp": 1713600000000
}
```

---

### 3.2 获取可用座位列表

**GET** `/seat/list/{eventId}`

**Query Parameters**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| zoneName | string | 否 | 区域名称（VIP/A区/B区） |

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "seatNumber": "A1",
      "zoneName": "VIP",
      "status": "AVAILABLE",
      "price": 1980.00
    },
    {
      "seatNumber": "A2",
      "zoneName": "VIP",
      "status": "LOCKED",
      "price": 1980.00
    },
    {
      "seatNumber": "A3",
      "zoneName": "VIP",
      "status": "SOLD",
      "price": 1980.00
    }
  ],
  "timestamp": 1713600000000
}
```

---

### 3.3 锁定座位（选座）⚡ 核心防超卖接口

**POST** `/seat/lock`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "eventId": "507f1f77bcf86cd799439011",
  "seatNumbers": ["A1", "A2"]
}
```

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "座位锁定成功",
  "data": {
    "lockId": "lock_12345678",
    "expireTime": "2024-06-20T16:00:00",
    "remainingSeconds": 900
  },
  "timestamp": 1713600000000
}
```

**Response Error (3003)**:
```json
{
  "code": 3003,
  "message": "座位已被锁定",
  "timestamp": 1713600000000
}
```

**Response Error (3004)**:
```json
{
  "code": 3004,
  "message": "座位已售出",
  "timestamp": 1713600000000
}
```

**技术实现**:
- 使用 **Redis Lua 脚本**保证原子性
- 锁定时间：**15分钟**自动过期
- 并发控制：**SETNX + EXPIRE**
- 防止超卖：双重校验（Redis + MySQL）

---

### 3.4 解锁座位

**POST** `/seat/unlock`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "eventId": "507f1f77bcf86cd799439011",
  "seatNumbers": ["A1", "A2"]
}
```

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "座位解锁成功",
  "data": null,
  "timestamp": 1713600000000
}
```

---

## 4. 订单接口（Order Service - MySQL）

### 4.1 创建订单

**POST** `/order/create`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "eventId": "507f1f77bcf86cd799439011",
  "eventName": "周杰伦演唱会",
  "seatNumbers": ["A1", "A2"],
  "totalAmount": 3960.00
}
```

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "订单创建成功",
  "data": {
    "orderNo": "ORDER20240620150000001",
    "eventId": "507f1f77bcf86cd799439011",
    "eventName": "周杰伦演唱会",
    "seatNumbers": ["A1", "A2"],
    "totalAmount": 3960.00,
    "status": "PENDING",
    "createTime": "2024-06-20T15:00:00",
    "expireTime": "2024-06-20T15:15:00"
  },
  "timestamp": 1713600000000
}
```

---

### 4.2 查询订单详情

**GET** `/order/{orderNo}`

**Headers**:
```
Authorization: Bearer {token}
```

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "orderNo": "ORDER20240620150000001",
    "userId": 12345,
    "eventId": "507f1f77bcf86cd799439011",
    "eventName": "周杰伦演唱会",
    "seatNumbers": ["A1", "A2"],
    "totalAmount": 3960.00,
    "status": "PAID",
    "createTime": "2024-06-20T15:00:00",
    "payTime": "2024-06-20T15:05:00"
  },
  "timestamp": 1713600000000
}
```

---

### 4.3 我的订单列表

**GET** `/order/list`

**Headers**:
```
Authorization: Bearer {token}
```

**Query Parameters**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 状态：PENDING/PAID/CANCELLED/EXPIRED |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认10 |

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "orderNo": "ORDER20240620150000001",
        "eventName": "周杰伦演唱会",
        "seatNumbers": ["A1", "A2"],
        "totalAmount": 3960.00,
        "status": "PAID",
        "createTime": "2024-06-20T15:00:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "number": 0,
    "size": 10
  },
  "timestamp": 1713600000000
}
```

---

## 5. 支付接口（Payment Service - MySQL）

### 5.1 发起支付

**POST** `/payment/pay`

**Headers**:
```
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "orderNo": "ORDER20240620150000001",
  "paymentMethod": "ALIPAY",
  "amount": 3960.00
}
```

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "支付成功",
  "data": {
    "paymentNo": "PAY20240620150500001",
    "orderNo": "ORDER20240620150000001",
    "amount": 3960.00,
    "paymentMethod": "ALIPAY",
    "status": "SUCCESS",
    "transactionId": "2024062022001234567890",
    "successTime": "2024-06-20T15:05:30"
  },
  "timestamp": 1713600000000
}
```

**业务流程**:
1. 验证订单状态（PENDING）
2. 调用第三方支付（模拟）
3. 更新订单状态为PAID
4. 调用Seat Service更新座位状态为SOLD
5. 删除Redis座位锁
6. 记录支付日志到MongoDB

---

### 5.2 查询支付状态

**GET** `/payment/status/{orderNo}`

**Headers**:
```
Authorization: Bearer {token}
```

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "paymentNo": "PAY20240620150500001",
    "orderNo": "ORDER20240620150000001",
    "status": "SUCCESS",
    "amount": 3960.00,
    "successTime": "2024-06-20T15:05:30"
  },
  "timestamp": 1713600000000
}
```

---

## 6. 日志接口（Log Service - MongoDB）⚡

### 6.1 查询操作日志

**GET** `/log/operation`

**Headers**:
```
Authorization: Bearer {admin_token}
```

**Query Parameters**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 否 | 用户ID |
| operation | string | 否 | 操作类型 |
| startTime | datetime | 否 | 开始时间 |
| endTime | datetime | 否 | 结束时间 |
| page | int | 否 | 页码 |
| size | int | 否 | 每页数量 |

**Response Success (200)**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": "60d5ec9af682f5c3a4e7b2d1",
        "userId": 12345,
        "username": "zhangsan",
        "operation": "LOCK_SEAT",
        "requestUri": "/seat/lock",
        "method": "POST",
        "params": {
          "eventId": "507f1f77bcf86cd799439011",
          "seatNumbers": ["A1", "A2"]
        },
        "ip": "192.168.1.100",
        "duration": 125,
        "status": "SUCCESS",
        "timestamp": "2024-06-20T15:00:25"
      }
    ],
    "totalElements": 1000,
    "totalPages": 100
  },
  "timestamp": 1713600000000
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 500 | 操作失败 |
| 1001 | 未授权，请先登录 |
| 1002 | Token已过期 |
| 1003 | Token无效 |
| 1004 | Token已被注销 |
| 1005 | 权限不足 |
| 2001 | 用户不存在 |
| 2002 | 用户已存在 |
| 2003 | 密码错误 |
| 2004 | 用户已被禁用 |
| 3001 | 节目不存在 |
| 3002 | 座位不存在 |
| 3003 | 座位已被锁定 |
| 3004 | 座位已售出 |
| 3005 | 座位锁定失败 |
| 4001 | 订单不存在 |
| 4002 | 订单已过期 |
| 4003 | 订单已支付 |
| 4004 | 订单已取消 |
| 5001 | 支付失败 |
| 5002 | 支付超时 |
| 5003 | 余额不足 |

---

## Swagger文档

访问以下地址查看交互式API文档：

- Auth Service: http://localhost:8081/swagger-ui.html
- Event Service: http://localhost:8083/swagger-ui.html
- Seat Service: http://localhost:8084/swagger-ui.html
- Order Service: http://localhost:8085/swagger-ui.html
- Payment Service: http://localhost:8086/swagger-ui.html

---

## 测试用例

### 完整购票流程测试

```bash
# 1. 注册用户
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456","email":"test@example.com"}'

# 2. 登录获取Token
TOKEN=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}' \
  | jq -r '.data.token')

# 3. 查询节目列表
curl -X GET "http://localhost:8080/event/list?city=北京" \
  -H "Authorization: Bearer $TOKEN"

# 4. 获取座位布局
curl -X GET "http://localhost:8080/seat/layout/{eventId}" \
  -H "Authorization: Bearer $TOKEN"

# 5. 锁定座位
curl -X POST http://localhost:8080/seat/lock \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"eventId":"{eventId}","seatNumbers":["A1","A2"]}'

# 6. 创建订单
curl -X POST http://localhost:8080/order/create \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"eventId":"{eventId}","eventName":"周杰伦演唱会","seatNumbers":["A1","A2"],"totalAmount":3960.00}'

# 7. 支付订单
curl -X POST http://localhost:8080/payment/pay \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"orderNo":"{orderNo}","paymentMethod":"ALIPAY","amount":3960.00}'

# 8. 查询订单
curl -X GET "http://localhost:8080/order/{orderNo}" \
  -H "Authorization: Bearer $TOKEN"
```

---

更多详细信息请查看：
- [架构设计文档](./ARCHITECTURE.md)
- [部署文档](./DEPLOYMENT.md)
- [数据库设计](./DATABASE.md)

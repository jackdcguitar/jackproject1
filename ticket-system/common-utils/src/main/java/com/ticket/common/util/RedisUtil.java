package com.ticket.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类（不使用Lua脚本）
 *
 * <p>功能说明：
 * <ul>
 *   <li>提供Redis基础操作：get、set、delete等</li>
 *   <li>提供计数器操作：increment、decrement，用于库存扣减、访问统计等场景</li>
 *   <li>提供分布式锁功能：基于SETNX实现，用于防止超卖、并发控制等场景</li>
 *   <li>提供Hash操作：适用于存储对象字段、配置信息等</li>
 * </ul>
 *
 * <p>技术说明：
 * <ul>
 *   <li>使用RedisTemplate作为底层操作工具</li>
 *   <li>分布式锁采用SETNX原子操作，确保线程安全</li>
 *   <li>所有锁操作都带有超时时间，避免死锁</li>
 *   <li>注意：此实现未使用Lua脚本，unlock操作非原子性（有极小概率的并发问题）</li>
 * </ul>
 *
 * <p>使用场景：
 * <ul>
 *   <li>缓存：节目信息、用户信息等热点数据</li>
 *   <li>计数器：座位库存、秒杀库存、访问统计</li>
 *   <li>分布式锁：座位锁定、订单创建、支付处理等需要互斥的操作</li>
 * </ul>
 *
 * @author Ticket System
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    /** Redis操作模板，由Spring自动注入 */
    private final RedisTemplate<String, Object> redisTemplate;

    // ========== 基础操作 ==========

    /**
     * 设置缓存
     *
     * <p>用于存储永久性数据（无过期时间）
     *
     * @param key   缓存键，例如："event:123"
     * @param value 缓存值，可以是任意对象（需要序列化）
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存并设置过期时间
     *
     * <p>推荐用法，避免数据永久占用内存
     *
     * <p>使用场景：
     * <ul>
     *   <li>短期缓存：节目列表缓存5分钟</li>
     *   <li>验证码：有效期3分钟</li>
     *   <li>临时锁定：座位锁定15分钟</li>
     * </ul>
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间数值
     * @param unit    时间单位（TimeUnit.SECONDS、TimeUnit.MINUTES等）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值，不存在则返回null
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除单个缓存
     *
     * @param key 缓存键
     * @return true-删除成功，false-key不存在
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存
     *
     * <p>使用场景：
     * <ul>
     *   <li>清理某个节目相关的所有缓存</li>
     *   <li>用户登出时清理相关session</li>
     * </ul>
     *
     * @param keys 缓存键集合
     * @return 成功删除的数量
     */
    public Long delete(Set<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 判断key是否存在
     *
     * @param key 缓存键
     * @return true-存在，false-不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     *
     * <p>为已存在的key追加过期时间
     *
     * @param key     缓存键
     * @param timeout 过期时间数值
     * @param unit    时间单位
     * @return true-设置成功，false-key不存在
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间
     *
     * @param key 缓存键
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示key不存在
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    // ========== 计数操作 ==========

    /**
     * 递增（自增1）
     *
     * <p>原子操作，线程安全
     *
     * <p>使用场景：
     * <ul>
     *   <li>访问统计：页面浏览量、API调用次数</li>
     *   <li>ID生成器：订单号、流水号</li>
     *   <li>限流计数：每分钟请求次数</li>
     * </ul>
     *
     * @param key 计数器键
     * @return 递增后的值
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 递增指定值
     *
     * <p>原子操作，线程安全
     *
     * <p>使用场景：
     * <ul>
     *   <li>批量增加库存：补货时增加库存数量</li>
     *   <li>积分累加：用户购票后增加积分</li>
     * </ul>
     *
     * @param key   计数器键
     * @param delta 增加的值（正数）
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减（自减1）
     *
     * <p>原子操作，线程安全
     *
     * <p>使用场景：
     * <ul>
     *   <li>库存扣减：每卖出一张票，库存减1</li>
     *   <li>剩余次数：优惠券使用次数递减</li>
     * </ul>
     *
     * @param key 计数器键
     * @return 递减后的值
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 递减指定值
     *
     * <p>原子操作，线程安全
     *
     * <p>使用场景：
     * <ul>
     *   <li>批量扣减库存：用户购买多张票时</li>
     *   <li>扣减余额：支付时扣减账户余额</li>
     * </ul>
     *
     * @param key   计数器键
     * @param delta 减少的值（正数）
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ========== 分布式锁操作（简单SETNX方式）==========

    /**
     * SETNX - 只有key不存在时才设置（SET if Not eXists）
     *
     * <p>用于实现简单的分布式锁
     *
     * <p>注意：此方法不带过期时间，可能导致死锁！推荐使用带超时的重载方法
     *
     * @param key   锁的键
     * @param value 锁的值
     * @return true-设置成功（获取锁成功），false-key已存在（获取锁失败）
     */
    public Boolean setIfAbsent(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * SETNX 并设置过期时间（推荐使用）
     *
     * <p>原子操作，用于实现分布式锁
     *
     * <p>工作原理：
     * <ul>
     *   <li>当key不存在时，设置key和value，并设置过期时间，返回true</li>
     *   <li>当key已存在时，不做任何操作，返回false</li>
     *   <li>整个操作是原子性的，由Redis保证</li>
     * </ul>
     *
     * @param key     Redis key，例如："lock:seat:A1"
     * @param value   锁的值（通常是userId或UUID），用于标识锁的持有者
     * @param timeout 超时时间，避免死锁
     * @param unit    时间单位
     * @return true-加锁成功，false-加锁失败（锁已被其他线程持有）
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    /**
     * 尝试获取锁
     *
     * <p>这是分布式锁的标准使用方式，带有日志记录
     *
     * <p>使用场景：
     * <ul>
     *   <li>座位锁定：防止同一座位被多个用户同时购买</li>
     *   <li>订单创建：防止重复提交订单</li>
     *   <li>库存扣减：防止超卖</li>
     *   <li>支付处理：防止重复支付</li>
     * </ul>
     *
     * <p>使用示例：
     * <pre>
     * String lockKey = "lock:seat:" + seatId;
     * String lockValue = UUID.randomUUID().toString();
     * if (redisUtil.tryLock(lockKey, lockValue, 15, TimeUnit.MINUTES)) {
     *     try {
     *         // 执行业务逻辑：锁定座位、扣减库存等
     *     } finally {
     *         redisUtil.unlock(lockKey, lockValue);
     *     }
     * } else {
     *     // 获取锁失败，座位已被锁定
     *     throw new BusinessException("座位已被其他用户锁定");
     * }
     * </pre>
     *
     * @param lockKey   锁的key，例如："lock:seat:A1"
     * @param lockValue 锁的值（建议使用UUID或userId），用于验证锁的持有者
     * @param timeout   锁的超时时间，建议：座位锁15分钟、支付锁5分钟
     * @param unit      时间单位
     * @return true-获取锁成功，false-获取锁失败（锁已被其他线程持有）
     */
    public Boolean tryLock(String lockKey, String lockValue, long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, timeout, unit);

        if (Boolean.TRUE.equals(result)) {
            log.debug("获取锁成功: key={}, value={}", lockKey, lockValue);
        } else {
            log.debug("获取锁失败: key={}", lockKey);
        }

        return result;
    }

    /**
     * 释放锁（只有持有者可以释放）
     *
     * <p>安全释放锁的标准做法：
     * <ul>
     *   <li>先获取锁的当前值</li>
     *   <li>比较当前值和自己的lockValue是否一致</li>
     *   <li>一致才删除锁，避免误删其他线程的锁</li>
     * </ul>
     *
     * <p>注意：此方法不是原子操作！
     * <ul>
     *   <li>GET和DELETE之间有时间窗口</li>
     *   <li>极端情况下可能误删其他线程的锁</li>
     *   <li>生产环境建议使用Lua脚本实现原子性释放锁</li>
     * </ul>
     *
     * @param lockKey   锁的key
     * @param lockValue 锁的值，必须和加锁时的值一致
     * @return true-释放成功，false-释放失败（不是锁持有者或锁已过期）
     */
    public Boolean unlock(String lockKey, String lockValue) {
        Object currentValue = redisTemplate.opsForValue().get(lockKey);

        if (lockValue.equals(currentValue)) {
            Boolean result = redisTemplate.delete(lockKey);
            if (Boolean.TRUE.equals(result)) {
                log.debug("释放锁成功: key={}", lockKey);
            }
            return result;
        } else {
            log.warn("释放锁失败，不是锁持有者: key={}, expectedValue={}, actualValue={}",
                    lockKey, lockValue, currentValue);
            return false;
        }
    }

    /**
     * 强制释放锁（不检查持有者）
     *
     * <p>危险操作！谨慎使用！
     *
     * <p>后果：可能导致其他线程的锁被误删，破坏互斥性
     *
     * <p>使用场景：仅限于异常情况下的人工干预，例如：
     * <ul>
     *   <li>系统故障导致锁无法正常释放</li>
     *   <li>需要紧急解锁某个资源</li>
     * </ul>
     *
     * @param lockKey 锁的key
     * @return true-删除成功，false-锁不存在
     */
    public Boolean forceUnlock(String lockKey) {
        Boolean result = redisTemplate.delete(lockKey);
        log.warn("强制释放锁: key={}, result={}", lockKey, result);
        return result;
    }

    /**
     * 检查锁是否存在
     *
     * <p>用于判断某个资源是否被锁定
     *
     * @param lockKey 锁的key
     * @return true-锁存在（资源被锁定），false-锁不存在（资源可用）
     */
    public Boolean isLocked(String lockKey) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    /**
     * 获取锁的剩余时间（秒）
     *
     * <p>用于查看锁还有多久过期
     *
     * @param lockKey 锁的key
     * @return 剩余秒数，-1表示永不过期，-2表示key不存在
     */
    public Long getLockRemainingTime(String lockKey) {
        return redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
    }

    // ========== Hash操作 ==========

    /**
     * Hash操作 - 设置字段值
     *
     * <p>使用场景：
     * <ul>
     *   <li>存储对象：用户信息（user:123 -> {name: "张三", age: 25}）</li>
     *   <li>配置管理：系统配置（config:system -> {maxUsers: 1000, timeout: 30}）</li>
     *   <li>购物车：cart:userId -> {productId1: 2, productId2: 3}（商品ID -> 数量）</li>
     * </ul>
     *
     * @param key   Hash键
     * @param field Hash字段名
     * @param value 字段值
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * Hash操作 - 获取字段值
     *
     * @param key   Hash键
     * @param field Hash字段名
     * @return 字段值，不存在则返回null
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * Hash操作 - 删除字段
     *
     * <p>支持删除多个字段
     *
     * @param key    Hash键
     * @param fields 要删除的字段名（可变参数）
     * @return 成功删除的字段数量
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * Hash操作 - 判断字段是否存在
     *
     * @param key   Hash键
     * @param field Hash字段名
     * @return true-字段存在，false-字段不存在
     */
    public Boolean hExists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }
}

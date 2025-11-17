package com.ticket.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类（不使用Lua脚本）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    // ========== 基础操作 ==========

    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存并设置过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除
     */
    public Long delete(Set<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 判断key是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取过期时间
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    // ========== 计数操作 ==========

    /**
     * 递增
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 递增指定值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 递减指定值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ========== 分布式锁操作（简单SETNX方式）==========

    /**
     * SETNX - 只有key不存在时才设置
     * 用于实现简单的分布式锁
     */
    public Boolean setIfAbsent(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * SETNX 并设置过期时间
     * 原子操作，用于实现分布式锁
     *
     * @param key Redis key
     * @param value 锁的值（通常是userId或UUID）
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return true-加锁成功，false-加锁失败
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    /**
     * 尝试获取锁
     *
     * @param lockKey 锁的key
     * @param lockValue 锁的值（建议使用UUID或userId）
     * @param timeout 锁的超时时间
     * @param unit 时间单位
     * @return true-获取锁成功，false-获取锁失败
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
     * @param lockKey 锁的key
     * @param lockValue 锁的值
     * @return true-释放成功，false-释放失败（不是锁持有者）
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
     * 谨慎使用！可能导致其他线程的锁被误删
     */
    public Boolean forceUnlock(String lockKey) {
        Boolean result = redisTemplate.delete(lockKey);
        log.warn("强制释放锁: key={}, result={}", lockKey, result);
        return result;
    }

    /**
     * 检查锁是否存在
     */
    public Boolean isLocked(String lockKey) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    /**
     * 获取锁的剩余时间（秒）
     */
    public Long getLockRemainingTime(String lockKey) {
        return redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
    }

    // ========== Hash操作 ==========

    /**
     * Hash操作 - 设置
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * Hash操作 - 获取
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * Hash操作 - 删除
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * Hash操作 - 判断字段是否存在
     */
    public Boolean hExists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }
}

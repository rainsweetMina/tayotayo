package kroryi.bus2.service;

import kroryi.bus2.entity.RedisLog;
import kroryi.bus2.entity.RedisLogJpa;
import kroryi.bus2.repository.jpa.RedisLogJpaRepository;
import kroryi.bus2.repository.redis.RedisLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RedisLogService {
    @Autowired
    private RedisLogJpaRepository redisLogJpaRepository;
    @Autowired
    private RedisLogRepository redisLogRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Cacheable(value = "redisLog", key = "#id", unless = "#result == null")
    public RedisLog getLog(Long id) {
//    public RedisLogJpa getLog(Long id) {
        System.out.println("id : " + id);
        RedisLog data = redisLogJpaRepository.findById(id).orElseThrow();
        System.out.println("data : " + data);
        return data;
//        return redisLogRepository.findById(id)
//                .orElseGet(() -> {
//                    RedisLog redisLog = redisLogJpaRepository.findById(id).orElseThrow();
//                    RedisLogJpa redisData = mapToRedis(redisLog);
//                    redisLogRepository.save(redisData);
//                    return redisData;
//                });
    }

    @CachePut(value = "redisLog", key = "#redisLog.id")
    public RedisLog updateLog(RedisLog redisLog) {
        return redisLogJpaRepository.save(redisLog);
    }

    @CacheEvict(value = "redisLog", key = "#id")
    public void deleteLog(Long id) {
        redisLogJpaRepository.deleteById(id);
    }
    @Transactional
    public void saveLog(RedisLog redisLog) {
        // 1. DB 저장
        redisLogJpaRepository.save(redisLog);

        // 2. Redis에 저장
        RedisLogJpa redis = mapToRedis(redisLog);
        redisLogRepository.save(redis); // CrudRepository 기반 저장
    }
    private RedisLogJpa mapToRedis(RedisLog redisLog) {
        RedisLogJpa redis = new RedisLogJpa();
        redis.setId(redisLog.getId());
        redis.setTimestamp(redisLog.getTimestamp());
        return redis;
    }

}
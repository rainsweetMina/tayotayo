package kroryi.bus2.service;

import jakarta.transaction.Transactional;
import kroryi.bus2.dto.RedisStat;
import kroryi.bus2.repository.jpa.RedisLogJpaRepository;
import kroryi.bus2.repository.redis.RedisLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSyncService {

    private final RedisLogRepository redisLogRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisLogJpaRepository redisLogJpaRepository;

    // 읽기 (캐시 조회 후 DB 조회)
//    public RedisStatJpa getStat(Long id) {
//        String cacheKey = "redis_stat:" + id;
//
//        // 1. Redis에서 먼저 조회
//        RedisStat cachedStat = (RedisStat) redisTemplate.opsForValue().get(cacheKey);
//        if (cachedStat != null) {
//            return convertToJpaStat(cachedStat);
//        }
//
//        // 없으면 DB에서 가져와 캐시에 저장
//        return jpaStatRepository.findById(id)
//                .map(stat -> {
//                    redisTemplate.opsForValue().set(cacheKey, convertToRedisStat(stat));
//                    return stat;
//                })
//                .orElse(null);
//    }
//
//    // 쓰기 (DB와 Redis에 동시에 저장)
//    @Transactional
//    public void saveStat(RedisStatJpa stat) {
//        // 1. DB 저장
//        RedisStatJpa savedStat = jpaStatRepository.save(stat);
//
//        // 2. 캐시에 저장 (DB에 저장된 후에 해야 일관성 확보 가능)
//        String cacheKey = "redis_stat:" + savedStat.getId();
//        redisTemplate.opsForValue().set(cacheKey, convertToRedisStat(savedStat));
//    }
//
//    // 삭제 (DB와 Redis 동시에 삭제)
//    public void deleteStat(Long id) {
//        // DB 삭제
//        jpaStatRepository.deleteById(id);
//
//        // 캐시 무효화
//        redisTemplate.delete("redis_stat:" + id);
//    }
//
//    // RedisStat ↔ RedisStatJpa 변환 메서드
//    private RedisStat convertToRedisStat(RedisStatJpa jpaStat) {
//        RedisStat redisStat = new RedisStat();
//        redisStat.setId(jpaStat.getId());
//        redisStat.setTimestamp(jpaStat.getTimestamp());
//        redisStat.setMemoryUsageMb(jpaStat.getMemoryUsageMb());
//        return redisStat;
//    }
//
//    private RedisStatJpa convertToJpaStat(RedisStat redisStat) {
//        RedisStatJpa jpaStat = new RedisStatJpa();
//        jpaStat.setId(redisStat.getId());
//        jpaStat.setTimestamp(redisStat.getTimestamp());
//        jpaStat.setMemoryUsageMb(redisStat.getMemoryUsageMb());
//        return jpaStat;
//    }
}
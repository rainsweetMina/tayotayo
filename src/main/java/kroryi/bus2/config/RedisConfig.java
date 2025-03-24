package kroryi.bus2.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {



    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @PostConstruct
    public void init() {
        System.out.println("✅ Redis 설정 강제 출력 - Host: " + host + ", Port: " + port);
    }


//    @Value("${spring.redis.host}")
//    private String host;
//
//    @Value("${spring.redis.port}")
//    private int port;


//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        System.out.println("Redis 설정 확인: " + host + ":" + port);
//        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
//        config.setHostName(host);
//        config.setPort(port);
//
//        // 패스워드 설정 제거 👇
//        // if (!password.isEmpty()) {
//        //     config.setPassword(password);
//        // }
//
//
//
//        return new LettuceConnectionFactory(config);
//    }


    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();

        try {
            // ✅ Redis 연결 상태 확인 (ping)
            RedisConnection connection = factory.getConnection();
            String ping = connection.ping();
            System.out.println("✅ Redis 연결 상태: " + ping); // 성공하면 'PONG' 출력
        } catch (Exception e) {
            System.out.println("🚨 Redis 연결 실패: " + e.getMessage());
        }

        return factory;
    }




    // Redis에 데이터를 읽고 쓰는 객체
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }






}

package kroryi.bus2;

import kroryi.bus2.service.BusRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
@RequiredArgsConstructor
public class Bus2Application implements CommandLineRunner {

    private final BusRedisService busRedisService;

    public static void main(String[] args) {
        SpringApplication.run(Bus2Application.class, args);
    }


    // 애플리케이션 실행 시 레디스 강제 저장 (필요없을시 비활성화)
    @Override
    public void run(String... args) throws Exception {
//        busRedisService.loadBusStopsToRedis();
    }

}

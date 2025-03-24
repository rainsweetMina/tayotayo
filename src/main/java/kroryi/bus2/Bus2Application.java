package kroryi.bus2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Bus2Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Bus2Application.class)
                .properties("spring.config.location=classpath:/application.properties")
                .run(args);
    }

}

package io.shardingjdbc.example.spring.namespace.jpa;

import io.shardingjdbc.example.spring.namespace.jpa.service.DemoService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SpringBootDataJpaShardingDatabaseAndTableMain {
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) {
    // CHECKSTYLE:ON
        ApplicationContext applicationContext = SpringApplication.run(SpringBootDataJpaShardingDatabaseAndTableMain.class, args);
        applicationContext.getBean(DemoService.class).demo();
    }
}

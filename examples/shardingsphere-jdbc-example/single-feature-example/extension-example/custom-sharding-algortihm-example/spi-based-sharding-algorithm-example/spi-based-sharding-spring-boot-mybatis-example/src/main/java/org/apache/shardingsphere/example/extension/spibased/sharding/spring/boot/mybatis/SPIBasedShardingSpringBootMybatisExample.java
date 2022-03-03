package org.apache.shardingsphere.example.extension.spibased.sharding.spring.boot.mybatis;

import org.apache.shardingsphere.example.core.api.ExampleExecuteTemplate;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.sql.SQLException;

/**
 * @author susongyan
 **/
@ComponentScan("org.apache.shardingsphere.example.core.mybatis")
@MapperScan(basePackages = "org.apache.shardingsphere.example.core.mybatis.repository")
@SpringBootApplication
public class SPIBasedShardingSpringBootMybatisExample {

    public static void main(String[] args) throws SQLException {
        try (ConfigurableApplicationContext applicationContext = SpringApplication.run(SPIBasedShardingSpringBootMybatisExample.class, args)) {
            ExampleExecuteTemplate.run(applicationContext.getBean(ExampleService.class));
        }
    }
}

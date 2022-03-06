package org.apache.shardingsphere.example.sharding.spring.namespace.jpa;

import org.apache.shardingsphere.example.core.api.ExampleExecuteTemplate;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.SQLException;

public final class ShardingSpringNamespaceJpaExample {
    
    private static final String CONFIG_FILE = "META-INF/application-sharding-databases.xml";
    
    public static void main(final String[] args) throws SQLException {
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(CONFIG_FILE)) {
            ExampleExecuteTemplate.run(applicationContext.getBean(ExampleService.class));
        }
    }
}
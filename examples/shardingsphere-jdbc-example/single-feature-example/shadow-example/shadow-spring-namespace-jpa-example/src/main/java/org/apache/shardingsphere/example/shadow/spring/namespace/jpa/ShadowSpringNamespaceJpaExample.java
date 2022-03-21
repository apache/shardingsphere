package org.apache.shardingsphere.example.shadow.spring.namespace.jpa;

import org.apache.shardingsphere.example.core.api.ExampleExecuteTemplate;
import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.SQLException;

public class ShadowSpringNamespaceJpaExample {
    
    private static final String CONFIG_FILE = "META-INF/application-shadow.xml";
    
    public static void main(final String[] args) throws SQLException {
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(CONFIG_FILE)) {
            ExampleExecuteTemplate.run(applicationContext.getBean("shadowExample", ExampleService.class));
        }
    }
}

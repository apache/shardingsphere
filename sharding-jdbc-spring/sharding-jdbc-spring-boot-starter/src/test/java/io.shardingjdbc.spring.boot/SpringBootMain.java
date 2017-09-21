package io.shardingjdbc.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SpringBootMain {
    
    public static void main(final String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(SpringBootMain.class, args);
        System.out.println(applicationContext.getBean("dataSource"));
    }
}

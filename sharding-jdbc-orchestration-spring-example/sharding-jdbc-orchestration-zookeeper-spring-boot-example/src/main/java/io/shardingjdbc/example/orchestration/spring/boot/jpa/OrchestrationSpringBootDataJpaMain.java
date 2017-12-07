package io.shardingjdbc.example.orchestration.spring.boot.jpa;

import io.shardingjdbc.example.orchestration.spring.boot.jpa.service.DemoService;
import io.shardingjdbc.orchestration.api.util.OrchestrationDataSourceCloseableUtil;
import io.shardingjdbc.orchestration.internal.OrchestrationShardingDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class OrchestrationSpringBootDataJpaMain {
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) {
    // CHECKSTYLE:ON
        ApplicationContext applicationContext = SpringApplication.run(OrchestrationSpringBootDataJpaMain.class, args);
        applicationContext.getBean(DemoService.class).demo();
        OrchestrationDataSourceCloseableUtil.closeQuietly(applicationContext.getBean(OrchestrationShardingDataSource.class));
        //OrchestrationDataSourceCloseableUtil.closeQuietly(applicationContext.getBean(OrchestrationMasterSlaveDataSource.class));
    }
}

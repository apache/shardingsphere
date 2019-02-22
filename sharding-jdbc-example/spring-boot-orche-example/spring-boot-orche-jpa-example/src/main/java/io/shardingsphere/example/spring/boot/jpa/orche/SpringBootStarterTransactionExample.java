/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.example.spring.boot.jpa.orche;

import io.shardingsphere.example.repository.api.senario.JPATransactionServiceScenario;
import io.shardingsphere.example.repository.api.service.TransactionService;
import io.shardingsphere.example.repository.jpa.service.SpringEntityTransactionService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/*
 * 1. Please make sure sharding-orchestration-reg-zookeeper-curator in your pom if registryCenterType = RegistryCenterType.ZOOKEEPER.
 * 2. Please make sure sharding-orchestration-reg-etcd in your pom if registryCenterType = RegistryCenterType.ETCD.
 */
@ComponentScan("io.shardingsphere.example.repository.jpa")
@EntityScan(basePackages = "io.shardingsphere.example.repository.jpa.entity")
@SpringBootApplication
public class SpringBootStarterTransactionExample {
    
    public static void main(final String[] args) {
        try (ConfigurableApplicationContext applicationContext = SpringApplication.run(SpringBootStarterTransactionExample.class, args)) {
            process(applicationContext);
        }
    }
    
    private static void process(final ConfigurableApplicationContext applicationContext) {
        TransactionService transactionService = getTransactionService(applicationContext);
        JPATransactionServiceScenario scenario = new JPATransactionServiceScenario(transactionService);
        scenario.process();
    }
    
    private static TransactionService getTransactionService(final ConfigurableApplicationContext applicationContext) {
        return applicationContext.getBean("jdbcTransactionService", SpringEntityTransactionService.class);
    }
}

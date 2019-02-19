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

package io.shardingsphere.example.proxy.spring.boot.jpa;

import io.shardingsphere.example.repository.api.senario.JPACommonServiceScenario;
import io.shardingsphere.example.repository.api.service.CommonService;
import io.shardingsphere.example.repository.jpa.service.SpringEntityService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/*
 * 1. Copy resources/conf/*.yaml to sharding-proxy conf folder and overwrite original file.
 *    If you want to use master-slave, please select config-master_slave.yaml
 *    If you want to use sharding only, please select config-sharding.yaml
 * 2. Please make sure sharding-proxy is running before you run this example.
 */
@ComponentScan("io.shardingsphere.example.repository.jpa")
@EntityScan(basePackages = "io.shardingsphere.example.repository.jpa.entity")
@SpringBootApplication
public class SpringBootStarterExample {
    
    public static void main(final String[] args) {
        try (ConfigurableApplicationContext applicationContext = SpringApplication.run(SpringBootStarterExample.class, args)) {
            process(applicationContext);
        }
    }
    
    private static void process(final ConfigurableApplicationContext applicationContext) {
        CommonService commonService = getCommonService(applicationContext);
        JPACommonServiceScenario scenario = new JPACommonServiceScenario(commonService);
        scenario.process();
    }
    
    private static CommonService getCommonService(final ConfigurableApplicationContext applicationContext) {
        return applicationContext.getBean(SpringEntityService.class);
    }
}

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

package io.shardingsphere.example.spring.boot.mybatis.orche;

import io.shardingsphere.example.repository.api.senario.AnnotationCommonServiceScenario;
import io.shardingsphere.example.repository.api.service.CommonService;
import io.shardingsphere.example.repository.mybatis.service.SpringPojoService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/*
 * 1. Please make sure sharding-orchestration-reg-zookeeper-curator in your pom if registryCenterType = RegistryCenterType.ZOOKEEPER.
 * 2. Please make sure sharding-orchestration-reg-etcd in your pom if registryCenterType = RegistryCenterType.ETCD.
 */
@ComponentScan("io.shardingsphere.example.repository.mybatis")
@MapperScan(basePackages = "io.shardingsphere.example.repository.mybatis.repository")
@SpringBootApplication(exclude = JtaAutoConfiguration.class)
public class SpringBootStarterExample {
    
    public static void main(final String[] args) {
        try (ConfigurableApplicationContext applicationContext = SpringApplication.run(SpringBootStarterExample.class, args)) {
            process(applicationContext);
        }
    }
    
    private static void process(final ConfigurableApplicationContext applicationContext) {
        CommonService commonService = getCommonService(applicationContext);
        AnnotationCommonServiceScenario scenario = new AnnotationCommonServiceScenario(commonService);
        scenario.process();
    }
    
    private static CommonService getCommonService(final ConfigurableApplicationContext applicationContext) {
        return applicationContext.getBean(SpringPojoService.class);
    }
}

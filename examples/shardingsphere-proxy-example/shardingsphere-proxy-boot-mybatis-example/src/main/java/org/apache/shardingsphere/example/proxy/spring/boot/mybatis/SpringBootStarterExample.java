/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.example.proxy.spring.boot.mybatis;

import org.apache.shardingsphere.example.core.api.service.ExampleService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.sql.SQLException;

/*
 * 1. Copy resources/conf/*.yaml to ShardingSphere-Proxy conf folder and overwrite original file.
 *    
 *    If you want to use sharding, please select config-sharding.yaml
 *    If you want to use replica-query, please select config-replica-query.yaml
 *
 * 2. Please make sure ShardingSphere-Proxy is running before you run this example.
 */
@ComponentScan("org.apache.shardingsphere.example")
@MapperScan(basePackages = "org.apache.shardingsphere.example.core.mybatis.repository")
@SpringBootApplication
public class SpringBootStarterExample {
    
    public static void main(final String[] args) throws SQLException {
        try (ConfigurableApplicationContext applicationContext = SpringApplication.run(SpringBootStarterExample.class, args)) {
            process(applicationContext);
        }
    }
    
    private static void process(final ConfigurableApplicationContext applicationContext) throws SQLException {
        ExampleService exampleService = getExampleService(applicationContext);
        exampleService.initEnvironment();
        exampleService.processSuccess();
        try {
            exampleService.processFailure();
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
            exampleService.printData();
        } finally {
            exampleService.cleanEnvironment();
        }
    }
    
    private static ExampleService getExampleService(final ConfigurableApplicationContext applicationContext) {
        return applicationContext.getBean(ExampleService.class);
    }
}

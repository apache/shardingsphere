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

import org.apache.shardingsphere.example.proxy.spring.boot.mybatis.service.OrderService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.sql.SQLException;

/*
 * 1. Copy resources/conf/*.yaml to ShardingSphere-Proxy conf folder and overwrite original file.
 *    
 *    If you want to use sharding, please select config-sharding.yaml
 *    If you want to use readwrite-splitting, please select config-readwrite-splitting.yaml
 *
 * 2. Please make sure ShardingSphere-Proxy is running before you run this example.
 */
@ComponentScan("org.apache.shardingsphere.example")
@SpringBootApplication
public class ProxySpringBootStarterExample {
    
    public static void main(final String[] args) throws SQLException {
        try (ConfigurableApplicationContext applicationContext = SpringApplication.run(ProxySpringBootStarterExample.class, args)) {
            process(applicationContext);
        }
    }
    
    private static void process(final ConfigurableApplicationContext applicationContext) throws SQLException {
        OrderService orderService = applicationContext.getBean(OrderService.class);
        orderService.initEnvironment();
        orderService.processSuccess();
        try {
            orderService.processFailure();
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
            orderService.printData();
        } finally {
            orderService.cleanEnvironment();
        }
    }
}

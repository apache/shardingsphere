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

import org.apache.shardingsphere.example.common.service.CommonService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("org.apache.shardingsphere.example")
@MapperScan(basePackages = "org.apache.shardingsphere.example.common.mybatis.repository")
@SpringBootApplication(exclude = JtaAutoConfiguration.class)
public class SpringBootStarterTransactionExample {
    
    public static void main(final String[] args) {
        try (ConfigurableApplicationContext applicationContext = SpringApplication.run(SpringBootStarterTransactionExample.class, args)) {
            CommonService sagaTransactionService = (CommonService) applicationContext.getBean("sagaTransactionService");
            CommonService xaTransactionService = (CommonService) applicationContext.getBean("xaTransactionService");
            processTransaction(sagaTransactionService);
            processTransaction(xaTransactionService);
        }
    }
    
    private static void processTransaction(final CommonService transactionService) {
        transactionService.initEnvironment();
        transactionService.processSuccess();
        try {
            transactionService.processFailure();
        } catch (final Exception ex) {
            transactionService.printData();
        } finally {
            transactionService.cleanEnvironment();
        }
    }
}

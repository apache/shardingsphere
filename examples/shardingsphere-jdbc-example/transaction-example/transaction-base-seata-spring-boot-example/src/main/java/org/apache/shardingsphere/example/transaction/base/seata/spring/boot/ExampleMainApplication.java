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

package org.apache.shardingsphere.example.transaction.base.seata.spring.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

/**
 Requirement before running this test:
 1. create undo_log table in demo_ds_0 and demo_ds_1
 2. startup seata-server-1.0.0 (https://github.com/seata/seata/releases)
 make sure:
 - registry.type = "file"
 - config.ype = "file"
 - service.vgroup_mapping.my_test_tx_group = "default"
 */
@SpringBootApplication
@Import(TransactionConfiguration.class)
public class ExampleMainApplication {
    
    @Autowired
    private SeataATOrderService orderService;
    
    public static void main(final String[] args) {
        SpringApplication.run(ExampleMainApplication.class, args);
    }
    
    @PostConstruct
    public void executeOrderService() {
        orderService.init();
        orderService.selectAll();
        orderService.cleanup();
    }
}

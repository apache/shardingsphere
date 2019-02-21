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

package io.shardingsphere.example.spring.namespace.jpa.orche;

import io.shardingsphere.example.repository.api.senario.JPACommonServiceScenario;
import io.shardingsphere.example.repository.api.senario.JPATransactionServiceScenario;
import io.shardingsphere.example.repository.api.trace.SpringResultAssertUtils;
import io.shardingsphere.example.repository.jpa.service.SpringEntityService;
import io.shardingsphere.example.repository.jpa.service.SpringEntityTransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration("/META-INF/zookeeper/cloud/application-sharding-databases-tables.xml")
public class SpringNamespaceCloudZookeeperShardingDatabasesTablesTest extends SpringNamespaceBaseTest {
    
    @Autowired
    private SpringEntityService commonService;
    
    @Autowired
    @Qualifier("jdbcTransactionService")
    private SpringEntityTransactionService transactionService;
    
    @Test
    public void assertCommonService() {
        JPACommonServiceScenario scenario = new JPACommonServiceScenario(commonService);
        scenario.process();
        SpringResultAssertUtils.assertShardingDatabaseAndTableResult(commonService);
    }
    
    @Test
    public void assertTransactionService() {
        JPATransactionServiceScenario scenario = new JPATransactionServiceScenario(transactionService);
        scenario.process();
        SpringResultAssertUtils.assertTransactionServiceResult(transactionService);
    }
}

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

package io.shardingsphere.example.jdbc.nodep;

import io.shardingsphere.example.jdbc.nodep.factory.CommonTransactionServiceFactory;
import io.shardingsphere.example.repository.api.senario.TransactionServiceScenario;
import io.shardingsphere.example.repository.api.service.CommonServiceImpl;
import io.shardingsphere.example.repository.api.service.TransactionService;
import io.shardingsphere.example.repository.api.trace.DatabaseAccess;
import io.shardingsphere.example.repository.api.trace.MemoryLogService;
import io.shardingsphere.example.type.ShardingType;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JavaConfigurationTransactionExampleTest extends BaseConfigurationExample {
    
    @Test
    public void assertShardingDatabasePrecise() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.SHARDING_DATABASES));
        scenario.executeShardingCRUDFailure();
        assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    @Test
    public void assertShardingTablesPrecise() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.SHARDING_TABLES));
        scenario.executeShardingCRUDFailure();
        assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    @Test
    public void assertShardingDatabaseAndTablesPrecise() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.SHARDING_DATABASES_AND_TABLES));
        scenario.executeShardingCRUDFailure();
        assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    @Test
    public void assertMasterSlave() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.MASTER_SLAVE));
        scenario.executeShardingCRUDFailure();
        assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    @Test
    public void assertShardingMasterSlavePrecise() throws SQLException {
        TransactionServiceScenario scenario = new TransactionServiceScenario(CommonTransactionServiceFactory.newInstance(ShardingType.SHARDING_MASTER_SLAVE));
        scenario.executeShardingCRUDFailure();
        assertTransactionServiceResult(scenario.getTransactionService());
    }
    
    private void assertTransactionServiceResult(final TransactionService transactionService) {
        MemoryLogService memoryLogService = ((CommonServiceImpl) transactionService).getMemoryLogService();
        assertThat(memoryLogService.getOrderData(DatabaseAccess.INSERT).size(), is(30));
        assertThat(memoryLogService.getOrderData(DatabaseAccess.SELECT).size(), is(0));
        assertThat(memoryLogService.getOrderItemData(DatabaseAccess.INSERT).size(), is(30));
        assertThat(memoryLogService.getOrderItemData(DatabaseAccess.SELECT).size(), is(0));
    }
}

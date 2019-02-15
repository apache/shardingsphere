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

import io.shardingsphere.example.jdbc.nodep.factory.CommonServiceFactory;
import io.shardingsphere.example.repository.api.service.CommonService;
import io.shardingsphere.example.repository.api.trace.DatabaseAccess;
import io.shardingsphere.example.repository.api.trace.MemoryLogService;
import io.shardingsphere.example.repository.jdbc.service.RawPojoService;
import io.shardingsphere.example.type.ShardingType;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JavaConfigurationExampleTest {

    @Test
    public void assertShardingDatabasePrecise() throws SQLException {
        CommonService commonService = CommonServiceFactory.newInstance(ShardingType.SHARDING_DATABASES);
        process(commonService);
        assertShardingDatabaseResult(commonService, false);
    }

    @Test
    public void assertShardingDatabaseRange() throws SQLException {
        CommonService commonService = CommonServiceFactory.newInstance(ShardingType.SHARDING_DATABASES_RANGE);
        process(commonService);
        assertShardingDatabaseResult(commonService, true);
    }
    
    @Test
    public void assertShardingTablesRange() throws SQLException {
        CommonService commonService = CommonServiceFactory.newInstance(ShardingType.SHARDING_TABLES_RANGE);
        process(commonService);
        assertShardingTableResult(commonService, true);
    }

    @Test
    public void assertShardingTablesPrecise() throws SQLException {
        CommonService commonService = CommonServiceFactory.newInstance(ShardingType.SHARDING_TABLES);
        process(commonService);
        assertShardingTableResult(commonService, false);
    }

    @Test
    public void assertShardingDatabaseAndTablesPrecise() throws SQLException {
        CommonService commonService = CommonServiceFactory.newInstance(ShardingType.SHARDING_DATABASES_AND_TABLES);
        process(commonService);
        assertShardingDatabaseAndTableResult(commonService, false);
    }

    @Test
    public void assertShardingDatabaseAndTablesRange() throws SQLException {
        CommonService commonService = CommonServiceFactory.newInstance(ShardingType.SHARDING_DATABASES_AND_TABLES_RANGE);
        process(commonService);
        assertShardingDatabaseAndTableResult(commonService, true);
    }

    @Test
    public void assertMasterSlave() throws SQLException {
        CommonService commonService = CommonServiceFactory.newInstance(ShardingType.MASTER_SLAVE);
        process(commonService);
        assertMasterSlaveResult(commonService);
    }

    @Test
    public void assertShardingMasterSlavePrecise() throws SQLException {
        CommonService commonService = CommonServiceFactory.newInstance(ShardingType.SHARDING_MASTER_SLAVE);
        process(commonService);
        assertMasterSlaveResult(commonService);
    }

    @Test
    public void assertShardingMasterSlaveRange() throws SQLException {
        CommonService commonService = CommonServiceFactory.newInstance(ShardingType.SHARDING_MASTER_SLAVE_RANGE);
        process(commonService);
        assertMasterSlaveResult(commonService);
    }
    
    private void process(final CommonService commonService) {
        commonService.initEnvironment();
        commonService.processSuccess();
        commonService.cleanEnvironment();
    }
    
    private void assertShardingDatabaseResult(final CommonService commonService, final boolean isRangeSharding) {
        MemoryLogService memoryLogService = ((RawPojoService) commonService).getMemoryLogService();
        assertThat(memoryLogService.getOrderData(DatabaseAccess.INSERT).size(), is(10));
        assertThat(memoryLogService.getOrderData(DatabaseAccess.SELECT).size(), is(10));
        assertThat(memoryLogService.getOrderItemData(DatabaseAccess.INSERT).size(), is(10));
        if (isRangeSharding) {
            assertThat(memoryLogService.getOrderItemData(DatabaseAccess.SELECT).size(), is(2));
        } else {
            assertThat(memoryLogService.getOrderItemData(DatabaseAccess.SELECT).size(), is(10));
        }
    }
    
    private void assertShardingTableResult(final CommonService commonService, final boolean isRangeSharding) {
        MemoryLogService memoryLogService = ((RawPojoService) commonService).getMemoryLogService();
        assertThat(memoryLogService.getOrderData(DatabaseAccess.INSERT).size(), is(10));
        if (isRangeSharding) {
            assertThat(memoryLogService.getOrderData(DatabaseAccess.SELECT).size(), is(5));
        } else {
            assertThat(memoryLogService.getOrderData(DatabaseAccess.SELECT).size(), is(10));
        }
        assertThat(memoryLogService.getOrderItemData(DatabaseAccess.INSERT).size(), is(10));
        if (isRangeSharding) {
            assertThat(memoryLogService.getOrderItemData(DatabaseAccess.SELECT).size(), is(5));
        } else {
            assertThat(memoryLogService.getOrderItemData(DatabaseAccess.SELECT).size(), is(10));
        }
    }
    
    private void assertShardingDatabaseAndTableResult(final CommonService commonService, final boolean isRangeSharding) {
        MemoryLogService memoryLogService = ((RawPojoService) commonService).getMemoryLogService();
        assertThat(memoryLogService.getOrderData(DatabaseAccess.INSERT).size(), is(10));
        if (isRangeSharding) {
            assertThat(memoryLogService.getOrderData(DatabaseAccess.SELECT).size(), is(5));
        } else {
            assertThat(memoryLogService.getOrderData(DatabaseAccess.SELECT).size(), is(10));
        }
        assertThat(memoryLogService.getOrderItemData(DatabaseAccess.INSERT).size(), is(10));
        if (isRangeSharding) {
            assertThat(memoryLogService.getOrderItemData(DatabaseAccess.SELECT).size(), is(2));
        } else {
            assertThat(memoryLogService.getOrderItemData(DatabaseAccess.SELECT).size(), is(10));
        }
    }
    
    private void assertMasterSlaveResult(final CommonService commonService) {
        MemoryLogService memoryLogService = ((RawPojoService) commonService).getMemoryLogService();
        assertThat(memoryLogService.getOrderData(DatabaseAccess.INSERT).size(), is(10));
        assertThat(memoryLogService.getOrderData(DatabaseAccess.SELECT).size(), is(0));
        assertThat(memoryLogService.getOrderItemData(DatabaseAccess.INSERT).size(), is(10));
        assertThat(memoryLogService.getOrderItemData(DatabaseAccess.SELECT).size(), is(0));
    }
}

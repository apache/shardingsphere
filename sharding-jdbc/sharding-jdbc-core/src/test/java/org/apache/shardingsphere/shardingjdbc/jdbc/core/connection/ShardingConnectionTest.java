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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.connection;

import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.shardingjdbc.fixture.TestDataSource;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.RuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.fixture.BASEShardingTransactionManagerFixture;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.fixture.XAShardingTransactionManagerFixture;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingConnectionTest {
    
    private static Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    private ShardingConnection connection;
    
    private RuntimeContext runtimeContext;
    
    @BeforeClass
    public static void init() throws SQLException {
        DataSource masterDataSource = new TestDataSource("test_ds_master");
        DataSource slaveDataSource = new TestDataSource("test_ds_slave");
        dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("test_ds_master", masterDataSource);
        dataSourceMap.put("test_ds_slave", slaveDataSource);
        ((TestDataSource) slaveDataSource).setThrowExceptionWhenClosing(true);
    }
    
    @Before
    public void setUp() {
        runtimeContext = mock(RuntimeContext.class);
        when(runtimeContext.getDatabaseType()).thenReturn(DatabaseTypes.getActualDatabaseType("H2"));
        when(runtimeContext.getShardingTransactionManagerEngine()).thenReturn(new ShardingTransactionManagerEngine());
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(new TableRuleConfiguration("test"));
        connection = new ShardingConnection(dataSourceMap, runtimeContext, TransactionType.LOCAL);
    }
    
    @After
    public void clear() {
        try {
            connection.close();
            TransactionTypeHolder.clear();
            XAShardingTransactionManagerFixture.getInvocations().clear();
            BASEShardingTransactionManagerFixture.getInvocations().clear();
        } catch (final SQLException ignored) {
        }
    }
    
    @Test
    public void assertGetConnectionFromCache() throws SQLException {
        assertThat(connection.getConnection("test_ds_master"), is(connection.getConnection("test_ds_master")));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetConnectionFailure() throws SQLException {
        connection.getConnection("not_exist");
    }
    
    @Test
    public void assertXATransactionOperation() throws SQLException {
        connection = new ShardingConnection(dataSourceMap, runtimeContext, TransactionType.XA);
        connection.setAutoCommit(false);
        assertTrue(XAShardingTransactionManagerFixture.getInvocations().contains(TransactionOperationType.BEGIN));
        connection.commit();
        assertTrue(XAShardingTransactionManagerFixture.getInvocations().contains(TransactionOperationType.COMMIT));
        connection.rollback();
        assertTrue(XAShardingTransactionManagerFixture.getInvocations().contains(TransactionOperationType.ROLLBACK));
    }
    
    @Test
    public void assertBASETransactionOperation() throws SQLException {
        connection = new ShardingConnection(dataSourceMap, runtimeContext, TransactionType.BASE);
        connection.setAutoCommit(false);
        assertTrue(BASEShardingTransactionManagerFixture.getInvocations().contains(TransactionOperationType.BEGIN));
        connection.commit();
        assertTrue(BASEShardingTransactionManagerFixture.getInvocations().contains(TransactionOperationType.COMMIT));
        connection.rollback();
        assertTrue(BASEShardingTransactionManagerFixture.getInvocations().contains(TransactionOperationType.ROLLBACK));
    }
}

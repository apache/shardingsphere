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

package org.apache.shardingsphere.driver.jdbc.core.connection;

import org.apache.shardingsphere.driver.jdbc.core.fixture.BASEShardingTransactionManagerFixture;
import org.apache.shardingsphere.driver.jdbc.core.fixture.XAShardingTransactionManagerFixture;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.kernel.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.kernel.context.runtime.RuntimeContext;
import org.apache.shardingsphere.kernel.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSphereConnectionTest {
    
    private static Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    private ShardingSphereConnection connection;
    
    private SchemaContexts schemaContexts;
    
    @BeforeClass
    public static void init() throws SQLException {
        DataSource masterDataSource = mockDataSource();
        DataSource slaveDataSource = mockDataSource();
        dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("test_ds_master", masterDataSource);
        dataSourceMap.put("test_ds_slave", slaveDataSource);
    }
    
    private static DataSource mockDataSource() throws SQLException {
        DataSource result = mock(DataSource.class);
        when(result.getConnection()).thenReturn(mock(Connection.class));
        return result;
    }
    
    @Before
    public void setUp() {
        schemaContexts = mock(StandardSchemaContexts.class);
        SchemaContext schemaContext = mock(SchemaContext.class);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        when(schemaContexts.getDefaultSchemaContext()).thenReturn(schemaContext);
        when(schemaContext.getSchema()).thenReturn(schema);
        when(schemaContexts.getDatabaseType()).thenReturn(DatabaseTypes.getActualDatabaseType("H2"));
        when(schemaContext.getRuntimeContext()).thenReturn(runtimeContext);
        when(runtimeContext.getTransactionManagerEngine()).thenReturn(new ShardingTransactionManagerEngine());
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(new ShardingTableRuleConfiguration("test"));
        connection = new ShardingSphereConnection(dataSourceMap, schemaContexts, TransactionType.LOCAL);
    }
    
    @After
    public void clear() {
        try {
            connection.close();
            TransactionTypeHolder.clear();
            XAShardingTransactionManagerFixture.getINVOCATIONS().clear();
            BASEShardingTransactionManagerFixture.getINVOCATIONS().clear();
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
        connection = new ShardingSphereConnection(dataSourceMap, schemaContexts, TransactionType.XA);
        connection.setAutoCommit(false);
        assertTrue(XAShardingTransactionManagerFixture.getINVOCATIONS().contains(TransactionOperationType.BEGIN));
        connection.commit();
        assertTrue(XAShardingTransactionManagerFixture.getINVOCATIONS().contains(TransactionOperationType.COMMIT));
        connection.rollback();
        assertTrue(XAShardingTransactionManagerFixture.getINVOCATIONS().contains(TransactionOperationType.ROLLBACK));
    }
    
    @Test
    public void assertBASETransactionOperation() throws SQLException {
        connection = new ShardingSphereConnection(dataSourceMap, schemaContexts, TransactionType.BASE);
        connection.setAutoCommit(false);
        assertTrue(BASEShardingTransactionManagerFixture.getINVOCATIONS().contains(TransactionOperationType.BEGIN));
        connection.commit();
        assertTrue(BASEShardingTransactionManagerFixture.getINVOCATIONS().contains(TransactionOperationType.COMMIT));
        connection.rollback();
        assertTrue(BASEShardingTransactionManagerFixture.getINVOCATIONS().contains(TransactionOperationType.ROLLBACK));
    }

    @Test
    public void assertIsValid() throws SQLException {
        Connection masterConnection = mock(Connection.class);
        Connection upSlaveConnection = mock(Connection.class);
        Connection downSlaveConnection = mock(Connection.class);

        when(masterConnection.isValid(anyInt())).thenReturn(true);
        when(upSlaveConnection.isValid(anyInt())).thenReturn(true);
        when(downSlaveConnection.isValid(anyInt())).thenReturn(false);

        connection.getCachedConnections().put("test_master", masterConnection);
        connection.getCachedConnections().put("test_slave_up", upSlaveConnection);
        assertTrue(connection.isValid(0));

        connection.getCachedConnections().put("test_slave_down", downSlaveConnection);
        assertFalse(connection.isValid(0));
    }
}

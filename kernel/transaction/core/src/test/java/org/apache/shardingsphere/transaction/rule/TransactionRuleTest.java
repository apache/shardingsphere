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

package org.apache.shardingsphere.transaction.rule;

import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule.GlobalRuleChangedType;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.core.fixture.ShardingSphereTransactionManagerFixture;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("resource")
class TransactionRuleTest {
    
    private static final String FOO_DB = "foo_db";
    
    private static final String BAR_DB = "bar_db";
    
    @Test
    void assertRefreshWithNotDatabaseChange() {
        TransactionRule actual = new TransactionRule(new TransactionRuleConfiguration("XA", "Atomikos", new Properties()), Collections.emptyList());
        actual.refresh(Collections.singleton(createAddDatabase()), GlobalRuleChangedType.SCHEMA_CHANGED);
        assertThat(actual.getResource().getTransactionManager(TransactionType.XA), isA(ShardingSphereTransactionManagerFixture.class));
    }
    
    @Test
    void assertRefreshWithDatabaseChange() {
        TransactionRule actual = new TransactionRule(new TransactionRuleConfiguration("XA", "Atomikos", new Properties()), Collections.singleton(createDatabase()));
        actual.refresh(Collections.singleton(createAddDatabase()), GlobalRuleChangedType.DATABASE_CHANGED);
        assertThat(actual.getResource().getTransactionManager(TransactionType.XA), isA(ShardingSphereTransactionManagerFixture.class));
    }
    
    @Test
    void assertIsNotImplicitCommitTransactionWhenNotAutoCommit() {
        assertFalse(new TransactionRule(new TransactionRuleConfiguration("XA", "Atomikos", new Properties()), Collections.emptyList())
                .isImplicitCommitTransaction(mock(SQLStatement.class), true, mock(ConnectionTransaction.class), false));
    }
    
    @Test
    void assertIsNotImplicitCommitTransactionWhenDefaultTypeIsNotDistributedTransaction() {
        assertFalse(new TransactionRule(new TransactionRuleConfiguration("LOCAL", null, new Properties()), Collections.emptyList())
                .isImplicitCommitTransaction(mock(SQLStatement.class), true, mock(ConnectionTransaction.class), true));
    }
    
    @Test
    void assertIsNotImplicitCommitTransactionWhenInDistributedTransaction() {
        ConnectionTransaction connectionTransaction = mock(ConnectionTransaction.class);
        when(connectionTransaction.isInDistributedTransaction()).thenReturn(true);
        assertFalse(new TransactionRule(new TransactionRuleConfiguration("XA", null, new Properties()), Collections.emptyList())
                .isImplicitCommitTransaction(mock(SQLStatement.class), true, connectionTransaction, true));
    }
    
    @Test
    void assertIsNotImplicitCommitTransactionWhenQuery() {
        ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(executionContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(SelectStatement.class));
        assertFalse(new TransactionRule(new TransactionRuleConfiguration("XA", null, new Properties()), Collections.emptyList())
                .isImplicitCommitTransaction(mock(SelectStatement.class), false, mock(ConnectionTransaction.class), true));
    }
    
    @Test
    void assertIsNotImplicitCommitTransactionForSingleExecutionUnit() {
        ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(executionContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(UpdateStatement.class));
        when(executionContext.getExecutionUnits()).thenReturn(Collections.singleton(mock(ExecutionUnit.class)));
        assertFalse(new TransactionRule(new TransactionRuleConfiguration("XA", null, new Properties()), Collections.emptyList())
                .isImplicitCommitTransaction(mock(UpdateStatement.class), false, mock(ConnectionTransaction.class), true));
    }
    
    @Test
    void assertIsImplicitCommitTransaction() {
        ExecutionContext executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(executionContext.getSqlStatementContext().getSqlStatement()).thenReturn(mock(UpdateStatement.class));
        when(executionContext.getExecutionUnits()).thenReturn(Arrays.asList(mock(ExecutionUnit.class), mock(ExecutionUnit.class)));
        assertTrue(new TransactionRule(new TransactionRuleConfiguration("XA", null, new Properties()), Collections.emptyList())
                .isImplicitCommitTransaction(mock(UpdateStatement.class), true, mock(ConnectionTransaction.class), true));
    }
    
    @Test
    void assertClose() {
        TransactionRule actual = new TransactionRule(new TransactionRuleConfiguration("XA", "Atomikos", new Properties()), Collections.singleton(createDatabase()));
        actual.close();
        assertNull(actual.getResource());
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        ResourceMetaData resourceMetaData = createResourceMetaData();
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        when(result.getName()).thenReturn(FOO_DB);
        return result;
    }
    
    private ResourceMetaData createResourceMetaData() {
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        DataSourcePoolProperties dataSourcePoolProps0 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps0.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Maps.of("url", "jdbc:mock://127.0.0.1/ds_0", "username", "test"));
        storageUnits.put("ds_0", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps0, new MockedDataSource()));
        DataSourcePoolProperties dataSourcePoolProps1 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps1.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Maps.of("url", "jdbc:mock://127.0.0.1/ds_1", "username", "test"));
        storageUnits.put("ds_1", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps1, new MockedDataSource()));
        ResourceMetaData result = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getStorageUnits()).thenReturn(storageUnits);
        return result;
    }
    
    private ShardingSphereDatabase createAddDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        ResourceMetaData resourceMetaData = createAddResourceMetaData();
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        when(result.getName()).thenReturn(BAR_DB);
        return result;
    }
    
    private ResourceMetaData createAddResourceMetaData() {
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        DataSourcePoolProperties dataSourcePoolProps0 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps0.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Maps.of("url", "jdbc:mock://127.0.0.1/ds_0", "username", "test"));
        storageUnits.put("ds_0", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps0, new MockedDataSource()));
        DataSourcePoolProperties dataSourcePoolProps1 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps1.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Maps.of("url", "jdbc:mock://127.0.0.1/ds_1", "username", "test"));
        storageUnits.put("ds_1", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps1, new MockedDataSource()));
        ResourceMetaData result = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getStorageUnits()).thenReturn(storageUnits);
        return result;
    }
}

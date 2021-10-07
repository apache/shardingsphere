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

import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.fixture.BASEShardingSphereTransactionManagerFixture;
import org.apache.shardingsphere.driver.jdbc.core.fixture.XAShardingSphereTransactionManagerFixture;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.TransactionHolder;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ShardingSphereConnectionTest {
    
    private ShardingSphereConnection connection;
    
    @Before
    public void setUp() throws SQLException {
        connection = new ShardingSphereConnection(DefaultSchema.LOGIC_NAME, mockContextManager());
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds_0", mock(DataSource.class, RETURNS_DEEP_STUBS));
        dataSourceMap.put("ds_1", mock(DataSource.class, RETURNS_DEEP_STUBS));
        when(result.getDataSourceMap(DefaultSchema.LOGIC_NAME)).thenReturn(dataSourceMap);
        when(result.getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME).getResource().getDatabaseType()).thenReturn(DatabaseTypeRegistry.getActualDatabaseType("H2"));
        when(result.getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME)).thenReturn(mock(ShardingSphereMetaData.class));
        when(result.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(TransactionRule.class)).thenReturn(Optional.empty());
        when(result.getTransactionContexts().getEngines()).thenReturn(mock(Map.class));
        when(result.getTransactionContexts().getEngines().get(DefaultSchema.LOGIC_NAME)).thenReturn(new ShardingSphereTransactionManagerEngine());
        return result;
    }
    
    @After
    public void clear() {
        try {
            connection.close();
            TransactionTypeHolder.clear();
            XAShardingSphereTransactionManagerFixture.getInvocations().clear();
            BASEShardingSphereTransactionManagerFixture.getInvocations().clear();
        } catch (final SQLException ignored) {
        }
    }
    
    @Test
    public void assertGetRandomPhysicalDataSourceNameFromContextManager() {
        String actual = connection.getRandomPhysicalDataSourceName();
        assertTrue(Arrays.asList("ds_0", "ds_1").contains(actual));
    }
    
    @Test
    public void assertGetRandomPhysicalDataSourceNameFromCache() throws SQLException {
        connection.getConnection("ds_0");
        String actual = connection.getRandomPhysicalDataSourceName();
        assertThat(actual, is("ds_0"));
    }
    
    @Test
    public void assertGetConnectionFromCache() throws SQLException {
        assertThat(connection.getConnection("ds_0"), is(connection.getConnection("ds_0")));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetConnectionFailure() throws SQLException {
        connection.getConnection("not_exist");
    }
    
    @Test
    public void assertLocalTransactionOperation() throws SQLException {
        connection.setAutoCommit(true);
        assertFalse(TransactionHolder.isTransaction());
        connection.setAutoCommit(false);
        assertTrue(TransactionHolder.isTransaction());
    }
    
    @Test
    public void assertXATransactionOperation() throws SQLException {
        ContextManager contextManager = mockContextManager();
        TransactionRule transactionRule = new TransactionRule(new TransactionRuleConfiguration("XA", null));
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(TransactionRule.class)).thenReturn(Optional.of(transactionRule));
        connection = new ShardingSphereConnection(connection.getSchema(), contextManager);
        connection.setAutoCommit(false);
        assertTrue(XAShardingSphereTransactionManagerFixture.getInvocations().contains(TransactionOperationType.BEGIN));
        connection.commit();
        assertTrue(XAShardingSphereTransactionManagerFixture.getInvocations().contains(TransactionOperationType.COMMIT));
        connection.rollback();
        assertTrue(XAShardingSphereTransactionManagerFixture.getInvocations().contains(TransactionOperationType.ROLLBACK));
    }
    
    @Test
    public void assertBASETransactionOperation() throws SQLException {
        ContextManager contextManager = mockContextManager();
        TransactionRule transactionRule = new TransactionRule(new TransactionRuleConfiguration("BASE", null));
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(TransactionRule.class)).thenReturn(Optional.of(transactionRule));
        connection = new ShardingSphereConnection(connection.getSchema(), contextManager);
        connection.setAutoCommit(false);
        assertTrue(BASEShardingSphereTransactionManagerFixture.getInvocations().contains(TransactionOperationType.BEGIN));
        connection.commit();
        assertTrue(BASEShardingSphereTransactionManagerFixture.getInvocations().contains(TransactionOperationType.COMMIT));
        connection.rollback();
        assertTrue(BASEShardingSphereTransactionManagerFixture.getInvocations().contains(TransactionOperationType.ROLLBACK));
    }
    
    @Test
    public void assertIsValidWhenEmptyConnection() throws SQLException {
        assertTrue(connection.isValid(0));
    }
    
    @Test
    public void assertIsInvalid() throws SQLException {
        connection.getConnection("ds_1");
        assertFalse(connection.isValid(0));
    }
    
    @Test
    public void assertSetReadOnly() throws SQLException {
        ShardingSphereConnection actual = createShardingSphereConnection();
        assertFalse(actual.isReadOnly());
        Connection connection = actual.getConnection("ds_1");
        actual.setReadOnly(true);
        assertTrue(actual.isReadOnly());
        verify(connection).setReadOnly(true);
    }
    
    @Test
    public void assertGetTransactionIsolationWithoutCachedConnections() throws SQLException {
        assertThat(createShardingSphereConnection().getTransactionIsolation(), is(Connection.TRANSACTION_READ_UNCOMMITTED));
    }
    
    @Test
    public void assertSetTransactionIsolation() throws SQLException {
        ShardingSphereConnection actual = createShardingSphereConnection();
        Connection connection = actual.getConnection("ds_1");
        actual.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        verify(connection).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Multimap<String, Connection> getCachedConnections(final ShardingSphereConnection connectionAdapter) {
        Field field = ShardingSphereConnection.class.getDeclaredField("cachedConnections");
        field.setAccessible(true);
        return (Multimap<String, Connection>) field.get(connectionAdapter);
    }
    
    @Test
    public void assertClose() throws SQLException {
        ShardingSphereConnection actual = createShardingSphereConnection();
        actual.close();
        assertTrue(actual.isClosed());
        assertTrue(getCachedConnections(actual).isEmpty());
    }
    
    @Test
    public void assertCloseShouldNotClearTransactionType() throws SQLException {
        ShardingSphereConnection actual = createShardingSphereConnection();
        TransactionTypeHolder.set(TransactionType.XA);
        actual.close();
        assertTrue(actual.isClosed());
        assertTrue(getCachedConnections(actual).isEmpty());
        assertThat(TransactionTypeHolder.get(), is(TransactionType.XA));
    }
    
    private ShardingSphereConnection createShardingSphereConnection() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(TransactionRule.class)).thenReturn(Optional.empty());
        return new ShardingSphereConnection(DefaultSchema.LOGIC_NAME, contextManager);
    }
}

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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.fixture.CallTimeRecordDataSource;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class JDBCBackendDataSourceTest {
    
    private static final String DATA_SOURCE_PATTERN = "ds_%s";
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), createMetaDataMap(), mock(ShardingSphereRuleMetaData.class),
                mock(ExecutorEngine.class), mock(OptimizerContext.class), new ConfigurationProperties(new Properties()));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        TransactionContexts transactionContexts = createTransactionContexts();
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(contextManager.getTransactionContexts()).thenReturn(transactionContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getName()).thenReturn("schema");
        when(metaData.getResource().getDatabaseType()).thenReturn(new H2DatabaseType());
        when(metaData.getResource().getDataSources()).thenReturn(mockDataSources(2));
        return Collections.singletonMap("schema", metaData);
    }
    
    private TransactionContexts createTransactionContexts() {
        TransactionContexts result = mock(TransactionContexts.class, RETURNS_DEEP_STUBS);
        when(result.getEngines().get("schema")).thenReturn(mock(ShardingSphereTransactionManagerEngine.class));
        return result;
    }
    
    private Map<String, DataSource> mockDataSources(final int size) {
        Map<String, DataSource> result = new HashMap<>(size, 1);
        for (int i = 0; i < size; i++) {
            result.put(String.format(DATA_SOURCE_PATTERN, i), new CallTimeRecordDataSource());
        }
        return result;
    }
    
    @Test
    public void assertGetConnectionsSucceed() throws SQLException {
        List<Connection> actual = ProxyContext.getInstance().getBackendDataSource().getConnections("schema", String.format(DATA_SOURCE_PATTERN, 1), 5, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actual.size(), is(5));
    }
    
    @Test(expected = SQLException.class)
    public void assertGetConnectionsFailed() throws SQLException {
        ProxyContext.getInstance().getBackendDataSource().getConnections("schema", String.format(DATA_SOURCE_PATTERN, 1), 6, ConnectionMode.MEMORY_STRICTLY);
    }
    
    @Test
    public void assertGetConnectionsByMultiThread() {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        Collection<Future<List<Connection>>> futures = new LinkedList<>();
        for (int i = 0; i < 200; i++) {
            futures.add(executorService.submit(new CallableTask(String.format(DATA_SOURCE_PATTERN, 1), 6, ConnectionMode.MEMORY_STRICTLY)));
        }
        Collection<Connection> actual = new LinkedList<>();
        for (Future<List<Connection>> each : futures) {
            try {
                actual.addAll(each.get());
            } catch (final InterruptedException | ExecutionException ex) {
                assertThat(ex.getMessage(), containsString("Could not get 6 connections at once. The 5 obtained connections have been released. "
                        + "Please consider increasing the `maxPoolSize` of the data sources or decreasing the `max-connections-size-per-query` in props."));
            }
        }
        assertTrue(actual.isEmpty());
        executorService.shutdown();
    }
    
    @RequiredArgsConstructor
    private static class CallableTask implements Callable<List<Connection>> {
        
        private final String datasourceName;
        
        private final int connectionSize;
    
        private final ConnectionMode connectionMode;
        
        @Override
        public List<Connection> call() throws SQLException {
            return ProxyContext.getInstance().getBackendDataSource().getConnections("schema", datasourceName, connectionSize, connectionMode);
        }
    }
}

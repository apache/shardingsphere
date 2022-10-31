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
import org.apache.shardingsphere.infra.exception.OverallConnectionNotEnoughException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.fixture.CallTimeRecordDataSource;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class JDBCBackendDataSourceTest extends ProxyContextRestorer {
    
    private static final String DATA_SOURCE_PATTERN = "ds_%s";
    
    @Before
    public void setUp() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(createDatabases(), mockGlobalRuleMetaData(), new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    private Map<String, ShardingSphereDatabase> createDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("schema");
        when(database.getResourceMetaData().getDatabaseType()).thenReturn(new H2DatabaseType());
        when(database.getResourceMetaData().getDataSources()).thenReturn(mockDataSources(2));
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        result.put("schema", database);
        return result;
    }
    
    private ShardingSphereRuleMetaData mockGlobalRuleMetaData() {
        ShardingSphereRuleMetaData result = mock(ShardingSphereRuleMetaData.class);
        TransactionRule transactionRule = mock(TransactionRule.class);
        when(transactionRule.getResource()).thenReturn(mock(ShardingSphereTransactionManagerEngine.class));
        when(result.getSingleRule(TransactionRule.class)).thenReturn(transactionRule);
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
    
    @Test(expected = OverallConnectionNotEnoughException.class)
    public void assertGetConnectionsFailed() throws SQLException {
        ProxyContext.getInstance().getBackendDataSource().getConnections("schema", String.format(DATA_SOURCE_PATTERN, 1), 6, ConnectionMode.MEMORY_STRICTLY);
    }
    
    @Test
    public void assertGetConnectionsByMultiThread() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        Collection<Future<List<Connection>>> futures = new LinkedList<>();
        for (int i = 0; i < 200; i++) {
            futures.add(executorService.submit(new CallableTask(String.format(DATA_SOURCE_PATTERN, 1), 6, ConnectionMode.MEMORY_STRICTLY)));
        }
        Collection<Connection> actual = new LinkedList<>();
        for (Future<List<Connection>> each : futures) {
            try {
                actual.addAll(each.get());
            } catch (final ExecutionException ex) {
                assertThat(ex.getCause(), instanceOf(OverallConnectionNotEnoughException.class));
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

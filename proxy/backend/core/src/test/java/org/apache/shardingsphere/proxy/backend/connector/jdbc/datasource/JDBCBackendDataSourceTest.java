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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.datasource;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.kernel.connection.OverallConnectionNotEnoughException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.datasource.fixture.CallTimeRecordDataSource;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JDBCBackendDataSourceTest {
    
    private static final String DATA_SOURCE_PATTERN = "ds_%s";
    
    @BeforeEach
    void setUp() {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(mockDatabase()),
                mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(mock(TransactionRule.class, RETURNS_DEEP_STUBS))), new ConfigurationProperties(new Properties()));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("schema");
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        mockDataSources(2).forEach((key, value) -> when(result.getResourceMetaData().getStorageUnits().get(key).getDataSource()).thenReturn(value));
        return result;
    }
    
    private Map<String, DataSource> mockDataSources(final int size) {
        Map<String, DataSource> result = new HashMap<>(size, 1F);
        for (int i = 0; i < size; i++) {
            result.put(String.format(DATA_SOURCE_PATTERN, i), new CallTimeRecordDataSource());
        }
        return result;
    }
    
    @Test
    void assertGetConnectionsSucceed() throws SQLException {
        List<Connection> actual = new JDBCBackendDataSource().getConnections("schema", String.format(DATA_SOURCE_PATTERN, 1), 5, ConnectionMode.MEMORY_STRICTLY);
        assertThat(actual.size(), is(5));
    }
    
    @Test
    void assertGetConnectionsFailed() {
        assertThrows(OverallConnectionNotEnoughException.class, () -> new JDBCBackendDataSource().getConnections("schema", String.format(DATA_SOURCE_PATTERN, 1), 6, ConnectionMode.MEMORY_STRICTLY));
    }
    
    @Test
    void assertGetConnectionsByMultiThreads() throws InterruptedException {
        JDBCBackendDataSource jdbcBackendDataSource = new JDBCBackendDataSource();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        Collection<Future<List<Connection>>> futures = new LinkedList<>();
        for (int i = 0; i < 200; i++) {
            futures.add(executorService.submit(new CallableTask(jdbcBackendDataSource, String.format(DATA_SOURCE_PATTERN, 1), 6, ConnectionMode.MEMORY_STRICTLY)));
        }
        Collection<Connection> actual = new LinkedList<>();
        for (Future<List<Connection>> each : futures) {
            try {
                actual.addAll(each.get());
            } catch (final ExecutionException ex) {
                assertThat(ex.getCause(), isA(OverallConnectionNotEnoughException.class));
            }
        }
        assertTrue(actual.isEmpty());
        executorService.shutdown();
    }
    
    @RequiredArgsConstructor
    private class CallableTask implements Callable<List<Connection>> {
        
        private final JDBCBackendDataSource jdbcBackendDataSource;
        
        private final String datasourceName;
        
        private final int connectionSize;
        
        private final ConnectionMode connectionMode;
        
        @Override
        public List<Connection> call() throws SQLException {
            try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
                ContextManager contextManager = mockContextManager();
                proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
                return jdbcBackendDataSource.getConnections("schema", datasourceName, connectionSize, connectionMode);
            }
        }
    }
}

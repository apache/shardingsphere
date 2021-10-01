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

package org.apache.shardingsphere.driver.executor;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractBaseExecutorTest {
    
    private ExecutorEngine executorEngine;
    
    private ShardingSphereConnection connection;
    
    @Before
    public void setUp() throws SQLException {
        SQLExecutorExceptionHandler.setExceptionThrown(true);
        executorEngine = new ExecutorEngine(Runtime.getRuntime().availableProcessors());
        TransactionTypeHolder.set(TransactionType.LOCAL);
        connection = new ShardingSphereConnection(DefaultSchema.LOGIC_NAME, mockContextManager());
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = mockMetaDataContexts();
        TransactionContexts transactionContexts = mockTransactionContexts();
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getTransactionContexts()).thenReturn(transactionContexts);
        when(result.getDataSourceMap(DefaultSchema.LOGIC_NAME)).thenReturn(mockDataSourceMap());
        return result;
    }
    
    private Map<String, DataSource> mockDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        result.put("ds_0", dataSource);
        result.put("ds_1", dataSource);
        return result;
    }
    
    private MetaDataContexts mockMetaDataContexts() {
        MetaDataContexts result = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData(DefaultSchema.LOGIC_NAME).getResource().getDatabaseType()).thenReturn(DatabaseTypeRegistry.getActualDatabaseType("H2"));
        ShardingRule shardingRule = mockShardingRule();
        when(result.getMetaData(DefaultSchema.LOGIC_NAME).getRuleMetaData().getRules()).thenReturn(Collections.singletonList(shardingRule));
        return result;
    }
    
    private TransactionContexts mockTransactionContexts() {
        TransactionContexts result = mock(TransactionContexts.class);
        when(result.getEngines()).thenReturn(mock(Map.class));
        when(result.getEngines().get(DefaultSchema.LOGIC_NAME)).thenReturn(new ShardingSphereTransactionManagerEngine());
        return result;
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        when(result.isNeedAccumulate(any())).thenReturn(true);
        return result;
    }
    
    @After
    public void tearDown() {
        executorEngine.close();
    }
}

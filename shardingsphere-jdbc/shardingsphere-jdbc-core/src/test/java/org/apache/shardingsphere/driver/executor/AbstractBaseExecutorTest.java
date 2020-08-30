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
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.ExecutorExceptionHandler;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.kernel.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.kernel.context.runtime.RuntimeContext;
import org.apache.shardingsphere.kernel.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractBaseExecutorTest {
    
    private ExecutorKernel executorKernel;
    
    private ShardingSphereConnection connection;
    
    @Before
    public void setUp() throws SQLException {
        MockitoAnnotations.initMocks(this);
        ExecutorExceptionHandler.setExceptionThrown(false);
        executorKernel = new ExecutorKernel(Runtime.getRuntime().availableProcessors());
        setConnection();
    }
    
    private void setConnection() throws SQLException {
        SchemaContexts schemaContexts = mock(StandardSchemaContexts.class);
        SchemaContext schemaContext = mock(SchemaContext.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schemaContexts.getDefaultSchemaContext()).thenReturn(schemaContext);
        when(schemaContext.getRuntimeContext()).thenReturn(runtimeContext);
        when(runtimeContext.getExecutorKernel()).thenReturn(executorKernel);
        when(schemaContexts.getProps()).thenReturn(getProperties());
        when(schemaContext.getSchema()).thenReturn(schema);
        when(schemaContexts.getDatabaseType()).thenReturn(DatabaseTypes.getActualDatabaseType("H2"));
        ShardingRule shardingRule = getShardingRule();
        when(schema.getRules()).thenReturn(Collections.singletonList(shardingRule));
        TransactionContexts transactionContexts = mock(TransactionContexts.class);
        when(transactionContexts.getDefaultTransactionManagerEngine()).thenReturn(new ShardingTransactionManagerEngine());
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(mock(Connection.class));
        Map<String, DataSource> dataSourceSourceMap = new LinkedHashMap<>(2, 1);
        dataSourceSourceMap.put("ds_0", dataSource);
        dataSourceSourceMap.put("ds_1", dataSource);
        connection = new ShardingSphereConnection(dataSourceSourceMap, schemaContexts, transactionContexts, TransactionType.LOCAL);
    }
    
    private ShardingRule getShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        when(result.findTableRuleByActualTable("table_x")).thenReturn(Optional.empty());
        when(result.isNeedAccumulate(any())).thenReturn(true);
        return result;
    }
    
    protected final SQLStatementContext<?> getSQLStatementContext() {
        SQLStatementContext<?> result = mock(SQLStatementContext.class);
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getTableNames()).thenReturn(Collections.singleton("table_x"));
        when(result.getTablesContext()).thenReturn(tablesContext);
        return result;
    }
    
    private ConfigurationProperties getProperties() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY.getKey(), ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY.getDefaultValue());
        return new ConfigurationProperties(props);
    }
    
    @After
    public void tearDown() {
        executorKernel.close();
    }
}

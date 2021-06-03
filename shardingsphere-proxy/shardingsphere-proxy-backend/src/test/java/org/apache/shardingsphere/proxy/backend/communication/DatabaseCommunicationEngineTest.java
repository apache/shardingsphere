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

package org.apache.shardingsphere.proxy.backend.communication;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeaderBuilder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.FieldSetter;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseCommunicationEngineTest {
    
    @Before
    public void setUp() {
        MetaDataContexts metaDataContexts = new StandardMetaDataContexts(mockMetaDataMap(), mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), 
                new ConfigurationProperties(new Properties()));
        ProxyContext.getInstance().init(metaDataContexts, new StandardTransactionContexts());
    }
    
    private Map<String, ShardingSphereMetaData> mockMetaDataMap() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getResource().getDatabaseType()).thenReturn(new H2DatabaseType());
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return Collections.singletonMap("schema", result);
    }
    
    @Test
    public void assertBinaryProtocolQueryHeader() throws SQLException, NoSuchFieldException {
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getSchemaName()).thenReturn("schema");
        DatabaseCommunicationEngine engine =
                DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(mock(MySQLStatement.class), "schemaName", Collections.emptyList(), backendConnection);
        assertNotNull(engine);
        assertThat(engine, instanceOf(DatabaseCommunicationEngine.class));
        Field queryHeadersField = engine.getClass().getDeclaredField("queryHeaders");
        FieldSetter.setField(engine, queryHeadersField, Collections.singletonList(QueryHeaderBuilder.build(createQueryResultMetaData(), createMetaData(), 1)));
        Field mergedResultField = engine.getClass().getDeclaredField("mergedResult");
        FieldSetter.setField(engine, mergedResultField, new MemoryMergedResult<ShardingSphereRule>(null, null, null, Collections.emptyList()) {
            
            private MemoryQueryResultRow memoryQueryResultRow;
            
            @Override
            protected List<MemoryQueryResultRow> init(final ShardingSphereRule rule, final ShardingSphereSchema schema, 
                                                      final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults, final MergedResult mergedResult) {
                memoryQueryResultRow = mock(MemoryQueryResultRow.class);
                return Collections.singletonList(memoryQueryResultRow);
            }
        });
        Exception ex = null;
        try {
            engine.getQueryResponseRow();
        } catch (final SQLException | IndexOutOfBoundsException e) {
            ex = e;
        } finally {
            assertFalse(ex instanceof IndexOutOfBoundsException);
        }
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        ColumnMetaData columnMetaData = new ColumnMetaData("order_id", Types.INTEGER, true, false, false);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.get("t_logic_order")).thenReturn(new TableMetaData(Collections.singletonList(columnMetaData), Collections.singletonList(new IndexMetaData("order_id"))));
        DataSourcesMetaData dataSourcesMetaData = mock(DataSourcesMetaData.class);
        when(dataSourcesMetaData.getDataSourceMetaData("ds_0")).thenReturn(mock(DataSourceMetaData.class));
        when(result.getResource().getDataSourcesMetaData()).thenReturn(dataSourcesMetaData);
        when(result.getSchema()).thenReturn(schema);
        ShardingRule shardingRule = mock(ShardingRule.class);
        when(shardingRule.findLogicTableByActualTable("t_order")).thenReturn(Optional.of("t_logic_order"));
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.singletonList(shardingRule));
        when(result.getName()).thenReturn("sharding_schema");
        return result;
    }
    
    private QueryResultMetaData createQueryResultMetaData() throws SQLException {
        QueryResultMetaData result = mock(QueryResultMetaData.class);
        when(result.getTableName(1)).thenReturn("t_order");
        when(result.getColumnLabel(1)).thenReturn("order_id");
        when(result.getColumnName(1)).thenReturn("order_id");
        when(result.getColumnName(2)).thenReturn("expr");
        when(result.getColumnType(1)).thenReturn(Types.INTEGER);
        when(result.isSigned(1)).thenReturn(true);
        when(result.isAutoIncrement(1)).thenReturn(true);
        when(result.getColumnLength(1)).thenReturn(1);
        when(result.getDecimals(1)).thenReturn(1);
        when(result.isNotNull(1)).thenReturn(true);
        return result;
    }
}

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

package org.apache.shardingsphere.proxy.backend.response.header.query.impl;

import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MySQLQueryHeaderBuilderTest {
    
    private final QueryHeaderBuilder queryHeaderBuilder = new MySQLQueryHeaderBuilder();
    
    @Test
    public void assertBuild() throws SQLException {
        QueryResultMetaData queryResultMetaData = createQueryResultMetaData();
        ShardingSphereMetaData metaData = createMetaData();
        QueryHeader actual = queryHeaderBuilder.build(
                queryResultMetaData, metaData, queryResultMetaData.getColumnName(1), queryResultMetaData.getColumnLabel(1), 1, getDataNodeContainedRule(metaData));
        assertThat(actual.getSchema(), is("sharding_schema"));
        assertThat(actual.getTable(), is("t_logic_order"));
        assertThat(actual.getColumnLabel(), is("order_id"));
        assertThat(actual.getColumnName(), is("order_id"));
        assertThat(actual.getColumnLength(), is(1));
        assertThat(actual.getColumnType(), is(Types.INTEGER));
        assertThat(actual.getDecimals(), is(1));
        assertTrue(actual.isSigned());
        assertTrue(actual.isPrimaryKey());
        assertTrue(actual.isNotNull());
        assertTrue(actual.isAutoIncrement());
    }
    
    @Test
    public void assertQueryHeaderPrimaryKeyWithoutColumn() throws SQLException {
        QueryResultMetaData queryResultMetaData = createQueryResultMetaData();
        ShardingSphereMetaData metaData = createMetaData();
        QueryHeader actual = queryHeaderBuilder.build(
                queryResultMetaData, metaData, queryResultMetaData.getColumnName(2), queryResultMetaData.getColumnLabel(2), 2, getDataNodeContainedRule(metaData));
        assertFalse(actual.isPrimaryKey());
    }
    
    @Test
    public void assertDataNodeContainedRuleIsNotPresent() throws SQLException {
        QueryResultMetaData queryResultMetaData = createQueryResultMetaData();
        ShardingSphereMetaData metaData = createMetaData();
        QueryHeader actual = queryHeaderBuilder.build(queryResultMetaData, metaData, queryResultMetaData.getColumnName(1), queryResultMetaData.getColumnLabel(1), 1, 
                new LazyInitializer<DataNodeContainedRule>() {
                    
                    @Override
                    protected DataNodeContainedRule initialize() {
                        return null;
                    }
                });
        assertFalse(actual.isPrimaryKey());
        assertThat(actual.getTable(), is("t_order"));
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        ColumnMetaData columnMetaData = new ColumnMetaData("order_id", Types.INTEGER, true, false, false);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.get("t_logic_order")).thenReturn(new TableMetaData("t_logic_order", 
                Collections.singletonList(columnMetaData), Collections.singletonList(new IndexMetaData("order_id")), Collections.emptyList()));
        DataSourcesMetaData dataSourcesMetaData = mock(DataSourcesMetaData.class);
        when(dataSourcesMetaData.getDataSourceMetaData("ds_0")).thenReturn(mock(DataSourceMetaData.class));
        when(result.getResource().getDataSourcesMetaData()).thenReturn(dataSourcesMetaData);
        when(result.getDefaultSchema()).thenReturn(schema);
        ShardingRule shardingRule = mock(ShardingRule.class);
        when(shardingRule.findLogicTableByActualTable("t_order")).thenReturn(Optional.of("t_logic_order"));
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.singletonList(shardingRule));
        when(result.getName()).thenReturn("sharding_schema");
        return result;
    }
    
    private QueryResultMetaData createQueryResultMetaData() throws SQLException {
        QueryResultMetaData result = mock(QueryResultMetaData.class);
        when(result.getTableName(1)).thenReturn("t_order");
        when(result.getTableName(2)).thenReturn("t_order");
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
    
    private LazyInitializer<DataNodeContainedRule> getDataNodeContainedRule(final ShardingSphereMetaData metaData) {
        return new LazyInitializer<DataNodeContainedRule>() {
            
            @Override
            protected DataNodeContainedRule initialize() {
                return metaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof DataNodeContainedRule).findFirst().map(rule -> (DataNodeContainedRule) rule).get();
            }
        };
    }
}

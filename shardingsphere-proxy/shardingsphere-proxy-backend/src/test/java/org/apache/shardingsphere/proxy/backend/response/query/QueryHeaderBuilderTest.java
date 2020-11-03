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

package org.apache.shardingsphere.proxy.backend.response.query;

import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.infra.metadata.model.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.model.datasource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.column.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.index.PhysicalIndexMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.table.PhysicalTableMetaData;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class QueryHeaderBuilderTest {
    
    @Test
    public void assertQueryHeaderSchema() throws SQLException {
        QueryHeader header = QueryHeaderBuilder.build(createResultSetMetaData(), createSchema(), 1);
        assertThat(header.getSchema(), is("sharding_schema"));
    }
    
    @Test
    public void assertQueryHeaderTable() throws SQLException {
        QueryHeader header = QueryHeaderBuilder.build(createResultSetMetaData(), createSchema(), 1);
        assertThat(header.getTable(), is("t_logic_order"));
    }
    
    @Test
    public void assertQueryHeaderColumnLabel() throws SQLException {
        QueryHeader header = QueryHeaderBuilder.build(createResultSetMetaData(), createSchema(), 1);
        assertThat(header.getColumnLabel(), is("order_id"));
    }
    
    @Test
    public void assertQueryHeaderColumnNameWithoutProjectionsContext() throws SQLException {
        QueryHeader header = QueryHeaderBuilder.build(createResultSetMetaData(), createSchema(), 1);
        assertThat(header.getColumnName(), is("order_id"));
    }
    
    @Test
    public void assertQueryHeaderColumnNameFromProjectionsContext() throws SQLException {
        QueryHeader header = QueryHeaderBuilder.build(createProjectionsContext(), createResultSetMetaData(), createSchema(), 1);
        assertThat(header.getColumnName(), is("order_id"));
    }
    
    @Test
    public void assertQueryHeaderColumnNameFromMetaData() throws SQLException {
        QueryHeader header = QueryHeaderBuilder.build(createProjectionsContext(), createResultSetMetaData(), createSchema(), 2);
        assertThat(header.getColumnName(), is("expr"));
    }
    
    @Test
    public void assertQueryHeaderColumnLength() throws SQLException {
        QueryHeader header = QueryHeaderBuilder.build(createResultSetMetaData(), createSchema(), 1);
        assertThat(header.getColumnLength(), is(1));
    }
    
    @Test
    public void assertQueryHeaderColumnType() throws SQLException {
        QueryHeader header = QueryHeaderBuilder.build(createResultSetMetaData(), createSchema(), 1);
        assertThat(header.getColumnType(), is(Types.INTEGER));
    }
    
    @Test
    public void assertQueryHeaderDecimals() throws SQLException {
        QueryHeader header = QueryHeaderBuilder.build(createResultSetMetaData(), createSchema(), 1);
        assertThat(header.getDecimals(), is(1));
    }
    
    @Test
    public void assertQueryHeaderSigned() throws SQLException {
        QueryHeader header = QueryHeaderBuilder.build(createResultSetMetaData(), createSchema(), 1);
        assertTrue(header.isSigned());
    }
    
    @Test
    public void assertQueryHeaderPrimaryKey() throws SQLException {
        QueryHeader header = QueryHeaderBuilder.build(createResultSetMetaData(), createSchema(), 1);
        assertTrue(header.isPrimaryKey());
    }
    
    @Test
    public void assertQueryHeaderNotNull() throws SQLException {
        QueryHeader header = QueryHeaderBuilder.build(createResultSetMetaData(), createSchema(), 1);
        assertTrue(header.isNotNull());
    }
    
    @Test
    public void assertQueryHeaderAutoIncrement() throws SQLException {
        QueryHeader header = QueryHeaderBuilder.build(createResultSetMetaData(), createSchema(), 1);
        assertTrue(header.isAutoIncrement());
    }
    
    private ShardingSphereSchema createSchema() {
        ShardingSphereSchema result = mock(ShardingSphereSchema.class);
        PhysicalColumnMetaData columnMetaData = new PhysicalColumnMetaData("order_id", Types.INTEGER, "int", true, false, false);
        PhysicalSchemaMetaData schemaMetaData = mock(PhysicalSchemaMetaData.class);
        when(schemaMetaData.get("t_logic_order")).thenReturn(new PhysicalTableMetaData(Collections.singletonList(columnMetaData), Collections.singletonList(new PhysicalIndexMetaData("order_id"))));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getSchemaMetaData()).thenReturn(schemaMetaData);
        DataSourcesMetaData dataSourcesMetaData = mock(DataSourcesMetaData.class);
        when(dataSourcesMetaData.getDataSourceMetaData("ds_0")).thenReturn(mock(DataSourceMetaData.class));
        when(metaData.getDataSourcesMetaData()).thenReturn(dataSourcesMetaData);
        when(result.getMetaData()).thenReturn(metaData);
        ShardingRule shardingRule = mock(ShardingRule.class);
        when(shardingRule.findLogicTableByActualTable("t_order")).thenReturn(Optional.of("t_logic_order"));
        when(result.getRules()).thenReturn(Collections.singletonList(shardingRule));
        when(result.getName()).thenReturn("sharding_schema");
        return result;
    }
    
    private ProjectionsContext createProjectionsContext() {
        return new ProjectionsContext(0, 0, false, Arrays.asList(new ColumnProjection("o", "order_id", "id"), new ExpressionProjection("o.order_id + 1", "expr")));
    }
    
    private ResultSetMetaData createResultSetMetaData() throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getTableName(1)).thenReturn("t_order");
        when(result.getColumnLabel(1)).thenReturn("order_id");
        when(result.getColumnName(1)).thenReturn("order_id");
        when(result.getColumnName(2)).thenReturn("expr");
        when(result.getColumnType(1)).thenReturn(Types.INTEGER);
        when(result.isSigned(1)).thenReturn(true);
        when(result.isAutoIncrement(1)).thenReturn(true);
        when(result.isNullable(1)).thenReturn(ResultSetMetaData.columnNoNulls);
        when(result.getColumnDisplaySize(1)).thenReturn(1);
        when(result.getScale(1)).thenReturn(1);
        return result;
    }
}

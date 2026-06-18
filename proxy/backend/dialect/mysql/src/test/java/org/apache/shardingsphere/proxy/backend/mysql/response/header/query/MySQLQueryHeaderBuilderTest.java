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

package org.apache.shardingsphere.proxy.backend.mysql.response.header.query;

import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.junit.jupiter.api.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLQueryHeaderBuilderTest {
    
    @Test
    void assertBuild() throws SQLException {
        ShardingSphereResultSetMetaData resultSetMetaData = createResultSetMetaData();
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(resultSetMetaData, createDatabase(), resultSetMetaData.getColumnName(1), resultSetMetaData.getColumnLabel(1), 1);
        assertThat(actual.getSchema(), is("foo_db"));
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
    void assertBuildWithoutPrimaryKeyColumn() throws SQLException {
        ShardingSphereResultSetMetaData resultSetMetaData = createResultSetMetaData();
        assertFalse(new MySQLQueryHeaderBuilder().build(resultSetMetaData, createDatabase(), resultSetMetaData.getColumnName(2), resultSetMetaData.getColumnLabel(2), 2).isPrimaryKey());
    }
    
    @Test
    void assertBuildWithAliasColumnAndOriginalPrimaryKey() throws SQLException {
        ShardingSphereResultSetMetaData resultSetMetaData = createResultSetMetaData();
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(resultSetMetaData, createDatabase(), "order_id_alias", "order_id_alias", 1);
        assertTrue(actual.isPrimaryKey());
    }
    
    @Test
    void assertBuildWithNullDatabase() throws SQLException {
        ShardingSphereResultSetMetaData resultSetMetaData = createActualResultSetMetaData("t_order");
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(resultSetMetaData, null, resultSetMetaData.getColumnName(1), resultSetMetaData.getColumnLabel(1), 1);
        assertFalse(actual.isPrimaryKey());
        assertThat(actual.getTable(), is("t_order"));
    }
    
    @Test
    void assertBuildWithNullSchema() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getAllSchemas()).thenReturn(Collections.emptyList());
        DataNodeRuleAttribute ruleAttribute = mock(DataNodeRuleAttribute.class);
        when(ruleAttribute.findLogicTableByActualTable("t_order")).thenReturn(Optional.of("t_order"));
        when(database.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)).thenReturn(Collections.singleton(ruleAttribute));
        ShardingSphereResultSetMetaData resultSetMetaData = createResultSetMetaData("t_order");
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(resultSetMetaData, database, resultSetMetaData.getColumnName(1), resultSetMetaData.getColumnLabel(1), 1);
        assertFalse(actual.isPrimaryKey());
        assertThat(actual.getTable(), is("t_order"));
    }
    
    @Test
    void assertBuildWithoutDataNodeContainedRule() throws SQLException {
        ShardingSphereResultSetMetaData resultSetMetaData = createResultSetMetaData();
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(
                resultSetMetaData, mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS), resultSetMetaData.getColumnName(1), resultSetMetaData.getColumnLabel(1), 1);
        assertFalse(actual.isPrimaryKey());
        assertThat(actual.getTable(), is(actual.getTable()));
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereColumn column = new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getTable("t_logic_order")).thenReturn(new ShardingSphereTable(
                "t_logic_order", Collections.singleton(column), Collections.singleton(new ShardingSphereIndex("order_id", Collections.emptyList(), false)), Collections.emptyList()));
        when(result.getSchema("foo_db")).thenReturn(schema);
        DataNodeRuleAttribute ruleAttribute = mock(DataNodeRuleAttribute.class);
        when(ruleAttribute.findLogicTableByActualTable("t_order")).thenReturn(Optional.of("t_logic_order"));
        when(result.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)).thenReturn(Collections.singleton(ruleAttribute));
        when(result.getName()).thenReturn("foo_db");
        return result;
    }
    
    private ShardingSphereResultSetMetaData createResultSetMetaData() throws SQLException {
        return createResultSetMetaData("t_logic_order");
    }
    
    private ShardingSphereResultSetMetaData createResultSetMetaData(final String tableName) throws SQLException {
        ShardingSphereResultSetMetaData result = mock(ShardingSphereResultSetMetaData.class);
        when(result.getTableName(1)).thenReturn(tableName);
        when(result.getTableName(2)).thenReturn(tableName);
        when(result.getColumnLabel(1)).thenReturn("order_id");
        when(result.getColumnName(1)).thenReturn("order_id");
        when(result.getColumnName(2)).thenReturn("expr");
        when(result.getColumnType(1)).thenReturn(Types.INTEGER);
        when(result.isSigned(1)).thenReturn(true);
        when(result.isAutoIncrement(1)).thenReturn(true);
        when(result.getColumnDisplaySize(1)).thenReturn(1);
        when(result.getScale(1)).thenReturn(1);
        when(result.isNullable(1)).thenReturn(ResultSetMetaData.columnNoNulls);
        return result;
    }
    
    private ShardingSphereResultSetMetaData createActualResultSetMetaData(final String tableName) throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getTableName(1)).thenReturn(tableName);
        when(result.getColumnLabel(1)).thenReturn("order_id");
        when(result.getColumnName(1)).thenReturn("order_id");
        when(result.getColumnType(1)).thenReturn(Types.INTEGER);
        when(result.isSigned(1)).thenReturn(true);
        when(result.isAutoIncrement(1)).thenReturn(true);
        when(result.getColumnDisplaySize(1)).thenReturn(1);
        when(result.getScale(1)).thenReturn(1);
        when(result.isNullable(1)).thenReturn(ResultSetMetaData.columnNoNulls);
        return new ShardingSphereResultSetMetaData(result, null, null);
    }
}

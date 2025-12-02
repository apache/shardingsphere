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

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLQueryHeaderBuilderTest {
    
    @Test
    void assertBuild() throws SQLException {
        QueryResultMetaData queryResultMetaData = createQueryResultMetaData();
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(queryResultMetaData, createDatabase(), queryResultMetaData.getColumnName(1), queryResultMetaData.getColumnLabel(1), 1);
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
        QueryResultMetaData queryResultMetaData = createQueryResultMetaData();
        assertFalse(new MySQLQueryHeaderBuilder().build(queryResultMetaData, createDatabase(), queryResultMetaData.getColumnName(2), queryResultMetaData.getColumnLabel(2), 2).isPrimaryKey());
    }
    
    @Test
    void assertBuildWithNullDatabase() throws SQLException {
        QueryResultMetaData queryResultMetaData = createQueryResultMetaData();
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(queryResultMetaData, null, queryResultMetaData.getColumnName(1), queryResultMetaData.getColumnLabel(1), 1);
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
        QueryResultMetaData queryResultMetaData = createQueryResultMetaData();
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(queryResultMetaData, database, queryResultMetaData.getColumnName(1), queryResultMetaData.getColumnLabel(1), 1);
        assertFalse(actual.isPrimaryKey());
        assertThat(actual.getTable(), is("t_order"));
    }
    
    @Test
    void assertBuildWithoutDataNodeContainedRule() throws SQLException {
        QueryResultMetaData queryResultMetaData = createQueryResultMetaData();
        QueryHeader actual = new MySQLQueryHeaderBuilder().build(
                queryResultMetaData, mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS), queryResultMetaData.getColumnName(1), queryResultMetaData.getColumnLabel(1), 1);
        assertFalse(actual.isPrimaryKey());
        assertThat(actual.getTable(), is(actual.getTable()));
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereColumn column = new ShardingSphereColumn("order_id", Types.INTEGER, true, false, "int", false, true, false, false);
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
}

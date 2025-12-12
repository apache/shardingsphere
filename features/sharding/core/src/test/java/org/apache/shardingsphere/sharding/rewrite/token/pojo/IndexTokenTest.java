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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IndexTokenTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertToStringWithNotShardingTable() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        IndexToken indexToken = new IndexToken(0, 0, new IdentifierValue("foo_idx"), selectStatementContext, mock(ShardingRule.class), mockSchema());
        assertThat(indexToken.toString(mockRouteUnit()), is("foo_idx"));
    }
    
    @Test
    void assertToStringWithShardingTable() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.isShardingTable("foo_tbl")).thenReturn(true);
        IndexToken indexToken = new IndexToken(0, 0, new IdentifierValue("foo_idx"), selectStatementContext, rule, mockSchema());
        assertThat(indexToken.toString(mockRouteUnit()), is("foo_idx_foo_tbl_0"));
    }
    
    @Test
    void assertToStringWithShardingTableAndGeneratedIndex() {
        CommonSQLStatementContext sqlStatementContext = new CommonSQLStatementContext(new CreateIndexStatement(databaseType));
        IndexToken indexToken = new IndexToken(0, 0, new IdentifierValue("bar_idx"), sqlStatementContext, mock(ShardingRule.class), mockSchema());
        assertThat(indexToken.toString(mockRouteUnit()), is(" bar_idx_foo_tbl_0 "));
    }
    
    private ShardingSphereSchema mockSchema() {
        ShardingSphereSchema result = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        ShardingSphereTable table1 = mock(ShardingSphereTable.class);
        when(table1.getName()).thenReturn("no_tbl");
        ShardingSphereTable table2 = mock(ShardingSphereTable.class);
        when(table2.getName()).thenReturn("foo_tbl");
        when(table2.containsIndex("foo_idx")).thenReturn(true);
        when(result.getAllTables()).thenReturn(Arrays.asList(table1, table2));
        when(result.getTable("foo_tbl").containsIndex("foo_idx")).thenReturn(true);
        return result;
    }
    
    private RouteUnit mockRouteUnit() {
        RouteUnit result = mock(RouteUnit.class);
        when(result.getTableMappers()).thenReturn(Collections.singleton(new RouteMapper("foo_tbl", "foo_tbl_0")));
        when(result.getDataSourceMapper()).thenReturn(new RouteMapper("foo_db", "ds_0"));
        return result;
    }
}

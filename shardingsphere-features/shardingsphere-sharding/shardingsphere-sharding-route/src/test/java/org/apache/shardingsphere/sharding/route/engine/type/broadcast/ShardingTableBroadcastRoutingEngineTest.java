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

package org.apache.shardingsphere.sharding.route.engine.type.broadcast;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropIndexStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingTableBroadcastRoutingEngineTest {
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @Mock
    private TablesContext tablesContext;
    
    @Mock
    private SchemaMetaData schemaMetaData;
    
    @Mock
    private TableMetaData tableMetaData;
    
    private ShardingTableBroadcastRoutingEngine tableBroadcastRoutingEngine;
    
    private ShardingRule shardingRule;
    
    @Before
    public void setUp() {
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("t_order", "ds${0..1}.t_order_${0..2}");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(tableRuleConfig);
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.getTableNames()).thenReturn(Lists.newArrayList("t_order"));
        when(schemaMetaData.getAllTableNames()).thenReturn(Lists.newArrayList("t_order"));
        when(schemaMetaData.get("t_order")).thenReturn(tableMetaData);
        Map<String, IndexMetaData> indexMetaDataMap = new HashMap<>(1, 1);
        indexMetaDataMap.put("index_name", new IndexMetaData("index_name"));
        when(tableMetaData.getIndexes()).thenReturn(indexMetaDataMap);
        tableBroadcastRoutingEngine = new ShardingTableBroadcastRoutingEngine(schemaMetaData, sqlStatementContext);
        shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds0", "ds1"));
    }
    
    @Test
    public void assertRouteForNormalDDL() {
        DDLStatement ddlStatement = mock(DDLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(ddlStatement);
        RouteResult actual = tableBroadcastRoutingEngine.route(shardingRule);
        assertRouteResult(actual);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertRouteForNonExistMySQLDropIndex() {
        assertRouteForNonExistDropIndex(mock(MySQLDropIndexStatement.class));
    }

    @Test(expected = IllegalStateException.class)
    public void assertRouteForNonExistOracleDropIndex() {
        assertRouteForNonExistDropIndex(mock(OracleDropIndexStatement.class));
    }

    @Test(expected = IllegalStateException.class)
    public void assertRouteForNonExistPostgreSQLDropIndex() {
        assertRouteForNonExistDropIndex(mock(PostgreSQLDropIndexStatement.class));
    }

    @Test(expected = IllegalStateException.class)
    public void assertRouteForNonExistSQLServerDropIndex() {
        assertRouteForNonExistDropIndex(mock(SQLServerDropIndexStatement.class));
    }

    private void assertRouteForNonExistDropIndex(final DropIndexStatement indexStatement) {
        IndexSegment indexSegment = new IndexSegment(0, 0, new IdentifierValue("no_index"));
        when(indexStatement.getIndexes()).thenReturn(Lists.newArrayList(indexSegment));
        when(sqlStatementContext.getSqlStatement()).thenReturn(indexStatement);
        tableBroadcastRoutingEngine.route(shardingRule);
    }
    
    @Test
    public void assertRouteForMySQLDropIndex() {
        assertRouteForDropIndex(mock(MySQLDropIndexStatement.class));
    }

    @Test
    public void assertRouteForOracleDropIndex() {
        assertRouteForDropIndex(mock(OracleDropIndexStatement.class));
    }

    @Test
    public void assertRouteForPostgreSQLDropIndex() {
        assertRouteForDropIndex(mock(PostgreSQLDropIndexStatement.class));
    }

    @Test
    public void assertRouteForSQLServerDropIndex() {
        assertRouteForDropIndex(mock(SQLServerDropIndexStatement.class));
    }

    private void assertRouteForDropIndex(final DropIndexStatement indexStatement) {
        IndexSegment indexSegment = new IndexSegment(0, 0, new IdentifierValue("index_name"));
        when(indexStatement.getIndexes()).thenReturn(Lists.newArrayList(indexSegment));
        when(sqlStatementContext.getSqlStatement()).thenReturn(indexStatement);
        RouteResult actual = tableBroadcastRoutingEngine.route(shardingRule);
        assertRouteResult(actual);
    }
    
    private void assertRouteResult(final RouteResult actual) {
        assertThat(actual.getActualDataSourceNames().size(), is(2));
        assertThat(actual.getRouteUnits().size(), is(6));
        Iterator<RouteUnit> routeUnits = actual.getRouteUnits().iterator();
        assertRouteUnit(routeUnits.next(), "ds0", "t_order_0");
        assertRouteUnit(routeUnits.next(), "ds0", "t_order_1");
        assertRouteUnit(routeUnits.next(), "ds0", "t_order_2");
        assertRouteUnit(routeUnits.next(), "ds1", "t_order_0");
        assertRouteUnit(routeUnits.next(), "ds1", "t_order_1");
        assertRouteUnit(routeUnits.next(), "ds1", "t_order_2");
    }
    
    private void assertRouteUnit(final RouteUnit routeUnit, final String dataSourceName, final String actualTableName) {
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is(dataSourceName));
        assertThat(routeUnit.getTableMappers().size(), is(1));
        assertThat(routeUnit.getTableMappers().iterator().next(), is(new RouteMapper("t_order", actualTableName)));
    }
}

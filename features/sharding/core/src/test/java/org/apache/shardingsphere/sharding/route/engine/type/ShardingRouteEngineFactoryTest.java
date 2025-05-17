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

package org.apache.shardingsphere.sharding.route.engine.type;

import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dcl.GrantStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingInstanceBroadcastRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingTableBroadcastRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.complex.ShardingComplexRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.ignore.ShardingIgnoreRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.unicast.ShardingUnicastRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.OptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.sql92.dcl.SQL92GrantStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingRouteEngineFactoryTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock(extraInterfaces = TableAvailable.class)
    private SQLStatementContext sqlStatementContext;
    
    @Mock
    private TablesContext tablesContext;
    
    @Mock
    private ShardingConditions shardingConditions;
    
    private Collection<String> tableNames;
    
    private final ConfigurationProperties props = new ConfigurationProperties(new Properties());
    
    @BeforeEach
    void setUp() {
        when(((TableAvailable) sqlStatementContext).getTablesContext()).thenReturn(tablesContext);
        tableNames = new ArrayList<>();
        when(tablesContext.getTableNames()).thenReturn(tableNames);
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    @Test
    void assertNewInstanceForDDLWithShardingRule() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DDLStatement.class));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.emptyList(), props);
        assertThat(actual, instanceOf(ShardingTableBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForDALWithTables() {
        tableNames.add("tbl");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        DALStatement dalStatement = mock(DALStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, tableNames, props);
        assertThat(actual, instanceOf(ShardingUnicastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForDCLForSingleTable() {
        GrantStatement grantStatement = new SQL92GrantStatement();
        grantStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        GrantStatementContext sqlStatementContext = new GrantStatementContext(grantStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.singletonList("tbl"), props);
        assertThat(actual, instanceOf(ShardingTableBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForDCLForNoSingleTable() {
        DCLStatement dclStatement = mock(DCLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dclStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.emptyList(), props);
        assertThat(actual, instanceOf(ShardingInstanceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForSelectWithoutSingleTable() {
        SQLStatement sqlStatement = mock(SelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual =
                ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.emptyList(), props);
        assertThat(actual, instanceOf(ShardingUnicastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForSelectBroadcastTable() {
        SQLStatement sqlStatement = mock(SelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.emptyList(), props);
        assertThat(actual, instanceOf(ShardingUnicastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForAlwaysFalse() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.emptyList(), props);
        assertThat(actual, instanceOf(ShardingUnicastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForStandard() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        tableNames.add("");
        when(shardingRule.getShardingLogicTableNames(((TableAvailable) sqlStatementContext).getTablesContext().getTableNames())).thenReturn(tableNames);
        when(shardingRule.isAllShardingTables(tableNames)).thenReturn(true);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, tableNames, props);
        assertThat(actual, instanceOf(ShardingStandardRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForComplex() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        tableNames.add("1");
        tableNames.add("2");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, tableNames, props);
        assertThat(actual, instanceOf(ShardingComplexRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForShowCreateTableWithTableRule() {
        DALStatement dalStatement = mock(ShowCreateTableStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        tableNames.add("table_1");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, tableNames, props);
        assertThat(actual, instanceOf(ShardingUnicastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForShowColumnsWithTableRule() {
        DALStatement dalStatement = mock(ShowColumnsStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        tableNames.add("table_1");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, tableNames, props);
        assertThat(actual, instanceOf(ShardingUnicastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForSubqueryWithSameConditions() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        tableNames.add("t_order");
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(tableNames);
        when(sqlStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        ShardingRule shardingRule = mock(ShardingRule.class, RETURNS_DEEP_STUBS);
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        when(shardingRule.getShardingTable("t_order").getActualDataSourceNames()).thenReturn(Arrays.asList("ds_0", "ds_1"));
        when(shardingRule.isAllShardingTables(Collections.singletonList("t_order"))).thenReturn(true);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, tableNames, mock(ConfigurationProperties.class));
        assertThat(actual, instanceOf(ShardingStandardRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForOptimizeTableWithShardingTable() {
        OptimizeTableStatement sqlStatement = mock(OptimizeTableStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        tableNames.add("table_1");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, tableNames, props);
        assertThat(actual, instanceOf(ShardingTableBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForCursorStatementWithShardingTable() {
        CursorStatementContext cursorStatementContext = mock(CursorStatementContext.class, RETURNS_DEEP_STUBS);
        CursorStatement cursorStatement = mock(CursorStatement.class);
        when(cursorStatementContext.getSqlStatement()).thenReturn(cursorStatement);
        Collection<SimpleTableSegment> tableSegments = createSimpleTableSegments();
        Collection<String> tableNames = tableSegments.stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet());
        when(cursorStatementContext.getTablesContext().getSimpleTables()).thenReturn(tableSegments);
        when(cursorStatementContext.getTablesContext().getTableNames()).thenReturn(tableNames);
        when(cursorStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        when(shardingRule.isAllShardingTables(tableNames)).thenReturn(true);
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        QueryContext queryContext = new QueryContext(cursorStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, tableNames, props);
        assertThat(actual, instanceOf(ShardingStandardRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForCursorStatementWithSingleTable() {
        CursorStatementContext cursorStatementContext = mock(CursorStatementContext.class, RETURNS_DEEP_STUBS);
        CursorStatement cursorStatement = mock(CursorStatement.class);
        when(cursorStatementContext.getSqlStatement()).thenReturn(cursorStatement);
        Collection<SimpleTableSegment> tableSegments = createSimpleTableSegments();
        when(cursorStatementContext.getTablesContext().getSimpleTables()).thenReturn(tableSegments);
        when(cursorStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        QueryContext queryContext = new QueryContext(cursorStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.emptyList(), props);
        assertThat(actual, instanceOf(ShardingIgnoreRouteEngine.class));
    }
    
    private Collection<SimpleTableSegment> createSimpleTableSegments() {
        return Collections.singletonList(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
    }
}

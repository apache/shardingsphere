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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
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
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingRouteEngineFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @Mock
    private TablesContext tablesContext;
    
    @Mock
    private ShardingConditions shardingConditions;
    
    private final Collection<String> tableNames = new ArrayList<>();
    
    private final ConfigurationProperties props = new ConfigurationProperties(new Properties());
    
    @BeforeEach
    void setUp() {
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.getTableNames()).thenReturn(tableNames);
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    @Test
    void assertNewInstanceForDDLWithShardingRule() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DDLStatement.class, RETURNS_DEEP_STUBS));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.emptyList(), props);
        assertThat(actual, isA(ShardingTableBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForDALWithTables() {
        tableNames.add("tbl");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        DALStatement sqlStatement = mock(DALStatement.class);
        when(sqlStatement.getDatabaseType()).thenReturn(databaseType);
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes());
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, tableNames, props);
        assertThat(actual, isA(ShardingUnicastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForDCLForSingleTable() {
        GrantStatement sqlStatement = new GrantStatement(databaseType);
        sqlStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        sqlStatement.buildAttributes();
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.singletonList("tbl"), props);
        assertThat(actual, isA(ShardingTableBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForDCLForNoSingleTable() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DCLStatement.class));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.emptyList(), props);
        assertThat(actual, isA(ShardingInstanceBroadcastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForSelectWithoutSingleTable() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual =
                ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.emptyList(), props);
        assertThat(actual, isA(ShardingUnicastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForSelectBroadcastTable() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.emptyList(), props);
        assertThat(actual, isA(ShardingUnicastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForAlwaysFalse() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SQLStatement.class));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.emptyList(), props);
        assertThat(actual, isA(ShardingUnicastRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForStandard() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SQLStatement.class));
        tableNames.add("");
        when(shardingRule.getShardingLogicTableNames(sqlStatementContext.getTablesContext().getTableNames())).thenReturn(tableNames);
        when(shardingRule.isAllShardingTables(tableNames)).thenReturn(true);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, tableNames, props);
        assertThat(actual, isA(ShardingStandardRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForComplex() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SQLStatement.class));
        tableNames.add("1");
        tableNames.add("2");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, tableNames, props);
        assertThat(actual, isA(ShardingComplexRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForCommonDALStatement() {
        SQLStatement sqlStatement = mock(DALStatement.class);
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes());
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        tableNames.add("table_1");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, tableNames, props);
        assertThat(actual, isA(ShardingUnicastRouteEngine.class));
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
        assertThat(actual, isA(ShardingStandardRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForCursorStatementWithShardingTable() {
        CursorStatementContext cursorStatementContext = mock(CursorStatementContext.class, RETURNS_DEEP_STUBS);
        CursorStatement cursorStatement = new CursorStatement(databaseType, null, null);
        cursorStatement.buildAttributes();
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
        assertThat(actual, isA(ShardingStandardRouteEngine.class));
    }
    
    @Test
    void assertNewInstanceForCursorStatementWithSingleTable() {
        CursorStatementContext cursorStatementContext = mock(CursorStatementContext.class, RETURNS_DEEP_STUBS);
        CursorStatement cursorStatement = new CursorStatement(databaseType, null, null);
        cursorStatement.buildAttributes();
        when(cursorStatementContext.getSqlStatement()).thenReturn(cursorStatement);
        Collection<SimpleTableSegment> tableSegments = createSimpleTableSegments();
        when(cursorStatementContext.getTablesContext().getSimpleTables()).thenReturn(tableSegments);
        when(cursorStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        QueryContext queryContext = new QueryContext(cursorStatementContext, "", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock(ShardingSphereMetaData.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, queryContext, shardingConditions, Collections.emptyList(), props);
        assertThat(actual, isA(ShardingIgnoreRouteEngine.class));
    }
    
    private Collection<SimpleTableSegment> createSimpleTableSegments() {
        return Collections.singletonList(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
    }
}

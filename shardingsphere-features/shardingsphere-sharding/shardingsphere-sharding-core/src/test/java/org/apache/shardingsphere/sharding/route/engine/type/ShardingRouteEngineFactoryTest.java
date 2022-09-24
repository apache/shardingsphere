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

import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dcl.GrantStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingDataSourceGroupBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingDatabaseBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingInstanceBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingTableBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.complex.ShardingComplexRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.ignore.ShardingIgnoreRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.unicast.ShardingUnicastRoutingEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCloseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCursorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dcl.OracleGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dcl.PostgreSQLGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dcl.SQL92GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dcl.SQLServerGrantStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingRouteEngineFactoryTest {
    
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
    
    private Collection<String> tableNames;
    
    private final ConfigurationProperties props = new ConfigurationProperties(new Properties());
    
    @Before
    public void setUp() {
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        tableNames = new ArrayList<>();
        when(tablesContext.getTableNames()).thenReturn(tableNames);
    }
    
    @Test
    public void assertNewInstanceForTCL() {
        TCLStatement tclStatement = mock(TCLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(tclStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingDatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDDLWithShardingRule() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DDLStatement.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingTableBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALWithTables() {
        tableNames.add("tbl");
        when(shardingRule.getShardingRuleTableNames(tableNames)).thenReturn(tableNames);
        DALStatement dalStatement = mock(DALStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALWithoutTables() {
        DALStatement dalStatement = mock(DALStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingDataSourceGroupBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALShow() {
        DALStatement dalStatement = mock(MySQLShowDatabasesStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingDatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALSetForMySQL() {
        assertNewInstanceForDALSet(mock(MySQLSetStatement.class));
    }
    
    @Test
    public void assertNewInstanceForDALSetForPostgreSQL() {
        assertNewInstanceForDALSet(mock(PostgreSQLSetStatement.class));
    }
    
    private void assertNewInstanceForDALSet(final DALStatement dalStatement) {
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingDatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDCLForSingleTableForMySQL() {
        assertNewInstanceForDCLForSingleTableWithShardingRule(new MySQLGrantStatement());
        assertNewInstanceForDCLForSingleTableWithoutShardingRule(new MySQLGrantStatement());
    }
    
    @Test
    public void assertNewInstanceForDCLForSingleTableForOracle() {
        assertNewInstanceForDCLForSingleTableWithShardingRule(new OracleGrantStatement());
        assertNewInstanceForDCLForSingleTableWithoutShardingRule(new OracleGrantStatement());
    }
    
    @Test
    public void assertNewInstanceForDCLForSingleTableForPostgreSQL() {
        assertNewInstanceForDCLForSingleTableWithShardingRule(new PostgreSQLGrantStatement());
        assertNewInstanceForDCLForSingleTableWithoutShardingRule(new PostgreSQLGrantStatement());
    }
    
    @Test
    public void assertNewInstanceForDCLForSingleTableForSQLServer() {
        assertNewInstanceForDCLForSingleTableWithShardingRule(new SQLServerGrantStatement());
        assertNewInstanceForDCLForSingleTableWithoutShardingRule(new SQLServerGrantStatement());
    }
    
    @Test
    public void assertNewInstanceForDCLForSingleTableForSQL92() {
        assertNewInstanceForDCLForSingleTableWithShardingRule(new SQL92GrantStatement());
        assertNewInstanceForDCLForSingleTableWithoutShardingRule(new SQL92GrantStatement());
    }
    
    private void assertNewInstanceForDCLForSingleTableWithShardingRule(final GrantStatement grantStatement) {
        grantStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        GrantStatementContext sqlStatementContext = new GrantStatementContext(grantStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingIgnoreRoutingEngine.class));
    }
    
    private void assertNewInstanceForDCLForSingleTableWithoutShardingRule(final GrantStatement grantStatement) {
        grantStatement.getTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        GrantStatementContext sqlStatementContext = new GrantStatementContext(grantStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingIgnoreRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDCLForNoSingleTable() {
        DCLStatement dclStatement = mock(DCLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dclStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingInstanceBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSelectWithoutSingleTable() {
        SQLStatement sqlStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForInsertBroadcastTable() {
        when(shardingRule.isAllBroadcastTables(tableNames)).thenReturn(true);
        SQLStatement sqlStatement = mock(InsertStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingDatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSelectBroadcastTable() {
        when(shardingRule.isAllBroadcastTables(tableNames)).thenReturn(true);
        SQLStatement sqlStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForAlwaysFalse() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForStandard() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        tableNames.add("");
        when(shardingRule.getShardingLogicTableNames(sqlStatementContext.getTablesContext().getTableNames())).thenReturn(tableNames);
        when(shardingRule.isAllShardingTables(tableNames)).thenReturn(true);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingStandardRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForComplex() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        tableNames.add("1");
        tableNames.add("2");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingComplexRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowCreateTableWithTableRule() {
        DALStatement dalStatement = mock(MySQLShowCreateTableStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        tableNames.add("table_1");
        when(shardingRule.getShardingRuleTableNames(tableNames)).thenReturn(tableNames);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowColumnsWithTableRule() {
        DALStatement dalStatement = mock(MySQLShowColumnsStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        tableNames.add("table_1");
        when(shardingRule.getShardingRuleTableNames(tableNames)).thenReturn(tableNames);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSubqueryWithSameConditions() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        tableNames.add("t_order");
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(tableNames);
        ShardingRule shardingRule = mock(ShardingRule.class, RETURNS_DEEP_STUBS);
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        when(shardingRule.getTableRule("t_order").getActualDataSourceNames()).thenReturn(Arrays.asList("ds_0", "ds_1"));
        when(shardingRule.isAllShardingTables(Collections.singletonList("t_order"))).thenReturn(true);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, mock(ConfigurationProperties.class),
                new ConnectionContext());
        assertThat(actual, instanceOf(ShardingStandardRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForCreateResourceGroup() {
        MySQLCreateResourceGroupStatement resourceGroupStatement = mock(MySQLCreateResourceGroupStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(resourceGroupStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingInstanceBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSetResourceGroup() {
        MySQLSetResourceGroupStatement resourceGroupStatement = mock(MySQLSetResourceGroupStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(resourceGroupStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingInstanceBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForOptimizeTableWithShardingTable() {
        MySQLOptimizeTableStatement optimizeTableStatement = mock(MySQLOptimizeTableStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(optimizeTableStatement);
        tableNames.add("table_1");
        when(shardingRule.getShardingRuleTableNames(tableNames)).thenReturn(tableNames);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingTableBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForOptimizeTableWithSingleTable() {
        MySQLOptimizeTableStatement optimizeTableStatement = mock(MySQLOptimizeTableStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(optimizeTableStatement);
        tableNames.add("table_1");
        when(shardingRule.getShardingRuleTableNames(tableNames)).thenReturn(Collections.emptyList());
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, sqlStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingIgnoreRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForCursorStatementWithBroadcastTable() {
        CursorStatementContext cursorStatementContext = mock(CursorStatementContext.class);
        OpenGaussCursorStatement cursorStatement = mock(OpenGaussCursorStatement.class);
        when(cursorStatementContext.getSqlStatement()).thenReturn(cursorStatement);
        Collection<SimpleTableSegment> tableSegments = createSimpleTableSegments();
        Collection<String> tableNames = tableSegments.stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet());
        when(cursorStatementContext.getAllTables()).thenReturn(tableSegments);
        when(shardingRule.isAllBroadcastTables(tableNames)).thenReturn(true);
        when(shardingRule.getShardingRuleTableNames(tableNames)).thenReturn(tableNames);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, cursorStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForCursorStatementWithShardingTable() {
        CursorStatementContext cursorStatementContext = mock(CursorStatementContext.class);
        OpenGaussCursorStatement cursorStatement = mock(OpenGaussCursorStatement.class);
        when(cursorStatementContext.getSqlStatement()).thenReturn(cursorStatement);
        Collection<SimpleTableSegment> tableSegments = createSimpleTableSegments();
        Collection<String> tableNames = tableSegments.stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet());
        when(cursorStatementContext.getAllTables()).thenReturn(tableSegments);
        when(shardingRule.isAllShardingTables(tableNames)).thenReturn(true);
        when(shardingRule.getShardingRuleTableNames(tableNames)).thenReturn(tableNames);
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, cursorStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingStandardRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForCursorStatementWithSingleTable() {
        CursorStatementContext cursorStatementContext = mock(CursorStatementContext.class);
        OpenGaussCursorStatement cursorStatement = mock(OpenGaussCursorStatement.class);
        when(cursorStatementContext.getSqlStatement()).thenReturn(cursorStatement);
        Collection<SimpleTableSegment> tableSegments = createSimpleTableSegments();
        when(cursorStatementContext.getAllTables()).thenReturn(tableSegments);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, cursorStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingIgnoreRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForCloseAllStatement() {
        CloseStatementContext closeStatementContext = mock(CloseStatementContext.class, RETURNS_DEEP_STUBS);
        OpenGaussCloseStatement closeStatement = mock(OpenGaussCloseStatement.class);
        when(closeStatement.isCloseAll()).thenReturn(true);
        tableNames.add("t_order");
        when(closeStatementContext.getTablesContext().getTableNames()).thenReturn(tableNames);
        when(closeStatementContext.getSqlStatement()).thenReturn(closeStatement);
        when(shardingRule.getShardingRuleTableNames(tableNames)).thenReturn(tableNames);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, database, closeStatementContext, shardingConditions, props, new ConnectionContext());
        assertThat(actual, instanceOf(ShardingDatabaseBroadcastRoutingEngine.class));
    }
    
    private Collection<SimpleTableSegment> createSimpleTableSegments() {
        return Collections.singletonList(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
    }
}

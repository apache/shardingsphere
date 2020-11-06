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
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingDataSourceGroupBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingDatabaseBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingInstanceBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingTableBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.complex.ShardingComplexRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.single.SingleTableRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.unicast.ShardingUnicastRoutingEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingRouteEngineFactoryTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
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
        when(metaData.getSchema().getSchemaMetaData()).thenReturn(mock(PhysicalSchemaMetaData.class));
        tableNames = new ArrayList<>();
        when(tablesContext.getTableNames()).thenReturn(tableNames);
    }
    
    @Test
    public void assertNewInstanceForTCL() {
        TCLStatement tclStatement = mock(TCLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(tclStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingDatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDDLWithShardingRule() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DDLStatement.class));
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("tbl"));
        when(shardingRule.tableRuleExists(sqlStatementContext.getTablesContext().getTableNames())).thenReturn(true);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingTableBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDDLWithoutShardingRule() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DDLStatement.class));
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("tbl"));
        when(shardingRule.tableRuleExists(sqlStatementContext.getTablesContext().getTableNames())).thenReturn(false);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(SingleTableRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALWithTables() {
        tableNames.add("tbl");
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        DALStatement dalStatement = mock(DALStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALWithoutTables() {
        DALStatement dalStatement = mock(DALStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingDataSourceGroupBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALShow() {
        DALStatement dalStatement = mock(MySQLShowDatabasesStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
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
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
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
        grantStatement.getTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        GrantStatementContext sqlStatementContext = new GrantStatementContext(grantStatement);
        when(shardingRule.tableRuleExists(sqlStatementContext.getTablesContext().getTableNames())).thenReturn(true);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingTableBroadcastRoutingEngine.class));
    }
    
    private void assertNewInstanceForDCLForSingleTableWithoutShardingRule(final GrantStatement grantStatement) {
        grantStatement.getTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        GrantStatementContext sqlStatementContext = new GrantStatementContext(grantStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingTableBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDCLForNoSingleTable() {
        DCLStatement dclStatement = mock(DCLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dclStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingInstanceBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSingleTable() {
        tableNames.add("tbl");
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(false);
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(SingleTableRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSelectWithSingleTable() {
        tableNames.add("tbl");
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(false);
        SQLStatement sqlStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(SingleTableRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSelectWithoutSingleTable() {
        SQLStatement sqlStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForInsertBroadcastTable() {
        when(shardingRule.isAllBroadcastTables(tableNames)).thenReturn(true);
        SQLStatement sqlStatement = mock(InsertStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingDatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSelectBroadcastTable() {
        when(shardingRule.isAllBroadcastTables(tableNames)).thenReturn(true);
        SQLStatement sqlStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForAlwaysFalse() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForStandard() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        tableNames.add("");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingStandardRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForComplex() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        tableNames.add("1");
        tableNames.add("2");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingComplexRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowCreateTableWithTableRule() {
        DALStatement dalStatement = mock(MySQLShowCreateTableStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        tableNames.add("table_1");
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowCreateTableWithSingleTable() {
        DALStatement dalStatement = mock(MySQLShowCreateTableStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(false);
        tableNames.add("table_1");
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(SingleTableRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowColumnsWithTableRule() {
        DALStatement dalStatement = mock(MySQLShowColumnsStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        tableNames.add("table_1");
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowColumnsWithSingleTable() {
        DALStatement dalStatement = mock(MySQLShowColumnsStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(false);
        tableNames.add("table_1");
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(SingleTableRoutingEngine.class));
    }
    
}

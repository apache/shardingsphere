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

import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingDataSourceGroupBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingDatabaseBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingInstanceBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingTableBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.complex.ShardingComplexRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.unconfigured.ShardingUnconfiguredTablesRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.unicast.ShardingUnicastRoutingEngine;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dcl.GrantStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.dialect.mysql.ShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.dialect.mysql.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.dialect.mysql.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingRouteEngineFactoryTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
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
        when(shardingSphereMetaData.getSchema()).thenReturn(mock(RuleSchemaMetaData.class));
        tableNames = new ArrayList<>();
        when(tablesContext.getTableNames()).thenReturn(tableNames);
    }
    
    @Test
    public void assertNewInstanceForTCL() {
        TCLStatement tclStatement = mock(TCLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(tclStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingDatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDDL() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DDLStatement.class));
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingTableBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALWithTables() {
        tableNames.add("tbl");
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        DALStatement dalStatement = mock(DALStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALWithoutTables() {
        DALStatement dalStatement = mock(DALStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingDataSourceGroupBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALShow() {
        DALStatement dalStatement = mock(ShowDatabasesStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingDatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALSet() {
        DALStatement dalStatement = mock(SetStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingDatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDCLForSingleTable() {
        GrantStatement grantStatement = new GrantStatement();
        grantStatement.getTables().add(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        GrantStatementContext sqlStatementContext = new GrantStatementContext(grantStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingTableBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDCLForNoSingleTable() {
        DCLStatement dclStatement = mock(DCLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dclStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingInstanceBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForUnconfiguredTables() {
        tableNames.add("tbl");
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(false);
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnconfiguredTablesRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSelectWithUnconfiguredTables() {
        tableNames.add("tbl");
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(false);
        SQLStatement sqlStatement = mock(SelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnconfiguredTablesRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSelectWithoutUnconfiguredTables() {
        SQLStatement sqlStatement = mock(SelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForInsertBroadcastTable() {
        when(shardingRule.isAllBroadcastTables(tableNames)).thenReturn(true);
        SQLStatement sqlStatement = mock(InsertStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingDatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSelectBroadcastTable() {
        when(shardingRule.isAllBroadcastTables(tableNames)).thenReturn(true);
        SQLStatement sqlStatement = mock(SelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForAlwaysFalse() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForStandard() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        tableNames.add("");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
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
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingComplexRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowCreateTableWithTableRule() {
        DALStatement dalStatement = mock(ShowCreateTableStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        tableNames.add("table_1");
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowCreateTableWithUnconfiguredTables() {
        DALStatement dalStatement = mock(ShowCreateTableStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(false);
        tableNames.add("table_1");
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnconfiguredTablesRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowColumnsWithTableRule() {
        DALStatement dalStatement = mock(ShowColumnsStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        tableNames.add("table_1");
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowColumnsWithUnconfiguredTables() {
        DALStatement dalStatement = mock(ShowColumnsStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(false);
        tableNames.add("table_1");
        ShardingRouteEngine actual = ShardingRouteEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions, props);
        assertThat(actual, instanceOf(ShardingUnconfiguredTablesRoutingEngine.class));
    }
    
}

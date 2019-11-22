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

package org.apache.shardingsphere.core.route.router.sharding;

import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.relation.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.postgresql.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.TCLStatement;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.type.RoutingEngine;
import org.apache.shardingsphere.core.route.type.broadcast.DataSourceGroupBroadcastRoutingEngine;
import org.apache.shardingsphere.core.route.type.broadcast.DatabaseBroadcastRoutingEngine;
import org.apache.shardingsphere.core.route.type.broadcast.MasterInstanceBroadcastRoutingEngine;
import org.apache.shardingsphere.core.route.type.broadcast.TableBroadcastRoutingEngine;
import org.apache.shardingsphere.core.route.type.complex.ComplexRoutingEngine;
import org.apache.shardingsphere.core.route.type.defaultdb.DefaultDatabaseRoutingEngine;
import org.apache.shardingsphere.core.route.type.standard.StandardRoutingEngine;
import org.apache.shardingsphere.core.route.type.unicast.UnicastRoutingEngine;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class RoutingEngineFactoryTest {
    
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
    
    @Before
    public void setUp() {
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        tableNames = new ArrayList<>();
        when(tablesContext.getTableNames()).thenReturn(tableNames);
    }
    
    @Test
    public void assertNewInstanceForTCL() {
        TCLStatement tclStatement = mock(TCLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(tclStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(DatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDDL() {
        DDLStatement ddlStatement = mock(DDLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(ddlStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(TableBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALWithTables() {
        tableNames.add("");
        DALStatement dalStatement = mock(DALStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(UnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALWithoutTables() {
        DALStatement dalStatement = mock(DALStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(DataSourceGroupBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALShow() {
        DALStatement dalStatement = mock(ShowDatabasesStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(DatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDALSet() {
        DALStatement dalStatement = mock(SetStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(DatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDCLForSingleTable() {
        when(tablesContext.isEmpty()).thenReturn(false);
        when(tablesContext.getSingleTableName()).thenReturn("");
        DCLStatement dclStatement = mock(DCLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dclStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(TableBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDCLForNoSingleTable() {
        when(tablesContext.isEmpty()).thenReturn(true);
        DCLStatement dclStatement = mock(DCLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dclStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(MasterInstanceBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForDefaultDataSource() {
        when(shardingRule.isAllInDefaultDataSource(tableNames)).thenReturn(true);
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(DefaultDatabaseRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSelectWithDefaultDataSource() {
        when(shardingRule.isAllInDefaultDataSource(tableNames)).thenReturn(false);
        when(shardingRule.hasDefaultDataSourceName()).thenReturn(true);
        SQLStatement sqlStatement = mock(SelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(DefaultDatabaseRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSelectWithoutDefaultDataSource() {
        when(shardingRule.isAllInDefaultDataSource(tableNames)).thenReturn(false);
        when(shardingRule.hasDefaultDataSourceName()).thenReturn(false);
        SQLStatement sqlStatement = mock(SelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(UnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForInsertBroadcastTable() {
        when(shardingRule.isAllBroadcastTables(tableNames)).thenReturn(true);
        SQLStatement sqlStatement = mock(InsertStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(DatabaseBroadcastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForSelectBroadcastTable() {
        when(shardingRule.isAllBroadcastTables(tableNames)).thenReturn(true);
        SQLStatement sqlStatement = mock(SelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(UnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForAlwaysFalse() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(UnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForStandard() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        tableNames.add("");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(StandardRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForComplex() {
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        tableNames.add("1");
        tableNames.add("2");
        when(shardingRule.getShardingLogicTableNames(tableNames)).thenReturn(tableNames);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(ComplexRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForTableRuleNotExists() {
        when(shardingRule.isAllInDefaultDataSource(tableNames)).thenReturn(false);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(false);
        SQLStatement sqlStatement = mock(SelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        tableNames.add("table_1");
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(UnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowCreateTableWithTableRule() {
        DALStatement dalStatement = mock(ShowCreateTableStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        tableNames.add("table_1");
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(UnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowCreateTableWithDefaultDataSource() {
        DALStatement dalStatement = mock(ShowCreateTableStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(false);
        when(shardingRule.hasDefaultDataSourceName()).thenReturn(true);
        tableNames.add("table_1");
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(DefaultDatabaseRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowColumnsWithTableRule() {
        DALStatement dalStatement = mock(ShowColumnsStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        tableNames.add("table_1");
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(UnicastRoutingEngine.class));
    }
    
    @Test
    public void assertNewInstanceForShowColumnsWithDefaultDataSource() {
        DALStatement dalStatement = mock(ShowColumnsStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(dalStatement);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(false);
        when(shardingRule.hasDefaultDataSourceName()).thenReturn(true);
        tableNames.add("table_1");
        RoutingEngine actual = RoutingEngineFactory.newInstance(shardingRule, shardingSphereMetaData, sqlStatementContext, shardingConditions);
        assertThat(actual, instanceOf(DefaultDatabaseRoutingEngine.class));
    }
    
}

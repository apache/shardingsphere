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

package org.apache.shardingsphere.single.rule;

import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleRuleTest {
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String VIEW_TYPE = "VIEW";
    
    private static final String SYSTEM_TABLE_TYPE = "SYSTEM TABLE";
    
    private static final String SYSTEM_VIEW_TYPE = "SYSTEM VIEW";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private Map<String, DataSource> dataSourceMap;
    
    private SingleRuleConfiguration ruleConfig;
    
    @BeforeEach
    void setUp() throws SQLException {
        dataSourceMap = new LinkedHashMap<>(2, 1F);
        dataSourceMap.put("foo_ds", mockDataSource("foo_ds", Arrays.asList("employee", "t_order_0")));
        dataSourceMap.put("bar_ds", mockDataSource("bar_ds", Arrays.asList("student", "t_order_1")));
        Collection<String> configuredTables = new LinkedList<>(Arrays.asList("foo_ds.employee", "foo_ds.t_order_0", "bar_ds.student", "bar_ds.t_order_1"));
        ruleConfig = new SingleRuleConfiguration(configuredTables, null);
    }
    
    private DataSource mockDataSource(final String dataSourceName, final List<String> tableNames) throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getCatalog()).thenReturn(dataSourceName);
        when(connection.getMetaData().getURL()).thenReturn(String.format("jdbc:h2:mem:%s", dataSourceName));
        DataSource result = new MockedDataSource(connection);
        ResultSet resultSet = mockResultSet(tableNames);
        when(result.getConnection().getMetaData().getTables(dataSourceName, null, null, new String[]{TABLE_TYPE, VIEW_TYPE, SYSTEM_TABLE_TYPE, SYSTEM_VIEW_TYPE})).thenReturn(resultSet);
        return result;
    }
    
    private ResultSet mockResultSet(final List<String> tableNames) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        Collection<Boolean> nextResults = tableNames.stream().map(each -> true).collect(Collectors.toList());
        nextResults.add(false);
        when(result.next()).thenReturn(true, nextResults.toArray(new Boolean[tableNames.size()]));
        String firstTableName = tableNames.get(0);
        String[] nextTableNames = tableNames.subList(1, tableNames.size()).toArray(new String[tableNames.size() - 1]);
        when(result.getString(TABLE_NAME)).thenReturn(firstTableName, nextTableNames);
        return result;
    }
    
    @Test
    void assertGetSingleTableDataNodes() {
        TableContainedRule tableContainedRule = mock(TableContainedRule.class, RETURNS_DEEP_STUBS);
        when(tableContainedRule.getDistributedTableMapper().getTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(tableContainedRule.getActualTableMapper().getTableNames()).thenReturn(Arrays.asList("t_order_0", "t_order_1"));
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(tableContainedRule));
        Map<String, Collection<DataNode>> actual = singleRule.getSingleTableDataNodes();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsKey("employee"));
        assertTrue(actual.containsKey("student"));
    }
    
    @Test
    void assertGetSingleTableDataNodesWithUpperCase() {
        TableContainedRule tableContainedRule = mock(TableContainedRule.class, RETURNS_DEEP_STUBS);
        when(tableContainedRule.getDistributedTableMapper().getTableNames()).thenReturn(Collections.singletonList("T_ORDER"));
        when(tableContainedRule.getActualTableMapper().getTableNames()).thenReturn(Arrays.asList("T_ORDER_0", "T_ORDER_1"));
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(tableContainedRule));
        Map<String, Collection<DataNode>> actual = singleRule.getSingleTableDataNodes();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsKey("employee"));
        assertTrue(actual.containsKey("student"));
    }
    
    @Test
    void assertFindSingleTableDataNode() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        Optional<DataNode> actual = singleRule.findTableDataNode(DefaultDatabase.LOGIC_NAME, "employee");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDataSourceName(), is("foo_ds"));
        assertThat(actual.get().getTableName(), is("employee"));
    }
    
    @Test
    void assertFindSingleTableDataNodeWithUpperCase() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        Optional<DataNode> actual = singleRule.findTableDataNode(DefaultDatabase.LOGIC_NAME, "EMPLOYEE");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDataSourceName(), is("foo_ds"));
        assertThat(actual.get().getTableName(), is("employee"));
    }
    
    @Test
    void assertIsSingleTablesInSameDataSource() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        Collection<QualifiedTable> singleTableNames = new LinkedList<>();
        singleTableNames.add(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "employee"));
        assertTrue(singleRule.isSingleTablesInSameDataSource(singleTableNames));
    }
    
    @Test
    void assertIsAllTablesInSameDataSource() {
        Collection<QualifiedTable> singleTableNames = new LinkedList<>();
        singleTableNames.add(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "employee"));
        RouteMapper dataSourceMapper = new RouteMapper("foo_ds", null);
        Collection<RouteMapper> tableMappers = new LinkedList<>();
        tableMappers.add(dataSourceMapper);
        RouteContext routeContext = new RouteContext();
        routeContext.putRouteUnit(dataSourceMapper, tableMappers);
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        assertTrue(singleRule.isAllTablesInSameDataSource(routeContext, singleTableNames));
    }
    
    @Test
    void assertAssignNewDataSourceName() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRuleConfiguration singleRuleConfig = new SingleRuleConfiguration();
        singleRuleConfig.setDefaultDataSource("foo_ds");
        SingleRule singleRule = new SingleRule(singleRuleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        assertThat(singleRule.assignNewDataSourceName(), is("foo_ds"));
    }
    
    @Test
    void assertGetSingleTableNames() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        Collection<QualifiedTable> tableNames = new LinkedList<>();
        tableNames.add(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "employee"));
        assertThat(singleRule.getSingleTableNames(tableNames).iterator().next().getSchemaName(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(singleRule.getSingleTableNames(tableNames).iterator().next().getTableName(), is("employee"));
    }
    
    @Test
    void assertPut() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        String tableName = "teacher";
        String dataSourceName = "foo_ds";
        singleRule.put(dataSourceName, DefaultDatabase.LOGIC_NAME, tableName);
        Collection<QualifiedTable> tableNames = new LinkedList<>();
        tableNames.add(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "teacher"));
        assertThat(singleRule.getSingleTableNames(tableNames).iterator().next().getSchemaName(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(singleRule.getSingleTableNames(tableNames).iterator().next().getTableName(), is("teacher"));
        assertTrue(singleRule.getLogicTableMapper().contains("employee"));
        assertTrue(singleRule.getLogicTableMapper().contains("student"));
        assertTrue(singleRule.getLogicTableMapper().contains("t_order_0"));
        assertTrue(singleRule.getLogicTableMapper().contains("t_order_1"));
        assertTrue(singleRule.getLogicTableMapper().contains("teacher"));
    }
    
    @Test
    void assertRemove() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        String tableName = "employee";
        singleRule.remove(DefaultDatabase.LOGIC_NAME, tableName);
        Collection<QualifiedTable> tableNames = new LinkedList<>();
        tableNames.add(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "employee"));
        assertTrue(singleRule.getSingleTableNames(tableNames).isEmpty());
        assertTrue(singleRule.getLogicTableMapper().contains("student"));
        assertTrue(singleRule.getLogicTableMapper().contains("t_order_0"));
        assertTrue(singleRule.getLogicTableMapper().contains("t_order_1"));
    }
    
    @Test
    void assertGetAllDataNodes() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        assertTrue(singleRule.getAllDataNodes().containsKey("employee"));
        assertTrue(singleRule.getAllDataNodes().containsKey("student"));
        assertTrue(singleRule.getAllDataNodes().containsKey("t_order_0"));
        assertTrue(singleRule.getAllDataNodes().containsKey("t_order_1"));
    }
    
    @Test
    void assertGetDataNodesByTableName() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        Collection<DataNode> actual = singleRule.getDataNodesByTableName("EMPLOYEE");
        assertThat(actual.size(), is(1));
        DataNode dataNode = actual.iterator().next();
        assertThat(dataNode.getDataSourceName(), is("foo_ds"));
        assertThat(dataNode.getTableName(), is("employee"));
    }
    
    @Test
    void assertFindFirstActualTable() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        String logicTable = "employee";
        assertFalse(singleRule.findFirstActualTable(logicTable).isPresent());
    }
    
    @Test
    void assertIsNeedAccumulate() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        assertFalse(singleRule.isNeedAccumulate(Collections.emptyList()));
    }
    
    @Test
    void assertFindLogicTableByActualTable() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        String actualTable = "student";
        assertFalse(singleRule.findLogicTableByActualTable(actualTable).isPresent());
    }
    
    @Test
    void assertFindActualTableByCatalog() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleRule singleRule = new SingleRule(ruleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        String catalog = "employee";
        String logicTable = "t_order_0";
        assertFalse(singleRule.findActualTableByCatalog(catalog, logicTable).isPresent());
    }
}

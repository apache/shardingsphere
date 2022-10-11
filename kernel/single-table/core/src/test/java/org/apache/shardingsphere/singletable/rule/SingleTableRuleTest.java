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

package org.apache.shardingsphere.singletable.rule;

import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;

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
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SingleTableRuleTest {
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String VIEW_TYPE = "VIEW";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private Map<String, DataSource> dataSourceMap;
    
    @Before
    public void setUp() throws SQLException {
        dataSourceMap = new LinkedHashMap<>(2, 1);
        dataSourceMap.put("foo_ds", mockDataSource("foo_ds", Arrays.asList("employee", "t_order_0")));
        dataSourceMap.put("bar_ds", mockDataSource("bar_ds", Arrays.asList("student", "t_order_1")));
    }
    
    private DataSource mockDataSource(final String dataSourceName, final List<String> tableNames) throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getCatalog()).thenReturn(dataSourceName);
        when(connection.getMetaData().getURL()).thenReturn(String.format("jdbc:h2:mem:%s", dataSourceName));
        DataSource result = new MockedDataSource(connection);
        ResultSet resultSet = mockResultSet(tableNames);
        when(result.getConnection().getMetaData().getTables(dataSourceName, null, null, new String[]{TABLE_TYPE, VIEW_TYPE})).thenReturn(resultSet);
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
    public void assertGetSingleTableDataNodes() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        when(dataNodeContainedRule.getAllTables()).thenReturn(Arrays.asList("t_order", "t_order_0", "t_order_1"));
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        Map<String, Collection<DataNode>> actual = singleTableRule.getSingleTableDataNodes();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsKey("employee"));
        assertTrue(actual.containsKey("student"));
    }
    
    @Test
    public void assertGetSingleTableDataNodesWithUpperCase() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        when(dataNodeContainedRule.getAllTables()).thenReturn(Arrays.asList("T_ORDER", "T_ORDER_0", "T_ORDER_1"));
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        Map<String, Collection<DataNode>> actual = singleTableRule.getSingleTableDataNodes();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsKey("employee"));
        assertTrue(actual.containsKey("student"));
    }
    
    @Test
    public void assertFindSingleTableDataNode() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        Optional<DataNode> actual = singleTableRule.findSingleTableDataNode(DefaultDatabase.LOGIC_NAME, "employee");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDataSourceName(), is("foo_ds"));
        assertThat(actual.get().getTableName(), is("employee"));
    }
    
    @Test
    public void assertFindSingleTableDataNodeWithUpperCase() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        Optional<DataNode> actual = singleTableRule.findSingleTableDataNode(DefaultDatabase.LOGIC_NAME, "EMPLOYEE");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDataSourceName(), is("foo_ds"));
        assertThat(actual.get().getTableName(), is("employee"));
    }
    
    @Test
    public void assertIsSingleTablesInSameDataSource() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        Collection<QualifiedTable> singleTableNames = new LinkedList<>();
        singleTableNames.add(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "employee"));
        assertTrue(singleTableRule.isSingleTablesInSameDataSource(singleTableNames));
    }
    
    @Test
    public void assertIsAllTablesInSameDataSource() {
        Collection<QualifiedTable> singleTableNames = new LinkedList<>();
        singleTableNames.add(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "employee"));
        RouteMapper dataSourceMapper = new RouteMapper("foo_ds", null);
        Collection<RouteMapper> tableMappers = new LinkedList<>();
        tableMappers.add(dataSourceMapper);
        RouteContext routeContext = new RouteContext();
        routeContext.putRouteUnit(dataSourceMapper, tableMappers);
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        assertTrue(singleTableRule.isAllTablesInSameDataSource(routeContext, singleTableNames));
    }
    
    @Test
    public void assertAssignNewDataSourceName() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRuleConfiguration singleTableRuleConfig = new SingleTableRuleConfiguration();
        singleTableRuleConfig.setDefaultDataSource("foo_ds");
        SingleTableRule singleTableRule = new SingleTableRule(singleTableRuleConfig, DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        assertThat(singleTableRule.assignNewDataSourceName(), is("foo_ds"));
    }
    
    @Test
    public void assertGetSingleTableNames() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        Collection<QualifiedTable> tableNames = new LinkedList<>();
        tableNames.add(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "employee"));
        assertThat(singleTableRule.getSingleTableNames(tableNames).iterator().next().getSchemaName(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(singleTableRule.getSingleTableNames(tableNames).iterator().next().getTableName(), is("employee"));
    }
    
    @Test
    public void assertPut() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        String tableName = "teacher";
        String dataSourceName = "foo_ds";
        singleTableRule.put(dataSourceName, DefaultDatabase.LOGIC_NAME, tableName);
        Collection<QualifiedTable> tableNames = new LinkedList<>();
        tableNames.add(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "teacher"));
        assertThat(singleTableRule.getSingleTableNames(tableNames).iterator().next().getSchemaName(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(singleTableRule.getSingleTableNames(tableNames).iterator().next().getTableName(), is("teacher"));
        assertTrue(singleTableRule.getAllTables().contains("employee"));
        assertTrue(singleTableRule.getAllTables().contains("student"));
        assertTrue(singleTableRule.getAllTables().contains("t_order_0"));
        assertTrue(singleTableRule.getAllTables().contains("t_order_1"));
        assertTrue(singleTableRule.getAllTables().contains("teacher"));
    }
    
    @Test
    public void assertRemove() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        String tableName = "employee";
        singleTableRule.remove(DefaultDatabase.LOGIC_NAME, tableName);
        Collection<QualifiedTable> tableNames = new LinkedList<>();
        tableNames.add(new QualifiedTable(DefaultDatabase.LOGIC_NAME, "employee"));
        assertTrue(singleTableRule.getSingleTableNames(tableNames).isEmpty());
        assertTrue(singleTableRule.getAllTables().contains("student"));
        assertTrue(singleTableRule.getAllTables().contains("t_order_0"));
        assertTrue(singleTableRule.getAllTables().contains("t_order_1"));
    }
    
    @Test
    public void assertGetAllDataNodes() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        assertTrue(singleTableRule.getAllDataNodes().containsKey("employee"));
        assertTrue(singleTableRule.getAllDataNodes().containsKey("student"));
        assertTrue(singleTableRule.getAllDataNodes().containsKey("t_order_0"));
        assertTrue(singleTableRule.getAllDataNodes().containsKey("t_order_1"));
    }
    
    @Test
    public void assertGetDataNodesByTableName() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        Collection<DataNode> actual = singleTableRule.getDataNodesByTableName("EMPLOYEE");
        assertThat(actual.size(), is(1));
        DataNode dataNode = actual.iterator().next();
        assertThat(dataNode.getDataSourceName(), is("foo_ds"));
        assertThat(dataNode.getTableName(), is("employee"));
    }
    
    @Test
    public void assertFindFirstActualTable() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        String logicTable = "employee";
        assertFalse(singleTableRule.findFirstActualTable(logicTable).isPresent());
    }
    
    @Test
    public void assertIsNeedAccumulate() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        assertFalse(singleTableRule.isNeedAccumulate(Collections.emptyList()));
    }
    
    @Test
    public void assertFindLogicTableByActualTable() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        String actualTable = "student";
        assertFalse(singleTableRule.findLogicTableByActualTable(actualTable).isPresent());
    }
    
    @Test
    public void assertFindActualTableByCatalog() {
        DataNodeContainedRule dataNodeContainedRule = mock(DataNodeContainedRule.class);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, Collections.singleton(dataNodeContainedRule));
        String catalog = "employee";
        String logicTable = "t_order_0";
        assertFalse(singleTableRule.findActualTableByCatalog(catalog, logicTable).isPresent());
    }
}

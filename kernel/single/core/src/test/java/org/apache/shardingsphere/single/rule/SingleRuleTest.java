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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
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
    
    private static final String PARTITIONED_TABLE_TYPE = "PARTITIONED TABLE";
    
    private static final String VIEW_TYPE = "VIEW";
    
    private static final String SYSTEM_TABLE_TYPE = "SYSTEM TABLE";
    
    private static final String SYSTEM_VIEW_TYPE = "SYSTEM VIEW";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private Map<String, DataSource> dataSourceMap;
    
    private SingleRuleConfiguration ruleConfig;
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @BeforeEach
    void setUp() throws SQLException {
        dataSourceMap = new LinkedHashMap<>(2, 1F);
        dataSourceMap.put("foo_ds", mockDataSource("foo_ds", Arrays.asList("employee", "t_order_0")));
        dataSourceMap.put("bar_ds", mockDataSource("bar_ds", Arrays.asList("student", "t_order_1")));
        Collection<String> configuredTables = new LinkedList<>(Arrays.asList("foo_ds.employee", "foo_ds.t_order_0", "bar_ds.student", "bar_ds.t_order_1"));
        ruleConfig = new SingleRuleConfiguration(configuredTables, null);
    }
    
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    private DataSource mockDataSource(final String dataSourceName, final List<String> tableNames) throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getCatalog()).thenReturn(dataSourceName);
        when(connection.getMetaData().getURL()).thenReturn(String.format("jdbc:mock://127.0.0.1/%s", dataSourceName));
        DataSource result = new MockedDataSource(connection);
        ResultSet resultSet = mockResultSet(tableNames);
        when(result.getConnection().getMetaData().getTables(dataSourceName, null, null, new String[]{TABLE_TYPE, PARTITIONED_TABLE_TYPE, VIEW_TYPE, SYSTEM_TABLE_TYPE, SYSTEM_VIEW_TYPE}))
                .thenReturn(resultSet);
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
        TableMapperRuleAttribute ruleAttribute = mock(TableMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getDistributedTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(ruleAttribute.getActualTableNames()).thenReturn(Arrays.asList("t_order_0", "t_order_1"));
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class);
        when(builtRule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        Map<String, Collection<DataNode>> actual = singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsKey("employee"));
        assertTrue(actual.containsKey("student"));
    }
    
    @Test
    void assertGetSingleTableDataNodesWithUpperCase() {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class);
        TableMapperRuleAttribute ruleAttribute = mock(TableMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getDistributedTableNames()).thenReturn(Collections.singleton("T_ORDER"));
        when(ruleAttribute.getActualTableNames()).thenReturn(Arrays.asList("T_ORDER_0", "T_ORDER_1"));
        when(builtRule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        Map<String, Collection<DataNode>> actual = singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsKey("employee"));
        assertTrue(actual.containsKey("student"));
    }
    
    @Test
    void assertFindSingleTableDataNode() {
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        Optional<DataNode> actual = singleRule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).findTableDataNode("foo_db", "employee");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDataSourceName(), is("foo_ds"));
        assertThat(actual.get().getTableName(), is("employee"));
    }
    
    @Test
    void assertFindSingleTableDataNodeWithUpperCase() {
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        Optional<DataNode> actual = singleRule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).findTableDataNode("foo_db", "EMPLOYEE");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDataSourceName(), is("foo_ds"));
        assertThat(actual.get().getTableName(), is("employee"));
    }
    
    @Test
    void assertIsAllTablesInSameDataSource() {
        Collection<QualifiedTable> singleTables = new LinkedList<>();
        singleTables.add(new QualifiedTable("foo_db", "employee"));
        RouteMapper dataSourceMapper = new RouteMapper("foo_ds", null);
        Collection<RouteMapper> tableMappers = new LinkedList<>();
        tableMappers.add(dataSourceMapper);
        RouteContext routeContext = new RouteContext();
        routeContext.putRouteUnit(dataSourceMapper, tableMappers);
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        assertTrue(singleRule.isAllTablesInSameComputeNode(routeContext.getOriginalDataNodes().stream().flatMap(Collection::stream).collect(Collectors.toList()), singleTables));
    }
    
    @Test
    void assertAssignNewDataSourceName() {
        SingleRuleConfiguration singleRuleConfig = new SingleRuleConfiguration();
        singleRuleConfig.setDefaultDataSource("foo_ds");
        SingleRule singleRule = new SingleRule(
                singleRuleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        assertThat(singleRule.assignNewDataSourceName(), is("foo_ds"));
    }
    
    @Test
    void assertGetSingleTables() {
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        Collection<QualifiedTable> tableNames = new LinkedList<>();
        tableNames.add(new QualifiedTable("foo_db", "employee"));
        assertThat(singleRule.getSingleTables(tableNames).iterator().next().getSchemaName(), is("foo_db"));
        assertThat(singleRule.getSingleTables(tableNames).iterator().next().getTableName(), is("employee"));
    }
    
    @Test
    void assertPut() {
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        String tableName = "teacher";
        String dataSourceName = "foo_ds";
        singleRule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).put(dataSourceName, "foo_db", tableName);
        Collection<QualifiedTable> tableNames = new LinkedList<>();
        tableNames.add(new QualifiedTable("foo_db", "teacher"));
        assertThat(singleRule.getSingleTables(tableNames).iterator().next().getSchemaName(), is("foo_db"));
        assertThat(singleRule.getSingleTables(tableNames).iterator().next().getTableName(), is("teacher"));
        assertTrue(singleRule.getAttributes().getAttribute(TableMapperRuleAttribute.class).getLogicTableNames().contains("employee"));
        assertTrue(singleRule.getAttributes().getAttribute(TableMapperRuleAttribute.class).getLogicTableNames().contains("student"));
        assertTrue(singleRule.getAttributes().getAttribute(TableMapperRuleAttribute.class).getLogicTableNames().contains("t_order_0"));
        assertTrue(singleRule.getAttributes().getAttribute(TableMapperRuleAttribute.class).getLogicTableNames().contains("t_order_1"));
        assertTrue(singleRule.getAttributes().getAttribute(TableMapperRuleAttribute.class).getLogicTableNames().contains("teacher"));
    }
    
    @Test
    void assertRemove() {
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        String tableName = "employee";
        singleRule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).remove("foo_db", tableName);
        Collection<QualifiedTable> tableNames = new LinkedList<>();
        tableNames.add(new QualifiedTable("foo_db", "employee"));
        assertTrue(singleRule.getSingleTables(tableNames).isEmpty());
        assertTrue(singleRule.getAttributes().getAttribute(TableMapperRuleAttribute.class).getLogicTableNames().contains("student"));
        assertTrue(singleRule.getAttributes().getAttribute(TableMapperRuleAttribute.class).getLogicTableNames().contains("t_order_0"));
        assertTrue(singleRule.getAttributes().getAttribute(TableMapperRuleAttribute.class).getLogicTableNames().contains("t_order_1"));
    }
    
    @Test
    void assertGetAllDataNodes() {
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        assertTrue(singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes().containsKey("employee"));
        assertTrue(singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes().containsKey("student"));
        assertTrue(singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes().containsKey("t_order_0"));
        assertTrue(singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes().containsKey("t_order_1"));
    }
    
    @Test
    void assertGetDataNodesByTableName() {
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        Collection<DataNode> actual = singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getDataNodesByTableName("EMPLOYEE");
        assertThat(actual.size(), is(1));
        DataNode dataNode = actual.iterator().next();
        assertThat(dataNode.getDataSourceName(), is("foo_ds"));
        assertThat(dataNode.getTableName(), is("employee"));
    }
    
    @Test
    void assertFindFirstActualTable() {
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        String logicTable = "employee";
        assertFalse(singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).findFirstActualTable(logicTable).isPresent());
    }
    
    @Test
    void assertIsNeedAccumulate() {
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        assertFalse(singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).isNeedAccumulate(Collections.emptyList()));
    }
    
    @Test
    void assertFindLogicTableByActualTable() {
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        String actualTable = "student";
        assertFalse(singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).findLogicTableByActualTable(actualTable).isPresent());
    }
    
    @Test
    void assertFindActualTableByCatalog() {
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
        String catalog = "employee";
        String logicTable = "t_order_0";
        assertFalse(singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).findActualTableByCatalog(catalog, logicTable).isPresent());
    }
}

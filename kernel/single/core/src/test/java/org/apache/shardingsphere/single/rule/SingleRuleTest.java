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
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.IndexSQLStatementAttribute;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleRuleTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private Map<String, DataSource> dataSourceMap;
    
    private SingleRuleConfiguration ruleConfig;
    
    @BeforeEach
    void setUp() throws SQLException {
        dataSourceMap = new LinkedHashMap<>(2, 1F);
        dataSourceMap.put("foo_ds", mockDataSource("foo_ds", Arrays.asList("employee", "t_order_0")));
        dataSourceMap.put("bar_ds", mockDataSource("bar_ds", Arrays.asList("student", "t_order_1")));
        ruleConfig = new SingleRuleConfiguration(new LinkedList<>(Arrays.asList("foo_ds.employee", "foo_ds.t_order_0", "bar_ds.student", "bar_ds.t_order_1")), null);
    }
    
    private DataSource mockDataSource(final String dataSourceName, final List<String> tableNames) throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getCatalog()).thenReturn(dataSourceName);
        when(connection.getMetaData().getURL()).thenReturn(String.format("jdbc:mock://127.0.0.1/%s", dataSourceName));
        ResultSet resultSet = mockResultSet(tableNames);
        when(connection.getMetaData().getTables(dataSourceName, null, null, new String[]{"TABLE", "PARTITIONED TABLE", "VIEW", "SYSTEM TABLE", "SYSTEM VIEW"})).thenReturn(resultSet);
        return new MockedDataSource(connection);
    }
    
    private ResultSet mockResultSet(final List<String> tableNames) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        Collection<Boolean> nextResults = tableNames.stream().map(each -> true).collect(Collectors.toList());
        nextResults.add(false);
        when(result.next()).thenReturn(true, nextResults.toArray(new Boolean[tableNames.size()]));
        String firstTableName = tableNames.get(0);
        String[] nextTableNames = tableNames.subList(1, tableNames.size()).toArray(new String[tableNames.size() - 1]);
        when(result.getString("TABLE_NAME")).thenReturn(firstTableName, nextTableNames);
        return result;
    }
    
    @Test
    void assertOrder() {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        assertThat(new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule)).getOrder(), is(SingleOrder.ORDER));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSingleTableDataNodesArguments")
    void assertGetSingleTableDataNodes(final String name, final String distributedTableName, final String actualTableName1, final String actualTableName2) {
        TableMapperRuleAttribute ruleAttribute = mock(TableMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        Collection<String> distributedTableNames = Collections.singleton(distributedTableName);
        when(ruleAttribute.getDistributedTableNames()).thenReturn(distributedTableNames);
        when(ruleAttribute.getActualTableNames()).thenReturn(Arrays.asList(actualTableName1, actualTableName2));
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class);
        when(builtRule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        Map<String, Collection<DataNode>> actual = singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsKey("employee"));
        assertTrue(actual.containsKey("student"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("findSingleTableDataNodeArguments")
    void assertFindSingleTableDataNode(final String name, final String actualTableName, final String expectedDataSourceName, final String expectedTableName) {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        Optional<DataNode> actual = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule)).getAttributes().getAttribute(MutableDataNodeRuleAttribute.class)
                .findTableDataNode("foo_db", actualTableName);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDataSourceName(), is(expectedDataSourceName));
        assertThat(actual.get().getTableName(), is(expectedTableName));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isAllTablesInSameComputeNodeArguments")
    void assertIsAllTablesInSameComputeNode(final String name, final Collection<DataNode> dataNodes, final Collection<QualifiedTable> singleTables, final boolean expectedMatched) {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        assertThat(singleRule.isAllTablesInSameComputeNode(dataNodes, singleTables), is(expectedMatched));
    }
    
    @Test
    void assertAssignNewDataSourceName() {
        SingleRuleConfiguration singleRuleConfig = new SingleRuleConfiguration();
        singleRuleConfig.setDefaultDataSource("foo_ds");
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        SingleRule singleRule = new SingleRule(singleRuleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        assertThat(singleRule.assignNewDataSourceName(), is("foo_ds"));
    }
    
    @Test
    void assertAssignNewDataSourceNameWithoutDefaultDataSource() {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        assertTrue(singleRule.getDataSourceNames().contains(singleRule.assignNewDataSourceName()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSingleTablesArguments")
    void assertGetSingleTables(final String name, final QualifiedTable inputTable, final boolean expectedPresent, final String expectedSchemaName, final String expectedTableName) {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        Collection<QualifiedTable> actualTables = singleRule.getSingleTables(Collections.singleton(inputTable));
        if (expectedPresent) {
            QualifiedTable actualTable = actualTables.iterator().next();
            assertThat(actualTable.getSchemaName(), is(expectedSchemaName));
            assertThat(actualTable.getTableName(), is(expectedTableName));
        } else {
            assertTrue(actualTables.isEmpty());
        }
    }
    
    @Test
    void assertPut() {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        String tableName = "teacher";
        String dataSourceName = "foo_ds";
        Collection<QualifiedTable> tableNames = new LinkedList<>();
        tableNames.add(new QualifiedTable("foo_db", "teacher"));
        singleRule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).put(dataSourceName, "foo_db", tableName);
        Collection<QualifiedTable> actualTables = singleRule.getSingleTables(tableNames);
        QualifiedTable actualTable = actualTables.iterator().next();
        assertThat(actualTable.getSchemaName(), is("foo_db"));
        assertThat(actualTable.getTableName(), is("teacher"));
        Collection<String> actualLogicTableNames = singleRule.getAttributes().getAttribute(TableMapperRuleAttribute.class).getLogicTableNames();
        assertTrue(actualLogicTableNames.contains("employee"));
        assertTrue(actualLogicTableNames.contains("student"));
        assertTrue(actualLogicTableNames.contains("t_order_0"));
        assertTrue(actualLogicTableNames.contains("t_order_1"));
        assertTrue(actualLogicTableNames.contains("teacher"));
    }
    
    @Test
    void assertRemove() {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        String tableName = "employee";
        Collection<QualifiedTable> tableNames = Collections.singleton(new QualifiedTable("foo_db", "employee"));
        singleRule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).remove("foo_db", tableName);
        assertTrue(singleRule.getSingleTables(tableNames).isEmpty());
        Collection<String> actualLogicTableNames = singleRule.getAttributes().getAttribute(TableMapperRuleAttribute.class).getLogicTableNames();
        assertTrue(actualLogicTableNames.contains("student"));
        assertTrue(actualLogicTableNames.contains("t_order_0"));
        assertTrue(actualLogicTableNames.contains("t_order_1"));
    }
    
    @Test
    void assertGetAllDataNodes() {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        assertFalse(singleRule.getConfiguration().getDefaultDataSource().isPresent());
        Map<String, Collection<DataNode>> actualDataNodes = singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes();
        assertTrue(actualDataNodes.containsKey("employee"));
        assertTrue(actualDataNodes.containsKey("student"));
        assertTrue(actualDataNodes.containsKey("t_order_0"));
        assertTrue(actualDataNodes.containsKey("t_order_1"));
    }
    
    @Test
    void assertGetDataNodesByTableName() {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        assertTrue(singleRule.getDataSourceNames().contains("foo_ds"));
        Collection<DataNode> actual = singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getDataNodesByTableName("EMPLOYEE");
        assertThat(actual.size(), is(1));
        DataNode dataNode = actual.iterator().next();
        assertThat(dataNode.getDataSourceName(), is("foo_ds"));
        assertThat(dataNode.getTableName(), is("employee"));
    }
    
    @Test
    void assertFindFirstActualTable() {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        assertTrue(singleRule.getSingleTableDataNodes().containsKey("employee"));
        assertFalse(singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).findFirstActualTable("employee").isPresent());
    }
    
    @Test
    void assertIsNeedAccumulate() {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        assertFalse(singleRule.getConfiguration().getDefaultDataSource().isPresent());
        assertFalse(singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).isNeedAccumulate(Collections.emptyList()));
    }
    
    @Test
    void assertFindLogicTableByActualTable() {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        assertTrue(singleRule.getDataSourceNames().contains("bar_ds"));
        assertFalse(singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).findLogicTableByActualTable("student").isPresent());
    }
    
    @Test
    void assertFindActualTableByCatalog() {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        assertTrue(singleRule.getSingleTableDataNodes().containsKey("student"));
        assertFalse(singleRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).findActualTableByCatalog("employee", "t_order_0").isPresent());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getQualifiedTablesArguments")
    void assertGetQualifiedTables(final String name, final boolean useSimpleTableSegments, final boolean useIndexAttribute) {
        ShardingSphereRule builtRule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        SingleRule singleRule = new SingleRule(ruleConfig, "foo_db", databaseType, dataSourceMap, Collections.singleton(builtRule));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        String defaultSchemaName = new DatabaseTypeRegistry(databaseType).getDefaultSchemaName("foo_db");
        if (useSimpleTableSegments) {
            Collection<SimpleTableSegment> simpleTableSegments = createSimpleTableSegments();
            when(sqlStatementContext.getTablesContext().getSimpleTables()).thenReturn(simpleTableSegments);
            when(sqlStatementContext.getSqlStatement().getAttributes().findAttribute(IndexSQLStatementAttribute.class)).thenReturn(Optional.empty());
        } else {
            when(sqlStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.emptyList());
            Optional<IndexSQLStatementAttribute> indexAttribute = useIndexAttribute ? Optional.of(createIndexAttribute(database, defaultSchemaName)) : Optional.empty();
            when(sqlStatementContext.getSqlStatement().getAttributes().findAttribute(IndexSQLStatementAttribute.class)).thenReturn(indexAttribute);
        }
        Collection<QualifiedTable> actual = singleRule.getQualifiedTables(sqlStatementContext, database);
        if (useSimpleTableSegments) {
            assertThat(actual.size(), is(2));
            assertTrue(actual.contains(new QualifiedTable(defaultSchemaName, "employee")));
            assertTrue(actual.contains(new QualifiedTable("custom_schema", "student")));
            return;
        }
        if (useIndexAttribute) {
            assertThat(actual.size(), is(1));
            assertTrue(actual.contains(new QualifiedTable(defaultSchemaName, "employee")));
            return;
        }
        assertTrue(actual.isEmpty());
    }
    
    private static Collection<SimpleTableSegment> createSimpleTableSegments() {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        result.add(createSimpleTableSegment(null, "employee"));
        result.add(createSimpleTableSegment("custom_schema", "student"));
        return result;
    }
    
    private static SimpleTableSegment createSimpleTableSegment(final String schemaName, final String tableName) {
        SimpleTableSegment result = mock(SimpleTableSegment.class, RETURNS_DEEP_STUBS);
        when(result.getTableName().getIdentifier().getValue()).thenReturn(tableName);
        if (null == schemaName) {
            when(result.getOwner()).thenReturn(Optional.empty());
        } else {
            OwnerSegment ownerSegment = mock(OwnerSegment.class, RETURNS_DEEP_STUBS);
            when(ownerSegment.getIdentifier().getValue()).thenReturn(schemaName);
            when(result.getOwner()).thenReturn(Optional.of(ownerSegment));
        }
        return result;
    }
    
    private static IndexSQLStatementAttribute createIndexAttribute(final ShardingSphereDatabase database, final String schemaName) {
        IndexSegment indexSegment = mock(IndexSegment.class, RETURNS_DEEP_STUBS);
        when(indexSegment.getOwner()).thenReturn(Optional.empty());
        when(indexSegment.getIndexName().getIdentifier().getValue()).thenReturn("idx_employee");
        IndexSQLStatementAttribute result = mock(IndexSQLStatementAttribute.class);
        when(result.getIndexes()).thenReturn(Collections.singleton(indexSegment));
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(database.getSchema(schemaName)).thenReturn(schema);
        when(schema.getAllTables()).thenReturn(Collections.singleton(table));
        when(table.containsIndex("idx_employee")).thenReturn(true);
        when(table.getName()).thenReturn("employee");
        return result;
    }
    
    private static Stream<Arguments> getSingleTableDataNodesArguments() {
        return Stream.of(
                Arguments.of("lower case distributed table", "t_order", "t_order_0", "t_order_1"),
                Arguments.of("upper case distributed table", "T_ORDER", "T_ORDER_0", "T_ORDER_1"),
                Arguments.of("mixed case distributed table", "t_Order", "t_order_0", "T_ORDER_1"));
    }
    
    private static Stream<Arguments> findSingleTableDataNodeArguments() {
        return Stream.of(
                Arguments.of("lower case table", "employee", "foo_ds", "employee"),
                Arguments.of("upper case table", "EMPLOYEE", "foo_ds", "employee"),
                Arguments.of("other table", "student", "bar_ds", "student"));
    }
    
    private static Stream<Arguments> isAllTablesInSameComputeNodeArguments() {
        return Stream.of(
                Arguments.of("all in same compute node", Collections.singleton(new DataNode("foo_ds", "foo_db", "employee")),
                        Collections.singleton(new QualifiedTable("foo_db", "employee")), true),
                Arguments.of("single tables are in same compute node", Collections.singleton(new DataNode("foo_ds", "foo_db", "employee")),
                        Arrays.asList(new QualifiedTable("foo_db", "employee"), new QualifiedTable("foo_db", "t_order_0")), true),
                Arguments.of("single tables are in different compute node", Collections.emptyList(),
                        Arrays.asList(new QualifiedTable("foo_db", "employee"), new QualifiedTable("foo_db", "student")), false),
                Arguments.of("route data nodes are in different compute node", Collections.singleton(new DataNode("bar_ds", "foo_db", "employee")),
                        Collections.singleton(new QualifiedTable("foo_db", "employee")), false),
                Arguments.of("single table metadata not found", Collections.emptyList(),
                        Collections.singleton(new QualifiedTable("foo_db", "missing_table")), true));
    }
    
    private static Stream<Arguments> getSingleTablesArguments() {
        return Stream.of(
                Arguments.of("table exists in same schema", new QualifiedTable("foo_db", "employee"), true, "foo_db", "employee"),
                Arguments.of("table exists in different schema", new QualifiedTable("bar_db", "employee"), false, null, null),
                Arguments.of("table does not exist", new QualifiedTable("foo_db", "missing_table"), false, null, null));
    }
    
    private static Stream<Arguments> getQualifiedTablesArguments() {
        return Stream.of(
                Arguments.of("qualified tables from simple table segments", true, false),
                Arguments.of("qualified tables from index attribute", false, true),
                Arguments.of("empty qualified tables when no tables and no index", false, false));
    }
}

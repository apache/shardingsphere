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

package org.apache.shardingsphere.single.datanode;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.exception.SingleTablesLoadingException;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SingleTableDataNodeLoaderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private Map<String, DataSource> dataSourceMap;
    
    @BeforeEach
    void setUp() throws SQLException {
        dataSourceMap = new LinkedHashMap<>(2, 1F);
        dataSourceMap.put("foo_ds", mockDataSource("foo_ds", Arrays.asList("foo_tbl1", "foo_tbl2")));
        dataSourceMap.put("bar_ds", mockDataSource("bar_ds", Arrays.asList("bar_tbl1", "bar_tbl2")));
    }
    
    private DataSource mockDataSource(final String dataSourceName, final List<String> tableNames) throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getCatalog()).thenReturn(dataSourceName);
        ResultSet resultSet = mockResultSet(tableNames);
        when(connection.getMetaData().getTables(dataSourceName, null, null, new String[]{"TABLE", "PARTITIONED TABLE", "VIEW", "SYSTEM TABLE", "SYSTEM VIEW"})).thenReturn(resultSet);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/foo_ds");
        return new MockedDataSource(connection);
    }
    
    private ResultSet mockResultSet(final List<String> tableNames) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        String firstTableName = tableNames.get(0);
        Collection<String> remainTableNames = tableNames.subList(1, tableNames.size());
        Collection<Boolean> remainNextResults = remainTableNames.stream().map(each -> true).collect(Collectors.toList());
        remainNextResults.add(false);
        when(result.next()).thenReturn(true, remainNextResults.toArray(new Boolean[tableNames.size()]));
        when(result.getString("TABLE_NAME")).thenReturn(firstTableName, remainTableNames.toArray(new String[tableNames.size() - 1]));
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadWithConfiguredTableExpressionArguments")
    void assertLoadWithConfiguredTableExpressions(final String name, final Collection<ShardingSphereRule> builtRules,
                                                  final Collection<String> configuredTables, final Map<String, Collection<String>> expectedTableDataSources) {
        Map<String, Collection<DataNode>> actual = SingleTableDataNodeLoader.load("foo_db", databaseType, dataSourceMap, builtRules, configuredTables);
        assertThat(new TreeSet<>(actual.keySet()), is(new TreeSet<>(expectedTableDataSources.keySet())));
        assertTableDataSources(actual, expectedTableDataSources);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadWithConfiguredTableMapRuleArguments")
    void assertLoadWithConfiguredTableMapRules(final String name, final Collection<String> splitTables,
                                               final Collection<DataNode> configuredDataNodes, final Map<String, Collection<String>> expectedTableDataSources) {
        try (MockedStatic<SingleTableLoadUtils> mockedSingleTableLoadUtils = mockStatic(SingleTableLoadUtils.class, Answers.CALLS_REAL_METHODS)) {
            mockedSingleTableLoadUtils.when(() -> SingleTableLoadUtils.splitTableLines(Collections.singleton("foo_ds.foo_tbl2"))).thenReturn(splitTables);
            mockedSingleTableLoadUtils.when(() -> SingleTableLoadUtils.convertToDataNodes("foo_db", databaseType, splitTables)).thenReturn(configuredDataNodes);
            Map<String, Collection<DataNode>> actual = SingleTableDataNodeLoader.load("foo_db", databaseType, dataSourceMap, Collections.emptyList(), Collections.singleton("foo_ds.foo_tbl2"));
            assertThat(new TreeSet<>(actual.keySet()), is(new TreeSet<>(expectedTableDataSources.keySet())));
            assertTableDataSources(actual, expectedTableDataSources);
        }
    }
    
    private void assertTableDataSources(final Map<String, Collection<DataNode>> actual, final Map<String, Collection<String>> expectedTableDataSources) {
        for (Entry<String, Collection<String>> entry : expectedTableDataSources.entrySet()) {
            Collection<String> actualDataSourceNames = actual.get(entry.getKey()).stream().map(DataNode::getDataSourceName).collect(Collectors.toCollection(TreeSet::new));
            assertThat(actualDataSourceNames, is(new TreeSet<>(entry.getValue())));
        }
    }
    
    @Test
    void assertLoadWithFeatureRequiredSingleTables() {
        Map<String, Collection<DataNode>> actual = SingleTableDataNodeLoader.load(
                "foo_db", databaseType, dataSourceMap, Collections.singleton(createRule(Collections.emptyList(), Collections.singleton("foo_tbl1"))), Collections.singleton("foo_ds.foo_tbl2"));
        assertTrue(actual.containsKey("foo_tbl1"));
        assertTrue(actual.containsKey("foo_tbl2"));
        assertFalse(actual.containsKey("bar_tbl1"));
        assertThat(actual.get("foo_tbl1").iterator().next().getDataSourceName(), is("foo_ds"));
        assertThat(actual.get("foo_tbl2").iterator().next().getDataSourceName(), is("foo_ds"));
    }
    
    @Test
    void assertLoadWithEmptyConfiguredTablesAndFeatureRequiredSingleTables() {
        assertTrue(SingleTableDataNodeLoader.load(
                "foo_db", databaseType, dataSourceMap, Collections.singleton(createRule(Collections.emptyList(), Collections.singleton("foo_tbl1"))), Collections.emptyList()).isEmpty());
    }
    
    @Test
    void assertLoadWithDataSourceMap() {
        Map<String, Collection<DataNode>> actual = SingleTableDataNodeLoader.load("foo_db", dataSourceMap, Collections.singleton("foo_tbl2"));
        assertThat(new TreeSet<>(actual.keySet()), is(new TreeSet<>(Arrays.asList("foo_tbl1", "bar_tbl1", "bar_tbl2"))));
        assertThat(new TreeSet<>(actual.get("bar_tbl1").stream().map(DataNode::getDataSourceName).collect(Collectors.toList())), is(new TreeSet<>(Collections.singleton("bar_ds"))));
    }
    
    @Test
    void assertLoadSchemaTableNames() {
        Map<String, Collection<String>> actual = SingleTableDataNodeLoader.loadSchemaTableNames("foo_db", databaseType, dataSourceMap.get("foo_ds"), "foo_ds", Collections.singleton("foo_tbl2"));
        assertThat(new TreeSet<>(actual.keySet()), is(new TreeSet<>(Collections.singleton("foo_db"))));
        assertThat(new TreeSet<>(actual.get("foo_db")), is(new TreeSet<>(Collections.singleton("foo_tbl1"))));
    }
    
    @Test
    void assertLoadSchemaTableNamesWithSQLException() throws SQLException {
        SQLException expected = new SQLException("mocked_ex");
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(expected);
        SingleTablesLoadingException actual = assertThrows(SingleTablesLoadingException.class,
                () -> SingleTableDataNodeLoader.loadSchemaTableNames("foo_db", databaseType, dataSource, "foo_ds", Collections.emptyList()));
        assertThat(actual.getCause(), is(expected));
    }
    
    private static Stream<Arguments> loadWithConfiguredTableExpressionArguments() {
        Map<String, Collection<String>> excludedTablesExpectedDataSources = new LinkedHashMap<>(2, 1F);
        excludedTablesExpectedDataSources.put("foo_tbl2", Collections.singleton("foo_ds"));
        excludedTablesExpectedDataSources.put("bar_tbl2", Collections.singleton("bar_ds"));
        Map<String, Collection<String>> allSchemaTablesExpectedDataSources = new LinkedHashMap<>(4, 1F);
        allSchemaTablesExpectedDataSources.put("foo_tbl1", Collections.singleton("foo_ds"));
        allSchemaTablesExpectedDataSources.put("foo_tbl2", Collections.singleton("foo_ds"));
        allSchemaTablesExpectedDataSources.put("bar_tbl1", Collections.singleton("bar_ds"));
        allSchemaTablesExpectedDataSources.put("bar_tbl2", Collections.singleton("bar_ds"));
        return Stream.of(
                Arguments.arguments("empty configured tables", Collections.emptyList(), Collections.emptyList(), Collections.emptyMap()),
                Arguments.arguments("all tables with excluded tables", Collections.singleton(createRule(Arrays.asList("foo_tbl1", "bar_tbl1", "unused_tbl"), Collections.emptyList())),
                        Collections.singleton("*.*"), createExpectedTableDataSources(excludedTablesExpectedDataSources)),
                Arguments.arguments("all schema tables", Collections.emptyList(), Collections.singleton("*.*.*"), createExpectedTableDataSources(allSchemaTablesExpectedDataSources)));
    }
    
    private static ShardingSphereRule createRule(final Collection<String> distributedTableNames, final Collection<String> enhancedTableNames) {
        ShardingSphereRule result = mock(ShardingSphereRule.class);
        TableMapperRuleAttribute ruleAttribute = mock(TableMapperRuleAttribute.class);
        when(ruleAttribute.getDistributedTableNames()).thenReturn(distributedTableNames);
        when(ruleAttribute.getActualTableNames()).thenReturn(Collections.emptyList());
        when(ruleAttribute.getEnhancedTableNames()).thenReturn(enhancedTableNames);
        when(result.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        return result;
    }
    
    private static Stream<Arguments> loadWithConfiguredTableMapRuleArguments() {
        Map<String, Collection<String>> wildcardExpectedDataSources = new LinkedHashMap<>(2, 1F);
        wildcardExpectedDataSources.put("foo_tbl1", Collections.singleton("foo_ds"));
        wildcardExpectedDataSources.put("foo_tbl2", Collections.singleton("foo_ds"));
        return Stream.of(
                Arguments.arguments("configured data source not found", new LinkedHashSet<>(Collections.singleton("other_ds.foo_tbl2")),
                        Collections.singleton(new DataNode("other_ds", "foo_db", "foo_tbl2")), Collections.emptyMap()),
                Arguments.arguments("configured wildcard schema", new LinkedHashSet<>(Collections.singleton("foo_ds.foo_tbl2")),
                        Collections.singleton(new DataNode("foo_ds", "*", "foo_tbl2")),
                        createExpectedTableDataSources(wildcardExpectedDataSources)),
                Arguments.arguments("configured schema not matched", new LinkedHashSet<>(Collections.singleton("foo_ds.foo_tbl2")),
                        Collections.singleton(new DataNode("foo_ds", "other_schema", "foo_tbl2")), Collections.emptyMap()),
                Arguments.arguments("configured table wildcard", new LinkedHashSet<>(Collections.singleton("foo_ds.foo_tbl2")),
                        Collections.singleton(new DataNode("foo_ds", "foo_db", "*")),
                        createExpectedTableDataSources(wildcardExpectedDataSources)));
    }
    
    private static Map<String, Collection<String>> createExpectedTableDataSources(final Map<String, Collection<String>> tableDataSources) {
        Map<String, Collection<String>> result = new LinkedHashMap<>(tableDataSources.size(), 1F);
        for (Entry<String, Collection<String>> each : tableDataSources.entrySet()) {
            result.put(each.getKey(), new LinkedHashSet<>(each.getValue()));
        }
        return result;
    }
}

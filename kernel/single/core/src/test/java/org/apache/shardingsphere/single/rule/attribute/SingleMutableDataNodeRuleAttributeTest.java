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

package org.apache.shardingsphere.single.rule.attribute;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleMutableDataNodeRuleAttributeTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("putArguments")
    void assertPut(final String name, final String inputDataSourceName, final Collection<String> initialTables, final Collection<String> expectedTables, final boolean expectedDataNodePresent) {
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(new LinkedList<>(initialTables), null);
        Map<String, Collection<DataNode>> singleTableDataNodes = createSingleTableDataNodes(Collections.emptyList());
        SingleTableMapperRuleAttribute tableMapperRuleAttribute = new SingleTableMapperRuleAttribute(singleTableDataNodes.values());
        SingleMutableDataNodeRuleAttribute ruleAttribute = createRuleAttribute(ruleConfig, Collections.singleton("foo_ds"), singleTableDataNodes, tableMapperRuleAttribute);
        ruleAttribute.put(inputDataSourceName, "foo_schema", "foo_tbl");
        assertThat(ruleConfig.getTables(), is(new LinkedList<>(expectedTables)));
        if (expectedDataNodePresent) {
            assertTrue(singleTableDataNodes.containsKey("foo_tbl"));
            assertTrue(tableMapperRuleAttribute.getLogicTableNames().contains("foo_tbl"));
            DataNode actualDataNode = singleTableDataNodes.get("foo_tbl").iterator().next();
            assertThat(actualDataNode.getDataSourceName(), is(inputDataSourceName));
            assertThat(actualDataNode.getSchemaName(), is("foo_schema"));
            assertThat(actualDataNode.getTableName(), is("foo_tbl"));
        } else {
            assertFalse(singleTableDataNodes.containsKey("foo_tbl"));
            assertFalse(tableMapperRuleAttribute.getLogicTableNames().contains("foo_tbl"));
        }
    }
    
    @Test
    void assertRemoveWithSchemaName() {
        DataNode dataNode = new DataNode("foo_ds", "foo_schema", "foo_tbl");
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(
                new LinkedList<>(Collections.singleton(SingleTableLoadUtils.getDataNodeString(DATABASE_TYPE, "foo_ds", "foo_schema", "foo_tbl"))), null);
        Map<String, Collection<DataNode>> singleTableDataNodes = createSingleTableDataNodes(Collections.singleton(dataNode));
        SingleTableMapperRuleAttribute tableMapperRuleAttribute = new SingleTableMapperRuleAttribute(singleTableDataNodes.values());
        SingleMutableDataNodeRuleAttribute ruleAttribute = createRuleAttribute(ruleConfig, Collections.singleton("foo_ds"), singleTableDataNodes, tableMapperRuleAttribute);
        ruleAttribute.remove("foo_schema".toUpperCase(), "foo_tbl");
        assertFalse(singleTableDataNodes.containsKey("foo_tbl"));
        assertFalse(ruleConfig.getTables().contains(SingleTableLoadUtils.getDataNodeString(DATABASE_TYPE, "foo_ds", "foo_schema", "foo_tbl")));
        assertFalse(tableMapperRuleAttribute.getLogicTableNames().contains("foo_tbl"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("removeWithSchemaNamesArguments")
    void assertRemoveWithSchemaNames(final String name, final Collection<DataNode> initialDataNodes, final Collection<String> schemaNames,
                                     final int expectedDataNodeCount, final Collection<String> expectedTables, final boolean expectedTableInMap, final boolean expectedLogicTableExists) {
        SingleRuleConfiguration configuration = new SingleRuleConfiguration(new LinkedList<>(createDataNodeStrings(initialDataNodes)), null);
        Map<String, Collection<DataNode>> singleTableDataNodes = createSingleTableDataNodes(initialDataNodes);
        SingleTableMapperRuleAttribute tableMapperRuleAttribute = new SingleTableMapperRuleAttribute(singleTableDataNodes.values());
        SingleMutableDataNodeRuleAttribute ruleAttribute = createRuleAttribute(configuration, Collections.singleton("foo_ds"), singleTableDataNodes, tableMapperRuleAttribute);
        ruleAttribute.remove(schemaNames, "foo_tbl");
        assertThat(new LinkedList<>(configuration.getTables()), is(new LinkedList<>(expectedTables)));
        if (expectedTableInMap) {
            assertTrue(singleTableDataNodes.containsKey("foo_tbl"));
            assertThat(singleTableDataNodes.get("foo_tbl").size(), is(expectedDataNodeCount));
        } else {
            assertFalse(singleTableDataNodes.containsKey("foo_tbl"));
        }
        assertThat(tableMapperRuleAttribute.getLogicTableNames().contains("foo_tbl"), is(expectedLogicTableExists));
    }
    
    @Test
    void assertRemoveWithSchemaNamesWhenTableNotFound() {
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(new LinkedList<>(Collections.emptyList()), null);
        Map<String, Collection<DataNode>> singleTableDataNodes = createSingleTableDataNodes(Collections.emptyList());
        SingleTableMapperRuleAttribute tableMapperRuleAttribute = new SingleTableMapperRuleAttribute(singleTableDataNodes.values());
        SingleMutableDataNodeRuleAttribute ruleAttribute = createRuleAttribute(ruleConfig, Collections.singleton("foo_ds"), singleTableDataNodes, tableMapperRuleAttribute);
        ruleAttribute.remove(Collections.singleton("foo_schema"), "foo_tbl");
        assertTrue(singleTableDataNodes.isEmpty());
        assertTrue(ruleConfig.getTables().isEmpty());
        assertTrue(tableMapperRuleAttribute.getLogicTableNames().isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("findTableDataNodeArguments")
    void assertFindTableDataNode(final String name, final Collection<DataNode> initialDataNodes, final String schemaName, final String tableName,
                                 final boolean expectedPresent, final String expectedDataSourceName) {
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(new LinkedList<>(createDataNodeStrings(initialDataNodes)), null);
        Map<String, Collection<DataNode>> singleTableDataNodes = createSingleTableDataNodes(initialDataNodes);
        SingleTableMapperRuleAttribute tableMapperRuleAttribute = new SingleTableMapperRuleAttribute(singleTableDataNodes.values());
        SingleMutableDataNodeRuleAttribute ruleAttribute = createRuleAttribute(ruleConfig, Collections.singleton("foo_ds"), singleTableDataNodes, tableMapperRuleAttribute);
        Optional<DataNode> actual = ruleAttribute.findTableDataNode(schemaName, tableName);
        if (expectedPresent) {
            assertTrue(actual.isPresent());
            assertThat(actual.get().getDataSourceName(), is(expectedDataSourceName));
            assertThat(actual.get().getTableName(), is("foo_tbl"));
        } else {
            assertFalse(actual.isPresent());
        }
    }
    
    @Test
    void assertReloadRule() {
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(new LinkedList<>(Collections.emptyList()), null);
        Map<String, Collection<DataNode>> singleTableDataNodes = createSingleTableDataNodes(Collections.emptyList());
        SingleTableMapperRuleAttribute tableMapperRuleAttribute = new SingleTableMapperRuleAttribute(singleTableDataNodes.values());
        SingleMutableDataNodeRuleAttribute ruleAttribute = createRuleAttribute(ruleConfig, Collections.emptyList(), singleTableDataNodes, tableMapperRuleAttribute);
        ShardingSphereRule actual = ruleAttribute.reloadRule(ruleConfig, "foo_db", Collections.emptyMap(), Collections.emptyList());
        assertThat(((SingleRule) actual).getConfiguration(), is(ruleConfig));
    }
    
    private static Stream<Arguments> putArguments() {
        String allTablesNode = SingleTableLoadUtils.getAllTablesNodeStr(DATABASE_TYPE);
        String allTablesNodeFromDataSource = SingleTableLoadUtils.getAllTablesNodeStrFromDataSource(DATABASE_TYPE, "foo_ds", "foo_schema");
        String dataNodeString = SingleTableLoadUtils.getDataNodeString(DATABASE_TYPE, "foo_ds", "foo_schema", "foo_tbl");
        String existingDataNodeString = SingleTableLoadUtils.getDataNodeString(DATABASE_TYPE, "bar_ds", "bar_schema", "bar_table");
        return Stream.of(
                Arguments.of("skip unmanaged data source", "bar_ds",
                        Collections.singleton(existingDataNodeString), Collections.singleton(existingDataNodeString), false),
                Arguments.of("skip table config when all tables wildcard exists", "foo_ds",
                        Collections.singleton(allTablesNode), Collections.singleton(allTablesNode), true),
                Arguments.of("skip table config when data source wildcard exists", "foo_ds",
                        Collections.singleton(allTablesNodeFromDataSource), Collections.singleton(allTablesNodeFromDataSource), true),
                Arguments.of("append table config when absent", "foo_ds",
                        Collections.singleton(existingDataNodeString), Arrays.asList(existingDataNodeString, dataNodeString), true),
                Arguments.of("do not append duplicated table config", "foo_ds",
                        Collections.singleton(dataNodeString), Collections.singleton(dataNodeString), true));
    }
    
    private static Stream<Arguments> removeWithSchemaNamesArguments() {
        DataNode fooDataNode = new DataNode("foo_ds", "foo_schema", "foo_tbl");
        DataNode barDataNode = new DataNode("foo_ds", "bar_schema", "foo_tbl");
        return Stream.of(
                Arguments.of("keep data node when schema not matched", Collections.singleton(fooDataNode), Collections.singleton("baz_schema"), 1,
                        Collections.singleton(SingleTableLoadUtils.getDataNodeString(DATABASE_TYPE, "foo_ds", "foo_schema", "foo_tbl")), true, true),
                Arguments.of("remove matched schema and keep others", Arrays.asList(fooDataNode, barDataNode), Collections.singleton("foo_schema"), 1,
                        Collections.singleton(SingleTableLoadUtils.getDataNodeString(DATABASE_TYPE, "foo_ds", "bar_schema", "foo_tbl")), true, true),
                Arguments.of("remove all matched schemas", Collections.singleton(fooDataNode), Collections.singleton("foo_schema"), 0, Collections.emptyList(), false, false));
    }
    
    private static Stream<Arguments> findTableDataNodeArguments() {
        DataNode dataNode = new DataNode("foo_ds", "foo_schema", "foo_tbl");
        return Stream.of(
                Arguments.of("return data node when schema matched", Collections.singleton(dataNode), "foo_schema".toUpperCase(), "foo_tbl".toUpperCase(), true, "foo_ds"),
                Arguments.of("return empty when schema mismatched", Collections.singleton(dataNode), "bar_schema", "foo_tbl", false, ""),
                Arguments.of("return empty when table not exists", Collections.emptyList(), "foo_schema", "bar_table", false, ""));
    }
    
    private static Map<String, Collection<DataNode>> createSingleTableDataNodes(final Collection<DataNode> initialDataNodes) {
        Map<String, Collection<DataNode>> result = new LinkedHashMap<>(1, 1F);
        if (!initialDataNodes.isEmpty()) {
            result.put("foo_tbl", new LinkedHashSet<>(initialDataNodes));
        }
        return result;
    }
    
    private static SingleMutableDataNodeRuleAttribute createRuleAttribute(final SingleRuleConfiguration configuration, final Collection<String> dataSourceNames,
                                                                          final Map<String, Collection<DataNode>> singleTableDataNodes,
                                                                          final SingleTableMapperRuleAttribute tableMapperRuleAttribute) {
        return new SingleMutableDataNodeRuleAttribute(configuration, new LinkedHashSet<>(dataSourceNames), singleTableDataNodes, DATABASE_TYPE, tableMapperRuleAttribute);
    }
    
    private static Collection<String> createDataNodeStrings(final Collection<DataNode> dataNodes) {
        return dataNodes.stream().map(each -> SingleTableLoadUtils.getDataNodeString(DATABASE_TYPE, each.getDataSourceName(), each.getSchemaName(), each.getTableName())).collect(Collectors.toList());
    }
    
}

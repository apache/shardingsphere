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

package org.apache.shardingsphere.single.distsql.handler.query;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.PhysicalDataSourceAggregator;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.single.datanode.SingleTableDataNodeLoader;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowUnloadedSingleTablesStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({SingleTableDataNodeLoader.class, SingleTableLoadUtils.class, PhysicalDataSourceAggregator.class})
class ShowUnloadedSingleTablesExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final ShowUnloadedSingleTablesExecutor executor = (ShowUnloadedSingleTablesExecutor) TypedSPILoader.getService(DistSQLQueryExecutor.class, ShowUnloadedSingleTablesStatement.class);
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private SingleRule rule;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    @Mock
    private RuleMetaData ruleMetaData;
    
    @Mock
    private StorageUnit storageUnit;
    
    @Mock
    private DataSource dataSource;
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(database);
        executor.setRule(rule);
    }
    
    @Test
    void assertGetColumnNamesWithoutSchema() {
        when(database.getProtocolType()).thenReturn(databaseType);
        assertThat(executor.getColumnNames(new ShowUnloadedSingleTablesStatement(null, null, null)), is(Arrays.asList("table_name", "storage_unit_name")));
    }
    
    @Test
    void assertGetColumnNamesWithSchema() {
        when(database.getProtocolType()).thenReturn(databaseType);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockSchemaRegistry()) {
            assertThat(executor.getColumnNames(new ShowUnloadedSingleTablesStatement(null, null, null)), is(Arrays.asList("table_name", "storage_unit_name", "schema_name")));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getRowsArguments")
    void assertGetRows(final String name, final boolean schemaAvailable, final Map<String, Collection<DataNode>> actualDataNodes,
                       final Map<String, Collection<DataNode>> loadedDataNodes, final List<List<String>> expectedRows) {
        mockRowsDependencies(actualDataNodes, loadedDataNodes);
        if (schemaAvailable) {
            try (MockedConstruction<DatabaseTypeRegistry> ignored = mockSchemaRegistry()) {
                assertRows(expectedRows);
            }
            return;
        }
        assertRows(expectedRows);
    }
    
    private void mockRowsDependencies(final Map<String, Collection<DataNode>> actualDataNodes, final Map<String, Collection<DataNode>> loadedDataNodes) {
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        when(ruleMetaData.getRules()).thenReturn(Collections.emptyList());
        when(rule.getSingleTableDataNodes()).thenReturn(loadedDataNodes);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        when(storageUnit.getDataSource()).thenReturn(dataSource);
        when(PhysicalDataSourceAggregator.getAggregatedDataSources(any(), any())).thenReturn(Collections.singletonMap("ds_0", dataSource));
        when(SingleTableLoadUtils.getExcludedTables(Collections.emptyList())).thenReturn(Collections.emptySet());
        when(SingleTableDataNodeLoader.load(eq("foo_db"), any(), any())).thenReturn(actualDataNodes);
    }
    
    private void assertRows(final List<List<String>> expectedRows) {
        List<LocalDataQueryResultRow> actualRows = new LinkedList<>(executor.getRows(new ShowUnloadedSingleTablesStatement(null, null, null), mock(ContextManager.class)));
        assertThat(actualRows.size(), is(expectedRows.size()));
        for (int i = 0; i < actualRows.size(); i++) {
            List<String> expectedRow = expectedRows.get(i);
            assertThat(actualRows.get(i).getCell(1), is(expectedRow.get(0)));
            assertThat(actualRows.get(i).getCell(2), is(expectedRow.get(1)));
            if (3 == expectedRow.size()) {
                assertThat(actualRows.get(i).getCell(3), is(expectedRow.get(2)));
            }
        }
    }
    
    private MockedConstruction<DatabaseTypeRegistry> mockSchemaRegistry() {
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(dialectDatabaseMetaData.getSchemaOption().isSchemaAvailable()).thenReturn(true);
        return mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData));
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(SingleRule.class));
    }
    
    private static Stream<Arguments> getRowsArguments() {
        Map<String, Collection<DataNode>> allLoadedActualDataNodes = new HashMap<>(
                Collections.singletonMap("t_order", new LinkedList<>(Collections.singleton(new DataNode("ds_0", (String) null, "t_order")))));
        Map<String, Collection<DataNode>> allLoadedRuleDataNodes = new HashMap<>(
                Collections.singletonMap("t_order", new LinkedList<>(Collections.singleton(new DataNode("ds_0", (String) null, "t_order")))));
        Map<String, Collection<DataNode>> partiallyLoadedActualDataNodes = new HashMap<>(
                Collections.singletonMap("t_order", new LinkedList<>(Arrays.asList(new DataNode("ds_0", "public", "t_order"), new DataNode("ds_1", "public", "t_order")))));
        Map<String, Collection<DataNode>> partiallyLoadedRuleDataNodes = new HashMap<>(
                Collections.singletonMap("t_order", new LinkedList<>(Collections.singleton(new DataNode("ds_0", "public", "t_order")))));
        Map<String, Collection<DataNode>> unmatchedActualDataNodes = new HashMap<>(
                Collections.singletonMap("t_order_item", new LinkedList<>(Collections.singleton(new DataNode("ds_2", (String) null, "t_order_item")))));
        Map<String, Collection<DataNode>> unmatchedRuleDataNodes = new HashMap<>(
                Collections.singletonMap("t_order", new LinkedList<>(Collections.singleton(new DataNode("ds_0", (String) null, "t_order")))));
        return Stream.of(
                Arguments.of("all loaded tables are excluded", false, allLoadedActualDataNodes, allLoadedRuleDataNodes, Collections.<List<String>>emptyList()),
                Arguments.of("partially loaded table keeps remaining nodes",
                        true, partiallyLoadedActualDataNodes, partiallyLoadedRuleDataNodes, Collections.singletonList(Arrays.asList("t_order", "ds_1", "public"))),
                Arguments.of("unmatched rule key keeps actual table nodes",
                        false, unmatchedActualDataNodes, unmatchedRuleDataNodes, Collections.singletonList(Arrays.asList("t_order_item", "ds_2"))));
    }
}

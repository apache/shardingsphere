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

package org.apache.shardingsphere.single.distsql.handler.update;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.statement.rdl.UnloadSingleTableStatement;
import org.apache.shardingsphere.single.exception.SingleTableNotFoundException;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnloadSingleTableExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final UnloadSingleTableExecutor executor = (UnloadSingleTableExecutor) TypedSPILoader.getService(DatabaseRuleDefinitionExecutor.class, UnloadSingleTableStatement.class);
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private SingleRule rule;
    
    @Mock
    private TableMapperRuleAttribute tableMapperRuleAttribute;
    
    @Mock
    private DataNodeRuleAttribute dataNodeRuleAttribute;
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(database);
        executor.setRule(rule);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        when(rule.getAttributes()).thenReturn(new RuleAttributes(tableMapperRuleAttribute, dataNodeRuleAttribute));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCheckBeforeUpdateWithFailureArguments")
    void assertCheckBeforeUpdateWithFailure(final String name, final Collection<String> allTables, final Collection<String> singleTables, final Collection<DataNode> dataNodes,
                                            final Collection<String> configuredTables, final Class<? extends RuntimeException> expectedException) {
        Map<String, Collection<DataNode>> tableDataNodes = new HashMap<>(1, 1F);
        tableDataNodes.put("foo_tbl", dataNodes);
        prepareCheckBeforeUpdateContext(allTables, singleTables, tableDataNodes, configuredTables);
        assertThrows(expectedException, () -> executor.checkBeforeUpdate(new UnloadSingleTableStatement(false, Collections.singletonList("foo_tbl"))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCheckBeforeUpdateWithSuccessArguments")
    void assertCheckBeforeUpdate(final String name, final UnloadSingleTableStatement sqlStatement, final Collection<String> allTables,
                                 final Collection<String> singleTables, final Map<String, Collection<DataNode>> tableDataNodes, final Collection<String> configuredTables) {
        prepareCheckBeforeUpdateContext(allTables, singleTables, tableDataNodes, configuredTables);
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertBuildToBeAlteredRuleConfigurationArguments")
    void assertBuildToBeAlteredRuleConfiguration(final String name, final Collection<String> currentTables,
                                                 final UnloadSingleTableStatement sqlStatement, final Collection<String> expectedTables) {
        when(rule.getConfiguration()).thenReturn(new SingleRuleConfiguration(new LinkedList<>(currentTables), null));
        SingleRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        assertThat(new HashSet<>(actual.getTables()), is(new HashSet<>(expectedTables)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertBuildToBeDroppedRuleConfigurationArguments")
    void assertBuildToBeDroppedRuleConfiguration(final String name, final Collection<String> currentTables, final Collection<String> toBeAlteredTables,
                                                 final boolean expectedNull, final Collection<String> expectedTables) {
        when(rule.getConfiguration()).thenReturn(new SingleRuleConfiguration(new LinkedList<>(currentTables), null));
        SingleRuleConfiguration toBeAltered = new SingleRuleConfiguration(new LinkedList<>(toBeAlteredTables), null);
        SingleRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(toBeAltered);
        if (expectedNull) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            assertThat(new HashSet<>(actual.getTables()), is(new HashSet<>(expectedTables)));
        }
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(SingleRule.class));
    }
    
    private void prepareCheckBeforeUpdateContext(final Collection<String> allTables, final Collection<String> singleTables,
                                                 final Map<String, Collection<DataNode>> tableDataNodes, final Collection<String> configuredTables) {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getAllTables()).thenReturn(
                allTables.stream().map(each -> new ShardingSphereTable(each, Collections.emptyList(), Collections.emptyList(), Collections.emptyList())).collect(Collectors.toList()));
        when(database.getSchema("foo_db")).thenReturn(schema);
        when(tableMapperRuleAttribute.getLogicTableNames()).thenReturn(singleTables);
        when(dataNodeRuleAttribute.getDataNodesByTableName(anyString())).thenAnswer(invocation -> tableDataNodes.getOrDefault(invocation.getArgument(0), Collections.emptyList()));
        when(rule.getConfiguration()).thenReturn(new SingleRuleConfiguration(new LinkedList<>(configuredTables), null));
    }
    
    private static Stream<Arguments> assertCheckBeforeUpdateWithFailureArguments() {
        return Stream.of(
                Arguments.of("missing table throws NoSuchTableException", Collections.emptyList(), Collections.singleton("foo_tbl"), Collections.singleton(new DataNode("foo_ds.foo_tbl")),
                        Collections.singleton("foo_ds.foo_tbl"), NoSuchTableException.class),
                Arguments.of("non single table throws SingleTableNotFoundException", Collections.singleton("foo_tbl"), Collections.emptyList(), Collections.singleton(new DataNode("foo_ds.foo_tbl")),
                        Collections.singleton("foo_ds.foo_tbl"), SingleTableNotFoundException.class),
                Arguments.of("missing data node throws MissingRequiredRuleException", Collections.singleton("foo_tbl"), Collections.singleton("foo_tbl"), Collections.emptyList(),
                        Collections.singleton("foo_ds.foo_tbl"), MissingRequiredRuleException.class),
                Arguments.of("missing rule config throws MissingRequiredRuleException", Collections.singleton("foo_tbl"), Collections.singleton("foo_tbl"),
                        Collections.singleton(new DataNode("foo_ds.foo_tbl")),
                        Collections.emptyList(), MissingRequiredRuleException.class));
    }
    
    private static Stream<Arguments> assertCheckBeforeUpdateWithSuccessArguments() {
        Map<String, Collection<DataNode>> singleTableNodes = new HashMap<>(1, 1F);
        singleTableNodes.put("foo_tbl", Collections.singleton(new DataNode("foo_ds.foo_tbl")));
        Map<String, Collection<DataNode>> multipleTableNodes = new HashMap<>(2, 1F);
        multipleTableNodes.put("foo_tbl", Collections.singleton(new DataNode("foo_ds.foo_tbl")));
        multipleTableNodes.put("bar_tbl", Collections.singleton(new DataNode("foo_ds.foo_schema.bar_tbl")));
        return Stream.of(
                Arguments.of("unload all tables bypasses table checks", new UnloadSingleTableStatement(true, Collections.emptyList()),
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), Collections.emptyList()),
                Arguments.of("valid single table passes checks", new UnloadSingleTableStatement(false, Collections.singletonList("foo_tbl")),
                        Collections.singleton("foo_tbl"), Collections.singleton("foo_tbl"), singleTableNodes, Collections.singleton("foo_ds.foo_tbl")),
                Arguments.of("valid multiple tables pass checks", new UnloadSingleTableStatement(false, Arrays.asList("foo_tbl", "bar_tbl")),
                        Arrays.asList("foo_tbl", "bar_tbl"), Arrays.asList("foo_tbl", "bar_tbl"), multipleTableNodes, Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl")));
    }
    
    private static Stream<Arguments> assertBuildToBeAlteredRuleConfigurationArguments() {
        return Stream.of(
                Arguments.of("unload all tables keeps altered config empty", Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl"), new UnloadSingleTableStatement(true, Collections.emptyList()),
                        Collections.emptyList()),
                Arguments.of("unload one table removes matching two-segment node", Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl"),
                        new UnloadSingleTableStatement(false, Collections.singletonList("foo_tbl")), Collections.singleton("foo_ds.bar_tbl")),
                Arguments.of("unload one table removes matching three-segment node", Arrays.asList("foo_ds.foo_schema.bar_tbl", "foo_ds.foo_tbl"),
                        new UnloadSingleTableStatement(false, Collections.singletonList("bar_tbl")), Collections.singleton("foo_ds.foo_tbl")));
    }
    
    private static Stream<Arguments> assertBuildToBeDroppedRuleConfigurationArguments() {
        return Stream.of(
                Arguments.of("empty altered config drops current rule config", Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl"), Collections.emptyList(), false,
                        Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl")),
                Arguments.of("non-empty altered config does not drop rule", Collections.singleton("foo_ds.foo_tbl"), Collections.singleton("foo_ds.foo_tbl"), true, Collections.emptyList()),
                Arguments.of("multi-table altered config does not drop rule", Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl"), Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl"), true,
                        Collections.emptyList()));
    }
}

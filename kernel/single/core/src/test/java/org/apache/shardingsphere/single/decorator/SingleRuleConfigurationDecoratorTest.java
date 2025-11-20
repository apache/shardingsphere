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

package org.apache.shardingsphere.single.decorator;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.constant.SingleTableConstants;
import org.apache.shardingsphere.single.datanode.SingleTableDataNodeLoader;
import org.apache.shardingsphere.single.exception.InvalidSingleRuleConfigurationException;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({DatabaseTypeEngine.class, SingleTableDataNodeLoader.class, SingleTableLoadUtils.class})
class SingleRuleConfigurationDecoratorTest {
    
    private final SingleRuleConfigurationDecorator decorator =
            (SingleRuleConfigurationDecorator) TypedSPILoader.getService(RuleConfigurationDecorator.class, SingleRuleConfiguration.class);
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @BeforeEach
    void setUp() {
        when(DatabaseTypeEngine.getStorageType(any(DataSource.class))).thenReturn(databaseType);
    }
    
    @Test
    void assertDecorateReturnsEmptyWhenTablesAndRulesAbsent() {
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(Collections.emptyList(), null);
        SingleRuleConfiguration actual = decorator.decorate("foo_db", Collections.emptyMap(), Collections.emptyList(), ruleConfig);
        assertThat(actual.getTables(), is(Collections.emptyList()));
    }
    
    @Test
    void assertDecorateLoadsTablesWhenRulesPresentButConfigEmpty() {
        Map<String, Collection<DataNode>> actualDataNodes = Collections.singletonMap("t_order", Collections.singleton(new DataNode("foo_ds", "foo_schema", "t_order")));
        when(SingleTableDataNodeLoader.load(anyString(), anyMap(), anyCollection())).thenReturn(actualDataNodes);
        mockSplitAndConvert(Collections.singleton(SingleTableConstants.ALL_TABLES), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Map<String, DataSource> dataSources = Collections.singletonMap("foo_ds", mock(DataSource.class));
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(Collections.emptyList(), null);
        assertThat(decorator.decorate("foo_db", dataSources, Collections.singleton(mockSingleRule()), ruleConfig).getTables(), contains("foo_ds.t_order"));
    }
    
    @Test
    void assertDecorateReturnsSplitTablesWhenNoExpansionRequired() {
        Collection<String> tables = Arrays.asList("foo_ds.foo_tbl", "bar_ds.bar_tbl");
        mockSplitAndConvert(tables, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(tables, null);
        assertThat(decorator.decorate("foo_db", Collections.emptyMap(), Collections.emptyList(), ruleConfig).getTables(), contains("foo_ds.foo_tbl", "bar_ds.bar_tbl"));
    }
    
    @Test
    void assertDecorateLoadsAllSchemaTablesWhenSchemaSupported() {
        Map<String, Collection<DataNode>> actualDataNodes = Collections.singletonMap("t_order", Collections.singleton(new DataNode("foo_ds", "public", "t_order")));
        when(SingleTableDataNodeLoader.load(anyString(), anyMap(), anyCollection())).thenReturn(actualDataNodes);
        mockSplitAndConvert(Collections.singleton(SingleTableConstants.ALL_SCHEMA_TABLES), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Map<String, DataSource> dataSources = Collections.singletonMap("foo_ds", mock(DataSource.class));
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(Collections.singleton(SingleTableConstants.ALL_SCHEMA_TABLES), null);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockSchemaAwareRegistry()) {
            assertThat(decorator.decorate("foo_db", dataSources, Collections.emptyList(), ruleConfig).getTables(), contains("foo_ds.public.t_order"));
        }
    }
    
    @Test
    void assertDecorateUsesDefaultStorageTypeWhenNoDataSourceConfigured() {
        Map<String, Collection<DataNode>> actualDataNodes = createActualDataNodes(new DataNode("foo_ds", "foo_schema", "t_order"));
        when(SingleTableDataNodeLoader.load(anyString(), anyMap(), anyCollection())).thenReturn(actualDataNodes);
        Collection<String> splitTables = Collections.singleton("*.foo_schema.t_order");
        Collection<DataNode> configuredDataNodes = Collections.singleton(new DataNode("foo_ds", "foo_schema", "t_order"));
        mockSplitAndConvert(splitTables, configuredDataNodes, Collections.emptyList(), Collections.emptyList());
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(Collections.singleton("*.foo_schema.t_order"), null);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockSchemaAwareRegistry()) {
            assertThat(decorator.decorate("foo_db", Collections.emptyMap(), Collections.singleton(mockSingleRule()), ruleConfig).getTables(), contains("foo_ds.foo_schema.t_order"));
        }
    }
    
    @Test
    void assertDecorateThrowsWhenSpecifiedTableMismatch() {
        Map<String, Collection<DataNode>> actualDataNodes = createActualDataNodes(new DataNode("bar_ds", "foo_schema", "t_order"));
        when(SingleTableDataNodeLoader.load(anyString(), anyMap(), anyCollection())).thenReturn(actualDataNodes);
        Collection<String> splitTables = Collections.singleton("*.foo_schema.t_order");
        Collection<DataNode> configuredDataNodes = Collections.singleton(new DataNode("foo_ds", "foo_schema", "t_order"));
        mockSplitAndConvert(splitTables, configuredDataNodes, Collections.emptyList(), Collections.emptyList());
        Map<String, DataSource> dataSources = Collections.singletonMap("foo_ds", mock(DataSource.class));
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(Collections.singleton("*.foo_schema.t_order"), null);
        assertThrows(InvalidSingleRuleConfigurationException.class, () -> decorator.decorate("foo_db", dataSources, Collections.singleton(mockSingleRule()), ruleConfig));
    }
    
    @Test
    void assertDecorateLoadsSpecifiedTablesWithExpand() {
        Map<String, Collection<DataNode>> actualDataNodes = new LinkedHashMap<>(3, 1F);
        actualDataNodes.put("feature_tbl", Collections.singleton(new DataNode("skip_ds", "foo_schema", "feature_tbl")));
        actualDataNodes.put("expanded_tbl", Collections.singleton(new DataNode("expand_ds", "foo_schema", "expanded_tbl")));
        actualDataNodes.put("matched_tbl", Collections.singleton(new DataNode("bar_ds", "foo_schema", "matched_tbl")));
        when(SingleTableDataNodeLoader.load(anyString(), anyMap(), anyCollection())).thenReturn(actualDataNodes);
        Collection<String> splitTables = Arrays.asList("expand_ds.*", "bar_ds.bar_tbl");
        Collection<DataNode> configuredDataNodes = Arrays.asList(
                new DataNode("expand_ds", "foo_schema", SingleTableConstants.ASTERISK),
                new DataNode("bar_ds", "foo_schema", "matched_tbl"));
        mockSplitAndConvert(splitTables, configuredDataNodes, Collections.emptyList(), Collections.singleton("feature_tbl"));
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(Arrays.asList("expand_ds.*", "bar_ds.bar_tbl"), null);
        Map<String, DataSource> dataSources = Collections.singletonMap("foo_ds", mock(DataSource.class));
        assertThat(decorator.decorate("foo_db", dataSources, Collections.singleton(mockSingleRule()), ruleConfig).getTables(), contains("expand_ds.expanded_tbl", "bar_ds.matched_tbl"));
    }
    
    @Test
    void assertDecorateIgnoresUnexpectedTablesDuringExpand() {
        Map<String, Collection<DataNode>> actualDataNodes = new LinkedHashMap<>(3, 1F);
        actualDataNodes.put("expanded_tbl", Collections.singleton(new DataNode("expand_ds", "foo_schema", "expanded_tbl")));
        actualDataNodes.put("ignored_tbl", Collections.singleton(new DataNode("ignored_ds", "foo_schema", "ignored_tbl")));
        actualDataNodes.put("matched_tbl", Collections.singleton(new DataNode("bar_ds", "foo_schema", "matched_tbl")));
        when(SingleTableDataNodeLoader.load(anyString(), anyMap(), anyCollection())).thenReturn(actualDataNodes);
        Collection<String> splitTables = Arrays.asList("expand_ds.*", "bar_ds.bar_tbl");
        Collection<DataNode> configuredDataNodes = Arrays.asList(
                new DataNode("expand_ds", "foo_schema", SingleTableConstants.ASTERISK),
                new DataNode("bar_ds", "foo_schema", "matched_tbl"));
        mockSplitAndConvert(splitTables, configuredDataNodes, Collections.emptyList(), Collections.emptyList());
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(Arrays.asList("expand_ds.*", "bar_ds.bar_tbl"), null);
        Map<String, DataSource> dataSources = Collections.singletonMap("foo_ds", mock(DataSource.class));
        assertThat(decorator.decorate("foo_db", dataSources, Collections.singleton(mockSingleRule()), ruleConfig).getTables(), contains("expand_ds.expanded_tbl", "bar_ds.matched_tbl"));
    }
    
    @Test
    void assertDecorateThrowsWhenExpandedNodeMismatch() {
        Map<String, Collection<DataNode>> actualDataNodes = Collections.singletonMap("t_order", Collections.singleton(new DataNode("other_ds", "foo_schema", "t_order")));
        when(SingleTableDataNodeLoader.load(anyString(), anyMap(), anyCollection())).thenReturn(actualDataNodes);
        Collection<String> splitTables = Arrays.asList("expand_ds.*", "expand_ds.t_order");
        Collection<DataNode> configuredDataNodes = Arrays.asList(
                new DataNode("expand_ds", "foo_schema", SingleTableConstants.ASTERISK),
                new DataNode("expand_ds", "foo_schema", "t_order"));
        mockSplitAndConvert(splitTables, configuredDataNodes, Collections.emptyList(), Collections.emptyList());
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration(Arrays.asList("expand_ds.*", "expand_ds.t_order"), null);
        Map<String, DataSource> dataSources = Collections.singletonMap("foo_ds", mock(DataSource.class));
        assertThrows(InvalidSingleRuleConfigurationException.class, () -> decorator.decorate("foo_db", dataSources, Collections.singleton(mockSingleRule()), ruleConfig));
    }
    
    private MockedConstruction<DatabaseTypeRegistry> mockSchemaAwareRegistry() {
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(dialectDatabaseMetaData.getSchemaOption().getDefaultSchema()).thenReturn(Optional.of("public"));
        return mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData));
    }
    
    private void mockSplitAndConvert(final Collection<String> splitTables, final Collection<DataNode> configuredDataNodes,
                                     final Collection<String> excludedTables, final Collection<String> featureRequiredTables) {
        when(SingleTableLoadUtils.splitTableLines(anyCollection())).thenReturn(splitTables);
        when(SingleTableLoadUtils.getExcludedTables(anyCollection())).thenReturn(excludedTables);
        when(SingleTableLoadUtils.convertToDataNodes(anyString(), any(DatabaseType.class), anyCollection())).thenReturn(configuredDataNodes);
        when(SingleTableLoadUtils.getFeatureRequiredSingleTables(anyCollection())).thenReturn(featureRequiredTables);
    }
    
    private Map<String, Collection<DataNode>> createActualDataNodes(final DataNode dataNode) {
        return Collections.singletonMap(dataNode.getTableName(), Collections.singleton(dataNode));
    }
    
    private ShardingSphereRule mockSingleRule() {
        ShardingSphereRule result = mock(ShardingSphereRule.class);
        when(result.getAttributes()).thenReturn(new RuleAttributes(mock(TableMapperRuleAttribute.class)));
        return result;
    }
}

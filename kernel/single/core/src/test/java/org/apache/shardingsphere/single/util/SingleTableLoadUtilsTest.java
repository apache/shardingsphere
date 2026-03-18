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

package org.apache.shardingsphere.single.util;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
class SingleTableLoadUtilsTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetExcludedTables() {
        ShardingSphereRule builtRule1 = mock(ShardingSphereRule.class);
        TableMapperRuleAttribute tableMapperRuleAttribute = mock(TableMapperRuleAttribute.class);
        when(tableMapperRuleAttribute.getDistributedTableNames()).thenReturn(Collections.singleton("dist_tbl"));
        when(tableMapperRuleAttribute.getActualTableNames()).thenReturn(Collections.singleton("actual_tbl"));
        when(builtRule1.getAttributes()).thenReturn(new RuleAttributes(tableMapperRuleAttribute));
        ShardingSphereRule builtRule2 = mock(ShardingSphereRule.class);
        when(builtRule2.getAttributes()).thenReturn(new RuleAttributes());
        assertThat(SingleTableLoadUtils.getExcludedTables(Arrays.asList(builtRule1, builtRule2)), is(new TreeSet<>(Arrays.asList("dist_tbl", "actual_tbl"))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getFeatureRequiredSingleTablesArguments")
    void assertGetFeatureRequiredSingleTables(final String name, final Collection<String> enhancedTableNames,
                                              final Collection<String> distributedTableNames, final Collection<String> expectedTableNames) {
        ShardingSphereRule builtRule1 = mock(ShardingSphereRule.class);
        TableMapperRuleAttribute tableMapperRuleAttribute = mock(TableMapperRuleAttribute.class);
        when(tableMapperRuleAttribute.getEnhancedTableNames()).thenReturn(enhancedTableNames);
        when(tableMapperRuleAttribute.getDistributedTableNames()).thenReturn(distributedTableNames);
        when(builtRule1.getAttributes()).thenReturn(new RuleAttributes(tableMapperRuleAttribute));
        ShardingSphereRule builtRule2 = mock(ShardingSphereRule.class);
        when(builtRule2.getAttributes()).thenReturn(new RuleAttributes());
        assertThat(SingleTableLoadUtils.getFeatureRequiredSingleTables(Arrays.asList(builtRule1, builtRule2)), is(expectedTableNames));
    }
    
    @Test
    void assertSplitTableLines() {
        assertThat(SingleTableLoadUtils.splitTableLines(Arrays.asList("foo_tbl", "bar_tbl0,bar_tbl1")), is(new LinkedHashSet<>(Arrays.asList("foo_tbl", "bar_tbl0", "bar_tbl1"))));
    }
    
    @Test
    void assertConvertToDataNodes() {
        DataNode expectedDataNode1 = new DataNode("foo_ds", "foo_db", "foo_tbl");
        DataNode expectedDataNode2 = new DataNode("bar_ds", "foo_db", "bar_tbl");
        assertThat(SingleTableLoadUtils.convertToDataNodes("foo_db", databaseType, Arrays.asList("foo_ds.foo_tbl", "bar_ds.bar_tbl")),
                is(new LinkedHashSet<>(Arrays.asList(expectedDataNode1, expectedDataNode2))));
    }
    
    @Test
    void assertGetAllTablesNodeStr() {
        assertThat(SingleTableLoadUtils.getAllTablesNodeStr(databaseType), is("*.*"));
    }
    
    @Test
    void assertGetAllTablesNodeStrWithSchema() {
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockSchemaRegistry()) {
            assertThat(SingleTableLoadUtils.getAllTablesNodeStr(databaseType), is("*.*.*"));
        }
    }
    
    @Test
    void assertGetAllTablesNodeStrFromDataSource() {
        assertThat(SingleTableLoadUtils.getAllTablesNodeStrFromDataSource(databaseType, "foo_ds", "foo_schema"), is("foo_ds.*"));
    }
    
    @Test
    void assertGetAllTablesNodeStrFromDataSourceWithSchema() {
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockSchemaRegistry()) {
            assertThat(SingleTableLoadUtils.getAllTablesNodeStrFromDataSource(databaseType, "foo_ds", "foo_schema"), is("foo_ds.foo_schema.*"));
        }
    }
    
    @Test
    void assertGetDataNodeString() {
        assertThat(SingleTableLoadUtils.getDataNodeString(databaseType, "foo_ds", "foo_schema", "foo_tbl"), is("foo_ds.foo_tbl"));
    }
    
    @Test
    void assertGetDataNodeStringWithSchema() {
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockSchemaRegistry()) {
            assertThat(SingleTableLoadUtils.getDataNodeString(databaseType, "foo_ds", "foo_schema", "foo_tbl"), is("foo_ds.foo_schema.foo_tbl"));
        }
    }
    
    private MockedConstruction<DatabaseTypeRegistry> mockSchemaRegistry() {
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(dialectDatabaseMetaData.getSchemaOption().getDefaultSchema()).thenReturn(Optional.of("foo_schema"));
        return mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData));
    }
    
    private static Stream<Arguments> getFeatureRequiredSingleTablesArguments() {
        return Stream.of(
                Arguments.of("without enhanced table names", Collections.emptyList(), Collections.emptyList(), Collections.emptySet()),
                Arguments.of("with distributed table names", Collections.singleton("enhanced_tbl"), Collections.singleton("dist_tbl"), Collections.emptySet()),
                Arguments.of("without distributed table names", Collections.singleton("enhanced_tbl"), Collections.emptyList(), Collections.singleton("enhanced_tbl")));
    }
    
}

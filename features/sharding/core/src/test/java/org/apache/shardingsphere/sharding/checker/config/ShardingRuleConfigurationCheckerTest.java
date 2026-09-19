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

package org.apache.shardingsphere.sharding.checker.config;

import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShardingRuleConfigurationCheckerTest {
    
    private ShardingRuleConfigurationChecker checker;
    
    @BeforeEach
    void setUp() {
        checker = (ShardingRuleConfigurationChecker) OrderedSPILoader.getServicesByClass(
                DatabaseRuleConfigurationChecker.class, Collections.singleton(ShardingRuleConfiguration.class)).get(ShardingRuleConfiguration.class);
    }
    
    @Test
    void assertCheckWithMultipleSchemasForSameDataSource() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ruleConfig.setTables(Collections.singleton(new ShardingTableRuleConfiguration("foo_tbl", "ds_0.foo_schema_${0..1}.foo_tbl_${0..1}")));
        InvalidRuleConfigurationException actual = assertThrows(InvalidRuleConfigurationException.class,
                () -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
        assertThat(actual.getMessage(), is("Invalid 'sharding table' rules 'foo_tbl', error messages are: Multiple schemas are configured for storage unit 'ds_0'."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validSchemaDataNodesArguments")
    void assertCheckWithValidSchemaDataNodes(final String name, final Collection<ShardingTableRuleConfiguration> tableRuleConfigs) {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ruleConfig.setTables(tableRuleConfigs);
        assertDoesNotThrow(() -> checker.check("foo_db", ruleConfig, Collections.emptyMap(), Collections.emptyList()));
    }
    
    private static Stream<Arguments> validSchemaDataNodesArguments() {
        return Stream.of(
                Arguments.of("same schema for same data source", Collections.singleton(
                        new ShardingTableRuleConfiguration("foo_tbl", "ds_0.foo_schema.foo_tbl_0,ds_0.foo_schema.foo_tbl_1"))),
                Arguments.of("same schema in different cases for same data source", Collections.singleton(
                        new ShardingTableRuleConfiguration("foo_tbl", "ds_0.foo_schema.foo_tbl_0,ds_0.FOO_SCHEMA.foo_tbl_1"))),
                Arguments.of("omitted schema and explicit schema for same data source", Collections.singleton(
                        new ShardingTableRuleConfiguration("foo_tbl", "ds_0.foo_tbl_0,ds_0.foo_schema.foo_tbl_1"))),
                Arguments.of("different schemas for different data sources", Collections.singleton(
                        new ShardingTableRuleConfiguration("foo_tbl", "ds_0.foo_schema.foo_tbl_0,ds_1.bar_schema.foo_tbl_1"))),
                Arguments.of("different schemas for different logic tables", Arrays.asList(
                        new ShardingTableRuleConfiguration("foo_tbl", "ds_0.foo_schema.foo_tbl_0"),
                        new ShardingTableRuleConfiguration("bar_tbl", "ds_0.bar_schema.bar_tbl_0"))));
    }
    
    @Test
    void assertGetRequiredDataSourceNames() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ruleConfig.setTables(Collections.singleton(new ShardingTableRuleConfiguration("foo_tbl", "ds_0.foo_tbl")));
        ruleConfig.setAutoTables(Collections.singleton(new ShardingAutoTableRuleConfiguration("bar_tbl", "ds_1")));
        assertThat(checker.getRequiredDataSourceNames(ruleConfig), is(new LinkedHashSet<>(Arrays.asList("ds_0", "ds_1"))));
    }
    
    @Test
    void assertGetTableNames() {
        ShardingRuleConfiguration ruleConfig = createRuleConfiguration();
        ruleConfig.setTables(Collections.singleton(new ShardingTableRuleConfiguration("foo_tbl", "ds_0.foo_tbl")));
        ruleConfig.setAutoTables(Collections.singleton(new ShardingAutoTableRuleConfiguration("bar_tbl", "ds_1")));
        assertThat(checker.getTableNames(ruleConfig), is(Arrays.asList("foo_tbl", "bar_tbl")));
    }
    
    private ShardingRuleConfiguration createRuleConfiguration() {
        return new ShardingRuleConfiguration();
    }
}

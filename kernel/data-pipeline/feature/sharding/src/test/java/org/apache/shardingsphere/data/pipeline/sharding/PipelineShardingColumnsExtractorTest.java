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

package org.apache.shardingsphere.data.pipeline.sharding;

import org.apache.shardingsphere.data.pipeline.core.importer.PipelineRequiredColumnsExtractor;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlStandardShardingStrategyConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineShardingColumnsExtractorTest {
    
    @SuppressWarnings("unchecked")
    private final PipelineRequiredColumnsExtractor<YamlShardingRuleConfiguration> extractor = OrderedSPILoader.getServicesByClass(
            PipelineRequiredColumnsExtractor.class, Collections.singleton(YamlShardingRuleConfiguration.class)).get(YamlShardingRuleConfiguration.class);
    
    @Test
    void assertGetTableAndRequiredColumnsMap() {
        YamlShardingRuleConfiguration yamlConfig = new YamlShardingRuleConfiguration();
        yamlConfig.setDefaultDatabaseStrategy(createYAMLStandardStrategyConfiguration("default_db_col"));
        yamlConfig.setDefaultTableStrategy(createYAMLStandardStrategyConfiguration("default_tbl_col"));
        yamlConfig.getTables().put("t_explicit", getYamlExplicitTableRuleConfiguration());
        yamlConfig.getTables().put("t_default", getYamlTableRuleConfiguration("t_default"));
        yamlConfig.getTables().put("t_ignored", getYamlTableRuleConfiguration("t_ignored"));
        yamlConfig.getAutoTables().put("t_auto", getYamlShardingAutoTableRuleConfiguration("t_auto"));
        yamlConfig.getAutoTables().put("t_ignored_auto", getYamlShardingAutoTableRuleConfiguration("t_ignored_auto"));
        Collection<ShardingSphereIdentifier> logicTables = Arrays.asList(
                new ShardingSphereIdentifier("t_explicit"), new ShardingSphereIdentifier("t_default"), new ShardingSphereIdentifier("t_auto"));
        Map<ShardingSphereIdentifier, Collection<String>> actual = extractor.getTableAndRequiredColumnsMap(yamlConfig, logicTables);
        assertThat(actual.size(), is(3));
        assertThat(actual.get(new ShardingSphereIdentifier("t_explicit")), containsInAnyOrder("user_id", "order_id", "item_id"));
        assertThat(actual.get(new ShardingSphereIdentifier("t_default")), containsInAnyOrder("default_db_col", "default_tbl_col"));
        assertTrue(actual.get(new ShardingSphereIdentifier("t_auto")).isEmpty());
    }
    
    private YamlTableRuleConfiguration getYamlExplicitTableRuleConfiguration() {
        YamlTableRuleConfiguration result = new YamlTableRuleConfiguration();
        result.setLogicTable("t_explicit");
        result.setDatabaseStrategy(createYAMLComplexStrategyConfiguration("user_id,order_id"));
        result.setTableStrategy(createYAMLComplexStrategyConfiguration("item_id"));
        return result;
    }
    
    private YamlTableRuleConfiguration getYamlTableRuleConfiguration(final String tableName) {
        YamlTableRuleConfiguration result = new YamlTableRuleConfiguration();
        result.setLogicTable(tableName);
        return result;
    }
    
    private YamlShardingAutoTableRuleConfiguration getYamlShardingAutoTableRuleConfiguration(final String tableName) {
        YamlShardingAutoTableRuleConfiguration result = new YamlShardingAutoTableRuleConfiguration();
        result.setLogicTable(tableName);
        return result;
    }
    
    private YamlShardingStrategyConfiguration createYAMLStandardStrategyConfiguration(final String column) {
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        YamlStandardShardingStrategyConfiguration standard = new YamlStandardShardingStrategyConfiguration();
        standard.setShardingColumn(column);
        result.setStandard(standard);
        return result;
    }
    
    private YamlShardingStrategyConfiguration createYAMLComplexStrategyConfiguration(final String columns) {
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        YamlComplexShardingStrategyConfiguration complex = new YamlComplexShardingStrategyConfiguration();
        complex.setShardingColumns(columns);
        result.setComplex(complex);
        return result;
    }
}

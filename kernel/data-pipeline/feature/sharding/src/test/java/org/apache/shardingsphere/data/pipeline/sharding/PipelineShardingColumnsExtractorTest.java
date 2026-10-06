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
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineShardingColumnsExtractorTest {
    
    @SuppressWarnings("unchecked")
    private final PipelineRequiredColumnsExtractor<ShardingRuleConfiguration> extractor = OrderedSPILoader.getServicesByClass(
            PipelineRequiredColumnsExtractor.class, Collections.singleton(ShardingRuleConfiguration.class)).get(ShardingRuleConfiguration.class);
    
    @Test
    void assertOrderedSPILoader() {
        assertThat(extractor, isA(PipelineShardingColumnsExtractor.class));
    }
    
    @Test
    void assertGetTableAndRequiredColumnsMap() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setDefaultDatabaseShardingStrategy(createStandardShardingStrategyConfiguration("default_db_col"));
        ruleConfig.setDefaultTableShardingStrategy(createStandardShardingStrategyConfiguration("default_tbl_col"));
        ruleConfig.getTables().add(getExplicitTableRuleConfiguration());
        ruleConfig.getTables().add(getTableRuleConfiguration("t_default"));
        ruleConfig.getTables().add(getTableRuleConfiguration("t_ignored"));
        ruleConfig.getAutoTables().add(getShardingAutoTableRuleConfiguration("t_auto"));
        ruleConfig.getAutoTables().add(getShardingAutoTableRuleConfiguration("t_ignored_auto"));
        Collection<ShardingSphereIdentifier> logicTables = Arrays.asList(
                new ShardingSphereIdentifier("T_EXPLICIT"), new ShardingSphereIdentifier("t_default"), new ShardingSphereIdentifier("t_auto"));
        Map<ShardingSphereIdentifier, Collection<String>> actual = extractor.getTableAndRequiredColumnsMap(ruleConfig, logicTables);
        assertThat(actual.size(), is(3));
        assertThat(actual.get(new ShardingSphereIdentifier("t_explicit")), containsInAnyOrder("user_id", "order_id", "item_id"));
        assertThat(actual.get(new ShardingSphereIdentifier("t_default")), containsInAnyOrder("default_db_col", "default_tbl_col"));
        assertTrue(actual.get(new ShardingSphereIdentifier("t_auto")).isEmpty());
    }
    
    private ShardingTableRuleConfiguration getExplicitTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_explicit", "ds_0.t_explicit");
        result.setDatabaseShardingStrategy(createComplexShardingStrategyConfiguration("user_id,order_id"));
        result.setTableShardingStrategy(createComplexShardingStrategyConfiguration("item_id"));
        return result;
    }
    
    private ShardingTableRuleConfiguration getTableRuleConfiguration(final String tableName) {
        return new ShardingTableRuleConfiguration(tableName, "ds_0." + tableName);
    }
    
    private ShardingAutoTableRuleConfiguration getShardingAutoTableRuleConfiguration(final String tableName) {
        return new ShardingAutoTableRuleConfiguration(tableName, "ds_0");
    }
    
    private StandardShardingStrategyConfiguration createStandardShardingStrategyConfiguration(final String column) {
        return new StandardShardingStrategyConfiguration(column, "foo_algorithm");
    }
    
    private ComplexShardingStrategyConfiguration createComplexShardingStrategyConfiguration(final String columns) {
        return new ComplexShardingStrategyConfiguration(columns, "foo_algorithm");
    }
}

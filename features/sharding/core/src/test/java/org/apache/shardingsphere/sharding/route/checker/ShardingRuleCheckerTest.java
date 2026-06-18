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

package org.apache.shardingsphere.sharding.route.checker;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.BindingTableCheckedConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.checker.ShardingRuleChecker;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class ShardingRuleCheckerTest {
    
    @Test
    void assertIsValidBindingTableConfigurationWithInlineAlgorithm() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfigurationWithInlineAlgorithm("t_order", "ds_${0..1}.t_order_${0..1}", "ds_inline", "t_order_inline"));
        shardingRuleConfig.getTables().add(createTableRuleConfigurationWithInlineAlgorithm("t_order_item", "ds_${0..1}.t_order_item_${0..1}", "ds_inline", "t_order_item_inline"));
        shardingRuleConfig.getShardingAlgorithms().put("ds_inline",
                new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}"))));
        shardingRuleConfig.getShardingAlgorithms().put("t_order_inline",
                new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "t_order_${order_id % 2}"))));
        shardingRuleConfig.getShardingAlgorithms().put("t_order_item_inline",
                new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "t_order_item_${order_id % 2}"))));
        shardingRuleConfig.setDefaultDatabaseShardingStrategy(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, createDataSourcesForInline(), mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS), Collections.emptyList());
        ShardingTableReferenceRuleConfiguration bindingTableGroup = new ShardingTableReferenceRuleConfiguration("", "t_order,t_order_item");
        BindingTableCheckedConfiguration checkedConfig = new BindingTableCheckedConfiguration(
                shardingRule.getDataSourceNames(), shardingRule.getShardingAlgorithms(), shardingRuleConfig.getShardingAlgorithms(), Collections.singleton(bindingTableGroup),
                shardingRuleConfig.getDefaultDatabaseShardingStrategy(), shardingRuleConfig.getDefaultTableShardingStrategy(), shardingRule.getDefaultShardingColumn());
        ShardingRuleChecker checker = new ShardingRuleChecker(shardingRule);
        assertTrue(checker.isValidBindingTableConfiguration(shardingRule.getShardingTables(), checkedConfig));
    }
    
    @Test
    void assertIsValidBindingTableConfigurationWithSameDatabaseShardingStrategy() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfiguration("t_order", "ds_${0..7}.t_order"));
        shardingRuleConfig.getTables().add(createTableRuleConfiguration("t_order_item", "ds_${0..7}.t_order_item"));
        shardingRuleConfig.getTables().add(createTableRuleConfiguration("t_product", "ds_${0..7}.t_product"));
        shardingRuleConfig.getShardingAlgorithms().put("mod_hash",
                new AlgorithmConfiguration("MOD.HASH.FIXTURE", PropertiesBuilder.build(new Property("sharding-count", "8"))));
        shardingRuleConfig.setDefaultDatabaseShardingStrategy(new NoneShardingStrategyConfiguration());
        shardingRuleConfig.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        ShardingRule shardingRule =
                new ShardingRule(shardingRuleConfig, createDataSources(), mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS), Collections.emptyList());
        ShardingTableReferenceRuleConfiguration bindingTableGroup = new ShardingTableReferenceRuleConfiguration("",
                "t_order,t_order_item,t_product");
        BindingTableCheckedConfiguration checkedConfig = new BindingTableCheckedConfiguration(
                shardingRule.getDataSourceNames(), shardingRule.getShardingAlgorithms(), shardingRuleConfig.getShardingAlgorithms(),
                Collections.singleton(bindingTableGroup), shardingRuleConfig.getDefaultDatabaseShardingStrategy(), shardingRuleConfig.getDefaultTableShardingStrategy(),
                shardingRule.getDefaultShardingColumn());
        ShardingRuleChecker checker = new ShardingRuleChecker(shardingRule);
        assertTrue(checker.isValidBindingTableConfiguration(shardingRule.getShardingTables(), checkedConfig));
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfigurationWithInlineAlgorithm(final String tableName, final String actualDataNodes,
                                                                                           final String databaseAlgorithmName, final String tableAlgorithmName) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration(tableName, actualDataNodes);
        result.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", databaseAlgorithmName));
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", tableAlgorithmName));
        return result;
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfiguration(final String tableName, final String actualDataNodes) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration(tableName, actualDataNodes);
        result.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("route_group_id", "mod_hash"));
        result.setTableShardingStrategy(new NoneShardingStrategyConfiguration());
        return result;
    }
    
    private Map<String, DataSource> createDataSources() {
        Map<String, DataSource> result = new HashMap<>(8);
        for (int i = 0; i < 8; i++) {
            result.put("ds_" + i, new MockedDataSource());
        }
        return result;
    }
    
    private Map<String, DataSource> createDataSourcesForInline() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put("ds_0", new MockedDataSource());
        result.put("ds_1", new MockedDataSource());
        return result;
    }
}

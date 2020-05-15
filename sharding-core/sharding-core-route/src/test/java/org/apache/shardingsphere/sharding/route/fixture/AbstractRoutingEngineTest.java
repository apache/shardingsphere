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

package org.apache.shardingsphere.sharding.route.fixture;

import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.TableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.strategy.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.sharding.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.sharding.strategy.route.value.RouteValue;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractRoutingEngineTest {
    
    protected final ShardingRule createBasedShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createInlineTableRuleConfig("t_order", "ds_${0..1}.t_order_${0..1}", "t_order_${order_id % 2}", "ds_${user_id % 2}"));
        return new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
    }
    
    protected final ShardingRule createBindingShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createInlineTableRuleConfig("t_order", "ds_${0..1}.t_order_${0..1}", "t_order_${order_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getTableRuleConfigs().add(createInlineTableRuleConfig("t_order_item", "ds_${0..1}.t_order_item_${0..1}", "t_order_item_${order_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getBindingTableGroups().add("t_order,t_order_item");
        return new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
    }
    
    protected final ShardingRule createBroadcastShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createInlineTableRuleConfig("t_order", "ds_${0..1}.t_order_${0..1}", "t_order_${order_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getTableRuleConfigs().add(createInlineTableRuleConfig("t_order_item", "ds_${0..1}.t_order_item_${0..1}", "t_order_item_${order_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getBroadcastTables().add("t_config");
        return new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
    }
    
    protected final ShardingRule createHintShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleWithHintConfig());
        return new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
    }
    
    protected final ShardingRule createMixedShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfig("t_hint_ds_test", "ds_${0..1}.t_hint_ds_test_${0..1}",
            new HintShardingStrategyConfiguration(new HintShardingAlgorithmFixture()),
            createStandardShardingStrategyConfiguration("t_hint_ds_test_${order_id % 2}")));
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleConfig("t_hint_table_test", "ds_${0..1}.t_hint_table_test_${0..1}",
            createStandardShardingStrategyConfiguration("ds_${user_id % 2}"),
            new HintShardingStrategyConfiguration(new HintShardingAlgorithmFixture())));
        return new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
    }
    
    protected final ShardingRule createAllShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getBroadcastTables().add("t_product");
        InlineShardingAlgorithm shardingAlgorithm = new InlineShardingAlgorithm();
        shardingAlgorithm.getProperties().setProperty("algorithm.expression", "ds_${user_id % 2}");
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", shardingAlgorithm));
        shardingRuleConfig.getTableRuleConfigs().add(createInlineTableRuleConfig("t_order", "ds_${0..1}.t_order_${0..1}", "t_order_${user_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getTableRuleConfigs().add(createInlineTableRuleConfig("t_order_item", "ds_${0..1}.t_order_item_${0..1}", "t_order_item_${user_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getTableRuleConfigs().add(createInlineTableRuleConfig("t_user", "ds_${0..1}.t_user_${0..1}", "t_user_${user_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getTableRuleConfigs().add(createTableRuleWithHintConfig());
        shardingRuleConfig.getBindingTableGroups().add("t_order,t_order_item");
        return new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1", "main"));
    }
    
    private TableRuleConfiguration createInlineTableRuleConfig(final String tableName, final String actualDataNodes, final String algorithmExpression, final String dsAlgorithmExpression) {
        return createTableRuleConfig(tableName, actualDataNodes,
            createStandardShardingStrategyConfiguration(dsAlgorithmExpression), createStandardShardingStrategyConfiguration(algorithmExpression));
    }
    
    private StandardShardingStrategyConfiguration createStandardShardingStrategyConfiguration(final String algorithmExpression) {
        int startIndex = algorithmExpression.indexOf('{');
        int endIndex = algorithmExpression.indexOf('%');
        String shardingColumn = algorithmExpression.substring(startIndex + 1, endIndex).trim();
        InlineShardingAlgorithm shardingAlgorithm = new InlineShardingAlgorithm();
        shardingAlgorithm.getProperties().setProperty("algorithm.expression", algorithmExpression);
        return new StandardShardingStrategyConfiguration(shardingColumn, shardingAlgorithm);
    }
    
    private TableRuleConfiguration createTableRuleWithHintConfig() {
        TableRuleConfiguration result = new TableRuleConfiguration("t_hint_test", "ds_${0..1}.t_hint_test_${0..1}");
        result.setTableShardingStrategyConfig(new HintShardingStrategyConfiguration(new HintShardingAlgorithmFixture()));
        result.setDatabaseShardingStrategyConfig(new HintShardingStrategyConfiguration(new HintShardingAlgorithmFixture()));
        return result;
    }
    
    protected final TableRuleConfiguration createTableRuleConfig(final String tableName, final String actualDataNodes,
        final ShardingStrategyConfiguration dsShardingStrategyConfiguration, final ShardingStrategyConfiguration tableShardingStrategyConfiguration) {
        TableRuleConfiguration result = new TableRuleConfiguration(tableName, actualDataNodes);
        result.setDatabaseShardingStrategyConfig(dsShardingStrategyConfiguration);
        result.setTableShardingStrategyConfig(tableShardingStrategyConfiguration);
        return result;
    }
    
    protected final ShardingConditions createShardingConditions(final String tableName) {
        List<ShardingCondition> result = new ArrayList<>();
        RouteValue shardingValue1 = new ListRouteValue<>("user_id", tableName, Collections.singleton(1L));
        RouteValue shardingValue2 = new ListRouteValue<>("order_id", tableName, Collections.singleton(1L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getRouteValues().add(shardingValue1);
        shardingCondition.getRouteValues().add(shardingValue2);
        result.add(shardingCondition);
        return new ShardingConditions(result);
    }
}

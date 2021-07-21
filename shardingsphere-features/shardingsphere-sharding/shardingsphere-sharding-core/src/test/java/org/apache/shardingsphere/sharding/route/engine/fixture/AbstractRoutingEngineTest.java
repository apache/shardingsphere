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

package org.apache.shardingsphere.sharding.route.engine.fixture;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.CreateTableEvent;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.single.SingleTableRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public abstract class AbstractRoutingEngineTest {
    
    protected final ShardingRule createBasedShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order", "ds_${0..1}.t_order_${0..1}", "t_order_${order_id % 2}", "ds_${user_id % 2}"));
        Properties props0 = new Properties();
        props0.setProperty("algorithm-expression", "ds_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("ds_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props0));
        Properties props1 = new Properties();
        props1.setProperty("algorithm-expression", "t_order_${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props1));
        return new ShardingRule(shardingRuleConfig, createDataSourceMap());
    }
    
    protected final ShardingRule createErrorShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order", "ds_${0..1}.t_order_${0..1}", "t_order_${order_id % 2}", "ds_${user_id % 2}"));
        Properties props0 = new Properties();
        props0.setProperty("algorithm-expression", "ds_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("ds_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props0));
        Properties props1 = new Properties();
        props1.setProperty("algorithm-expression", "t_order_${order_id % 3}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props1));
        return new ShardingRule(shardingRuleConfig, createDataSourceMap());
    }
    
    protected final ShardingRule createBindingShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order", "ds_${0..1}.t_order_${0..1}", "t_order_${order_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order_item", "ds_${0..1}.t_order_item_${0..1}", "t_order_item_${order_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getBindingTableGroups().add("t_order,t_order_item");
        Properties props0 = new Properties();
        props0.setProperty("algorithm-expression", "ds_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("ds_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props0));
        Properties props1 = new Properties();
        props1.setProperty("algorithm-expression", "t_order_${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props1));
        Properties props2 = new Properties();
        props2.setProperty("algorithm-expression", "t_order_item_${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_item_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props2));
        return new ShardingRule(shardingRuleConfig, createDataSourceMap());
    }
    
    protected final ShardingRule createBroadcastShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order", "ds_${0..1}.t_order_${0..1}", "t_order_${order_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order_item", "ds_${0..1}.t_order_item_${0..1}", "t_order_item_${order_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getBroadcastTables().add("t_config");
        Properties props0 = new Properties();
        props0.setProperty("algorithm-expression", "ds_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("ds_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props0));
        Properties props1 = new Properties();
        props1.setProperty("algorithm-expression", "t_order_${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props1));
        Properties props2 = new Properties();
        props2.setProperty("algorithm-expression", "t_order_item_${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_item_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props2));
        return new ShardingRule(shardingRuleConfig, createDataSourceMap());
    }
    
    protected final ShardingRule createHintShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleWithHintConfig());
        shardingRuleConfig.getShardingAlgorithms().put("hint_test", new ShardingSphereAlgorithmConfiguration("HINT_TEST", new Properties()));
        return new ShardingRule(shardingRuleConfig, createDataSourceMap());
    }
    
    protected final ShardingRule createMixedShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfig("t_hint_ds_test", "ds_${0..1}.t_hint_ds_test_${0..1}",
            new HintShardingStrategyConfiguration("hint_test"), createStandardShardingStrategyConfiguration("t_hint_ds_test_inline", "t_hint_ds_test_${order_id % 2}")));
        shardingRuleConfig.getTables().add(createTableRuleConfig("t_hint_table_test", "ds_${0..1}.t_hint_table_test_${0..1}",
            createStandardShardingStrategyConfiguration("ds_inline", "ds_${user_id % 2}"), new HintShardingStrategyConfiguration("hint_test")));
        shardingRuleConfig.getShardingAlgorithms().put("hint_test", new ShardingSphereAlgorithmConfiguration("HINT_TEST", new Properties()));
        Properties props0 = new Properties();
        props0.setProperty("algorithm-expression", "ds_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("ds_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props0));
        Properties props1 = new Properties();
        props1.setProperty("algorithm-expression", "t_hint_ds_test_${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_hint_ds_test_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props1));
        return new ShardingRule(shardingRuleConfig, createDataSourceMap());
    }
    
    protected final ShardingRule createAllShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getBroadcastTables().add("t_product");
        Properties props0 = new Properties();
        props0.setProperty("algorithm-expression", "ds_${user_id % 2}");
        shardingRuleConfig.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "ds_inline"));
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order", "ds_${0..1}.t_order_${0..1}", "t_order_${user_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order_item", "ds_${0..1}.t_order_item_${0..1}", "t_order_item_${user_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_user", "ds_${0..1}.t_user_${0..1}", "t_user_${user_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getTables().add(createTableRuleWithHintConfig());
        shardingRuleConfig.getBindingTableGroups().add("t_order,t_order_item");
        shardingRuleConfig.getShardingAlgorithms().put("ds_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props0));
        Properties props1 = new Properties();
        props1.setProperty("algorithm-expression", "t_order_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props1));
        Properties props2 = new Properties();
        props2.setProperty("algorithm-expression", "t_order_item_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_item_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props2));
        Properties props3 = new Properties();
        props3.setProperty("algorithm-expression", "t_user_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_user_inline", new ShardingSphereAlgorithmConfiguration("INLINE", props3));
        shardingRuleConfig.getShardingAlgorithms().put("hint_test", new ShardingSphereAlgorithmConfiguration("HINT_TEST", new Properties()));
        return new ShardingRule(shardingRuleConfig, createDataSourceMapWithMain());
    }
    
    protected final ShardingRule createIntervalTableShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfig("t_interval_test", "ds_0.t_interval_test_202101,ds_1.t_interval_test_202102",
                null, new StandardShardingStrategyConfiguration("create_at", "interval_test")));
        Properties props0 = new Properties();
        props0.setProperty("datetime-pattern", "yyyy-MM-dd HH:mm:ss");
        props0.setProperty("datetime-lower", "2021-01-01 00:00:00");
        props0.setProperty("datetime-upper", "2021-01-02 00:00:00");
        props0.setProperty("sharding-suffix-pattern", "yyyyMM");
        props0.setProperty("datetime-interval-amount", "1");
        props0.setProperty("datetime-interval-unit", "MONTHS");
        shardingRuleConfig.getShardingAlgorithms().put("interval_test", new ShardingSphereAlgorithmConfiguration("INTERVAL", props0));
        return new ShardingRule(shardingRuleConfig, createDataSourceMap());
    }
    
    private ShardingTableRuleConfiguration createInlineTableRuleConfig(final String tableName, final String actualDataNodes, final String algorithmExpression, final String dsAlgorithmExpression) {
        return createTableRuleConfig(tableName, actualDataNodes,
            createStandardShardingStrategyConfiguration("ds_inline", dsAlgorithmExpression), createStandardShardingStrategyConfiguration(tableName + "_inline", algorithmExpression));
    }
    
    private StandardShardingStrategyConfiguration createStandardShardingStrategyConfiguration(final String algorithmName, final String algorithmExpression) {
        int startIndex = algorithmExpression.indexOf('{');
        int endIndex = algorithmExpression.indexOf('%');
        String shardingColumn = algorithmExpression.substring(startIndex + 1, endIndex).trim();
        return new StandardShardingStrategyConfiguration(shardingColumn, algorithmName);
    }
    
    private ShardingTableRuleConfiguration createTableRuleWithHintConfig() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_hint_test", "ds_${0..1}.t_hint_test_${0..1}");
        result.setTableShardingStrategy(new HintShardingStrategyConfiguration("hint_test"));
        result.setDatabaseShardingStrategy(new HintShardingStrategyConfiguration("hint_test"));
        return result;
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfig(final String tableName, final String actualDataNodes, 
                                                                 final ShardingStrategyConfiguration dsShardingStrategyConfig, final ShardingStrategyConfiguration tableShardingStrategyConfig) {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration(tableName, actualDataNodes);
        result.setDatabaseShardingStrategy(dsShardingStrategyConfig);
        result.setTableShardingStrategy(tableShardingStrategyConfig);
        return result;
    }
    
    protected final ShardingConditions createShardingConditions(final String tableName) {
        List<ShardingCondition> result = new ArrayList<>(1);
        ShardingConditionValue shardingConditionValue1 = new ListShardingConditionValue<>("user_id", tableName, Collections.singleton(1L));
        ShardingConditionValue shardingConditionValue2 = new ListShardingConditionValue<>("order_id", tableName, Collections.singleton(1L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getValues().add(shardingConditionValue1);
        shardingCondition.getValues().add(shardingConditionValue2);
        result.add(shardingCondition);
        return new ShardingConditions(result);
    }
    
    protected final ShardingConditions createErrorShardingConditions(final String tableName) {
        List<ShardingCondition> result = new ArrayList<>(1);
        ShardingConditionValue shardingConditionValue1 = new ListShardingConditionValue<>("user_id", tableName, Collections.singleton(1L));
        ShardingConditionValue shardingConditionValue2 = new ListShardingConditionValue<>("order_id", tableName, Collections.singleton(2L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getValues().add(shardingConditionValue1);
        shardingCondition.getValues().add(shardingConditionValue2);
        result.add(shardingCondition);
        return new ShardingConditions(result);
    }
    
    protected final ShardingConditions createIntervalShardingConditions(final String tableName) {
        List<ShardingCondition> result = new ArrayList<>(1);
        ShardingConditionValue shardingConditionValue = new ListShardingConditionValue<>("create_at", tableName, Collections.singleton("2021-01-01 20:20:20"));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getValues().add(shardingConditionValue);
        result.add(shardingCondition);
        return new ShardingConditions(result);
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_0", mock(DataSource.class, RETURNS_DEEP_STUBS));
        result.put("ds_1", mock(DataSource.class, RETURNS_DEEP_STUBS));
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMapWithMain() {
        Map<String, DataSource> result = new HashMap<>(3, 1);
        result.put("ds_0", mock(DataSource.class, RETURNS_DEEP_STUBS));
        result.put("ds_1", mock(DataSource.class, RETURNS_DEEP_STUBS));
        result.put("main", mock(DataSource.class, RETURNS_DEEP_STUBS));
        return result;
    }
    
    protected SingleTableRule createAllSingleTableRule(final Collection<ShardingSphereRule> rules) {
        Map<String, DataSource> dataSourceMap = createDataSourceMapWithMain();
        SingleTableRule singleTableRule = new SingleTableRule(mock(DatabaseType.class), dataSourceMap, rules);
        ShardingSphereEventBus.getInstance().post(new CreateTableEvent(dataSourceMap.keySet().iterator().next(), "t_category", mock(TableMetaData.class)));
        return singleTableRule;
    }
}

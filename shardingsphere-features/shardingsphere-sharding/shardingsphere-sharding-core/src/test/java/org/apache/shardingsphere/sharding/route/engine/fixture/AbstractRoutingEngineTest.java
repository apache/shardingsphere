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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
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
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractRoutingEngineTest {
    
    protected final ShardingRule createBasedShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order", "ds_${0..1}.t_order_${0..1}", "t_order_${order_id % 2}", "ds_${user_id % 2}"));
        Properties props0 = new Properties();
        props0.setProperty("algorithm-expression", "ds_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("ds_inline", new AlgorithmConfiguration("INLINE", props0));
        Properties props1 = new Properties();
        props1.setProperty("algorithm-expression", "t_order_${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_inline", new AlgorithmConfiguration("INLINE", props1));
        return new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
    }
    
    protected final ShardingRule createErrorShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order", "ds_${0..1}.t_order_${0..1}", "t_order_${order_id % 2}", "ds_${user_id % 2}"));
        Properties props0 = new Properties();
        props0.setProperty("algorithm-expression", "ds_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("ds_inline", new AlgorithmConfiguration("INLINE", props0));
        Properties props1 = new Properties();
        props1.setProperty("algorithm-expression", "t_order_${order_id % 3}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_inline", new AlgorithmConfiguration("INLINE", props1));
        return new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
    }
    
    protected final ShardingRule createBindingShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order", "ds_${0..1}.t_order_${0..1}", "t_order_${order_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order_item", "ds_${0..1}.t_order_item_${0..1}", "t_order_item_${order_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getBindingTableGroups().add("t_order,t_order_item");
        Properties props0 = new Properties();
        props0.setProperty("algorithm-expression", "ds_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("ds_inline", new AlgorithmConfiguration("INLINE", props0));
        Properties props1 = new Properties();
        props1.setProperty("algorithm-expression", "t_order_${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_inline", new AlgorithmConfiguration("INLINE", props1));
        Properties props2 = new Properties();
        props2.setProperty("algorithm-expression", "t_order_item_${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_item_inline", new AlgorithmConfiguration("INLINE", props2));
        return new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
    }
    
    protected final ShardingRule createBroadcastShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order", "ds_${0..1}.t_order_${0..1}", "t_order_${order_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getTables().add(createInlineTableRuleConfig("t_order_item", "ds_${0..1}.t_order_item_${0..1}", "t_order_item_${order_id % 2}", "ds_${user_id % 2}"));
        shardingRuleConfig.getBroadcastTables().add("t_config");
        Properties props0 = new Properties();
        props0.setProperty("algorithm-expression", "ds_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("ds_inline", new AlgorithmConfiguration("INLINE", props0));
        Properties props1 = new Properties();
        props1.setProperty("algorithm-expression", "t_order_${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_inline", new AlgorithmConfiguration("INLINE", props1));
        Properties props2 = new Properties();
        props2.setProperty("algorithm-expression", "t_order_item_${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_item_inline", new AlgorithmConfiguration("INLINE", props2));
        return new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
    }
    
    protected final ShardingRule createHintShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleWithHintConfig());
        shardingRuleConfig.getShardingAlgorithms().put("core_hint_fixture", new AlgorithmConfiguration("CORE.HINT.FIXTURE", new Properties()));
        return new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
    }
    
    protected final ShardingRule createMixedShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createTableRuleConfig("t_hint_ds_test", "ds_${0..1}.t_hint_ds_test_${0..1}",
                new HintShardingStrategyConfiguration("core_hint_fixture"), createStandardShardingStrategyConfiguration("t_hint_ds_test_inline", "t_hint_ds_test_${order_id % 2}")));
        shardingRuleConfig.getTables().add(createTableRuleConfig("t_hint_table_test", "ds_${0..1}.t_hint_table_test_${0..1}",
                createStandardShardingStrategyConfiguration("ds_inline", "ds_${user_id % 2}"), new HintShardingStrategyConfiguration("core_hint_fixture")));
        shardingRuleConfig.getShardingAlgorithms().put("core_hint_fixture", new AlgorithmConfiguration("CORE.HINT.FIXTURE", new Properties()));
        Properties props0 = new Properties();
        props0.setProperty("algorithm-expression", "ds_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("ds_inline", new AlgorithmConfiguration("INLINE", props0));
        Properties props1 = new Properties();
        props1.setProperty("algorithm-expression", "t_hint_ds_test_${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_hint_ds_test_inline", new AlgorithmConfiguration("INLINE", props1));
        return new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
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
        shardingRuleConfig.getShardingAlgorithms().put("ds_inline", new AlgorithmConfiguration("INLINE", props0));
        Properties props1 = new Properties();
        props1.setProperty("algorithm-expression", "t_order_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_inline", new AlgorithmConfiguration("INLINE", props1));
        Properties props2 = new Properties();
        props2.setProperty("algorithm-expression", "t_order_item_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_order_item_inline", new AlgorithmConfiguration("INLINE", props2));
        Properties props3 = new Properties();
        props3.setProperty("algorithm-expression", "t_user_${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("t_user_inline", new AlgorithmConfiguration("INLINE", props3));
        shardingRuleConfig.getShardingAlgorithms().put("core_hint_fixture", new AlgorithmConfiguration("CORE.HINT.FIXTURE", new Properties()));
        return new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1", "main"), mock(InstanceContext.class));
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
        shardingRuleConfig.getShardingAlgorithms().put("interval_test", new AlgorithmConfiguration("INTERVAL", props0));
        return new ShardingRule(shardingRuleConfig, createDataSourceNames(), mock(InstanceContext.class));
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
        result.setTableShardingStrategy(new HintShardingStrategyConfiguration("core_hint_fixture"));
        result.setDatabaseShardingStrategy(new HintShardingStrategyConfiguration("core_hint_fixture"));
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
        List<ShardingCondition> result = new LinkedList<>();
        ShardingConditionValue shardingConditionValue1 = new ListShardingConditionValue<>("user_id", tableName, Collections.singleton(1L));
        ShardingConditionValue shardingConditionValue2 = new ListShardingConditionValue<>("order_id", tableName, Collections.singleton(1L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getValues().add(shardingConditionValue1);
        shardingCondition.getValues().add(shardingConditionValue2);
        result.add(shardingCondition);
        return new ShardingConditions(result, mock(SQLStatementContext.class), mock(ShardingRule.class));
    }
    
    protected final ShardingConditions createErrorShardingConditions(final String tableName) {
        List<ShardingCondition> result = new LinkedList<>();
        ShardingConditionValue shardingConditionValue1 = new ListShardingConditionValue<>("user_id", tableName, Collections.singleton(1L));
        ShardingConditionValue shardingConditionValue2 = new ListShardingConditionValue<>("order_id", tableName, Collections.singleton(2L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getValues().add(shardingConditionValue1);
        shardingCondition.getValues().add(shardingConditionValue2);
        result.add(shardingCondition);
        return new ShardingConditions(result, mock(SQLStatementContext.class), mock(ShardingRule.class));
    }
    
    protected final ShardingConditions createIntervalShardingConditions(final String tableName) {
        List<ShardingCondition> result = new LinkedList<>();
        ShardingConditionValue shardingConditionValue = new ListShardingConditionValue<>("create_at", tableName, Collections.singleton("2021-01-01 20:20:20"));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getValues().add(shardingConditionValue);
        result.add(shardingCondition);
        return new ShardingConditions(result, mock(SQLStatementContext.class), mock(ShardingRule.class));
    }
    
    private Collection<String> createDataSourceNames() {
        return Arrays.asList("ds_0", "ds_1");
    }
    
    protected final SingleTableRule createSingleTableRule(final Collection<ShardingSphereRule> rules) {
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        SingleTableRule result = new SingleTableRule(new SingleTableRuleConfiguration(), DefaultDatabase.LOGIC_NAME, dataSourceMap, rules);
        result.put(dataSourceMap.keySet().iterator().next(), DefaultDatabase.LOGIC_NAME, "t_category");
        return result;
    }
    
    @SneakyThrows(SQLException.class)
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(3, 1);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:h2:mem:db");
        result.put("ds_0", new MockedDataSource(connection));
        result.put("ds_1", new MockedDataSource(connection));
        result.put("main", new MockedDataSource(connection));
        return result;
    }
}

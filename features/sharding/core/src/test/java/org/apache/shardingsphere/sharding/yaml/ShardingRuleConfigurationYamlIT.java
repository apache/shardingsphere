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

package org.apache.shardingsphere.sharding.yaml;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheOptionsConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheOptionsConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    ShardingRuleConfigurationYamlIT() {
        super("yaml/sharding-rule.yaml", getExpectedRuleConfiguration());
    }
    
    private static ShardingRuleConfiguration getExpectedRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration userTableRuleConfig = new ShardingTableRuleConfiguration("t_user", "ds_${0..1}.t_user_${0..15}");
        userTableRuleConfig.setDatabaseShardingStrategy(new ComplexShardingStrategyConfiguration("region_id, user_id", "core_complex_fixture"));
        userTableRuleConfig.setTableShardingStrategy(new ComplexShardingStrategyConfiguration("region_id, user_id", "core_complex_fixture"));
        result.getTables().add(userTableRuleConfig);
        ShardingTableRuleConfiguration stockTableRuleConfig = new ShardingTableRuleConfiguration("t_stock", "ds_${0..1}.t_stock{0..8}");
        stockTableRuleConfig.setDatabaseShardingStrategy(new HintShardingStrategyConfiguration("core_hint_fixture"));
        stockTableRuleConfig.setTableShardingStrategy(new HintShardingStrategyConfiguration("core_hint_fixture"));
        result.getTables().add(stockTableRuleConfig);
        ShardingTableRuleConfiguration orderTableRuleConfig = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}");
        orderTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "table_inline"));
        orderTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        result.getTables().add(orderTableRuleConfig);
        ShardingTableRuleConfiguration orderItemTableRuleConfig = new ShardingTableRuleConfiguration("t_order_item", "ds_${0..1}.t_order_item_${0..1}");
        orderItemTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "core_standard_fixture"));
        result.getTables().add(orderItemTableRuleConfig);
        result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo", "t_order, t_order_item"));
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "database_inline"));
        result.setDefaultTableShardingStrategy(new NoneShardingStrategyConfiguration());
        result.setDefaultShardingColumn("order_id");
        result.setDefaultKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("id", "snowflake"));
        result.setDefaultAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singletonList("sharding_key_required_auditor"), true));
        result.getShardingAlgorithms().put("core_standard_fixture", new AlgorithmConfiguration("CORE.STANDARD.FIXTURE", new Properties()));
        result.getShardingAlgorithms().put("core_complex_fixture", new AlgorithmConfiguration("CORE.COMPLEX.FIXTURE", new Properties()));
        result.getShardingAlgorithms().put("core_hint_fixture", new AlgorithmConfiguration("CORE.HINT.FIXTURE", new Properties()));
        result.getShardingAlgorithms().put("database_inline", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${order_id % 2}"))));
        result.getShardingAlgorithms().put("table_inline", new AlgorithmConfiguration("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "t_order_${order_id % 2}"))));
        result.getKeyGenerators().put("snowflake", new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        result.getAuditors().put("sharding_key_required_auditor", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties()));
        result.setShardingCache(new ShardingCacheConfiguration(512, new ShardingCacheOptionsConfiguration(true, 65536, 262144)));
        return result;
    }
    
    @Override
    protected boolean assertYamlConfiguration(final YamlRuleConfiguration actual) {
        assertShardingRule((YamlShardingRuleConfiguration) actual);
        return true;
    }
    
    private void assertShardingRule(final YamlShardingRuleConfiguration actual) {
        assertThat(actual.getTables().size(), is(4));
        assertTUser(actual);
        assertTStock(actual);
        assertTOrder(actual);
        assertTOrderItem(actual);
        assertBindingTable(actual);
        assertShardingCache(actual);
        assertThat(actual.getDefaultShardingColumn(), is("order_id"));
    }
    
    private void assertTUser(final YamlShardingRuleConfiguration actual) {
        assertThat(actual.getTables().get("t_user").getActualDataNodes(), is("ds_${0..1}.t_user_${0..15}"));
        assertThat(actual.getTables().get("t_user").getDatabaseStrategy().getComplex().getShardingColumns(), is("region_id, user_id"));
        assertThat(actual.getTables().get("t_user").getDatabaseStrategy().getComplex().getShardingAlgorithmName(), is("core_complex_fixture"));
        assertThat(actual.getTables().get("t_user").getTableStrategy().getComplex().getShardingColumns(), is("region_id, user_id"));
        assertThat(actual.getTables().get("t_user").getTableStrategy().getComplex().getShardingAlgorithmName(), is("core_complex_fixture"));
    }
    
    private void assertTStock(final YamlShardingRuleConfiguration actual) {
        assertThat(actual.getTables().get("t_stock").getActualDataNodes(), is("ds_${0..1}.t_stock{0..8}"));
        assertThat(actual.getTables().get("t_stock").getDatabaseStrategy().getHint().getShardingAlgorithmName(), is("core_hint_fixture"));
        assertThat(actual.getTables().get("t_stock").getTableStrategy().getHint().getShardingAlgorithmName(), is("core_hint_fixture"));
    }
    
    private void assertTOrder(final YamlShardingRuleConfiguration actual) {
        assertThat(actual.getTables().get("t_order").getActualDataNodes(), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(actual.getTables().get("t_order").getTableStrategy().getStandard().getShardingColumn(), is("order_id"));
        assertThat(actual.getTables().get("t_order").getTableStrategy().getStandard().getShardingAlgorithmName(), is("table_inline"));
        assertThat(actual.getTables().get("t_order").getKeyGenerateStrategy().getColumn(), is("order_id"));
        assertThat(actual.getTables().get("t_order").getKeyGenerateStrategy().getKeyGeneratorName(), is("snowflake"));
    }
    
    private void assertTOrderItem(final YamlShardingRuleConfiguration actual) {
        assertThat(actual.getTables().get("t_order_item").getActualDataNodes(), is("ds_${0..1}.t_order_item_${0..1}"));
        assertThat(actual.getTables().get("t_order_item").getTableStrategy().getStandard().getShardingColumn(), is("order_id"));
        assertThat(actual.getTables().get("t_order_item").getTableStrategy().getStandard().getShardingAlgorithmName(), is("core_standard_fixture"));
        assertThat(actual.getTables().get("t_order_item").getTableStrategy().getStandard().getShardingAlgorithmName(), is("core_standard_fixture"));
    }
    
    private void assertBindingTable(final YamlShardingRuleConfiguration actual) {
        assertThat(actual.getBindingTables().size(), is(1));
        assertThat(new ArrayList<>(actual.getBindingTables()).get(0), is("foo:t_order, t_order_item"));
    }
    
    private void assertShardingCache(final YamlShardingRuleConfiguration actual) {
        YamlShardingCacheConfiguration actualShardingCache = actual.getShardingCache();
        assertThat(actualShardingCache.getAllowedMaxSqlLength(), is(512));
        YamlShardingCacheOptionsConfiguration actualRouteCacheConfig = actualShardingCache.getRouteCache();
        assertThat(actualRouteCacheConfig.getInitialCapacity(), is(65536));
        assertThat(actualRouteCacheConfig.getMaximumSize(), is(262144));
        assertTrue(actualRouteCacheConfig.isSoftValues());
    }
}

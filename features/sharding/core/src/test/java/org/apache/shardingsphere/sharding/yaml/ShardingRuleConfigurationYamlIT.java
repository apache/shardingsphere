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

import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheOptionsConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    ShardingRuleConfigurationYamlIT() {
        super("yaml/sharding-rule.yaml");
    }
    
    @Override
    protected void assertYamlRootConfiguration(final YamlRootConfiguration actual) {
        assertDataSourceMap(actual);
        Optional<YamlShardingRuleConfiguration> shardingRuleConfig = actual.getRules().stream()
                .filter(each -> each instanceof YamlShardingRuleConfiguration).findFirst().map(optional -> (YamlShardingRuleConfiguration) optional);
        assertTrue(shardingRuleConfig.isPresent());
        assertThat(shardingRuleConfig.get().getTables().size(), is(4));
        assertTUser(shardingRuleConfig.get());
        assertTStock(shardingRuleConfig.get());
        assertTOrder(shardingRuleConfig.get());
        assertTOrderItem(shardingRuleConfig.get());
        assertBindingTable(shardingRuleConfig.get());
        assertShardingCache(shardingRuleConfig.get());
        assertProps(actual);
        assertThat(shardingRuleConfig.get().getDefaultShardingColumn(), is("order_id"));
    }
    
    private void assertDataSourceMap(final YamlRootConfiguration actual) {
        assertThat(actual.getDataSources().size(), is(3));
        assertTrue(actual.getDataSources().containsKey("ds_0"));
        assertTrue(actual.getDataSources().containsKey("ds_1"));
        assertTrue(actual.getDataSources().containsKey("default_ds"));
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
        assertThat(actual.getBindingTables().iterator().next(), is("t_order, t_order_item"));
    }
    
    private void assertShardingCache(final YamlShardingRuleConfiguration actual) {
        YamlShardingCacheConfiguration actualShardingCache = actual.getShardingCache();
        assertThat(actualShardingCache.getAllowedMaxSqlLength(), is(512));
        YamlShardingCacheOptionsConfiguration actualRouteCacheConfig = actualShardingCache.getRouteCache();
        assertThat(actualRouteCacheConfig.getInitialCapacity(), is(65536));
        assertThat(actualRouteCacheConfig.getMaximumSize(), is(262144));
        assertTrue(actualRouteCacheConfig.isSoftValues());
    }
    
    private void assertProps(final YamlRootConfiguration actual) {
        assertThat(actual.getProps().size(), is(1));
        assertTrue((boolean) actual.getProps().get(ConfigurationPropertyKey.SQL_SHOW.getKey()));
    }
}

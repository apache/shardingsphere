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

package org.apache.shardingsphere.core.yaml.engine;

import org.apache.shardingsphere.core.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.core.yaml.constructor.YamlRootRuleConfigurationsConstructor;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlEngineRootConfigurationsTest {
    
    @Test
    public void assertUnmarshalWithYamlFile() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/sharding-rule.yaml");
        assertNotNull(url);
        assertYamlShardingConfig(YamlEngine.unmarshal(new File(url.getFile()), YamlRootRuleConfigurations.class, new YamlRootRuleConfigurationsConstructor()));
    }
    
    @Test
    public void assertUnmarshalWithYamlBytes() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/sharding-rule.yaml");
        assertNotNull(url);
        StringBuilder yamlContent = new StringBuilder();
        try (
                FileReader fileReader = new FileReader(url.getFile());
                BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (null != (line = reader.readLine())) {
                yamlContent.append(line).append("\n");
            }
        }
        assertYamlShardingConfig(YamlEngine.unmarshal(yamlContent.toString().getBytes(), YamlRootRuleConfigurations.class, new YamlRootRuleConfigurationsConstructor()));
    }
    
    private void assertYamlShardingConfig(final YamlRootRuleConfigurations actual) {
        assertDataSourceMap(actual);
        assertThat(actual.getShardingRule().getTables().size(), is(4));
        assertTUser(actual);
        assertTStock(actual);
        assertTOrder(actual);
        assertTOrderItem(actual);
        assertBindingTable(actual);
        assertBroadcastTable(actual);
        assertMasterSlaveRules(actual);
        assertProps(actual);
    }
    
    private void assertDataSourceMap(final YamlRootRuleConfigurations actual) {
        assertThat(actual.getDataSources().size(), is(7));
        assertTrue(actual.getDataSources().containsKey("master_ds_0"));
        assertTrue(actual.getDataSources().containsKey("master_ds_0_slave_0"));
        assertTrue(actual.getDataSources().containsKey("master_ds_0_slave_1"));
        assertTrue(actual.getDataSources().containsKey("master_ds_1"));
        assertTrue(actual.getDataSources().containsKey("master_ds_1_slave_0"));
        assertTrue(actual.getDataSources().containsKey("master_ds_1_slave_1"));
        assertTrue(actual.getDataSources().containsKey("default_ds"));
    }
    
    private void assertTUser(final YamlRootRuleConfigurations actual) {
        assertThat(actual.getShardingRule().getTables().get("t_user").getActualDataNodes(), is("ds_${0..1}.t_user_${0..15}"));
        assertThat(actual.getShardingRule().getTables().get("t_user").getDatabaseStrategy().getComplex().getShardingColumns(), is("region_id, user_id"));
        assertThat(actual.getShardingRule().getTables().get("t_user").getDatabaseStrategy().getComplex().getShardingAlgorithm().getType(), is("COMPLEX_TEST"));
        assertThat(actual.getShardingRule().getTables().get("t_user").getTableStrategy().getComplex().getShardingColumns(), is("region_id, user_id"));
        assertThat(actual.getShardingRule().getTables().get("t_user").getTableStrategy().getComplex().getShardingAlgorithm().getType(), is("COMPLEX_TEST"));
    }
    
    private void assertTStock(final YamlRootRuleConfigurations actual) {
        assertThat(actual.getShardingRule().getTables().get("t_stock").getActualDataNodes(), is("ds_${0..1}.t_stock{0..8}"));
        assertThat(actual.getShardingRule().getTables().get("t_stock").getDatabaseStrategy().getHint().getShardingAlgorithm().getType(), is("HINT_TEST"));
        assertThat(actual.getShardingRule().getTables().get("t_stock").getTableStrategy().getHint().getShardingAlgorithm().getType(), is("HINT_TEST"));
    }
    
    private void assertTOrder(final YamlRootRuleConfigurations actual) {
        assertThat(actual.getShardingRule().getTables().get("t_order").getActualDataNodes(), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(actual.getShardingRule().getTables().get("t_order").getTableStrategy().getStandard().getShardingColumn(), is("order_id"));
        assertThat(actual.getShardingRule().getTables().get("t_order").getTableStrategy().getStandard().getShardingAlgorithm().getProps().getProperty("algorithm.expression"), 
                is("t_order_${order_id % 2}"));
        assertThat(actual.getShardingRule().getTables().get("t_order").getKeyGenerator().getColumn(), is("order_id"));
        assertThat(actual.getShardingRule().getTables().get("t_order").getKeyGenerator().getType(), is("SNOWFLAKE"));
    }
    
    private void assertTOrderItem(final YamlRootRuleConfigurations actual) {
        assertThat(actual.getShardingRule().getTables().get("t_order_item").getActualDataNodes(), is("ds_${0..1}.t_order_item_${0..1}"));
        assertThat(actual.getShardingRule().getTables().get("t_order_item").getTableStrategy().getStandard().getShardingColumn(), is("order_id"));
        assertThat(actual.getShardingRule().getTables().get("t_order_item").getTableStrategy().getStandard().getShardingAlgorithm().getType(), is("STANDARD_TEST"));
        assertThat(actual.getShardingRule().getTables().get("t_order_item").getTableStrategy().getStandard().getShardingAlgorithm().getType(), is("STANDARD_TEST"));
    }
    
    private void assertBindingTable(final YamlRootRuleConfigurations actual) {
        assertThat(actual.getShardingRule().getBindingTables().size(), is(1));
        assertThat(actual.getShardingRule().getBindingTables().iterator().next(), is("t_order, t_order_item"));
    }
    
    private void assertBroadcastTable(final YamlRootRuleConfigurations actual) {
        assertThat(actual.getShardingRule().getBroadcastTables().size(), is(1));
        assertThat(actual.getShardingRule().getBroadcastTables().iterator().next(), is("t_config"));
    }
    
    private void assertMasterSlaveRules(final YamlRootRuleConfigurations actual) {
        assertThat(actual.getMasterSlaveRules().size(), is(2));
        assertMasterSlaveRuleForDs0(actual);
        assertMasterSlaveRuleForDs1(actual);
    }
    
    private void assertMasterSlaveRuleForDs0(final YamlRootRuleConfigurations actual) {
        assertThat(actual.getMasterSlaveRules().get("ds_0").getMasterDataSourceName(), is("master_ds_0"));
        assertThat(actual.getMasterSlaveRules().get("ds_0").getSlaveDataSourceNames(), is(Arrays.asList("master_ds_0_slave_0", "master_ds_0_slave_1")));
        assertThat(actual.getMasterSlaveRules().get("ds_0").getLoadBalanceAlgorithmType(), is("ROUND_ROBIN"));
    }
    
    private void assertMasterSlaveRuleForDs1(final YamlRootRuleConfigurations actual) {
        assertThat(actual.getMasterSlaveRules().get("ds_1").getMasterDataSourceName(), is("master_ds_1"));
        assertThat(actual.getMasterSlaveRules().get("ds_1").getSlaveDataSourceNames(), is(Arrays.asList("master_ds_1_slave_0", "master_ds_1_slave_1")));
        assertThat(actual.getMasterSlaveRules().get("ds_1").getLoadBalanceAlgorithmType(), is("RANDOM"));
    }
    
    private void assertProps(final YamlRootRuleConfigurations actual) {
        assertThat(actual.getProps().size(), is(1));
        assertThat(actual.getProps().get("sql.show"), is(true));
    }
}

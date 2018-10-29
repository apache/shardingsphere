/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.yaml.sharding;

import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlShardingConfigurationTest {
    
    @Test
    public void assertUnmarshalWithYamlFile() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/sharding-rule.yaml");
        assertNotNull(url);
        assertYamlShardingConfig(YamlShardingConfiguration.unmarshal(new File(url.getFile())));
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
        assertYamlShardingConfig(YamlShardingConfiguration.unmarshal(yamlContent.toString().getBytes()));
    }
    
    private void assertYamlShardingConfig(final YamlShardingConfiguration actual) {
        assertDataSourceMap(actual);
        assertThat(actual.getShardingRule().getTables().size(), is(4));
        assertTUser(actual);
        assertTStock(actual);
        assertTOrder(actual);
        assertTOrderItem(actual);
        assertBindingTable(actual);
        assertShardingRuleDefault(actual);
        assertMasterSlaveRules(actual);
        assertConfigMap(actual);
        assertProps(actual);
    }
    
    private void assertDataSourceMap(final YamlShardingConfiguration actual) {
        assertThat(actual.getDataSources().size(), is(7));
        assertTrue(actual.getDataSources().containsKey("master_ds_0"));
        assertTrue(actual.getDataSources().containsKey("master_ds_0_slave_0"));
        assertTrue(actual.getDataSources().containsKey("master_ds_0_slave_1"));
        assertTrue(actual.getDataSources().containsKey("master_ds_1"));
        assertTrue(actual.getDataSources().containsKey("master_ds_1_slave_0"));
        assertTrue(actual.getDataSources().containsKey("master_ds_1_slave_1"));
        assertTrue(actual.getDataSources().containsKey("default_ds"));
    }
    
    private void assertTUser(final YamlShardingConfiguration actual) {
        assertThat(actual.getShardingRule().getTables().get("t_user").getActualDataNodes(), is("ds_${0..1}.t_user_${0..15}"));
        assertThat(actual.getShardingRule().getTables().get("t_user").getDatabaseStrategy().getComplex().getShardingColumns(), is("region_id, user_id"));
        assertThat(actual.getShardingRule().getTables().get("t_user").getDatabaseStrategy().getComplex().getAlgorithmClassName(), is("TestDatabaseComplexAlgorithmClassName"));
        assertThat(actual.getShardingRule().getTables().get("t_user").getTableStrategy().getComplex().getShardingColumns(), is("region_id, user_id"));
        assertThat(actual.getShardingRule().getTables().get("t_user").getTableStrategy().getComplex().getAlgorithmClassName(), is("TestTableComplexAlgorithmClassName"));
    }
    
    private void assertTStock(final YamlShardingConfiguration actual) {
        assertThat(actual.getShardingRule().getTables().get("t_stock").getActualDataNodes(), is("ds_${0..1}.t_stock{0..8}"));
        assertThat(actual.getShardingRule().getTables().get("t_stock").getDatabaseStrategy().getHint().getAlgorithmClassName(), is("TestDatabaseHintAlgorithmClassName"));
        assertThat(actual.getShardingRule().getTables().get("t_stock").getTableStrategy().getHint().getAlgorithmClassName(), is("TestTableHintAlgorithmClassName"));
    }
    
    private void assertTOrder(final YamlShardingConfiguration actual) {
        assertThat(actual.getShardingRule().getTables().get("t_order").getActualDataNodes(), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(actual.getShardingRule().getTables().get("t_order").getTableStrategy().getInline().getShardingColumn(), is("order_id"));
        assertThat(actual.getShardingRule().getTables().get("t_order").getTableStrategy().getInline().getAlgorithmExpression(), is("t_order_${order_id % 2}"));
        assertThat(actual.getShardingRule().getTables().get("t_order").getKeyGeneratorColumnName(), is("order_id"));
        assertThat(actual.getShardingRule().getTables().get("t_order").getKeyGeneratorClassName(), is(DefaultKeyGenerator.class.getName()));
        assertThat(actual.getShardingRule().getTables().get("t_order").getLogicIndex(), is("order_index"));
    }
    
    private void assertTOrderItem(final YamlShardingConfiguration actual) {
        assertThat(actual.getShardingRule().getTables().get("t_order_item").getActualDataNodes(), is("ds_${0..1}.t_order_item_${0..1}"));
        assertThat(actual.getShardingRule().getTables().get("t_order_item").getTableStrategy().getStandard().getShardingColumn(), is("order_id"));
        assertThat(actual.getShardingRule().getTables().get("t_order_item").getTableStrategy().getStandard().getPreciseAlgorithmClassName(), is("TestPreciseAlgorithmClassName"));
        assertThat(actual.getShardingRule().getTables().get("t_order_item").getTableStrategy().getStandard().getRangeAlgorithmClassName(), is("TestRangeAlgorithmClassName"));
    }
    
    private void assertBindingTable(final YamlShardingConfiguration actual) {
        assertThat(actual.getShardingRule().getBindingTables().size(), is(1));
        assertThat(actual.getShardingRule().getBindingTables().get(0), is("t_order, t_order_item"));
    }
    
    private void assertShardingRuleDefault(final YamlShardingConfiguration actual) {
        assertThat(actual.getShardingRule().getDefaultDataSourceName(), is("default_ds"));
        assertThat(actual.getShardingRule().getDefaultDatabaseStrategy().getInline().getShardingColumn(), is("order_id"));
        assertThat(actual.getShardingRule().getDefaultDatabaseStrategy().getInline().getAlgorithmExpression(), is("ds_${order_id % 2}"));
        assertThat(actual.getShardingRule().getDefaultKeyGeneratorClassName(), is(DefaultKeyGenerator.class.getName()));
    }
    
    private void assertMasterSlaveRules(final YamlShardingConfiguration actual) {
        assertThat(actual.getShardingRule().getMasterSlaveRules().size(), is(2));
        assertMasterSlaveRuleForDs0(actual);
        assertMasterSlaveRuleForDs1(actual);
    }
    
    private void assertMasterSlaveRuleForDs0(final YamlShardingConfiguration actual) {
        assertThat(actual.getShardingRule().getMasterSlaveRules().get("ds_0").getMasterDataSourceName(), is("master_ds_0"));
        assertThat(actual.getShardingRule().getMasterSlaveRules().get("ds_0").getSlaveDataSourceNames(), 
                CoreMatchers.<Collection<String>>is(Arrays.asList("master_ds_0_slave_0", "master_ds_0_slave_1")));
        assertThat(actual.getShardingRule().getMasterSlaveRules().get("ds_0").getLoadBalanceAlgorithmType(), is(MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN));
        assertConfigMap(actual);
    }
    
    private void assertMasterSlaveRuleForDs1(final YamlShardingConfiguration actual) {
        assertThat(actual.getShardingRule().getMasterSlaveRules().get("ds_1").getMasterDataSourceName(), is("master_ds_1"));
        assertThat(actual.getShardingRule().getMasterSlaveRules().get("ds_1").getSlaveDataSourceNames(), 
                CoreMatchers.<Collection<String>>is(Arrays.asList("master_ds_1_slave_0", "master_ds_1_slave_1")));
        assertThat(actual.getShardingRule().getMasterSlaveRules().get("ds_1").getLoadBalanceAlgorithmClassName(), is("TestAlgorithmClass"));
        assertConfigMap(actual);
    }
    
    private void assertConfigMap(final YamlShardingConfiguration actual) {
        assertThat(actual.getConfigMap().size(), is(4));
        assertThat(actual.getConfigMap().get("master-slave-key0"), is((Object) "master-slave-value0"));
        assertThat(actual.getConfigMap().get("master-slave-key1"), is((Object) "master-slave-value1"));
        assertThat(actual.getConfigMap().get("sharding-key1"), is((Object) "sharding-value1"));
        assertThat(actual.getConfigMap().get("sharding-key2"), is((Object) "sharding-value2"));
    }
    
    private void assertProps(final YamlShardingConfiguration actual) {
        assertThat(actual.getProps().size(), is(1));
        assertThat(actual.getProps().get("sql.show"), is((Object) true));
    }
}

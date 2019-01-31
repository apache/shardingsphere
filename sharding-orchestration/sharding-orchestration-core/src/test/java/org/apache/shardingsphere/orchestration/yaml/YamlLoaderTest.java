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

package org.apache.shardingsphere.orchestration.yaml;

import org.apache.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlLoaderTest {
    
    private static final String DATA_SOURCE_YAML = "master_ds: !!org.apache.shardingsphere.orchestration.yaml.YamlDataSourceConfiguration\n"
            + "  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n" + "  properties:\n"
            + "    url: jdbc:mysql://localhost:3306/demo_ds_master\n" + "    username: root\n" + "    password: null\n";
    
    private static final String SHARDING_RULE_YAML = "  tables:\n" + "    t_order:\n" + "      actualDataNodes: ds_${0..1}.t_order_${0..1}\n" + "      tableStrategy:\n" 
            + "        inline:\n" + "          shardingColumn: order_id\n" + "          algorithmExpression: t_order_${order_id % 2}\n" + "      keyGenerator:\n" 
            + "        column: order_id\n" + "    t_order_item:\n" + "      actualDataNodes: ds_${0..1}.t_order_item_${0..1}\n" + "      tableStrategy:\n" 
            + "        inline:\n" + "          shardingColumn: order_id\n" + "          algorithmExpression: t_order_item_${order_id % 2}\n" + "      keyGenerator:\n" 
            + "        column: order_item_id\n" + "  bindingTables:\n" + "    - t_order,t_order_item\n" + "  defaultDataSourceName: ds_1\n" 
            + "  defaultDatabaseStrategy:\n" + "    inline:\n" + "      shardingColumn: user_id\n" + "      algorithmExpression: ds_${user_id % 2}";
    
    private static final String MASTER_SLAVE_RULE_YAML = "masterDataSourceName: master_ds\n" + "name: ms_ds\n" + "slaveDataSourceNames:\n" + "- slave_ds_0\n" + "- slave_ds_1\n";
    
    private static final String AUTHENTICATION_YAML = "password: root\nusername: root\n";
    
    private static final String CONFIG_MAP_YAML = "sharding-key1: sharding-value1";
    
    private static final String PROPERTIES_YAML = "executor.size: 16\nsql.show: true";
    
    @Test
    public void assertLoadDataSourceConfigurations() {
        Map<String, DataSourceConfiguration> actual = YamlLoader.loadDataSourceConfigurations(DATA_SOURCE_YAML);
        assertThat(actual.size(), is(1));
        assertTrue(actual.keySet().contains("master_ds"));
    }
    
    @Test
    public void assertLoadShardingRuleConfiguration() {
        ShardingRuleConfiguration actual = YamlLoader.loadShardingRuleConfiguration(SHARDING_RULE_YAML);
        assertThat(actual.getTableRuleConfigs().size(), is(2));
        assertThat(actual.getBindingTableGroups().size(), is(1));
        assertThat(actual.getDefaultDataSourceName(), is("ds_1"));
    }
    
    @Test
    public void assertLoadMasterSlaveRuleConfiguration() {
        MasterSlaveRuleConfiguration actual = YamlLoader.loadMasterSlaveRuleConfiguration(MASTER_SLAVE_RULE_YAML);
        assertThat(actual.getMasterDataSourceName(), is("master_ds"));
        assertThat(actual.getSlaveDataSourceNames().size(), is(2));
    }
    
    @Test
    public void assertLoadAuthentication() {
        Authentication actual = YamlLoader.loadAuthentication(AUTHENTICATION_YAML);
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
    }
    
    @Test
    public void assertLoadConfigMap() {
        Map<String, Object> actual = YamlLoader.loadConfigMap(CONFIG_MAP_YAML);
        assertTrue(actual.containsKey("sharding-key1"));
        assertTrue(actual.containsValue("sharding-value1"));
    }
    
    @Test
    public void assertLoadProperties() {
        Properties actual = YamlLoader.loadProperties(PROPERTIES_YAML);
        assertThat(actual.get("executor.size"), is((Object) 16));
        assertThat(actual.get("sql.show"), is((Object) true));
    }
}

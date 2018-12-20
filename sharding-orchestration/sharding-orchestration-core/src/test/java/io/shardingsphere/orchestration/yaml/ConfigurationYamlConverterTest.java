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

package io.shardingsphere.orchestration.yaml;

import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.constant.PoolType;
import io.shardingsphere.core.rule.Authentication;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ConfigurationYamlConverterTest {
    
    private static final String DATA_SOURCE_YAML = "master_ds: !!io.shardingsphere.orchestration.yaml.YamlDataSourceConfiguration\n"
            + "  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n" + "  properties:\n"
            + "    url: jdbc:mysql://localhost:3306/demo_ds_master\n" + "    username: root\n" + "    password: null\n";
    
    private static final String SHARDING_RULE_YAML = "  tables:\n" + "    t_order:\n" + "      actualDataNodes: ds_${0..1}.t_order_${0..1}\n" + "      tableStrategy:\n"
            + "        inline:\n" + "          shardingColumn: order_id\n" + "          algorithmExpression: t_order_${order_id % 2}\n"
            + "      keyGeneratorColumnName: order_id\n" + "    t_order_item:\n" + "      actualDataNodes: ds_${0..1}.t_order_item_${0..1}\n" + "      tableStrategy:\n" + "        inline:\n"
            + "          shardingColumn: order_id\n" + "          algorithmExpression: t_order_item_${order_id % 2}\n" + "      keyGeneratorColumnName: order_item_id\n" + "  bindingTables:\n"
            + "    - t_order,t_order_item\n" + "  defaultDataSourceName: ds_1\n" + "  defaultDatabaseStrategy:\n" + "    inline:\n" + "      shardingColumn: user_id\n"
            + "      algorithmExpression: ds_${user_id % 2}";
    
    private static final String MASTER_SLAVE_RULE_YAML = "masterDataSourceName: master_ds\n" + "name: ms_ds\n" + "slaveDataSourceNames:\n" + "- slave_ds_0\n" + "- slave_ds_1\n";
    
    private static final String AUTHENTICATION_YAML = "password: root\nusername: root\n";
    
    private static final String CONFIG_MAP_YAML = "sharding-key1: sharding-value1";
    
    private static final String PROPERTIES_YAML = "executor.size: 16\nsql.show: true";
    
    @Test
    public void assertLoadDataSourceConfigurations() {
        Map<String, DataSourceConfiguration> actual = ConfigurationYamlConverter.loadDataSourceConfigurations(DATA_SOURCE_YAML);
        assertThat(actual.size(), is(1));
        assertTrue(actual.keySet().contains("master_ds"));
    }
    
    @Test
    public void assertLoadShardingRuleConfiguration() {
        ShardingRuleConfiguration actual = ConfigurationYamlConverter.loadShardingRuleConfiguration(SHARDING_RULE_YAML);
        assertThat(actual.getTableRuleConfigs().size(), is(2));
        assertThat(actual.getBindingTableGroups().size(), is(1));
        assertThat(actual.getDefaultDataSourceName(), is("ds_1"));
    }
    
    @Test
    public void assertLoadMasterSlaveRuleConfiguration() {
        MasterSlaveRuleConfiguration actual = ConfigurationYamlConverter.loadMasterSlaveRuleConfiguration(MASTER_SLAVE_RULE_YAML);
        assertThat(actual.getMasterDataSourceName(), is("master_ds"));
        assertThat(actual.getSlaveDataSourceNames().size(), is(2));
    }
    
    @Test
    public void assertLoadAuthentication() {
        Authentication actual = ConfigurationYamlConverter.loadAuthentication(AUTHENTICATION_YAML);
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
    }
    
    @Test
    public void assertLoadConfigMap() {
        Map<String, Object> actual = ConfigurationYamlConverter.loadConfigMap(CONFIG_MAP_YAML);
        assertTrue(actual.containsKey("sharding-key1"));
        assertTrue(actual.containsValue("sharding-value1"));
    }
    
    @Test
    public void assertLoadProperties() {
        Properties actual = ConfigurationYamlConverter.loadProperties(PROPERTIES_YAML);
        assertThat(actual.get("executor.size"), is((Object) 16));
        assertThat(actual.get("sql.show"), is((Object) true));
    }
    
    @Test
    public void assertDumpDataSourceConfigurations() {
        String actual = ConfigurationYamlConverter.dumpDataSourceConfigurations(createDataSourceConfigurations());
        assertTrue(actual.contains("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
    }
    
    private Map<String, DataSourceConfiguration> createDataSourceConfigurations() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("driverClassName", "org.h2.Driver");
        properties.put("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        properties.put("username", "root");
        properties.put("password", "root");
        DataSourceConfiguration result = new DataSourceConfiguration(PoolType.DRUID.getClassName());
        result.getProperties().putAll(properties);
        return Collections.singletonMap("test", result);
    }
    
    @Test
    public void assertDumpShardingRuleConfiguration() {
        String actual = ConfigurationYamlConverter.dumpShardingRuleConfiguration(createShardingRuleConfiguration());
        assertTrue(actual.contains("t_order"));
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("t_order");
        tableRuleConfig.setActualDataNodes("ds_${0..1}.t_order_${0..1}");
        result.getTableRuleConfigs().add(tableRuleConfig);
        result.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
        result.setDefaultTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("order_id", "t_order_${order_id % 2}"));
        return result;
    }
    
    @Test
    public void assertDumpMasterSlaveRuleConfiguration() {
        String actual = ConfigurationYamlConverter.dumpMasterSlaveRuleConfiguration(createMasterSlaveRuleConfiguration());
        assertTrue(actual.contains("ms_ds"));
    }
    
    private MasterSlaveRuleConfiguration createMasterSlaveRuleConfiguration() {
        return new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Collections.singletonList("slave_ds"), MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN.getAlgorithm());
    }
    
    @Test
    public void assertDumpAuthentication() {
        String actual = ConfigurationYamlConverter.dumpAuthentication(createAuthentication());
        assertTrue(actual.contains("root"));
    }
    
    private Authentication createAuthentication() {
        Authentication result = new Authentication();
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    @Test
    public void assertDumpConfigMap() {
        String actual = ConfigurationYamlConverter.dumpConfigMap(createConfigMap());
        assertTrue(actual.contains("key1"));
    }
    
    private Map<String, Object> createConfigMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key1", "value1");
        return result;
    }
    
    @Test
    public void assertDumpProperties() {
        String actual = ConfigurationYamlConverter.dumpProperties(createProperties());
        assertTrue(actual.contains("key1"));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("key1", "value1");
        return result;
    }
}

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

package org.apache.shardingsphere.orchestration.yaml.dumper;

import org.apache.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import org.apache.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public final class YamlDumperTest {
    
    @Test
    public void assertDumpDataSourceConfigurations() {
        String actual = YamlDumper.dumpDataSourceConfigurations(createDataSourceConfigurations());
        assertTrue(actual.contains("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
    }
    
    private Map<String, DataSourceConfiguration> createDataSourceConfigurations() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("driverClassName", "org.h2.Driver");
        properties.put("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        properties.put("username", "root");
        properties.put("password", "root");
        DataSourceConfiguration result = new DataSourceConfiguration("com.alibaba.druid.pool.DruidDataSource");
        result.getProperties().putAll(properties);
        return Collections.singletonMap("test", result);
    }
    
    @Test
    public void assertDumpShardingRuleConfiguration() {
        String actual = YamlDumper.dumpShardingRuleConfiguration(createShardingRuleConfiguration());
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
        String actual = YamlDumper.dumpMasterSlaveRuleConfiguration(createMasterSlaveRuleConfiguration());
        assertTrue(actual.contains("ms_ds"));
    }
    
    private MasterSlaveRuleConfiguration createMasterSlaveRuleConfiguration() {
        return new MasterSlaveRuleConfiguration("ms_ds", "master_ds", Collections.singletonList("slave_ds"), MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN.getAlgorithm());
    }
    
    @Test
    public void assertDumpAuthentication() {
        String actual = YamlDumper.dumpAuthentication(new Authentication("root", "root"));
        assertTrue(actual.contains("root"));
    }
    
    @Test
    public void assertDumpConfigMap() {
        String actual = YamlDumper.dumpConfigMap(createConfigMap());
        assertTrue(actual.contains("key1"));
    }
    
    private Map<String, Object> createConfigMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key1", "value1");
        return result;
    }
    
    @Test
    public void assertDumpProperties() {
        String actual = YamlDumper.dumpProperties(createProperties());
        assertTrue(actual.contains("key1"));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("key1", "value1");
        return result;
    }
}

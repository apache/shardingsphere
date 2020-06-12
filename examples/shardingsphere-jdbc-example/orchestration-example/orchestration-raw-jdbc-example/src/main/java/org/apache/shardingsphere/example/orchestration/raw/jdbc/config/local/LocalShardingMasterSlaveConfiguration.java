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

package org.apache.shardingsphere.example.orchestration.raw.jdbc.config.local;

import org.apache.shardingsphere.driver.orchestration.api.OrchestrationShardingSphereDataSourceFactory;
import org.apache.shardingsphere.example.algorithm.StandardModuloShardingDatabaseAlgorithm;
import org.apache.shardingsphere.example.algorithm.StandardModuloShardingTableAlgorithm;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.algorithm.KeyGenerateAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public final class LocalShardingMasterSlaveConfiguration implements ExampleConfiguration {
    
    private final Map<String, CenterConfiguration> centerConfigurationMap;
    
    public LocalShardingMasterSlaveConfiguration(final Map<String, CenterConfiguration> centerConfigurationMap) {
        this.centerConfigurationMap = centerConfigurationMap;
    }
    
    @Override
    public DataSource getDataSource() throws SQLException {
        OrchestrationConfiguration orchestrationConfig = new OrchestrationConfiguration(centerConfigurationMap);
        Collection<RuleConfiguration> configurations = new LinkedList<>();
        configurations.add(getShardingRuleConfiguration());
        configurations.add(getMasterSlaveRuleConfiguration());
        return OrchestrationShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), configurations, new Properties(), orchestrationConfig);
    }
    
    private ShardingRuleConfiguration getShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(getOrderTableRuleConfiguration());
        result.getTables().add(getOrderItemTableRuleConfiguration());
        result.getBindingTableGroups().add("t_order, t_order_item");
        result.getBroadcastTables().add("t_address");
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", new StandardModuloShardingDatabaseAlgorithm()));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", new StandardModuloShardingTableAlgorithm()));
        result.getKeyGenerators().put("snowflake", new KeyGenerateAlgorithmConfiguration("SNOWFLAKE", getProperties()));
        return result;
    }
    
    private ShardingTableRuleConfiguration getOrderTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${[0, 1]}");
        result.setKeyGenerateStrategy(getKeyGeneratorConfiguration());
        return result;
    }
    
    private ShardingTableRuleConfiguration getOrderItemTableRuleConfiguration() {
        return new ShardingTableRuleConfiguration("t_order_item", "ds_${0..1}.t_order_item_${[0, 1]}");
    }
    
    private static KeyGenerateStrategyConfiguration getKeyGeneratorConfiguration() {
        return new KeyGenerateStrategyConfiguration("order_id", "snowflake");
    }
    
    private MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration() {
        MasterSlaveDataSourceRuleConfiguration dataSourceConfiguration1 = new MasterSlaveDataSourceRuleConfiguration(
                "ds_0", "demo_ds_master_0", Arrays.asList("demo_ds_master_0_slave_0", "demo_ds_master_0_slave_1"), null);
        MasterSlaveDataSourceRuleConfiguration dataSourceConfiguration2 = new MasterSlaveDataSourceRuleConfiguration(
                "ds_1", "demo_ds_master_1", Arrays.asList("demo_ds_master_1_slave_0", "demo_ds_master_1_slave_1"), null);
        return new MasterSlaveRuleConfiguration(Arrays.asList(dataSourceConfiguration1, dataSourceConfiguration2), Collections.emptyMap());
    }
    
    private static Properties getProperties() {
        Properties result = new Properties();
        result.setProperty("worker.id", "123");
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        final Map<String, DataSource> result = new HashMap<>();
        result.put("demo_ds_master_0", DataSourceUtil.createDataSource("demo_ds_master_0"));
        result.put("demo_ds_master_0_slave_0", DataSourceUtil.createDataSource("demo_ds_master_0_slave_0"));
        result.put("demo_ds_master_0_slave_1", DataSourceUtil.createDataSource("demo_ds_master_0_slave_1"));
        result.put("demo_ds_master_1", DataSourceUtil.createDataSource("demo_ds_master_1"));
        result.put("demo_ds_master_1_slave_0", DataSourceUtil.createDataSource("demo_ds_master_1_slave_0"));
        result.put("demo_ds_master_1_slave_1", DataSourceUtil.createDataSource("demo_ds_master_1_slave_1"));
        return result;
    }
}

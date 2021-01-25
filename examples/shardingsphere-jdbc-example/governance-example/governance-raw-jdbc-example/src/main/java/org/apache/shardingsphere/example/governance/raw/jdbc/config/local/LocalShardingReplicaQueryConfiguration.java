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

package org.apache.shardingsphere.example.governance.raw.jdbc.config.local;

import org.apache.shardingsphere.driver.governance.api.GovernanceShardingSphereDataSourceFactory;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
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

public final class LocalShardingReplicaQueryConfiguration implements ExampleConfiguration {
    
    private final GovernanceConfiguration governanceConfig;
    
    public LocalShardingReplicaQueryConfiguration(final GovernanceConfiguration governanceConfig) {
        this.governanceConfig = governanceConfig;
    }
    
    @Override
    public DataSource getDataSource() throws SQLException {
        Collection<RuleConfiguration> configs = new LinkedList<>();
        configs.add(getShardingRuleConfiguration());
        configs.add(getReplicaQueryRuleConfiguration());
        return GovernanceShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), configs, new Properties(), governanceConfig);
    }
    
    private ShardingRuleConfiguration getShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(getOrderTableRuleConfiguration());
        result.getTables().add(getOrderItemTableRuleConfiguration());
        result.getBindingTableGroups().add("t_order, t_order_item");
        result.getBroadcastTables().add("t_address");
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "standard_test_db"));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "standard_test_tbl"));
        result.getShardingAlgorithms() .put("standard_test_db", new ShardingSphereAlgorithmConfiguration("STANDARD_TEST_DB", new Properties()));
        result.getShardingAlgorithms() .put("standard_test_tbl", new ShardingSphereAlgorithmConfiguration("STANDARD_TEST_TBL", new Properties()));
        result.getKeyGenerators().put("snowflake", new ShardingSphereAlgorithmConfiguration("SNOWFLAKE", getProperties()));
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
    
    private ReplicaQueryRuleConfiguration getReplicaQueryRuleConfiguration() {
        ReplicaQueryDataSourceRuleConfiguration dataSourceConfiguration1 = new ReplicaQueryDataSourceRuleConfiguration(
                "ds_0", "demo_primary_ds_0", Arrays.asList("demo_primary_ds_0_replica_0", "demo_primary_ds_0_replica_1"), null);
        ReplicaQueryDataSourceRuleConfiguration dataSourceConfiguration2 = new ReplicaQueryDataSourceRuleConfiguration(
                "ds_1", "demo_primary_ds_1", Arrays.asList("demo_primary_ds_1_replica_0", "demo_primary_ds_1_replica_1"), null);
        return new ReplicaQueryRuleConfiguration(Arrays.asList(dataSourceConfiguration1, dataSourceConfiguration2), Collections.emptyMap());
    }
    
    private static Properties getProperties() {
        Properties result = new Properties();
        result.setProperty("worker-id", "123");
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(6, 1);
        result.put("demo_primary_ds_0", DataSourceUtil.createDataSource("demo_primary_ds_0"));
        result.put("demo_primary_ds_0_replica_0", DataSourceUtil.createDataSource("demo_primary_ds_0_replica_0"));
        result.put("demo_primary_ds_0_replica_1", DataSourceUtil.createDataSource("demo_primary_ds_0_replica_1"));
        result.put("demo_primary_ds_1", DataSourceUtil.createDataSource("demo_primary_ds_1"));
        result.put("demo_primary_ds_1_replica_0", DataSourceUtil.createDataSource("demo_primary_ds_1_replica_0"));
        result.put("demo_primary_ds_1_replica_1", DataSourceUtil.createDataSource("demo_primary_ds_1_replica_1"));
        return result;
    }
}

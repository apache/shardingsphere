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

package org.apache.shardingsphere.example.cluster.mode.raw.jdbc.config.local;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
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

public final class LocalShardingReadwriteSplittingConfiguration implements ExampleConfiguration {
    
    private final ModeConfiguration modeConfig;
    
    public LocalShardingReadwriteSplittingConfiguration(final ModeConfiguration modeConfig) {
        this.modeConfig = modeConfig;
    }
    
    @Override
    public DataSource getDataSource() throws SQLException {
        Collection<RuleConfiguration> configs = new LinkedList<>();
        configs.add(getShardingRuleConfiguration());
        configs.add(getReadwriteSplittingRuleConfiguration());
        return ShardingSphereDataSourceFactory.createDataSource(modeConfig, createDataSourceMap(), configs, new Properties());
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
        result.getKeyGenerators().put("snowflake", new ShardingSphereAlgorithmConfiguration("SNOWFLAKE", new Properties()));
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
    
    private ReadwriteSplittingRuleConfiguration getReadwriteSplittingRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfiguration1 = new ReadwriteSplittingDataSourceRuleConfiguration(
                "ds_0", "Static", getReadWriteProperties("demo_write_ds_0", "demo_write_ds_0_read_0, demo_write_ds_0_read_1"), null);
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfiguration2 = new ReadwriteSplittingDataSourceRuleConfiguration(
                "ds_1", "Static", getReadWriteProperties("demo_write_ds_1", "demo_write_ds_1_read_0, demo_write_ds_1_read_1"), null);
        return new ReadwriteSplittingRuleConfiguration(Arrays.asList(dataSourceConfiguration1, dataSourceConfiguration2), Collections.emptyMap());
    }
    
    private Properties getReadWriteProperties(final String writeDataSourceName, final String readDataSourceNames) {
        Properties result = new Properties();
        result.setProperty("write-data-source-name", writeDataSourceName);
        result.setProperty("read-data-source-names", readDataSourceNames);
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(6, 1);
        result.put("demo_write_ds_0", DataSourceUtil.createDataSource("demo_write_ds_0"));
        result.put("demo_write_ds_0_read_0", DataSourceUtil.createDataSource("demo_write_ds_0_read_0"));
        result.put("demo_write_ds_0_read_1", DataSourceUtil.createDataSource("demo_write_ds_0_read_1"));
        result.put("demo_write_ds_1", DataSourceUtil.createDataSource("demo_write_ds_1"));
        result.put("demo_write_ds_1_read_0", DataSourceUtil.createDataSource("demo_write_ds_1_read_0"));
        result.put("demo_write_ds_1_read_1", DataSourceUtil.createDataSource("demo_write_ds_1_read_1"));
        return result;
    }
}

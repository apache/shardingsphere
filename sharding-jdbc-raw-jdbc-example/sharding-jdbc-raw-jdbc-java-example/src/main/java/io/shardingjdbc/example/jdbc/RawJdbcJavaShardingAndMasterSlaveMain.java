/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.example.jdbc;

import com.google.common.collect.Lists;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.example.jdbc.algorithm.ModuloShardingTableAlgorithm;
import io.shardingjdbc.example.jdbc.repository.RawJdbcRepository;
import io.shardingjdbc.example.jdbc.util.DataSourceUtil;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class RawJdbcJavaShardingAndMasterSlaveMain {
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws SQLException {
    // CHECKSTYLE:ON
        new RawJdbcRepository(getShardingDataSource()).testAll();
    }
    
    private static ShardingDataSource getShardingDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
    
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualTables("t_order_0, t_order_1");
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    
        TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        orderItemTableRuleConfig.setActualTables("t_order_item_0, t_order_item_1");
        shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
    
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
    
        StandardShardingStrategyConfiguration databaseShardingStrategyConfig = new StandardShardingStrategyConfiguration();
        databaseShardingStrategyConfig.setShardingColumn("user_id");
        databaseShardingStrategyConfig.setPreciseAlgorithmClassName(ModuloShardingTableAlgorithm.class.getName());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(databaseShardingStrategyConfig);
    
        StandardShardingStrategyConfiguration tableShardingStrategyConfig = new StandardShardingStrategyConfiguration();
        tableShardingStrategyConfig.setShardingColumn("order_id");
        tableShardingStrategyConfig.setPreciseAlgorithmClassName(ModuloShardingTableAlgorithm.class.getName());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(tableShardingStrategyConfig);
        
        MasterSlaveRuleConfiguration masterSlaveRuleConfig1 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig1.setName("ds_0");
        masterSlaveRuleConfig1.setMasterDataSourceName("ds_jdbc_master_0");
        masterSlaveRuleConfig1.setSlaveDataSourceNames(Arrays.asList("ds_jdbc_master_0_slave_0", "ds_jdbc_master_0_slave_1"));
        
        MasterSlaveRuleConfiguration masterSlaveRuleConfig2 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig2.setName("ds_1");
        masterSlaveRuleConfig2.setMasterDataSourceName("ds_jdbc_master_1");
        masterSlaveRuleConfig2.setSlaveDataSourceNames(Arrays.asList("ds_jdbc_master_1_slave_0", "ds_jdbc_master_1_slave_1"));
        shardingRuleConfig.setMasterSlaveRuleConfigs(Lists.newArrayList(masterSlaveRuleConfig1, masterSlaveRuleConfig2));
        return new ShardingDataSource(shardingRuleConfig.build(createDataSourceMap()));
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        final Map<String, DataSource> result = new HashMap<>(6, 1);
        result.put("ds_jdbc_master_0", DataSourceUtil.createDataSource("ds_jdbc_master_0"));
        result.put("ds_jdbc_master_0_slave_0", DataSourceUtil.createDataSource("ds_jdbc_master_0_slave_0"));
        result.put("ds_jdbc_master_0_slave_1", DataSourceUtil.createDataSource("ds_jdbc_master_0_slave_1"));
        result.put("ds_jdbc_master_1", DataSourceUtil.createDataSource("ds_jdbc_master_1"));
        result.put("ds_jdbc_master_1_slave_0", DataSourceUtil.createDataSource("ds_jdbc_master_1_slave_0"));
        result.put("ds_jdbc_master_1_slave_1", DataSourceUtil.createDataSource("ds_jdbc_master_1_slave_1"));
    
        MasterSlaveRuleConfiguration masterSlaveRuleConfig1 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig1.setName("ds_0");
        masterSlaveRuleConfig1.setMasterDataSourceName("ds_jdbc_master_0");
        masterSlaveRuleConfig1.setSlaveDataSourceNames(Arrays.asList("ds_jdbc_master_0_slave_0", "ds_jdbc_master_0_slave_1"));
    
        MasterSlaveRuleConfiguration masterSlaveRuleConfig2 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig2.setName("ds_1");
        masterSlaveRuleConfig2.setMasterDataSourceName("ds_jdbc_master_1");
        masterSlaveRuleConfig2.setSlaveDataSourceNames(Arrays.asList("ds_jdbc_master_1_slave_0", "ds_jdbc_master_1_slave_1"));
    
        return result;
    }
}

/*
 * Copyright 1999-2015 dangdang.com.
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

package com.dangdang.ddframe.rdb.integrate.type.sharding;

import com.dangdang.ddframe.rdb.common.base.AbstractSQLAssertTest;
import com.dangdang.ddframe.rdb.common.env.ShardingTestStrategy;
import com.dangdang.ddframe.rdb.integrate.fixture.PreciseModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.RangeModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.jaxb.SQLShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.StandardShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.rule.MasterSlaveRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.MasterSlaveDataSource;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.junit.After;
import org.junit.AfterClass;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ShardingMasterSlaveTest extends AbstractSQLAssertTest {
    
    private static boolean isShutdown;
    
    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();
    
    public ShardingMasterSlaveTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> sqlShardingRules) {
        super(testCaseName, sql, type, sqlShardingRules);
    }
    
    @Override
    protected ShardingTestStrategy getShardingStrategy() {
        return ShardingTestStrategy.masterslave;
    }
    
    @Override
    protected List<String> getInitDataSetFiles() {
        return Arrays.asList(
                "integrate/dataset/sharding/masterslave/init/master_0.xml",
                "integrate/dataset/sharding/masterslave/init/master_1.xml",
                "integrate/dataset/sharding/masterslave/init/master_2.xml",
                "integrate/dataset/sharding/masterslave/init/master_3.xml",
                "integrate/dataset/sharding/masterslave/init/master_4.xml",
                "integrate/dataset/sharding/masterslave/init/master_5.xml",
                "integrate/dataset/sharding/masterslave/init/master_6.xml",
                "integrate/dataset/sharding/masterslave/init/master_7.xml",
                "integrate/dataset/sharding/masterslave/init/master_8.xml",
                "integrate/dataset/sharding/masterslave/init/master_9.xml",
                "integrate/dataset/sharding/masterslave/init/slave_0.xml",
                "integrate/dataset/sharding/masterslave/init/slave_1.xml",
                "integrate/dataset/sharding/masterslave/init/slave_2.xml",
                "integrate/dataset/sharding/masterslave/init/slave_3.xml",
                "integrate/dataset/sharding/masterslave/init/slave_4.xml",
                "integrate/dataset/sharding/masterslave/init/slave_5.xml",
                "integrate/dataset/sharding/masterslave/init/slave_6.xml",
                "integrate/dataset/sharding/masterslave/init/slave_7.xml",
                "integrate/dataset/sharding/masterslave/init/slave_8.xml",
                "integrate/dataset/sharding/masterslave/init/slave_9.xml");
    }
    
    @Override
    protected final Map<DatabaseType, ShardingDataSource> getDataSources() throws SQLException {
        if (!shardingDataSources.isEmpty() && !isShutdown) {
            return shardingDataSources;
        }
        isShutdown = false;
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            Map<String, DataSource> masterSlaveDataSourceMap = each.getValue();
            MasterSlaveDataSource masterSlaveDs0 = getMasterSlaveDataSource(masterSlaveDataSourceMap, "ms_0", "dataSource_master_0", "dataSource_slave_0");
            MasterSlaveDataSource masterSlaveDs1 = getMasterSlaveDataSource(masterSlaveDataSourceMap, "ms_1", "dataSource_master_1", "dataSource_slave_1");
            MasterSlaveDataSource masterSlaveDs2 = getMasterSlaveDataSource(masterSlaveDataSourceMap, "ms_2", "dataSource_master_2", "dataSource_slave_2");
            MasterSlaveDataSource masterSlaveDs3 = getMasterSlaveDataSource(masterSlaveDataSourceMap, "ms_3", "dataSource_master_3", "dataSource_slave_3");
            MasterSlaveDataSource masterSlaveDs4 = getMasterSlaveDataSource(masterSlaveDataSourceMap, "ms_4", "dataSource_master_4", "dataSource_slave_4");
            MasterSlaveDataSource masterSlaveDs5 = getMasterSlaveDataSource(masterSlaveDataSourceMap, "ms_5", "dataSource_master_5", "dataSource_slave_5");
            MasterSlaveDataSource masterSlaveDs6 = getMasterSlaveDataSource(masterSlaveDataSourceMap, "ms_6", "dataSource_master_6", "dataSource_slave_6");
            MasterSlaveDataSource masterSlaveDs7 = getMasterSlaveDataSource(masterSlaveDataSourceMap, "ms_7", "dataSource_master_7", "dataSource_slave_7");
            MasterSlaveDataSource masterSlaveDs8 = getMasterSlaveDataSource(masterSlaveDataSourceMap, "ms_8", "dataSource_master_8", "dataSource_slave_8");
            MasterSlaveDataSource masterSlaveDs9 = getMasterSlaveDataSource(masterSlaveDataSourceMap, "ms_9", "dataSource_master_9", "dataSource_slave_9");
            Map<String, DataSource> msDataSourceMap = new HashMap<>(10);
            msDataSourceMap.put("ms_0", masterSlaveDs0);
            msDataSourceMap.put("ms_1", masterSlaveDs1);
            msDataSourceMap.put("ms_2", masterSlaveDs2);
            msDataSourceMap.put("ms_3", masterSlaveDs3);
            msDataSourceMap.put("ms_4", masterSlaveDs4);
            msDataSourceMap.put("ms_5", masterSlaveDs5);
            msDataSourceMap.put("ms_6", masterSlaveDs6);
            msDataSourceMap.put("ms_7", masterSlaveDs7);
            msDataSourceMap.put("ms_8", masterSlaveDs8);
            msDataSourceMap.put("ms_9", masterSlaveDs9);
            ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
            shardingRuleConfig.setDataSources(msDataSourceMap);
            TableRuleConfig orderTableRuleConfig = new TableRuleConfig();
            orderTableRuleConfig.setLogicTable("t_order");
            orderTableRuleConfig.setActualTables("t_order_${0..9}");
            shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
            TableRuleConfig orderItemTableRuleConfig = new TableRuleConfig();
            orderItemTableRuleConfig.setLogicTable("t_order_item");
            orderItemTableRuleConfig.setActualTables("t_order_item_${0..9}");
            shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
            TableRuleConfig configTableRuleConfig = new TableRuleConfig();
            configTableRuleConfig.setLogicTable("t_config");
            configTableRuleConfig.setActualTables("t_config");
            shardingRuleConfig.getTableRuleConfigs().add(configTableRuleConfig);
            StandardShardingStrategyConfig databaseShardingStrategy = new StandardShardingStrategyConfig();
            databaseShardingStrategy.setShardingColumn("t_order_item");
            databaseShardingStrategy.setPreciseAlgorithmClassName(PreciseModuloDatabaseShardingAlgorithm.class.getName());
            databaseShardingStrategy.setRangeAlgorithmClassName(RangeModuloDatabaseShardingAlgorithm.class.getName());
            shardingRuleConfig.setDefaultTableShardingStrategyConfig(databaseShardingStrategy);
            StandardShardingStrategyConfig tableShardingStrategy = new StandardShardingStrategyConfig();
            tableShardingStrategy.setShardingColumn("user_id");
            tableShardingStrategy.setPreciseAlgorithmClassName(PreciseModuloDatabaseShardingAlgorithm.class.getName());
            tableShardingStrategy.setRangeAlgorithmClassName(RangeModuloDatabaseShardingAlgorithm.class.getName());
            shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(tableShardingStrategy);
            shardingDataSources.put(each.getKey(), new ShardingDataSource(shardingRuleConfig.build()));
        }
        return shardingDataSources;
    }
    
    private MasterSlaveDataSource getMasterSlaveDataSource(final Map<String, DataSource> masterSlaveDataSourceMap, 
                                                           final String name, final String masterDataSourceName, final String slaveDataSourceName) throws SQLException {
        Map<String, DataSource> slaveDs0 = new HashMap<>(1, 1);
        slaveDs0.put(slaveDataSourceName, masterSlaveDataSourceMap.get(slaveDataSourceName));
        return new MasterSlaveDataSource(new MasterSlaveRule(name, masterDataSourceName, masterSlaveDataSourceMap.get(masterDataSourceName), slaveDs0));
    }
    
    @After
    public final void clearFlag() {
        HintManagerHolder.clear();
        MasterSlaveDataSource.resetDMLFlag();
    }
    
    @AfterClass
    public static void clear() {
        isShutdown = true;
        if (!shardingDataSources.isEmpty()) {
            for (ShardingDataSource each : shardingDataSources.values()) {
                each.close();
            }
        }
    }
}

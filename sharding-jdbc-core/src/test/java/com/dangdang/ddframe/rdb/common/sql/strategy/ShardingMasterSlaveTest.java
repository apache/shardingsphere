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

package com.dangdang.ddframe.rdb.common.sql.strategy;

import com.dangdang.ddframe.rdb.common.jaxb.SqlShardingRule;
import com.dangdang.ddframe.rdb.common.sql.base.AbstractSQLAssertTest;
import com.dangdang.ddframe.rdb.common.sql.common.ShardingTestStrategy;
import com.dangdang.ddframe.rdb.integrate.fixture.SingleKeyModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.SingleKeyModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.MasterSlaveDataSource;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(Parameterized.class)
public class ShardingMasterSlaveTest extends AbstractSQLAssertTest {
    
    private static boolean isShutdown;
    
    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();
    
    public ShardingMasterSlaveTest(final String testCaseName, final String sql, final Set<DatabaseType> types, final List<SqlShardingRule> sqlShardingRules) {
        super(testCaseName, sql, types, sqlShardingRules);
    }
    
    @Override
    protected ShardingTestStrategy getShardingStrategy() {
        return ShardingTestStrategy.masterslave;
    }
    
    @Override
    protected List<String> getDataSetFiles() {
        return Arrays.asList(
                "integrate/dataset/masterslave/init/master_0.xml",
                "integrate/dataset/masterslave/init/master_1.xml",
                "integrate/dataset/masterslave/init/master_2.xml",
                "integrate/dataset/masterslave/init/master_3.xml",
                "integrate/dataset/masterslave/init/master_4.xml",
                "integrate/dataset/masterslave/init/master_5.xml",
                "integrate/dataset/masterslave/init/master_6.xml",
                "integrate/dataset/masterslave/init/master_7.xml",
                "integrate/dataset/masterslave/init/master_8.xml",
                "integrate/dataset/masterslave/init/master_9.xml",
                "integrate/dataset/masterslave/init/slave_0.xml",
                "integrate/dataset/masterslave/init/slave_1.xml",
                "integrate/dataset/masterslave/init/slave_2.xml",
                "integrate/dataset/masterslave/init/slave_3.xml",
                "integrate/dataset/masterslave/init/slave_4.xml",
                "integrate/dataset/masterslave/init/slave_5.xml",
                "integrate/dataset/masterslave/init/slave_6.xml",
                "integrate/dataset/masterslave/init/slave_7.xml",
                "integrate/dataset/masterslave/init/slave_8.xml",
                "integrate/dataset/masterslave/init/slave_9.xml");
    }
    
    @Override
    protected final Map<DatabaseType, ShardingDataSource> getShardingDataSources() {
        if (!shardingDataSources.isEmpty() && !isShutdown) {
            return shardingDataSources;
        }
        isShutdown = false;
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            Map<String, DataSource> masterSlaveDataSourceMap = each.getValue();
            MasterSlaveDataSource masterSlaveDs0 = new MasterSlaveDataSource("ms_0", masterSlaveDataSourceMap.get("dataSource_master_0"),
                    Collections.singletonList(masterSlaveDataSourceMap.get("dataSource_slave_0")));
            MasterSlaveDataSource masterSlaveDs1 = new MasterSlaveDataSource("ms_1", masterSlaveDataSourceMap.get("dataSource_master_1"),
                    Collections.singletonList(masterSlaveDataSourceMap.get("dataSource_slave_1")));
            MasterSlaveDataSource masterSlaveDs2 = new MasterSlaveDataSource("ms_2", masterSlaveDataSourceMap.get("dataSource_master_2"),
                    Collections.singletonList(masterSlaveDataSourceMap.get("dataSource_slave_2")));
            MasterSlaveDataSource masterSlaveDs3 = new MasterSlaveDataSource("ms_3", masterSlaveDataSourceMap.get("dataSource_master_3"),
                    Collections.singletonList(masterSlaveDataSourceMap.get("dataSource_slave_3")));
            MasterSlaveDataSource masterSlaveDs4 = new MasterSlaveDataSource("ms_4", masterSlaveDataSourceMap.get("dataSource_master_4"),
                    Collections.singletonList(masterSlaveDataSourceMap.get("dataSource_slave_4")));
            MasterSlaveDataSource masterSlaveDs5 = new MasterSlaveDataSource("ms_5", masterSlaveDataSourceMap.get("dataSource_master_5"),
                    Collections.singletonList(masterSlaveDataSourceMap.get("dataSource_slave_5")));
            MasterSlaveDataSource masterSlaveDs6 = new MasterSlaveDataSource("ms_6", masterSlaveDataSourceMap.get("dataSource_master_6"),
                    Collections.singletonList(masterSlaveDataSourceMap.get("dataSource_slave_6")));
            MasterSlaveDataSource masterSlaveDs7 = new MasterSlaveDataSource("ms_7", masterSlaveDataSourceMap.get("dataSource_master_7"),
                    Collections.singletonList(masterSlaveDataSourceMap.get("dataSource_slave_7")));
            MasterSlaveDataSource masterSlaveDs8 = new MasterSlaveDataSource("ms_8", masterSlaveDataSourceMap.get("dataSource_master_8"),
                    Collections.singletonList(masterSlaveDataSourceMap.get("dataSource_slave_8")));
            MasterSlaveDataSource masterSlaveDs9 = new MasterSlaveDataSource("ms_9", masterSlaveDataSourceMap.get("dataSource_master_9"),
                    Collections.singletonList(masterSlaveDataSourceMap.get("dataSource_slave_9")));
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
            DataSourceRule dataSourceRule = new DataSourceRule(msDataSourceMap);
            TableRule orderTableRule = TableRule.builder("t_order").actualTables(Arrays.asList(
                    "t_order_0",
                    "t_order_1",
                    "t_order_2",
                    "t_order_3",
                    "t_order_4",
                    "t_order_5",
                    "t_order_6",
                    "t_order_7",
                    "t_order_8",
                    "t_order_9")).dataSourceRule(dataSourceRule).build();
            TableRule orderItemTableRule = TableRule.builder("t_order_item").actualTables(Arrays.asList(
                    "t_order_item_0",
                    "t_order_item_1",
                    "t_order_item_2",
                    "t_order_item_3",
                    "t_order_item_4",
                    "t_order_item_5",
                    "t_order_item_6",
                    "t_order_item_7",
                    "t_order_item_8",
                    "t_order_item_9")).dataSourceRule(dataSourceRule).build();
            TableRule configRule = TableRule.builder("t_config").dataSourceRule(dataSourceRule).build();
            ShardingRule shardingRule = ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Arrays.asList(orderTableRule, orderItemTableRule, configRule))
                    .bindingTableRules(Collections.singletonList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))))
                    .databaseShardingStrategy(new DatabaseShardingStrategy("user_id", new SingleKeyModuloDatabaseShardingAlgorithm()))
                    .tableShardingStrategy(new TableShardingStrategy("order_id", new SingleKeyModuloTableShardingAlgorithm())).build();
            shardingDataSources.put(each.getKey(), new ShardingDataSource(shardingRule));
        }
        return shardingDataSources;
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

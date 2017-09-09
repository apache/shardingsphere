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
import com.dangdang.ddframe.rdb.integrate.fixture.PreciseDynamicModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.PreciseModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.RangeDynamicModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.RangeModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.jaxb.SQLShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.config.BindingTableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.DataSourceRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.StandardShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.junit.AfterClass;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShardingDatabaseAndTableDynamicTest extends AbstractSQLAssertTest {
    
    private static boolean isShutdown;
    
    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();
    
    public ShardingDatabaseAndTableDynamicTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> sqlShardingRules) {
        super(testCaseName, sql, type, sqlShardingRules);
    }
    
    @Override
    protected ShardingTestStrategy getShardingStrategy() {
        return ShardingTestStrategy.dbtbl;
    }
    
    @Override
    protected List<String> getInitDataSetFiles() {
        return Arrays.asList(
                "integrate/dataset/sharding/dbtbl/init/dbtbl_0.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_1.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_2.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_3.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_4.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_5.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_6.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_7.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_8.xml",
                "integrate/dataset/sharding/dbtbl/init/dbtbl_9.xml");
    }
    
    @Override
    protected final Map<DatabaseType, ShardingDataSource> getDataSources() throws SQLException {
        if (!shardingDataSources.isEmpty() && !isShutdown) {
            return shardingDataSources;
        }
        isShutdown = false;
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            
            DataSourceRuleConfig dataSourceRuleConfig = new DataSourceRuleConfig();
            dataSourceRuleConfig.setDataSources(each.getValue());
            ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
            shardingRuleConfig.setDataSourceRule(dataSourceRuleConfig);
            TableRuleConfig orderTableRuleConfig = new TableRuleConfig();
            orderTableRuleConfig.setLogicTable("t_order");
            orderTableRuleConfig.setDynamic(true);
            TableRuleConfig orderItemTableRuleConfig = new TableRuleConfig();
            orderItemTableRuleConfig.setLogicTable("t_order_item");
            orderItemTableRuleConfig.setDynamic(true);
            Map<String, TableRuleConfig> tableRuleConfigMap = new HashMap<>(2, 1);
            tableRuleConfigMap.put("t_order", orderTableRuleConfig);
            tableRuleConfigMap.put("t_order_item", orderItemTableRuleConfig);
            shardingRuleConfig.setTableRules(tableRuleConfigMap);
            BindingTableRuleConfig bindingTableRuleConfig = new BindingTableRuleConfig();
            bindingTableRuleConfig.setTableNames("t_order, t_order_item");
            shardingRuleConfig.setBindingTableRules(Collections.singletonList(bindingTableRuleConfig));
            StandardShardingStrategyConfig databaseShardingStrategyConfig = new StandardShardingStrategyConfig();
            databaseShardingStrategyConfig.setShardingColumn("user_id");
            databaseShardingStrategyConfig.setPreciseAlgorithmClassName(PreciseModuloDatabaseShardingAlgorithm.class.getName());
            databaseShardingStrategyConfig.setRangeAlgorithmClassName(RangeModuloDatabaseShardingAlgorithm.class.getName());
            shardingRuleConfig.setDefaultDatabaseShardingStrategy(databaseShardingStrategyConfig);
            StandardShardingStrategyConfig tableShardingStrategyConfig = new StandardShardingStrategyConfig();
            tableShardingStrategyConfig.setShardingColumn("order_id");
            tableShardingStrategyConfig.setPreciseAlgorithmClassName(PreciseDynamicModuloTableShardingAlgorithm.class.getName());
            tableShardingStrategyConfig.setRangeAlgorithmClassName(RangeDynamicModuloTableShardingAlgorithm.class.getName());
            shardingRuleConfig.setDefaultTableShardingStrategy(tableShardingStrategyConfig);
            ShardingRule shardingRule = new ShardingRule(shardingRuleConfig);
            shardingDataSources.put(each.getKey(), new ShardingDataSource(shardingRule));
        }
        return shardingDataSources;
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

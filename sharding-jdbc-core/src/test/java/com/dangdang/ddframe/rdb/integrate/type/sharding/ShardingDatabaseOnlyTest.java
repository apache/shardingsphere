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
import com.dangdang.ddframe.rdb.integrate.fixture.ComplexKeysModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.jaxb.SQLShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.config.BindingTableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.DataSourceRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.GenerateKeyStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ComplexShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.NoneShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.keygen.fixture.IncrementKeyGenerator;
import org.junit.AfterClass;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShardingDatabaseOnlyTest extends AbstractSQLAssertTest {
    
    private static boolean isShutdown;
    
    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();
    
    public ShardingDatabaseOnlyTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> sqlShardingRules) {
        super(testCaseName, sql, type, sqlShardingRules);
    }
    
    @Override
    protected ShardingTestStrategy getShardingStrategy() {
        return ShardingTestStrategy.db;
    }
    
    @Override
    protected List<String> getInitDataSetFiles() {
        return Arrays.asList(
                "integrate/dataset/sharding/db/init/db_0.xml",
                "integrate/dataset/sharding/db/init/db_1.xml",
                "integrate/dataset/sharding/db/init/db_2.xml",
                "integrate/dataset/sharding/db/init/db_3.xml",
                "integrate/dataset/sharding/db/init/db_4.xml",
                "integrate/dataset/sharding/db/init/db_5.xml",
                "integrate/dataset/sharding/db/init/db_6.xml",
                "integrate/dataset/sharding/db/init/db_7.xml",
                "integrate/dataset/sharding/db/init/db_8.xml",
                "integrate/dataset/sharding/db/init/db_9.xml");
    }
    
    @Override
    protected Map<DatabaseType, ShardingDataSource> getDataSources() throws SQLException {
        if (!shardingDataSources.isEmpty() && !isShutdown) {
            return shardingDataSources;
        }
        isShutdown = false;
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
            DataSourceRuleConfig dataSourceRuleConfig = new DataSourceRuleConfig();
            dataSourceRuleConfig.setDataSources(each.getValue());
            shardingRuleConfig.setDataSourceRule(dataSourceRuleConfig);
            Map<String, TableRuleConfig> tableRuleConfigMap = new HashMap<>(2, 1);
            TableRuleConfig orderTableRuleConfig = new TableRuleConfig();
            orderTableRuleConfig.setLogicTable("t_order");
            GenerateKeyStrategyConfig orderGenerateKeyStrategyConfig = new GenerateKeyStrategyConfig();
            orderGenerateKeyStrategyConfig.setColumnName("order_id");
            orderGenerateKeyStrategyConfig.setKeyGeneratorClass(IncrementKeyGenerator.class.getName());
            orderTableRuleConfig.setGenerateKeyStrategy(orderGenerateKeyStrategyConfig);
            tableRuleConfigMap.put("t_order", orderTableRuleConfig);
            TableRuleConfig orderItemTableRuleConfig = new TableRuleConfig();
            orderItemTableRuleConfig.setLogicTable("t_order_item");
            tableRuleConfigMap.put("t_order_item", orderItemTableRuleConfig);
            shardingRuleConfig.setTableRules(tableRuleConfigMap);
            BindingTableRuleConfig bindingTableRuleConfig = new BindingTableRuleConfig();
            bindingTableRuleConfig.setTableNames("t_order, t_order_item");
            shardingRuleConfig.setBindingTableRules(Collections.singletonList(bindingTableRuleConfig));
            ComplexShardingStrategyConfig databaseShardingStrategyConfig = new ComplexShardingStrategyConfig();
            databaseShardingStrategyConfig.setShardingColumns("user_id");
            databaseShardingStrategyConfig.setAlgorithmClassName(ComplexKeysModuloDatabaseShardingAlgorithm.class.getName());
            shardingRuleConfig.setDefaultDatabaseShardingStrategy(databaseShardingStrategyConfig);
            shardingRuleConfig.setDefaultTableShardingStrategy(new NoneShardingStrategyConfig());
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

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

package com.dangdang.ddframe.rdb.common.base;

import com.dangdang.ddframe.rdb.sharding.api.config.BindingTableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.DataSourceRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.GenerateKeyStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.ShardingRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.StandardShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.keygen.fixture.IncrementKeyGenerator;
import com.dangdang.ddframe.rdb.sharding.routing.fixture.PreciseOrderShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.fixture.RangeOrderShardingAlgorithm;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public abstract class AbstractShardingJDBCDatabaseAndTableTest extends AbstractSQLTest {
    
    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();
    
    private DatabaseType databaseType;
    
    public AbstractShardingJDBCDatabaseAndTableTest(final DatabaseType databaseType) {
        this.databaseType = databaseType;
    }
    
    @Before
    public void initShardingDataSources() throws SQLException {
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            TableRuleConfig orderTableRuleConfig = new TableRuleConfig();
            orderTableRuleConfig.setLogicTable("t_order");
            orderTableRuleConfig.setActualTables("t_order_0, t_order_1");
            TableRuleConfig orderItemTableRuleConfig = new TableRuleConfig();
            orderItemTableRuleConfig.setLogicTable("t_order_item");
            orderItemTableRuleConfig.setActualTables("t_order_item_0, t_order_item_1");
            GenerateKeyStrategyConfig generateKeyStrategyConfig = new GenerateKeyStrategyConfig();
            generateKeyStrategyConfig.setColumnName("item_id");
            generateKeyStrategyConfig.setKeyGeneratorClass(IncrementKeyGenerator.class.getName());
            orderItemTableRuleConfig.setGenerateKeyStrategy(generateKeyStrategyConfig);
            TableRuleConfig configTableRuleConfig = new TableRuleConfig();
            configTableRuleConfig.setLogicTable("t_config");
            Map<String, TableRuleConfig> tableRuleConfigMap = new HashMap<>(3, 1);
            tableRuleConfigMap.put("t_order", orderTableRuleConfig);
            tableRuleConfigMap.put("t_order_item", orderItemTableRuleConfig);
            tableRuleConfigMap.put("t_config", configTableRuleConfig);
            ShardingRuleConfig shardingRuleConfig = new ShardingRuleConfig();
            DataSourceRuleConfig dataSourceRuleConfig = new DataSourceRuleConfig();
            dataSourceRuleConfig.setDataSources(each.getValue());
            shardingRuleConfig.setDataSourceRule(dataSourceRuleConfig);
            shardingRuleConfig.setTableRules(tableRuleConfigMap);
            BindingTableRuleConfig bindingTableRuleConfig = new BindingTableRuleConfig();
            bindingTableRuleConfig.setTableNames("t_order, t_order_item");
            shardingRuleConfig.setBindingTableRules(Collections.singletonList(bindingTableRuleConfig));
            StandardShardingStrategyConfig databaseShardingStrategyConfig = new StandardShardingStrategyConfig();
            databaseShardingStrategyConfig.setShardingColumn("user_id");
            databaseShardingStrategyConfig.setPreciseAlgorithmClassName(PreciseOrderShardingAlgorithm.class.getName());
            databaseShardingStrategyConfig.setRangeAlgorithmClassName(RangeOrderShardingAlgorithm.class.getName());
            shardingRuleConfig.setDefaultDatabaseShardingStrategy(databaseShardingStrategyConfig);
            StandardShardingStrategyConfig tableShardingStrategyConfig = new StandardShardingStrategyConfig();
            tableShardingStrategyConfig.setShardingColumn("order_id");
            tableShardingStrategyConfig.setPreciseAlgorithmClassName(PreciseOrderShardingAlgorithm.class.getName());
            tableShardingStrategyConfig.setRangeAlgorithmClassName(RangeOrderShardingAlgorithm.class.getName());
            shardingRuleConfig.setDefaultTableShardingStrategy(tableShardingStrategyConfig);
            ShardingRule shardingRule = new ShardingRule(shardingRuleConfig);
            shardingDataSources.put(each.getKey(), new ShardingDataSource(shardingRule));
        }
    }
    
    @Override
    protected List<String> getInitDataSetFiles() {
        return Arrays.asList(
                "integrate/dataset/jdbc/jdbc_0.xml",
                "integrate/dataset/jdbc/jdbc_1.xml");
    }
    
    @Parameterized.Parameters(name = "{0}")
    public static Collection<DatabaseType> dataParameters() {
        return getDatabaseTypes();
    }
    
    @Override
    protected DatabaseType getCurrentDatabaseType() {
        return databaseType;
    }
    
    protected ShardingDataSource getShardingDataSource() {
        return shardingDataSources.get(databaseType);
    }
    
    @AfterClass
    public static void clear() {
        if (!shardingDataSources.isEmpty()) {
            for (ShardingDataSource each : shardingDataSources.values()) {
                each.close();
            }
        }
    }
}

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

import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.keygen.fixture.IncrementKeyGenerator;
import com.dangdang.ddframe.rdb.sharding.routing.fixture.OrderShardingAlgorithm;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
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
    public void initShardingDataSources() {
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            DataSourceRule dataSourceRule = new DataSourceRule(each.getValue());
            TableRule orderTableRule = TableRule.builder("t_order").actualTables(Arrays.asList(
                    "t_order_0",
                    "t_order_1")).dataSourceRule(dataSourceRule).build();
            TableRule orderItemTableRule = TableRule.builder("t_order_item").actualTables(Arrays.asList(
                    "t_order_item_0",
                    "t_order_item_1")).dataSourceRule(dataSourceRule).generateKeyColumn("item_id", IncrementKeyGenerator.class).build();
            TableRule configRule = TableRule.builder("t_config").dataSourceRule(dataSourceRule).build();
            ShardingRule shardingRule = ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Arrays.asList(orderTableRule, orderItemTableRule, configRule))
                    .bindingTableRules(Collections.singletonList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))))
                    .databaseShardingStrategy(new DatabaseShardingStrategy("user_id", new OrderShardingAlgorithm()))
                    .tableShardingStrategy(new TableShardingStrategy("order_id", new OrderShardingAlgorithm())).build();
            shardingDataSources.put(each.getKey(), new ShardingDataSource(shardingRule));
        }
    }
    
    @Override
    protected List<String> getDataSetFiles() {
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

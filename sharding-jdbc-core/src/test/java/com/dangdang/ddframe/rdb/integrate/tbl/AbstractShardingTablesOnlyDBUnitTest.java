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

package com.dangdang.ddframe.rdb.integrate.tbl;

import com.dangdang.ddframe.rdb.integrate.AbstractDBUnitTest;
import com.dangdang.ddframe.rdb.integrate.fixture.SingleKeyModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.NoneDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import org.junit.AfterClass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractShardingTablesOnlyDBUnitTest extends AbstractDBUnitTest {
    
    private static boolean isShutdown;
    
    private static ShardingDataSource shardingDataSource;
    
    @Override
    protected List<String> getSchemaFiles() {
        return Collections.singletonList("integrate/schema/tbl/db_single.sql");
    }
    
    @Override
    protected List<String> getDataSetFiles() {
        return Collections.singletonList("integrate/dataset/tbl/init/db_single.xml");
    }
    
    protected final ShardingDataSource getShardingDataSource() {
        if (null != shardingDataSource && !isShutdown) {
            return shardingDataSource;
        }
        isShutdown = false;
        DataSourceRule dataSourceRule = new DataSourceRule(createDataSourceMap("dataSource_%s"));
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
        ShardingRule shardingRule = ShardingRule.builder()
                .dataSourceRule(dataSourceRule)
                .tableRules(Arrays.asList(orderTableRule, orderItemTableRule))
                .bindingTableRules(Collections.singletonList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))))
                .databaseShardingStrategy(new DatabaseShardingStrategy("user_id", new NoneDatabaseShardingAlgorithm()))
                .tableShardingStrategy(new TableShardingStrategy("order_id", new SingleKeyModuloTableShardingAlgorithm())).build();
        shardingDataSource = new ShardingDataSource(shardingRule);
        return shardingDataSource;
    }
    
    
    @AfterClass
    public static void clear() {
        isShutdown = true;
        shardingDataSource.shutdown();
    }
}

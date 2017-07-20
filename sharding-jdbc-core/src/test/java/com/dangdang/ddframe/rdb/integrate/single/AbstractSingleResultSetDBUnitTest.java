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

package com.dangdang.ddframe.rdb.integrate.single;

import com.dangdang.ddframe.rdb.integrate.AbstractDBUnitTest;
import com.dangdang.ddframe.rdb.integrate.fixture.SingleKeyModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.NoneDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.junit.AfterClass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSingleResultSetDBUnitTest extends AbstractDBUnitTest {
    
    private static boolean isShutdown;
    
    private static ShardingDataSource shardingDataSource;
    
    @Override
    protected List<String> getDataSetFiles() {
        return Collections.singletonList("integrate/dataset/single/init/tbl.xml");
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
                "t_order_3")).dataSourceRule(dataSourceRule).build();
        ShardingRule shardingRule = ShardingRule.builder()
                .dataSourceRule(dataSourceRule)
                .tableRules(Collections.singletonList(orderTableRule))
                .bindingTableRules(Collections.singletonList(new BindingTableRule(Collections.singletonList(orderTableRule))))
                .databaseShardingStrategy(new DatabaseShardingStrategy("user_id", new NoneDatabaseShardingAlgorithm()))
                .tableShardingStrategy(new TableShardingStrategy("order_id", new SingleKeyModuloTableShardingAlgorithm())).build();
        shardingDataSource = new ShardingDataSource(shardingRule);
        return shardingDataSource;
    }
    
    
    @AfterClass
    public static void clear() {
        isShutdown = true;
        if (null != shardingDataSource) {
            shardingDataSource.close();
        }
    }
}

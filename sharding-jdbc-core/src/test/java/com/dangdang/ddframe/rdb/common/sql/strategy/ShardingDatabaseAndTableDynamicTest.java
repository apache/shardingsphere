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
import com.dangdang.ddframe.rdb.integrate.fixture.SingleKeyDynamicModuloTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.fixture.SingleKeyModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
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
public class ShardingDatabaseAndTableDynamicTest extends AbstractSQLAssertTest {
    
    private static boolean isShutdown;
    
    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();
    
    public ShardingDatabaseAndTableDynamicTest(final String testCaseName, final String sql, final Set<DatabaseType> types, final List<SqlShardingRule> sqlShardingRules) {
        super(testCaseName, sql, types, sqlShardingRules);
    }
    
    @Override
    protected ShardingTestStrategy getShardingStrategy() {
        return ShardingTestStrategy.dbtbl;
    }
    
    @Override
    protected List<String> getDataSetFiles() {
        return Arrays.asList(
                "integrate/dataset/dbtbl/init/dbtbl_0.xml",
                "integrate/dataset/dbtbl/init/dbtbl_1.xml",
                "integrate/dataset/dbtbl/init/dbtbl_2.xml",
                "integrate/dataset/dbtbl/init/dbtbl_3.xml",
                "integrate/dataset/dbtbl/init/dbtbl_4.xml",
                "integrate/dataset/dbtbl/init/dbtbl_5.xml",
                "integrate/dataset/dbtbl/init/dbtbl_6.xml",
                "integrate/dataset/dbtbl/init/dbtbl_7.xml",
                "integrate/dataset/dbtbl/init/dbtbl_8.xml",
                "integrate/dataset/dbtbl/init/dbtbl_9.xml");
    }
    
    @Override
    protected final Map<DatabaseType, ShardingDataSource> getShardingDataSources() {
        if (!shardingDataSources.isEmpty() && !isShutdown) {
            return shardingDataSources;
        }
        isShutdown = false;
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            DataSourceRule dataSourceRule = new DataSourceRule(each.getValue());
            TableRule orderTableRule = TableRule.builder("t_order").dynamic(true).dataSourceRule(dataSourceRule).build();
            TableRule orderItemTableRule = TableRule.builder("t_order_item").dynamic(true).dataSourceRule(dataSourceRule).build();
            ShardingRule shardingRule = ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Arrays.asList(orderTableRule, orderItemTableRule))
                    .bindingTableRules(Collections.singletonList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))))
                    .databaseShardingStrategy(new DatabaseShardingStrategy("user_id", new SingleKeyModuloDatabaseShardingAlgorithm()))
                    .tableShardingStrategy(new TableShardingStrategy("order_id", new SingleKeyDynamicModuloTableShardingAlgorithm("t_order_"))).build();
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

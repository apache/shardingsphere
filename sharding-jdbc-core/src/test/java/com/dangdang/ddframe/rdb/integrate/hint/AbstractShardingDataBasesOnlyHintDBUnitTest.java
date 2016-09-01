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

package com.dangdang.ddframe.rdb.integrate.hint;

import com.dangdang.ddframe.rdb.integrate.AbstractDBUnitTest;
import com.dangdang.ddframe.rdb.integrate.fixture.MultipleKeysModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.HintManager;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.google.common.collect.Lists;
import org.dbunit.DatabaseUnitException;
import org.junit.AfterClass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractShardingDataBasesOnlyHintDBUnitTest extends AbstractDBUnitTest {
    
    private static boolean isShutdown;
    
    private static ShardingDataSource shardingDataSource;
    
    @Override
    protected List<String> getSchemaFiles() {
        return Arrays.asList(
                "integrate/schema/db/db_0.sql",
                "integrate/schema/db/db_1.sql",
                "integrate/schema/db/db_2.sql",
                "integrate/schema/db/db_3.sql",
                "integrate/schema/db/db_4.sql",
                "integrate/schema/db/db_5.sql",
                "integrate/schema/db/db_6.sql",
                "integrate/schema/db/db_7.sql",
                "integrate/schema/db/db_8.sql",
                "integrate/schema/db/db_9.sql");
    }
    
    @Override
    protected List<String> getDataSetFiles() {
        return Arrays.asList(
                "integrate/dataset/db/init/db_0.xml",
                "integrate/dataset/db/init/db_1.xml",
                "integrate/dataset/db/init/db_2.xml",
                "integrate/dataset/db/init/db_3.xml",
                "integrate/dataset/db/init/db_4.xml",
                "integrate/dataset/db/init/db_5.xml",
                "integrate/dataset/db/init/db_6.xml",
                "integrate/dataset/db/init/db_7.xml",
                "integrate/dataset/db/init/db_8.xml",
                "integrate/dataset/db/init/db_9.xml");
    }
    
    protected final ShardingDataSource getShardingDataSource() {
        if (null != shardingDataSource && !isShutdown) {
            return shardingDataSource;
        }
        isShutdown = false;
        DataSourceRule dataSourceRule = new DataSourceRule(createDataSourceMap("dataSource_%s"));
        TableRule orderTableRule = TableRule.builder("t_order").dataSourceRule(dataSourceRule).build();
        TableRule orderItemTableRule = TableRule.builder("t_order_item").dataSourceRule(dataSourceRule).build();
        ShardingRule shardingRule = ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Lists.newArrayList(orderTableRule, orderItemTableRule))
                .bindingTableRules(Collections.singletonList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))))
                .databaseShardingStrategy(new DatabaseShardingStrategy(Collections.singletonList("user_id"), new MultipleKeysModuloDatabaseShardingAlgorithm()))
                .tableShardingStrategy(new TableShardingStrategy(Collections.singletonList("order_id"), new NoneTableShardingAlgorithm())).build();
        shardingDataSource = new ShardingDataSource(shardingRule);
        return shardingDataSource;
    }
    
    @AfterClass
    public static void clear() {
        isShutdown = true;
        shardingDataSource.shutdown();
    }
    
    protected void assertDataSet(final String expectedDataSetFile, final DynamicShardingValueHelper helper, 
                                 final Connection connection, final String actualTableName, final String sql, final Object... params) throws SQLException, DatabaseUnitException {
        try (DynamicShardingValueHelper anotherHelper = helper) {
            assertDataSet(expectedDataSetFile, connection, actualTableName, sql, params);
        }
    }
    
    protected void assertDataSet(final String expectedDataSetFile, final DynamicShardingValueHelper helper, final Connection connection, final String actualTableName, final String sql)
            throws SQLException, DatabaseUnitException {
        try (DynamicShardingValueHelper anotherHelper = helper) {
            assertDataSet(expectedDataSetFile, connection, actualTableName, sql);
        }
    }
    
    class DynamicShardingValueHelper implements AutoCloseable {
        
        private final HintManager hintManager;
        
        DynamicShardingValueHelper(final int userId, final int orderId) {
            hintManager = HintManager.getInstance();
            hintManager.addDatabaseShardingValue("t_order", "user_id", userId);
            hintManager.addTableShardingValue("t_order", "order_id", orderId);
        }
        
        DynamicShardingValueHelper(final List<Integer> userId, final Condition.BinaryOperator userIdOperator, final List<Integer> orderId, final Condition.BinaryOperator orderIdOperator) {
            hintManager = HintManager.getInstance();
            hintManager.addDatabaseShardingValue("t_order", "user_id", userIdOperator, userId.toArray(new Comparable[userId.size()]));
            hintManager.addTableShardingValue("t_order", "order_id", orderIdOperator, orderId.toArray(new Comparable[orderId.size()]));
        }
        
        @Override
        public void close() {
            hintManager.close();
        }
    }
}

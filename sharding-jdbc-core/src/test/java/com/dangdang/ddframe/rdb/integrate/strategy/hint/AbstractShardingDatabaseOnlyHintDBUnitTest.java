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

package com.dangdang.ddframe.rdb.integrate.strategy.hint;

import com.dangdang.ddframe.rdb.common.sql.base.AbstractSQLTest;
import com.dangdang.ddframe.rdb.integrate.fixture.MultipleKeysModuloDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.integrate.strategy.hint.helper.DynamicShardingValueHelper;
import com.dangdang.ddframe.rdb.common.util.DBUnitUtil;
import com.dangdang.ddframe.rdb.common.util.DataBaseEnvironment;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import com.google.common.collect.Lists;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.AfterClass;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.dbunit.Assertion.assertEquals;

public abstract class AbstractShardingDatabaseOnlyHintDBUnitTest extends AbstractSQLTest {
    
    private static boolean isShutdown;
    
    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();
    
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
    
    protected final Map<DatabaseType, ShardingDataSource> getShardingDataSources() {
        if (!shardingDataSources.isEmpty() && !isShutdown) {
            return shardingDataSources;
        }
        isShutdown = false;
        Map<DatabaseType, Map<String, DataSource>> dataSourceMap = createDataSourceMap();
        for (Map.Entry<DatabaseType, Map<String, DataSource>> each : dataSourceMap.entrySet()) {
            ShardingRule shardingRule = getShardingRule(each);
            shardingDataSources.put(each.getKey(), new ShardingDataSource(shardingRule));
        }
        return shardingDataSources;
    }
    
    protected ShardingRule getShardingRule(final Map.Entry<DatabaseType, Map<String, DataSource>> dataSourceEntry) {
        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceEntry.getValue());
        TableRule orderTableRule = TableRule.builder("t_order").dataSourceRule(dataSourceRule).build();
        TableRule orderItemTableRule = TableRule.builder("t_order_item").dataSourceRule(dataSourceRule).build();
        return ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Lists.newArrayList(orderTableRule, orderItemTableRule))
                .bindingTableRules(Collections.singletonList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))))
                .databaseShardingStrategy(new DatabaseShardingStrategy(Collections.singletonList("user_id"), new MultipleKeysModuloDatabaseShardingAlgorithm()))
                .tableShardingStrategy(new TableShardingStrategy(Collections.singletonList("order_id"), new NoneTableShardingAlgorithm())).build();
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
    
    protected void assertDataSet(final String expectedDataSetFile, final DynamicShardingValueHelper helper, 
                                 final Connection connection, final String actualTableName, final String sql, 
                                 final DatabaseType type, final Object... params) throws SQLException, DatabaseUnitException {
        try (DynamicShardingValueHelper anotherHelper = helper) {
            assertDataSetEquals(expectedDataSetFile, connection, actualTableName, sql, type, params);
        }
    }
    
    protected void assertDataSet(final String expectedDataSetFile, final Connection connection, final String actualTableName, final String sql, final DatabaseType type, final Object... params)
            throws SQLException, DatabaseUnitException {
        assertDataSetEquals(expectedDataSetFile, connection, actualTableName, sql, type, params);
    }
    
    private void assertDataSetEquals(final String expectedDataSetFile, final Connection connection, final String actualTableName, 
                                     final String sql, final DatabaseType type, final Object[] params) throws SQLException, DatabaseUnitException {
        try (
                Connection conn = connection;
                PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Object param : params) {
                ps.setObject(i++, param);
            }
            ITable actualTable = DBUnitUtil.getConnection(new DataBaseEnvironment(type), connection).createTable(actualTableName, ps);
            IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new InputStreamReader(AbstractShardingDatabaseOnlyHintDBUnitTest.class.getClassLoader()
                    .getResourceAsStream(expectedDataSetFile)));
            assertEquals(expectedDataSet.getTable(actualTableName), actualTable);
        }
    }
}

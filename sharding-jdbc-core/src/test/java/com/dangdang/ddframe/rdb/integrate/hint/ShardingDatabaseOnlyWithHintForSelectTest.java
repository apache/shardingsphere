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

import com.dangdang.ddframe.rdb.integrate.hint.helper.DynamicShardingValueHelper;
import com.dangdang.ddframe.rdb.sharding.constant.ShardingOperator;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import com.google.common.collect.Lists;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replacePreparedStatement;

public final class ShardingDatabaseOnlyWithHintForSelectTest extends AbstractShardingDatabaseOnlyHintDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertSelectEqualsWithSingleTable() throws SQLException, DatabaseUnitException {
        String statement = replacePreparedStatement(getDatabaseTestSQL().getSelectEqualsWithSingleTableSql());
        assertDataSet("integrate/dataset/db/expect/select/SelectEqualsWithSingleTable_0.xml", 
                new DynamicShardingValueHelper(10, 1000), shardingDataSource.getConnection(), "t_order", statement, 10, 1000);
        assertDataSet("integrate/dataset/db/expect/select/SelectEqualsWithSingleTable_1.xml", 
                new DynamicShardingValueHelper(12, 1201), shardingDataSource.getConnection(), "t_order", statement, 12, 1201);
        assertDataSet("integrate/dataset/Empty.xml", 
                new DynamicShardingValueHelper(12, 1000), shardingDataSource.getConnection(), "t_order", statement, 12, 1000);
    }
    
    @Test
    public void assertSelectBetweenWithSingleTable() throws SQLException, DatabaseUnitException {
        String statement = replacePreparedStatement(getDatabaseTestSQL().getSelectBetweenWithSingleTableSql());
        assertDataSet("integrate/dataset/db/expect/select/SelectBetweenWithSingleTable.xml", new DynamicShardingValueHelper(Lists.newArrayList(10, 12),
                ShardingOperator.BETWEEN, Lists.newArrayList(1001, 1200), ShardingOperator.BETWEEN), shardingDataSource.getConnection(), 
                "t_order", statement, 10, 12, 1001, 1200);
        assertDataSet("integrate/dataset/Empty.xml", new DynamicShardingValueHelper(Lists.newArrayList(10, 12),
                ShardingOperator.BETWEEN, Lists.newArrayList(1309, 1408), ShardingOperator.BETWEEN), shardingDataSource.getConnection(), "t_order", statement, 10, 12, 1309, 1408);
    }
    
    @Test
    public void assertSelectInWithSingleTable() throws SQLException, DatabaseUnitException {
        String statement = replacePreparedStatement(getDatabaseTestSQL().getSelectInWithSingleTableSql());
        assertDataSet("integrate/dataset/db/expect/select/SelectInWithSingleTable_0.xml", new DynamicShardingValueHelper(Lists.newArrayList(10, 12, 15),
                ShardingOperator.IN, Lists.newArrayList(1000, 1201), ShardingOperator.IN), shardingDataSource.getConnection(), "t_order", statement, 10, 12, 15, 1000, 1201);
        assertDataSet("integrate/dataset/db/expect/select/SelectInWithSingleTable_1.xml", new DynamicShardingValueHelper(Lists.newArrayList(10, 12, 15),
                ShardingOperator.IN, Lists.newArrayList(1000, 1101), ShardingOperator.IN), shardingDataSource.getConnection(), "t_order", statement, 10, 12, 15, 1000, 1101);
        assertDataSet("integrate/dataset/Empty.xml", new DynamicShardingValueHelper(Lists.newArrayList(10, 12, 15),
                ShardingOperator.IN, Lists.newArrayList(1309, 1408), ShardingOperator.IN), shardingDataSource.getConnection(), "t_order", statement, 10, 12, 15, 1309, 1408);
    }
    
}

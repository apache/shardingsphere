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

import com.dangdang.ddframe.rdb.integrate.hint.helper.DynamicDatabaseShardingValueHelper;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replacePreparedStatement;

public class RoutingDatabaseOnlyWithHintForSelectTest extends AbstractRoutingDatabaseOnlyTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertSelectEqualsWithSingleTable() throws SQLException, DatabaseUnitException {
        String statement = replacePreparedStatement(getDatabaseTestSQL().getSelectEqualsWithSingleTableSql());
        assertDataSet("integrate/dataset/db/expect/select/SelectEqualsWithSingleTable_0.xml", new DynamicDatabaseShardingValueHelper(10), shardingDataSource.getConnection(), "t_order", statement, 10, 1000);
        assertDataSet("integrate/dataset/db/expect/select/SelectEqualsWithSingleTable_1.xml", new DynamicDatabaseShardingValueHelper(12), shardingDataSource.getConnection(), "t_order", statement, 12, 1201);
        assertDataSet("integrate/dataset/Empty.xml", new DynamicDatabaseShardingValueHelper(12), shardingDataSource.getConnection(), "t_order", statement, 12, 1000);
    }
}

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

package com.dangdang.ddframe.rdb.integrate.dbtbl.dynamic.statement;

import com.dangdang.ddframe.rdb.integrate.dbtbl.common.statement.AbstractShardingBothForStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.dynamic.DynamicShardingBothHelper;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.AfterClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DynamicShardingBothForStatementWithDMLTest extends AbstractShardingBothForStatementWithDMLTest {
    
    private static ShardingDataSource shardingDataSource;
    
    @Override
    protected ShardingDataSource getShardingDataSource() {
        if (null != shardingDataSource) {
            return shardingDataSource;
        }
        shardingDataSource = DynamicShardingBothHelper.getShardingDataSource(createDataSourceMap("dataSource_%s"));
        return shardingDataSource;
    }
    
    @AfterClass
    public static void clear() {
        shardingDataSource.close();
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertUpdateWithoutShardingValue() throws SQLException, DatabaseUnitException {
        try (Connection connection = getShardingDataSource().getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(String.format(getDatabaseTestSQL().getUpdateWithoutShardingValueSql(), "updated", "init"));
        }
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertDeleteWithoutShardingValue() throws SQLException, DatabaseUnitException {
        try (Connection connection = getShardingDataSource().getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(String.format(getDatabaseTestSQL().getDeleteWithoutShardingValueSql(), "init"));
        }
    }
}

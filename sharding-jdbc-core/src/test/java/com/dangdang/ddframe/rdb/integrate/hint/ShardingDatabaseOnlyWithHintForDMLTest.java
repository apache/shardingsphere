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
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replacePreparedStatement;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Ignore
public final class ShardingDatabaseOnlyWithHintForDMLTest extends AbstractShardingDatabaseOnlyHintDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertInsertWithAllPlaceholders() throws SQLException, DatabaseUnitException {
        for (int i = 1; i <= 10; i++) {
            try (DynamicShardingValueHelper helper = new DynamicShardingValueHelper(i, i);
                 Connection connection = shardingDataSource.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(getDatabaseTestSQL().getInsertWithAllPlaceholdersSql());
                preparedStatement.setInt(1, i);
                preparedStatement.setInt(2, i);
                preparedStatement.setString(3, "insert");
                preparedStatement.executeUpdate();
            }
        }
        assertDataSet("insert", "insert");
    }
    
    @Test
    public void assertInsertWithoutPlaceholder() throws SQLException, DatabaseUnitException {
        for (int i = 1; i <= 10; i++) {
            try (DynamicShardingValueHelper helper = new DynamicShardingValueHelper(i, i);
                 Connection connection = shardingDataSource.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(String.format(getDatabaseTestSQL().getInsertWithoutPlaceholderSql(), i, i));
                preparedStatement.executeUpdate();
            }
        }
        assertDataSet("insert", "insert");
    }
    
    @Test
    public void assertInsertWithPartialPlaceholders() throws SQLException, DatabaseUnitException {
        for (int i = 1; i <= 10; i++) {
            try (DynamicShardingValueHelper helper = new DynamicShardingValueHelper(i, i);
                 Connection connection = shardingDataSource.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(String.format(getDatabaseTestSQL().getInsertWithPartialPlaceholdersSql(), i, i));
                preparedStatement.setString(1, "insert");
                preparedStatement.executeUpdate();
            }
        }
        assertDataSet("insert", "insert");
    }
    
    @Test
    public void assertUpdateWithoutAlias() throws SQLException, DatabaseUnitException {
        for (int i = 10; i < 30; i++) {
            for (int j = 0; j < 2; j++) {
                try (DynamicShardingValueHelper helper = new DynamicShardingValueHelper(i, i * 100 + j);
                     Connection connection = shardingDataSource.getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(replacePreparedStatement(getDatabaseTestSQL().getUpdateWithoutAliasSql()));
                    preparedStatement.setString(1, "updated");
                    preparedStatement.setInt(2, i * 100 + j);
                    preparedStatement.setInt(3, i);
                    assertThat(preparedStatement.executeUpdate(), is(1));
                }
            }
        }
        assertDataSet("update", "updated");
    }
    
    @Test
    public void assertUpdateWithAlias() throws SQLException, DatabaseUnitException {
        if (isAliasSupport()) {
            for (int i = 10; i < 30; i++) {
                for (int j = 0; j < 2; j++) {
                    try (DynamicShardingValueHelper helper = new DynamicShardingValueHelper(i, i * 100 + j);
                         Connection connection = shardingDataSource.getConnection()) {
                        PreparedStatement preparedStatement = connection.prepareStatement(getDatabaseTestSQL().getUpdateWithAliasSql());
                        preparedStatement.setString(1, "updated");
                        preparedStatement.setInt(2, i * 100 + j);
                        preparedStatement.setInt(3, i);
                        assertThat(preparedStatement.executeUpdate(), is(1));
                    }
                }
            }
            assertDataSet("update", "updated");
        }
    }
    
    @Test
    public void assertDeleteWithoutAlias() throws SQLException, DatabaseUnitException {
        for (int i = 10; i < 30; i++) {
            for (int j = 0; j < 2; j++) {
                try (DynamicShardingValueHelper helper = new DynamicShardingValueHelper(i, i * 100 + j);
                     Connection connection = shardingDataSource.getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(replacePreparedStatement(getDatabaseTestSQL().getDeleteWithoutAliasSql()));
                    preparedStatement.setInt(1, i * 100 + j);
                    preparedStatement.setInt(2, i);
                    preparedStatement.setString(3, "init");
                    assertThat(preparedStatement.executeUpdate(), is(1));
                }
            }
        }
        assertDataSet("delete", "init");
    }
    
    private void assertDataSet(final String expectedDataSetPattern, final String status) throws SQLException, DatabaseUnitException {
        for (int i = 0; i < 10; i++) {
            assertDataSet(String.format("integrate/dataset/db/expect/%s/db_%s.xml", expectedDataSetPattern, i),
                    shardingDataSource.getConnection().getConnection(String.format("dataSource_db_%s", i), SQLType.SELECT), 
                    "t_order", replacePreparedStatement(getDatabaseTestSQL().getAssertSelectWithStatusSql()), status);
        }
    }
}

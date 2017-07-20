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

package com.dangdang.ddframe.rdb.integrate.db.statement;

import com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil;
import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDatabaseOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import org.dbunit.DatabaseUnitException;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ShardingDatabaseOnlyForStatementWithDMLTest extends AbstractShardingDatabaseOnlyDBUnitTest {
    
    @Test
    public void assertInsertWithoutPlaceholder() throws SQLException, DatabaseUnitException {
        for (int i = 1; i <= 10; i++) {
            try (Connection connection = getShardingDataSource().getConnection()) {
                connection.setAutoCommit(false);
                connection.createStatement().executeUpdate(String.format(getDatabaseTestSQL().getInsertWithoutPlaceholderSql(), i, i));
                connection.commit();
                connection.close();
            }
        }
        assertDataSet("insert", "insert");
    }
    
    @Test
    public void assertInsertWithGenerateKeyColumn() throws SQLException, DatabaseUnitException {
        for (int i = 1; i <= 10; i++) {
            try (Connection connection = getShardingDataSource().getConnection()) {
                connection.setAutoCommit(false);
                connection.createStatement().executeUpdate(String.format(getDatabaseTestSQL().getInsertWithAutoIncrementColumnSql(), i, "'insert'"));
                connection.commit();
                connection.close();
            }
        }
        assertDataSet("insert", "insert");
    }
    
    @Test
    public void assertUpdateWithoutAliasSql() throws SQLException, DatabaseUnitException {
        for (int i = 10; i < 30; i++) {
            for (int j = 0; j < 2; j++) {
                try (Connection connection = getShardingDataSource().getConnection()) {
                    Statement stmt = connection.createStatement();
                    assertThat(stmt.executeUpdate(String.format(getDatabaseTestSQL().getUpdateWithoutAliasSql(), "'updated'", i * 100 + j, i)), is(1));
                }
            }
        }
        assertDataSet("update", "updated");
    }
    
    @Test
    public void assertUpdateWithoutShardingValue() throws SQLException, DatabaseUnitException {
        try (Connection connection = getShardingDataSource().getConnection()) {
            Statement stmt = connection.createStatement();
            assertThat(stmt.executeUpdate(String.format(getDatabaseTestSQL().getUpdateWithoutShardingValueSql(), "'updated'", "'init'")), is(40));
        }
        assertDataSet("update", "updated");
    }
    
    @Test
    public void assertDeleteWithoutAlias() throws SQLException, DatabaseUnitException {
        for (int i = 10; i < 30; i++) {
            for (int j = 0; j < 2; j++) {
                try (Connection connection = getShardingDataSource().getConnection()) {
                    Statement stmt = connection.createStatement();
                    assertThat(stmt.executeUpdate(String.format(getDatabaseTestSQL().getDeleteWithoutAliasSql(), i * 100 + j, i, "'init'")), is(1));
                }
            }
        }
        assertDataSet("delete", "init");
    }
    
    @Test
    public void assertDeleteWithoutShardingValue() throws SQLException, DatabaseUnitException {
        try (Connection connection = getShardingDataSource().getConnection()) {
            Statement stmt = connection.createStatement();
            assertThat(stmt.executeUpdate(String.format(getDatabaseTestSQL().getDeleteWithoutShardingValueSql(), "'init'")), is(40));
        }
        assertDataSet("delete", "init");
    }
    
    private void assertDataSet(final String expectedDataSetPattern, final String status) throws SQLException, DatabaseUnitException {
        for (int i = 0; i < 10; i++) {
            assertDataSet(String.format("integrate/dataset/db/expect/%s/db_%s.xml", expectedDataSetPattern, i),
                    getShardingDataSource().getConnection().getConnection(String.format("dataSource_db_%s", i), SQLType.SELECT), 
                    "t_order", SqlPlaceholderUtil.replacePreparedStatement(getDatabaseTestSQL().getAssertSelectWithStatusSql()), status);
        }
    }
}

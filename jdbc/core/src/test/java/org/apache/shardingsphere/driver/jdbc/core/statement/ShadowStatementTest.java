/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.driver.jdbc.core.statement;

import org.apache.shardingsphere.driver.jdbc.base.AbstractShardingSphereDataSourceForShadowTest;
import org.junit.After;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShadowStatementTest extends AbstractShardingSphereDataSourceForShadowTest {
    
    private static final String CLEAN_SQL = "DELETE FROM t_encrypt";
    
    private static final String INSERT_SQL = "INSERT INTO t_encrypt (id, cipher_pwd, plain_pwd) VALUES (2, 'cipher', 'plain')";
    
    private static final String INSERT_SHADOW_SQL = "INSERT INTO t_encrypt (id, cipher_pwd, plain_pwd) VALUES (1, 'cipher', 'plain')";
    
    private static final String UPDATE_SQL = "UPDATE t_encrypt SET cipher_pwd ='cipher_pwd' WHERE id = 2";
    
    private static final String UPDATE_SHADOW_SQL = "UPDATE t_encrypt SET cipher_pwd ='cipher_pwd' WHERE id = 1";
    
    private static final String DELETE_SQL = "DELETE FROM t_encrypt WHERE id = 2";
    
    private static final String DELETE_SHADOW_SQL = "DELETE FROM t_encrypt WHERE id = 1";
    
    private static final String SELECT_SQL = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt WHERE id = 2";
    
    private static final String SELECT_SHADOW_SQL = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt WHERE id = 1";
    
    private static final String RESULT_SELECT_SQL = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt";
    
    @Test
    public void assertInsertNativeCase() throws SQLException {
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(INSERT_SQL);
        }
        assertResultSet(true, 0, "cipher");
        assertResultSet(false, 1, "cipher");
    }
    
    private void assertResultSet(final boolean isShadow, final int resultSetCount, final Object cipherPwd) throws SQLException {
        DataSource dataSource = isShadow ? getActualDataSources().get("shadow_jdbc_1") : getActualDataSources().get("shadow_jdbc_0");
        try (Statement statement = dataSource.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(RESULT_SELECT_SQL);
            int count = 0;
            while (resultSet.next()) {
                assertThat(resultSet.getObject("cipher_pwd"), is(cipherPwd));
                count += 1;
            }
            assertThat(count, is(resultSetCount));
        }
    }
    
    @Test
    public void assertInsertShadowCase() throws SQLException {
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(INSERT_SHADOW_SQL);
        }
        assertResultSet(true, 1, "cipher");
        assertResultSet(false, 0, "cipher");
    }
    
    @Test
    public void assertUpdateNativeCase() throws SQLException {
        int result;
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(INSERT_SQL);
            result = statement.executeUpdate(UPDATE_SQL);
        }
        assertThat(result, is(1));
        assertResultSet(true, 0, "cipher_pwd");
        assertResultSet(false, 1, "cipher_pwd");
        
    }
    
    @Test
    public void assertUpdateShadowCase() throws SQLException {
        int result;
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(INSERT_SHADOW_SQL);
            result = statement.executeUpdate(UPDATE_SHADOW_SQL);
        }
        assertThat(result, is(1));
        assertResultSet(true, 1, "cipher_pwd");
        assertResultSet(false, 0, "cipher_pwd");
    }
    
    @Test
    public void assertDeleteNativeCase() throws SQLException {
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(INSERT_SQL);
            statement.execute(INSERT_SHADOW_SQL);
            statement.execute(DELETE_SQL);
        }
        assertResultSet(true, 1, "cipher");
        assertResultSet(false, 0, "cipher");
    }
    
    @Test
    public void assertDeleteShadowCase() throws SQLException {
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(INSERT_SQL);
            statement.execute(INSERT_SHADOW_SQL);
            statement.execute(DELETE_SHADOW_SQL);
        }
        assertResultSet(true, 0, "cipher");
        assertResultSet(false, 1, "cipher");
    }
    
    @Test
    public void assertSelectNativeCase() throws SQLException {
        ResultSet resultSet;
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(INSERT_SQL);
            resultSet = statement.executeQuery(SELECT_SQL);
            int count = 0;
            while (resultSet.next()) {
                assertThat(resultSet.getObject("cipher_pwd"), is("cipher"));
                count += 1;
            }
            assertThat(count, is(1));
            statement.execute(DELETE_SQL);
        }
        assertResultSet(false, 0, "cipher");
    }
    
    @Test
    public void assertSelectShadowCase() throws SQLException {
        ResultSet resultSet;
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(INSERT_SHADOW_SQL);
            resultSet = statement.executeQuery(SELECT_SHADOW_SQL);
            int count = 0;
            while (resultSet.next()) {
                assertThat(resultSet.getObject("cipher_pwd"), is("cipher"));
                count += 1;
            }
            assertThat(count, is(1));
            statement.execute(DELETE_SHADOW_SQL);
        }
        assertResultSet(true, 0, "cipher");
    }
    
    @After
    public void clean() throws SQLException {
        try (Statement statement = getActualDataSources().get("shadow_jdbc_0").getConnection().createStatement()) {
            statement.execute(CLEAN_SQL);
        }
        try (Statement statement = getActualDataSources().get("shadow_jdbc_1").getConnection().createStatement()) {
            statement.execute(CLEAN_SQL);
        }
    }
}

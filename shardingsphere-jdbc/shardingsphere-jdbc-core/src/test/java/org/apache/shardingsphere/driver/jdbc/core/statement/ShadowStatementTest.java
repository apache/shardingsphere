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

import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForShadowTest;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.junit.After;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShadowStatementTest extends AbstractShardingSphereDataSourceForShadowTest {
    
    private static final String INSERT_SQL = "INSERT INTO t_encrypt (id, cipher_pwd, plain_pwd) VALUES (1, 'cipher', 'plain')";
    
    private static final String SHADOW_INSERT_SQL = "INSERT INTO t_encrypt (id, cipher_pwd, plain_pwd, shadow) VALUES (1, 'cipher', 'plain', TRUE)";
    
    private static final String INSERT_GENERATED_KEY_SQL = "INSERT INTO t_encrypt (cipher_pwd, plain_pwd) VALUES ('cipher', 'plain')";
    
    private static final String DELETE_SQL = "DELETE FROM t_encrypt WHERE plain_pwd = 'plain'";
    
    private static final String SHADOW_DELETE_SQL = "DELETE FROM t_encrypt WHERE plain_pwd = 'plain' AND shadow = TRUE";
    
    private static final String SELECT_SQL = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt WHERE id = 1";
    
    private static final String CLEAN_SHADOW_SQL = "DELETE FROM t_encrypt WHERE shadow = TRUE";
    
    private static final String CLEAN_SQL = "DELETE FROM t_encrypt";
    
    private static final String UPDATE_SQL = "UPDATE t_encrypt SET cipher_pwd ='cipher_pwd' WHERE id = 1";
    
    private static final String SHADOW_UPDATE_SQL = "UPDATE t_encrypt SET cipher_pwd ='cipher_pwd' WHERE id = 1 AND shadow = TRUE";
    
    @Test
    public void assertInsertWithExecute() throws SQLException {
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(INSERT_SQL);
        }
        assertResultSet(false, 1, "cipher");
        assertResultSet(true, 0, "cipher");
    }
    
    @Test
    public void assertShadowInsertWithExecute() throws SQLException {
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(SHADOW_INSERT_SQL);
        }
        assertResultSet(false, 0, "cipher");
        assertResultSet(true, 1, "cipher");
    }
    
    @Test
    public void assertInsertWithExecuteWithGeneratedKey() throws SQLException {
        try (Statement statement = getShadowDataSource().getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            statement.execute(INSERT_GENERATED_KEY_SQL, Statement.RETURN_GENERATED_KEYS);
            ResultSet resultSet = statement.getGeneratedKeys();
            assertTrue(resultSet.next());
            assertTrue(resultSet.getInt(1) > 0);
            assertFalse(resultSet.next());
        }
        assertResultSet(true, 0, "cipher");
    }
    
    private void assertResultSet(final boolean isShadow, final int resultSetCount, final Object cipherPwd) throws SQLException {
        Map<String, DataSource> dataMaps = getDatabaseTypeMap().get(DatabaseTypeRegistry.getActualDatabaseType("H2"));
        DataSource dataSource = isShadow ? dataMaps.get("jdbc_1") : dataMaps.get("jdbc_0");
        try (Statement statement = dataSource.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(SELECT_SQL);
            int count = 1;
            while (resultSet.next()) {
                assertThat(resultSet.getObject("cipher_pwd"), is(cipherPwd));
                count += 1;
            }
            assertThat(count - 1, is(resultSetCount));
        }
    }
    
    @Test
    public void assertDeleteWithExecute() throws SQLException {
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(INSERT_SQL);
            statement.execute(DELETE_SQL);
        }
        assertResultSet(false, 0, "cipher");
    }
    
    @Test
    public void assertShadowDeleteWithExecute() throws SQLException {
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(SHADOW_INSERT_SQL);
            statement.execute(SHADOW_DELETE_SQL);
        }
        assertResultSet(true, 0, "cipher");
    }
    
    @Test
    public void assertUpdateWithExecuteUpdate() throws SQLException {
        int result;
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(INSERT_SQL);
            result = statement.executeUpdate(UPDATE_SQL);
        }
        assertThat(result, is(1));
        assertResultSet(false, 1, "cipher_pwd");
    }
    
    @Test
    public void assertShadowUpdateWithExecuteUpdate() throws SQLException {
        int result;
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(SHADOW_INSERT_SQL);
            result = statement.executeUpdate(SHADOW_UPDATE_SQL);
        }
        assertThat(result, is(1));
        assertResultSet(true, 1, "cipher_pwd");
    }
    
    @After
    public void clean() throws SQLException {
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(CLEAN_SQL);
        }
        try (Statement statement = getShadowDataSource().getConnection().createStatement()) {
            statement.execute(CLEAN_SHADOW_SQL);
        }
    }
}

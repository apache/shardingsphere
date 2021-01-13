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

import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForCalciteTest;
import org.junit.Before;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class CalciteStatementTest extends AbstractShardingSphereDataSourceForCalciteTest {

    private static final String INSERT_SQL = "INSERT INTO t_encrypt (id, cipher_pwd, plain_pwd) VALUES (?, ?, ?)";

    private static final String SELECT_SQL_BY_ID = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt WHERE id = 99";

    @Before
    public void init() throws SQLException {
        try (PreparedStatement statement = getShardingSphereDataSource().getConnection().prepareStatement(INSERT_SQL)) {
            statement.setInt(1, 99);
            statement.setString(2, "cipher");
            statement.setString(3, "plain");
            statement.execute();
        }
    }

    @Test
    public void assertQueryWithCalciteInSingleTable() throws SQLException {
        ShardingSphereStatement preparedStatement = (ShardingSphereStatement) getShardingSphereDataSource().getConnection().createStatement();
        preparedStatement.setToCalcite(true);
        ResultSet resultSet = preparedStatement.executeQuery(SELECT_SQL_BY_ID);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(99));
        assertThat(resultSet.getString(2), is("cipher"));
        assertThat(resultSet.getString(3), is("plain"));
    }
}

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

package org.apache.shardingsphere.driver.jdbc.unsupported;

import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForShardingTest;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

public final class UnsupportedOperationPreparedStatementTest extends AbstractShardingSphereDataSourceForShardingTest {
    
    private static final String SQL = "SELECT user_id AS usr_id FROM t_order WHERE status = 'init'";
    
    private final List<ShardingSphereConnection> shardingSphereConnections = new ArrayList<>();
    
    private final List<PreparedStatement> statements = new ArrayList<>();
    
    @Before
    public void init() throws SQLException {
        ShardingSphereConnection connection = getShardingSphereDataSource().getConnection();
        shardingSphereConnections.add(connection);
        PreparedStatement preparedStatement = connection.prepareStatement(SQL);
        statements.add(preparedStatement);
    }
    
    @After
    public void close() throws SQLException {
        for (PreparedStatement each : statements) {
            each.close();
        }
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.close();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetMetaData() throws SQLException {
        for (PreparedStatement each : statements) {
            each.getMetaData();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNString() throws SQLException {
        for (PreparedStatement each : statements) {
            each.setNString(1, "");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNClob() throws SQLException {
        for (PreparedStatement each : statements) {
            each.setNClob(1, (NClob) null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNClobForReader() throws SQLException {
        for (PreparedStatement each : statements) {
            each.setNClob(1, new StringReader(""));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNClobForReaderAndLength() throws SQLException {
        for (PreparedStatement each : statements) {
            each.setNClob(1, new StringReader(""), 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNCharacterStream() throws SQLException {
        for (PreparedStatement each : statements) {
            each.setNCharacterStream(1, new StringReader(""));
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNCharacterStreamWithLength() throws SQLException {
        for (PreparedStatement each : statements) {
            each.setNCharacterStream(1, new StringReader(""), 1);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetArray() throws SQLException {
        for (PreparedStatement each : statements) {
            each.setArray(1, null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetRowId() throws SQLException {
        for (PreparedStatement each : statements) {
            each.setRowId(1, null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetRef() throws SQLException {
        for (PreparedStatement each : statements) {
            each.setRef(1, null);
        }
    }
}

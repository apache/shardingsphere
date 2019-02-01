/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.jdbc.unsupported;

import io.shardingsphere.shardingjdbc.common.base.AbstractShardingJDBCDatabaseAndTableTest;
import io.shardingsphere.shardingjdbc.jdbc.JDBCTestSQL;
import io.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
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

public final class UnsupportedOperationPreparedStatementTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    private final List<ShardingConnection> shardingConnections = new ArrayList<>();
    
    private final List<PreparedStatement> statements = new ArrayList<>();
    
    @Before
    public void init() {
        ShardingConnection shardingConnection = getShardingDataSource().getConnection();
        shardingConnections.add(shardingConnection);
        PreparedStatement preparedStatement = shardingConnection.prepareStatement(JDBCTestSQL.SELECT_WITH_ALIAS_SQL);
        statements.add(preparedStatement);
    }
    
    @After
    public void close() throws SQLException {
        for (PreparedStatement each : statements) {
            each.close();
        }
        for (ShardingConnection each : shardingConnections) {
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
    public void assertGetParameterMetaData() throws SQLException {
        for (PreparedStatement each : statements) {
            each.getParameterMetaData();
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

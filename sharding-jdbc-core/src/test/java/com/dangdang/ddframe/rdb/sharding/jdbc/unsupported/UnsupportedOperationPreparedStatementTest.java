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

package com.dangdang.ddframe.rdb.sharding.jdbc.unsupported;

import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDatabaseOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.connection.ShardingConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

public final class UnsupportedOperationPreparedStatementTest extends AbstractShardingDatabaseOnlyDBUnitTest {
    
    private ShardingConnection shardingConnection;
    
    private PreparedStatement actual;
    
    @Before
    public void init() throws SQLException {
        shardingConnection = getShardingDataSource().getConnection();
        actual = shardingConnection.prepareStatement("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
    }
    
    @After
    public void close() throws SQLException {
        actual.close();
        shardingConnection.close();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetMetaData() throws SQLException {
        actual.getMetaData();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetParameterMetaData() throws SQLException {
        actual.getParameterMetaData();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNString() throws SQLException {
        actual.setNString(1, "");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNClob() throws SQLException {
        actual.setNClob(1, (NClob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNClobForReader() throws SQLException {
        actual.setNClob(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNClobForReaderAndLength() throws SQLException {
        actual.setNClob(1, new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNCharacterStream() throws SQLException {
        actual.setNCharacterStream(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNCharacterStreamWithLength() throws SQLException {
        actual.setNCharacterStream(1, new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetArray() throws SQLException {
        actual.setArray(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetRowId() throws SQLException {
        actual.setRowId(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetRef() throws SQLException {
        actual.setRef(1, null);
    }
}

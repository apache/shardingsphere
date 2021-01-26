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

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSpherePreparedStatement;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.sql.NClob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class UnsupportedOperationPreparedStatementTest {
    
    private ShardingSpherePreparedStatement shardingSpherePreparedStatement;
    
    @Before
    public void setUp() throws SQLException {
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaDataContexts().getDefaultMetaData().getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        shardingSpherePreparedStatement = new ShardingSpherePreparedStatement(connection, "SELECT 1");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetMetaData() throws SQLException {
        shardingSpherePreparedStatement.getMetaData();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNString() throws SQLException {
        shardingSpherePreparedStatement.setNString(1, "");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNClob() throws SQLException {
        shardingSpherePreparedStatement.setNClob(1, (NClob) null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNClobForReader() throws SQLException {
        shardingSpherePreparedStatement.setNClob(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNClobForReaderAndLength() throws SQLException {
        shardingSpherePreparedStatement.setNClob(1, new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNCharacterStream() throws SQLException {
        shardingSpherePreparedStatement.setNCharacterStream(1, new StringReader(""));
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNCharacterStreamWithLength() throws SQLException {
        shardingSpherePreparedStatement.setNCharacterStream(1, new StringReader(""), 1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetArray() throws SQLException {
        shardingSpherePreparedStatement.setArray(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetRowId() throws SQLException {
        shardingSpherePreparedStatement.setRowId(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetRef() throws SQLException {
        shardingSpherePreparedStatement.setRef(1, null);
    }
}

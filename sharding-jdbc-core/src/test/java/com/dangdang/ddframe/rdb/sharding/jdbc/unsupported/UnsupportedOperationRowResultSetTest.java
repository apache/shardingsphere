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

import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDataBasesOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.HashMap;

public class UnsupportedOperationRowResultSetTest extends AbstractShardingDataBasesOnlyDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    private ShardingConnection shardingConnection;
    
    private Statement statement;
    
    private ResultSet actual;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
        shardingConnection = shardingDataSource.getConnection();
        statement = shardingConnection.createStatement();
        actual = statement.executeQuery("SELECT user_id AS `uid` FROM `t_order` group by `user_id`");
    }
    
    @After
    public void close() throws SQLException {
        actual.close();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetObject() throws Exception {
        actual.getObject(1, new HashMap<String, Class<?>>());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetObject1() throws Exception {
        actual.getObject("no", new HashMap<String, Class<?>>());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetAsciiStream() throws Exception {
        actual.getAsciiStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetAsciiStream1() throws Exception {
        actual.getAsciiStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetBinaryStream() throws Exception {
        actual.getBinaryStream(1);   
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetBinaryStream1() throws Exception {
        actual.getBinaryStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetUnicodeStream() throws Exception {
        actual.getUnicodeStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetUnicodeStream1() throws Exception {
        actual.getUnicodeStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetCharacterStream() throws Exception {
        actual.getCharacterStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetCharacterStream1() throws Exception {
        actual.getCharacterStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetBlob() throws Exception {
        actual.getBlob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetBlob1() throws Exception {
        actual.getBlob("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetClob() throws Exception {
        actual.getClob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetClob1() throws Exception {
        actual.getClob("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetSQLXML() throws Exception {
        actual.getSQLXML(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testGetSQLXML1() throws Exception {
        actual.getSQLXML("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testSetFetchDirection() throws Exception {
        actual.setFetchDirection(ResultSet.FETCH_REVERSE);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void testSetFetchSize() throws Exception {
        actual.setFetchSize(100);
    }
}

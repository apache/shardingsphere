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

import io.shardingsphere.shardingjdbc.jdbc.core.resultset.GeneratedKeysResultSet;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;

public class UnsupportedGeneratedKeysResultSetTest {
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBoolean() throws SQLException {
        new GeneratedKeysResultSet().getBoolean(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBoolean1() throws SQLException {
        new GeneratedKeysResultSet().getBoolean("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getDate() throws SQLException {
        new GeneratedKeysResultSet().getDate(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getDate1() throws SQLException {
        new GeneratedKeysResultSet().getDate(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getDate2() throws SQLException {
        new GeneratedKeysResultSet().getDate("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getDate3() throws SQLException {
        new GeneratedKeysResultSet().getDate("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTime() throws SQLException {
        new GeneratedKeysResultSet().getTime(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTime1() throws SQLException {
        new GeneratedKeysResultSet().getTime(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTime2() throws SQLException {
        new GeneratedKeysResultSet().getTime("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTime3() throws SQLException {
        new GeneratedKeysResultSet().getTime("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimestamp() throws SQLException {
        new GeneratedKeysResultSet().getTimestamp(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimestamp1() throws SQLException {
        new GeneratedKeysResultSet().getTimestamp(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimestamp2() throws SQLException {
        new GeneratedKeysResultSet().getTimestamp("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimestamp3() throws SQLException {
        new GeneratedKeysResultSet().getTimestamp("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getAsciiStream() throws SQLException {
        new GeneratedKeysResultSet().getAsciiStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getAsciiStream1() throws SQLException {
        new GeneratedKeysResultSet().getAsciiStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getUnicodeStream() throws SQLException {
        new GeneratedKeysResultSet().getUnicodeStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getUnicodeStream1() throws SQLException {
        new GeneratedKeysResultSet().getUnicodeStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBinaryStream() throws SQLException {
        new GeneratedKeysResultSet().getBinaryStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBinaryStream1() throws SQLException {
        new GeneratedKeysResultSet().getBinaryStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getWarnings() throws SQLException {
        new GeneratedKeysResultSet().getWarnings();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void clearWarnings() throws SQLException {
        new GeneratedKeysResultSet().clearWarnings();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getCharacterStream() throws SQLException {
        new GeneratedKeysResultSet().getCharacterStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getCharacterStream1() throws SQLException {
        new GeneratedKeysResultSet().getCharacterStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setFetchDirection() throws SQLException {
        new GeneratedKeysResultSet().setFetchDirection(ResultSet.FETCH_FORWARD);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getFetchDirection() throws SQLException {
        new GeneratedKeysResultSet().getFetchDirection();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setFetchSize() throws SQLException {
        new GeneratedKeysResultSet().setFetchSize(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getFetchSize() throws SQLException {
        new GeneratedKeysResultSet().getFetchSize();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getObject() throws SQLException {
        new GeneratedKeysResultSet().getObject(1, new HashMap<String, Class<?>>());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getObject1() throws SQLException {
        new GeneratedKeysResultSet().getObject("", new HashMap<String, Class<?>>());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBlob() throws SQLException {
        new GeneratedKeysResultSet().getBlob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBlob1() throws SQLException {
        new GeneratedKeysResultSet().getBlob("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getClob() throws SQLException {
        new GeneratedKeysResultSet().getClob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getClob1() throws SQLException {
        new GeneratedKeysResultSet().getClob("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getURL() throws SQLException {
        new GeneratedKeysResultSet().getURL(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getURL1() throws SQLException {
        new GeneratedKeysResultSet().getURL("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getSQLXML() throws SQLException {
        new GeneratedKeysResultSet().getSQLXML(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getSQLXML1() throws SQLException {
        new GeneratedKeysResultSet().getSQLXML("");
    }
    
}

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

import com.dangdang.ddframe.rdb.sharding.jdbc.GeneratedKeysResultSet;
import org.junit.Test;

import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;

public class UnsupportedGeneratedKeysResultSetTest {
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBoolean() throws Exception {
        new GeneratedKeysResultSet().getBoolean(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBoolean1() throws Exception {
        new GeneratedKeysResultSet().getBoolean("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getDate() throws Exception {
        new GeneratedKeysResultSet().getDate(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getDate1() throws Exception {
        new GeneratedKeysResultSet().getDate(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getDate2() throws Exception {
        new GeneratedKeysResultSet().getDate("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getDate3() throws Exception {
        new GeneratedKeysResultSet().getDate("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTime() throws Exception {
        new GeneratedKeysResultSet().getTime(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTime1() throws Exception {
        new GeneratedKeysResultSet().getTime(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTime2() throws Exception {
        new GeneratedKeysResultSet().getTime("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTime3() throws Exception {
        new GeneratedKeysResultSet().getTime("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimestamp() throws Exception {
        new GeneratedKeysResultSet().getTimestamp(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimestamp1() throws Exception {
        new GeneratedKeysResultSet().getTimestamp(1, null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimestamp2() throws Exception {
        new GeneratedKeysResultSet().getTimestamp("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getTimestamp3() throws Exception {
        new GeneratedKeysResultSet().getTimestamp("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getAsciiStream() throws Exception {
        new GeneratedKeysResultSet().getAsciiStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getAsciiStream1() throws Exception {
        new GeneratedKeysResultSet().getAsciiStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getUnicodeStream() throws Exception {
        new GeneratedKeysResultSet().getUnicodeStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getUnicodeStream1() throws Exception {
        new GeneratedKeysResultSet().getUnicodeStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBinaryStream() throws Exception {
        new GeneratedKeysResultSet().getBinaryStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBinaryStream1() throws Exception {
        new GeneratedKeysResultSet().getBinaryStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getWarnings() throws Exception {
        new GeneratedKeysResultSet().getWarnings();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void clearWarnings() throws Exception {
        new GeneratedKeysResultSet().clearWarnings();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getCharacterStream() throws Exception {
        new GeneratedKeysResultSet().getCharacterStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getCharacterStream1() throws Exception {
        new GeneratedKeysResultSet().getCharacterStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setFetchDirection() throws Exception {
        new GeneratedKeysResultSet().setFetchDirection(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getFetchDirection() throws Exception {
        new GeneratedKeysResultSet().getFetchDirection();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setFetchSize() throws Exception {
        new GeneratedKeysResultSet().setFetchSize(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getFetchSize() throws Exception {
        new GeneratedKeysResultSet().getFetchSize();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getObject() throws Exception {
        new GeneratedKeysResultSet().getObject(1, new HashMap<String, Class<?>>());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getObject1() throws Exception {
        new GeneratedKeysResultSet().getObject("", new HashMap<String, Class<?>>());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBlob() throws Exception {
        new GeneratedKeysResultSet().getBlob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getBlob1() throws Exception {
        new GeneratedKeysResultSet().getBlob("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getClob() throws Exception {
        new GeneratedKeysResultSet().getClob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getClob1() throws Exception {
        new GeneratedKeysResultSet().getClob("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getURL() throws Exception {
        new GeneratedKeysResultSet().getURL(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getURL1() throws Exception {
        new GeneratedKeysResultSet().getURL("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getSQLXML() throws Exception {
        new GeneratedKeysResultSet().getSQLXML(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void getSQLXML1() throws Exception {
        new GeneratedKeysResultSet().getSQLXML("");
    }
    
}

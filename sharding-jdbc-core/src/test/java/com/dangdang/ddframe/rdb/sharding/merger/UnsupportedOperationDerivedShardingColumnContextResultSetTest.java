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

package com.dangdang.ddframe.rdb.sharding.merger;

import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDataBasesOnlyDBUnitTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;

public class UnsupportedOperationDerivedShardingColumnContextResultSetTest extends AbstractShardingDataBasesOnlyDBUnitTest {
    
    private ResultSet actual;
    
    @Before
    public void init() throws SQLException {
        actual = getShardingDataSource().getConnection().createStatement().executeQuery("SELECT user_id AS `uid` FROM `t_order` group by `uid`");
    }
    
    @After
    public void close() throws SQLException {
        actual.close();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetObjectForColumnIndex() throws Exception {
        actual.getObject(1, new HashMap<String, Class<?>>());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetObjectForColumnLabel() throws Exception {
        actual.getObject("no", new HashMap<String, Class<?>>());
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetAsciiStreamForColumnIndex() throws Exception {
        actual.getAsciiStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetAsciiStreamForColumnLabel() throws Exception {
        actual.getAsciiStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBinaryStreamForColumnIndex() throws Exception {
        actual.getBinaryStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBinaryStreamForColumnLabel() throws Exception {
        actual.getBinaryStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetUnicodeStreamForColumnIndex() throws Exception {
        actual.getUnicodeStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetUnicodeStreamForColumnLabel() throws Exception {
        actual.getUnicodeStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCharacterStreamForColumnIndex() throws Exception {
        actual.getCharacterStream(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCharacterStreamForColumnLabel() throws Exception {
        actual.getCharacterStream("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBlobForColumnIndex() throws Exception {
        actual.getBlob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetBlobForColumnLabel() throws Exception {
        actual.getBlob("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClobForColumnIndex() throws Exception {
        actual.getClob(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClobForColumnLabel() throws Exception {
        actual.getClob("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetSQLXMLForColumnIndex() throws Exception {
        actual.getSQLXML(1);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetSQLXMLForColumnLabel() throws Exception {
        actual.getSQLXML("");
    }
}

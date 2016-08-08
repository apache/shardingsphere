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
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

public final class UnsupportedOperationConnectionTest extends AbstractShardingDataBasesOnlyDBUnitTest {
    
    private ShardingConnection actual;
    
    
    @Before
    public void init() throws SQLException {
        actual = getShardingDataSource().getConnection();
    }
    
    @After
    public void close() throws SQLException {
        actual.close();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrepareCall() throws SQLException {
        actual.prepareCall("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrepareCallWithResultSetTypeAndResultSetConcurrency() throws SQLException {
        actual.prepareCall("", 0, 0);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrepareCallWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException {
        actual.prepareCall("", 0, 0, 0);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertNativeSQL() throws SQLException {
        actual.nativeSQL("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetSavepoint() throws SQLException {
        actual.setSavepoint();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetSavepointWithName() throws SQLException {
        actual.setSavepoint("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertReleaseSavepoint() throws SQLException {
        actual.releaseSavepoint(null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRollback() throws SQLException {
        actual.rollback(null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertAbort() throws SQLException {
        actual.abort(null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCatalog() throws SQLException {
        actual.getCatalog();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetCatalog() throws SQLException {
        actual.setCatalog("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetSchema() throws SQLException {
        actual.getSchema();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetSchema() throws SQLException {
        actual.setSchema("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetTypeMap() throws SQLException {
        actual.getTypeMap();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetTypeMap() throws SQLException {
        actual.setTypeMap(null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetNetworkTimeout() throws SQLException {
        actual.getNetworkTimeout();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNetworkTimeout() throws SQLException {
        actual.setNetworkTimeout(null, 0);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateClob() throws SQLException {
        actual.createClob();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateBlob() throws SQLException {
        actual.createBlob();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateNClob() throws SQLException {
        actual.createNClob();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateSQLXML() throws SQLException {
        actual.createSQLXML();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateArrayOf() throws SQLException {
        actual.createArrayOf("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateStruct() throws SQLException {
        actual.createStruct("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsValid() throws SQLException {
        actual.isValid(0);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClientInfo() throws SQLException {
        actual.getClientInfo();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClientInfoWithName() throws SQLException {
        actual.getClientInfo("");
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertSetClientInfo() throws SQLException {
        actual.setClientInfo("", "");
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertSetClientInfoWithProperties() throws SQLException {
        actual.setClientInfo(new Properties());
    }
}

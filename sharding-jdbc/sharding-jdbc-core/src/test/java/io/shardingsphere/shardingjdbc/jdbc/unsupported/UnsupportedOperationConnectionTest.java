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
import io.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class UnsupportedOperationConnectionTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    private final List<ShardingConnection> shardingConnections = new ArrayList<>();
    
    @Before
    public void init() {
        shardingConnections.add(getShardingDataSource().getConnection());
    }
    
    @After
    public void close() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.close();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrepareCall() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.prepareCall("");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrepareCallWithResultSetTypeAndResultSetConcurrency() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.prepareCall("", 0, 0);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrepareCallWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.prepareCall("", 0, 0, 0);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertNativeSQL() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.nativeSQL("");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetSavepoint() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.setSavepoint();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetSavepointWithName() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.setSavepoint("");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertReleaseSavepoint() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.releaseSavepoint(null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRollback() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.rollback(null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertAbort() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.abort(null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetCatalog() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.getCatalog();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetCatalog() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.setCatalog("");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetSchema() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.getSchema();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetSchema() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.setSchema("");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetTypeMap() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.getTypeMap();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetTypeMap() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.setTypeMap(null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetNetworkTimeout() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.getNetworkTimeout();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNetworkTimeout() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.setNetworkTimeout(null, 0);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateClob() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.createClob();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateBlob() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.createBlob();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateNClob() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.createNClob();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateSQLXML() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.createSQLXML();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateArrayOf() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.createArrayOf("", null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateStruct() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.createStruct("", null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsValid() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.isValid(0);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClientInfo() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.getClientInfo();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClientInfoWithName() throws SQLException {
        for (ShardingConnection each : shardingConnections) {
            each.getClientInfo("");
        }
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertSetClientInfo() {
        for (ShardingConnection each : shardingConnections) {
            each.setClientInfo("", "");
        }
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertSetClientInfoWithProperties() {
        for (ShardingConnection each : shardingConnections) {
            each.setClientInfo(new Properties());
        }
    }
}

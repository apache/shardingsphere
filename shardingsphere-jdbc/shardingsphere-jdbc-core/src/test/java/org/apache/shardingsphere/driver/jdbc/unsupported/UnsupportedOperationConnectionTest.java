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

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertNull;

public final class UnsupportedOperationConnectionTest extends AbstractShardingSphereDataSourceForShardingTest {
    
    private final List<ShardingSphereConnection> shardingSphereConnections = new ArrayList<>();
    
    @Before
    public void init() {
        shardingSphereConnections.add(getShardingSphereDataSource().getConnection());
    }
    
    @After
    public void close() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.close();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrepareCall() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.prepareCall("");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrepareCallWithResultSetTypeAndResultSetConcurrency() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.prepareCall("", 0, 0);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrepareCallWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.prepareCall("", 0, 0, 0);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertNativeSQL() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.nativeSQL("");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetSavepoint() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.setSavepoint();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetSavepointWithName() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.setSavepoint("");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertReleaseSavepoint() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.releaseSavepoint(null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertRollback() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.rollback(null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertAbort() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.abort(null);
        }
    }
    
    @Test
    public void assertGetCatalog() {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            assertNull(each.getCatalog());
        }
    }
    
    @Test
    public void assertSetCatalog() {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.setCatalog("");
        }
    }
    
    @Test
    public void assertGetSchema() {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            assertNull(each.getSchema());
        }
    }
    
    @Test
    public void assertSetSchema() {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.setSchema("");
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetTypeMap() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.getTypeMap();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetTypeMap() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.setTypeMap(null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetNetworkTimeout() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.getNetworkTimeout();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNetworkTimeout() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.setNetworkTimeout(null, 0);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateClob() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.createClob();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateBlob() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.createBlob();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateNClob() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.createNClob();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateSQLXML() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.createSQLXML();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateArrayOf() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.createArrayOf("", null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateStruct() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.createStruct("", null);
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClientInfo() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.getClientInfo();
        }
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClientInfoWithName() throws SQLException {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.getClientInfo("");
        }
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertSetClientInfo() {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.setClientInfo("", "");
        }
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertSetClientInfoWithProperties() {
        for (ShardingSphereConnection each : shardingSphereConnections) {
            each.setClientInfo(new Properties());
        }
    }
}

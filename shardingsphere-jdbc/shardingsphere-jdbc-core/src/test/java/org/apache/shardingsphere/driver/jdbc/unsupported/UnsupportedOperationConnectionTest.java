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
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class UnsupportedOperationConnectionTest {
    
    private final ShardingSphereConnection shardingSphereConnection;
    
    public UnsupportedOperationConnectionTest() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(TransactionRule.class)).thenReturn(Optional.empty());
        shardingSphereConnection = new ShardingSphereConnection(DefaultSchema.LOGIC_NAME, contextManager);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrepareCall() throws SQLException {
        shardingSphereConnection.prepareCall("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrepareCallWithResultSetTypeAndResultSetConcurrency() throws SQLException {
        shardingSphereConnection.prepareCall("", 0, 0);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertPrepareCallWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException {
        shardingSphereConnection.prepareCall("", 0, 0, 0);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertNativeSQL() throws SQLException {
        shardingSphereConnection.nativeSQL("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertAbort() throws SQLException {
        shardingSphereConnection.abort(null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetTypeMap() throws SQLException {
        shardingSphereConnection.getTypeMap();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetTypeMap() throws SQLException {
        shardingSphereConnection.setTypeMap(null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetNetworkTimeout() throws SQLException {
        shardingSphereConnection.getNetworkTimeout();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetNetworkTimeout() throws SQLException {
        shardingSphereConnection.setNetworkTimeout(null, 0);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateClob() throws SQLException {
        shardingSphereConnection.createClob();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateBlob() throws SQLException {
        shardingSphereConnection.createBlob();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateNClob() throws SQLException {
        shardingSphereConnection.createNClob();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateSQLXML() throws SQLException {
        shardingSphereConnection.createSQLXML();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCreateStruct() throws SQLException {
        shardingSphereConnection.createStruct("", null);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClientInfo() throws SQLException {
        shardingSphereConnection.getClientInfo();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertGetClientInfoWithName() throws SQLException {
        shardingSphereConnection.getClientInfo("");
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertSetClientInfo() {
        shardingSphereConnection.setClientInfo("", "");
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertSetClientInfoWithProperties() {
        shardingSphereConnection.setClientInfo(new Properties());
    }
}

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
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.Test;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnsupportedOperationConnectionTest {
    
    private final ShardingSphereConnection shardingSphereConnection;
    
    UnsupportedOperationConnectionTest() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(
                new ShardingSphereRuleMetaData(Arrays.asList(mock(TransactionRule.class, RETURNS_DEEP_STUBS), mock(TrafficRule.class))));
        shardingSphereConnection = new ShardingSphereConnection(DefaultDatabase.LOGIC_NAME, contextManager);
    }
    
    @Test
    void assertNativeSQL() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereConnection.nativeSQL(""));
    }
    
    @Test
    void assertAbort() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereConnection.abort(null));
    }
    
    @Test
    void assertGetTypeMap() {
        assertThrows(SQLFeatureNotSupportedException.class, shardingSphereConnection::getTypeMap);
    }
    
    @Test
    void assertSetTypeMap() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereConnection.setTypeMap(null));
    }
    
    @Test
    void assertGetNetworkTimeout() {
        assertThrows(SQLFeatureNotSupportedException.class, shardingSphereConnection::getNetworkTimeout);
    }
    
    @Test
    void assertSetNetworkTimeout() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereConnection.setNetworkTimeout(null, 0));
    }
    
    @Test
    void assertCreateClob() {
        assertThrows(SQLFeatureNotSupportedException.class, shardingSphereConnection::createClob);
    }
    
    @Test
    void assertCreateBlob() {
        assertThrows(SQLFeatureNotSupportedException.class, shardingSphereConnection::createBlob);
    }
    
    @Test
    void assertCreateNClob() {
        assertThrows(SQLFeatureNotSupportedException.class, shardingSphereConnection::createNClob);
    }
    
    @Test
    void assertCreateSQLXML() {
        assertThrows(SQLFeatureNotSupportedException.class, shardingSphereConnection::createSQLXML);
    }
    
    @Test
    void assertCreateStruct() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereConnection.createStruct("", null));
    }
    
    @Test
    void assertGetClientInfo() {
        assertThrows(SQLFeatureNotSupportedException.class, shardingSphereConnection::getClientInfo);
    }
    
    @Test
    void assertGetClientInfoWithName() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereConnection.getClientInfo(""));
    }
    
    @Test
    void assertSetClientInfo() {
        assertThrows(UnsupportedSQLOperationException.class, () -> shardingSphereConnection.setClientInfo("", ""));
    }
    
    @Test
    void assertSetClientInfoWithProperties() {
        assertThrows(UnsupportedSQLOperationException.class, () -> shardingSphereConnection.setClientInfo(new Properties()));
    }
}

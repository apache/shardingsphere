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
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSphereStatement;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.builder.DefaultTrafficRuleConfigurationBuilder;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class UnsupportedOperationStatementTest {
    
    private ShardingSphereStatement shardingSphereStatement;
    
    @Before
    public void setUp() {
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        TrafficRule trafficRule = new TrafficRule(new DefaultTrafficRuleConfigurationBuilder().build());
        SQLFederationRule sqlFederationRule = new SQLFederationRule(new DefaultSQLFederationRuleConfigurationBuilder().build());
        when(connection.getDatabaseName()).thenReturn("db");
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(globalRuleMetaData.getSingleRule(TrafficRule.class)).thenReturn(trafficRule);
        when(globalRuleMetaData.getSingleRule(SQLFederationRule.class)).thenReturn(sqlFederationRule);
        shardingSphereStatement = new ShardingSphereStatement(connection);
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertAddBatch() throws SQLException {
        shardingSphereStatement.addBatch("");
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertClearBatch() throws SQLException {
        shardingSphereStatement.clearBatch();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertExecuteBatch() throws SQLException {
        shardingSphereStatement.executeBatch();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertCloseOnCompletion() throws SQLException {
        shardingSphereStatement.closeOnCompletion();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertIsCloseOnCompletion() throws SQLException {
        shardingSphereStatement.isCloseOnCompletion();
    }
    
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void assertSetCursorName() throws SQLException {
        shardingSphereStatement.setCursorName("cursorName");
    }
}

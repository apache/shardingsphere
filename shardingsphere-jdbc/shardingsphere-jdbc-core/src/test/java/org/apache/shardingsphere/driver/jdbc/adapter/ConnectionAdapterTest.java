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

package org.apache.shardingsphere.driver.jdbc.adapter;

import org.apache.shardingsphere.driver.jdbc.context.JDBCContext;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.builder.DefaultTrafficRuleConfigurationBuilder;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.After;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ConnectionAdapterTest {
    
    @Test
    public void assertGetWarnings() throws SQLException {
        assertNull(createConnectionAdaptor().getWarnings());
    }
    
    @Test
    public void assertClearWarnings() throws SQLException {
        createConnectionAdaptor().clearWarnings();
    }
    
    @Test
    public void assertGetHoldability() throws SQLException {
        assertThat(createConnectionAdaptor().getHoldability(), is(ResultSet.CLOSE_CURSORS_AT_COMMIT));
    }
    
    @Test
    public void assertSetHoldability() throws SQLException {
        createConnectionAdaptor().setHoldability(ResultSet.CONCUR_READ_ONLY);
        assertThat(createConnectionAdaptor().getHoldability(), is(ResultSet.CLOSE_CURSORS_AT_COMMIT));
    }
    
    @Test
    public void assertGetCatalog() throws SQLException {
        assertNull(createConnectionAdaptor().getCatalog());
    }
    
    @Test
    public void assertSetCatalog() throws SQLException {
        Connection actual = createConnectionAdaptor();
        actual.setCatalog("");
        assertNull(actual.getCatalog());
    }
    
    @Test
    public void assertSetSchema() throws SQLException {
        Connection actual = createConnectionAdaptor();
        String originalSchema = actual.getSchema();
        actual.setSchema("");
        assertThat(actual.getSchema(), is(originalSchema));
    }
    
    private Connection createConnectionAdaptor() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(globalRuleMetaData.getSingleRule(TransactionRule.class)).thenReturn(mock(TransactionRule.class, RETURNS_DEEP_STUBS));
        when(globalRuleMetaData.getSingleRule(TrafficRule.class)).thenReturn(new TrafficRule(new DefaultTrafficRuleConfigurationBuilder().build()));
        return new ShardingSphereConnection(DefaultDatabase.LOGIC_NAME, contextManager, mock(JDBCContext.class));
    }
    
    @After
    public void tearDown() {
        TransactionTypeHolder.clear();
    }
}

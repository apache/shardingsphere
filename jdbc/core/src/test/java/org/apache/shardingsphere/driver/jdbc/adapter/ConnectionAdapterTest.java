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

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.builder.DefaultTrafficRuleConfigurationBuilder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConnectionAdapterTest {
    
    @Test
    void assertGetWarnings() throws SQLException {
        try (Connection actual = createConnectionAdaptor()) {
            assertNull(actual.getWarnings());
        }
    }
    
    @Test
    void assertClearWarnings() throws SQLException {
        try (Connection actual = createConnectionAdaptor()) {
            assertDoesNotThrow(actual::clearWarnings);
        }
    }
    
    @Test
    void assertGetHoldability() throws SQLException {
        try (Connection actual = createConnectionAdaptor()) {
            assertThat(actual.getHoldability(), is(ResultSet.CLOSE_CURSORS_AT_COMMIT));
        }
    }
    
    @Test
    void assertSetHoldability() throws SQLException {
        try (Connection actual = createConnectionAdaptor()) {
            actual.setHoldability(ResultSet.CONCUR_READ_ONLY);
        }
        try (Connection actual = createConnectionAdaptor()) {
            assertThat(actual.getHoldability(), is(ResultSet.CLOSE_CURSORS_AT_COMMIT));
        }
    }
    
    @Test
    void assertGetCatalog() throws SQLException {
        try (Connection actual = createConnectionAdaptor()) {
            assertNull(actual.getCatalog());
        }
    }
    
    @Test
    void assertSetCatalog() throws SQLException {
        try (Connection actual = createConnectionAdaptor()) {
            actual.setCatalog("");
            assertNull(actual.getCatalog());
        }
    }
    
    @Test
    void assertSetSchema() throws SQLException {
        try (Connection actual = createConnectionAdaptor()) {
            String originalSchema = actual.getSchema();
            actual.setSchema("");
            assertThat(actual.getSchema(), is(originalSchema));
        }
    }
    
    private Connection createConnectionAdaptor() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(
                new ShardingSphereRuleMetaData(Arrays.asList(mock(TransactionRule.class, RETURNS_DEEP_STUBS), new TrafficRule(new DefaultTrafficRuleConfigurationBuilder().build()))));
        return new ShardingSphereConnection(DefaultDatabase.LOGIC_NAME, contextManager);
    }
}

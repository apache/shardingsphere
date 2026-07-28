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

package org.apache.shardingsphere.mcp.support.database.metadata.jdbc;

import org.apache.shardingsphere.mcp.support.database.metadata.TransactionCapability;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPJdbcMetadataLoaderFailureTest {
    
    @Test
    void assertLoadWithFailedConnection() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = mock(RuntimeDatabaseConfiguration.class);
        SQLException expected = new SQLException("permission denied", "28000", 335544352);
        when(runtimeDatabaseConfiguration.openConnection("logic_db")).thenThrow(expected);
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                () -> metadataLoader.load("logic_db", runtimeDatabaseConfiguration, createDatabaseProfile()));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED));
        assertThat(actual.getCause(), is(expected));
    }
    
    @Test
    void assertLoadColumnsWithFailedConnection() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfiguration.openConnection("logic_db")).thenThrow(new SQLException("permission denied", "28000", 335544352));
        RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                () -> new MCPJdbcMetadataLoader().loadColumns("logic_db", runtimeDatabaseConfiguration, createDatabaseProfile(), "public", "t_order"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED));
    }
    
    @Test
    void assertLoadSchemaColumnsWithFailedConnection() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfiguration.openConnection("logic_db")).thenThrow(new SQLException("permission denied", "28000", 335544352));
        RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                () -> new MCPJdbcMetadataLoader().loadSchemaColumns("logic_db", runtimeDatabaseConfiguration, createDatabaseProfile(), "public"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED));
    }
    
    @Test
    void assertLoadIndexesWithFailedConnection() throws SQLException {
        RuntimeDatabaseConfiguration runtimeDatabaseConfiguration = mock(RuntimeDatabaseConfiguration.class);
        when(runtimeDatabaseConfiguration.openConnection("logic_db")).thenThrow(new SQLException("permission denied", "28000", 335544352));
        RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                () -> new MCPJdbcMetadataLoader().loadIndexes("logic_db", runtimeDatabaseConfiguration, createDatabaseProfile(), "public", "t_order"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED));
    }
    
    private static RuntimeDatabaseProfile createDatabaseProfile() {
        return new RuntimeDatabaseProfile("logic_db", "Firebird", "", TransactionCapability.LOCAL_WITH_SAVEPOINT,
                new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newInsensitivePolicySet()));
    }
}

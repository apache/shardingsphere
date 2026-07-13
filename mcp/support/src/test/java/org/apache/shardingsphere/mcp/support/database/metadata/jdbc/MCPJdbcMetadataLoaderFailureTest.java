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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
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
        SQLException expected = new SQLException("connection failed");
        when(runtimeDatabaseConfiguration.openConnection("logic_db")).thenThrow(expected);
        MCPJdbcMetadataLoader metadataLoader = new MCPJdbcMetadataLoader();
        RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                () -> metadataLoader.load("logic_db", runtimeDatabaseConfiguration,
                        new RuntimeDatabaseProfile("logic_db", "FixtureDB", "", true, true, IdentifierCasePolicyFactory.newInsensitivePolicySet())));
        assertThat(actual.getMessage(), is("Runtime database `logic_db` connection failed: connection_failed."));
        assertThat(actual.getCause(), is(expected));
    }
}

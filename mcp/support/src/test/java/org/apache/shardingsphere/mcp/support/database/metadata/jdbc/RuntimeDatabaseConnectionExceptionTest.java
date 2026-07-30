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

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class RuntimeDatabaseConnectionExceptionTest {
    
    @Test
    void assertMissingJdbcDriver() {
        RuntimeDatabaseConnectionException actual = RuntimeDatabaseConnectionException.missingJdbcDriver("logic_db", new ClassNotFoundException("org.example.Driver"));
        assertThat(actual.getMessage(), is("Runtime database `logic_db` connection failed: missing_jdbc_driver."));
        assertThat(actual.getDatabaseName(), is("logic_db"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_MISSING_JDBC_DRIVER));
    }
    
    @Test
    void assertInvalidConfiguration() {
        RuntimeDatabaseConnectionException actual = RuntimeDatabaseConnectionException.invalidConfiguration("logic_db", new IllegalStateException("bad config"));
        assertThat(actual.getMessage(), is("Runtime database `logic_db` connection failed: invalid_configuration."));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION));
    }
    
    @Test
    void assertConnectionFailedAsTimeout() {
        RuntimeDatabaseConnectionException actual = RuntimeDatabaseConnectionException.connectionFailed("logic_db", new SQLTimeoutException("timed out"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_TIMEOUT));
    }
    
    @Test
    void assertConnectionFailedAsAuthentication() {
        RuntimeDatabaseConnectionException actual = RuntimeDatabaseConnectionException.connectionFailed("logic_db", new SQLException("Access denied for user foo", "28000"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_AUTHENTICATION_FAILED));
    }
    
    @Test
    void assertConnectionFailedAsAuthorization() {
        RuntimeDatabaseConnectionException actual = RuntimeDatabaseConnectionException.connectionFailed("logic_db", new SQLException("permission denied", "42501"));
        assertThat(actual.getMessage(), is("Runtime database `logic_db` connection failed: authorization_failed."));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED));
    }
    
    @Test
    void assertConnectionFailedAsDialectAuthorization() {
        RuntimeDatabaseConnectionException actual = RuntimeDatabaseConnectionException.connectionFailed(
                "logic_db", "Firebird", new SQLException("permission denied", "28000", 335544352));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED));
    }
    
    @Test
    void assertConnectionFailedAsDatabaseUnavailable() {
        RuntimeDatabaseConnectionException actual = RuntimeDatabaseConnectionException.connectionFailed("logic_db", new SQLException("Connection refused", "08001"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_DATABASE_UNAVAILABLE));
    }
    
    @Test
    void assertConnectionFailed() {
        RuntimeDatabaseConnectionException actual = RuntimeDatabaseConnectionException.connectionFailed("logic_db", new SQLException("Broken connection"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_FAILED));
    }
    
    @Test
    void assertConnectionFailedDoesNotInspectMessage() {
        RuntimeDatabaseConnectionException actual = RuntimeDatabaseConnectionException.connectionFailed("logic_db", new SQLException("Access denied because the operation timed out"));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_FAILED));
    }
    
    @Test
    void assertDatabaseNotVisible() {
        RuntimeDatabaseConnectionException actual = RuntimeDatabaseConnectionException.databaseNotVisible("logic_db", new IllegalStateException("not visible"));
        assertThat(actual.getMessage(), is("Runtime database `logic_db` connection failed: database_not_visible."));
        assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_DATABASE_NOT_VISIBLE));
    }
}

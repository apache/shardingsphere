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

import lombok.Getter;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Locale;
import java.util.Objects;

/**
 * Runtime database connection exception with safe model-facing category.
 */
@Getter
public final class RuntimeDatabaseConnectionException extends RuntimeException {
    
    public static final String CATEGORY_MISSING_JDBC_DRIVER = "missing_jdbc_driver";
    
    public static final String CATEGORY_AUTHENTICATION_FAILED = "authentication_failed";
    
    public static final String CATEGORY_CONNECTION_TIMEOUT = "connection_timeout";
    
    public static final String CATEGORY_INVALID_CONFIGURATION = "invalid_configuration";
    
    public static final String CATEGORY_DATABASE_UNAVAILABLE = "database_unavailable";
    
    public static final String CATEGORY_CONNECTION_FAILED = "connection_failed";
    
    private static final long serialVersionUID = -757957427736251437L;
    
    private final String databaseName;
    
    private final String category;
    
    public RuntimeDatabaseConnectionException(final String databaseName, final String category, final Throwable cause) {
        super(String.format("Runtime database `%s` connection failed: %s.", databaseName, category), cause);
        this.databaseName = databaseName;
        this.category = category;
    }
    
    /**
     * Create missing JDBC driver exception.
     *
     * @param databaseName database name
     * @param cause cause
     * @return runtime database connection exception
     */
    public static RuntimeDatabaseConnectionException missingJdbcDriver(final String databaseName, final Throwable cause) {
        return new RuntimeDatabaseConnectionException(databaseName, CATEGORY_MISSING_JDBC_DRIVER, cause);
    }
    
    /**
     * Create invalid runtime database configuration exception.
     *
     * @param databaseName database name
     * @param cause cause
     * @return runtime database connection exception
     */
    public static RuntimeDatabaseConnectionException invalidConfiguration(final String databaseName, final Throwable cause) {
        return new RuntimeDatabaseConnectionException(databaseName, CATEGORY_INVALID_CONFIGURATION, cause);
    }
    
    /**
     * Create connection failure exception.
     *
     * @param databaseName database name
     * @param cause cause
     * @return runtime database connection exception
     */
    public static RuntimeDatabaseConnectionException connectionFailed(final String databaseName, final SQLException cause) {
        return new RuntimeDatabaseConnectionException(databaseName, resolveCategory(cause), cause);
    }
    
    private static String resolveCategory(final SQLException cause) {
        String sqlState = Objects.toString(cause.getSQLState(), "");
        String message = Objects.toString(cause.getMessage(), "").toLowerCase(Locale.ENGLISH);
        if (cause instanceof SQLTimeoutException || message.contains("timeout") || message.contains("timed out")) {
            return CATEGORY_CONNECTION_TIMEOUT;
        }
        if (sqlState.startsWith("28") || message.contains("authentication") || message.contains("access denied") || message.contains("password")) {
            return CATEGORY_AUTHENTICATION_FAILED;
        }
        if (sqlState.startsWith("08") || message.contains("no suitable driver")) {
            return CATEGORY_DATABASE_UNAVAILABLE;
        }
        return CATEGORY_CONNECTION_FAILED;
    }
}

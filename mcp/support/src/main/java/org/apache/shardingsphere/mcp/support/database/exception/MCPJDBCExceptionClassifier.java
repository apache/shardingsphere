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

package org.apache.shardingsphere.mcp.support.database.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.support.database.spi.MCPDialectSQLExceptionClassifier;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientConnectionException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;

/**
 * MCP JDBC exception classifier.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPJDBCExceptionClassifier {
    
    private static final Set<String> OBJECT_NOT_VISIBLE_SQL_STATES = Set.of("3F000", "42P01", "42703", "42704", "42S02", "42S22");
    
    /**
     * Classify JDBC exception without database dialect evidence.
     *
     * @param cause cause
     * @return error category
     */
    public static MCPJDBCErrorCategory classify(final Throwable cause) {
        return classify(Optional.empty(), cause);
    }
    
    /**
     * Classify JDBC exception for a database type.
     *
     * @param databaseType database type
     * @param cause cause
     * @return error category
     */
    public static MCPJDBCErrorCategory classify(final String databaseType, final Throwable cause) {
        return classify(TypedSPILoader.findService(MCPDialectSQLExceptionClassifier.class, databaseType), cause);
    }
    
    private static MCPJDBCErrorCategory classify(final Optional<MCPDialectSQLExceptionClassifier> dialectClassifier, final Throwable cause) {
        Deque<Throwable> pending = new ArrayDeque<>();
        pending.add(cause);
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        while (!pending.isEmpty()) {
            Throwable current = pending.removeFirst();
            if (!visited.add(current)) {
                continue;
            }
            if (current instanceof MCPDatabaseQueryFailedException) {
                return ((MCPDatabaseQueryFailedException) current).getCategory();
            }
            if (current instanceof MCPDatabaseSQLSyntaxException) {
                return MCPJDBCErrorCategory.SYNTAX;
            }
            if (current instanceof SQLException) {
                Optional<MCPJDBCErrorCategory> category = classifySQLException(dialectClassifier, (SQLException) current);
                if (category.isPresent() && MCPJDBCErrorCategory.QUERY_FAILED != category.get()) {
                    return category.get();
                }
                addIfPresent(pending, ((SQLException) current).getNextException());
            }
            addIfPresent(pending, current.getCause());
        }
        return MCPJDBCErrorCategory.QUERY_FAILED;
    }
    
    private static Optional<MCPJDBCErrorCategory> classifySQLException(final Optional<MCPDialectSQLExceptionClassifier> dialectClassifier, final SQLException cause) {
        Optional<MCPJDBCErrorCategory> standardCategory = classifyStandard(cause);
        if (standardCategory.isPresent()) {
            return standardCategory;
        }
        Optional<MCPJDBCErrorCategory> dialectCategory = dialectClassifier.flatMap(classifier -> classifier.classify(cause));
        return dialectCategory.isPresent() ? dialectCategory : classifySyntax(cause);
    }
    
    private static Optional<MCPJDBCErrorCategory> classifyStandard(final SQLException cause) {
        if (cause instanceof SQLTimeoutException) {
            return Optional.of(MCPJDBCErrorCategory.TIMEOUT);
        }
        String sqlState = cause.getSQLState();
        if (cause instanceof SQLFeatureNotSupportedException || startsWith(sqlState, "0A")) {
            return Optional.of(MCPJDBCErrorCategory.FEATURE_NOT_SUPPORTED);
        }
        if (cause instanceof SQLTransientConnectionException || cause instanceof SQLNonTransientConnectionException || startsWith(sqlState, "08")) {
            return Optional.of(MCPJDBCErrorCategory.CONNECTION);
        }
        if (startsWith(sqlState, "28")) {
            return Optional.of(MCPJDBCErrorCategory.AUTHENTICATION);
        }
        if ("42501".equals(sqlState)) {
            return Optional.of(MCPJDBCErrorCategory.AUTHORIZATION);
        }
        if (null != sqlState && OBJECT_NOT_VISIBLE_SQL_STATES.contains(sqlState)) {
            return Optional.of(MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE);
        }
        return Optional.empty();
    }
    
    private static Optional<MCPJDBCErrorCategory> classifySyntax(final SQLException cause) {
        return "42601".equals(cause.getSQLState()) || cause instanceof SQLSyntaxErrorException
                ? Optional.of(MCPJDBCErrorCategory.SYNTAX)
                : Optional.empty();
    }
    
    private static boolean startsWith(final String value, final String prefix) {
        return null != value && value.startsWith(prefix);
    }
    
    private static void addIfPresent(final Deque<Throwable> pending, final Throwable cause) {
        if (null != cause) {
            pending.addLast(cause);
        }
    }
}

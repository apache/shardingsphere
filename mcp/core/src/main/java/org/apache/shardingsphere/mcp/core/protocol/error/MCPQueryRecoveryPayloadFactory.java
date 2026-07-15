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

package org.apache.shardingsphere.mcp.core.protocol.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPTimeoutException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.RuleDistSQLExecutionException;
import org.apache.shardingsphere.mcp.support.diagnostic.MCPDiagnosticCategory;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientConnectionException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MCP query recovery payload factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPQueryRecoveryPayloadFactory {
    
    static boolean isQueryFailure(final Throwable cause) {
        if (cause instanceof RuleDistSQLExecutionException) {
            return false;
        }
        if (cause instanceof MCPQueryFailedException || cause instanceof SQLException) {
            return true;
        }
        Optional<SQLException> sqlException = findSQLException(cause);
        return cause instanceof MCPTimeoutException && sqlException.filter(SQLTimeoutException.class::isInstance).isPresent()
                || cause instanceof MCPUnsupportedException && sqlException.filter(SQLFeatureNotSupportedException.class::isInstance).isPresent()
                || cause instanceof MCPInvalidRequestException && sqlException.filter(SQLSyntaxErrorException.class::isInstance).isPresent();
    }
    
    static Map<String, Object> create(final Throwable cause) {
        String category = classify(cause);
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(category, createModelAction(category));
        result.put("secret_safe", true);
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, createNextActions(category));
        if (usesResourceHint(category)) {
            result.put(MCPPayloadFieldNames.RESOURCES_TO_READ, createResourcesToRead(category));
        }
        return result;
    }
    
    private static String classify(final Throwable cause) {
        Optional<SQLException> sqlException = findSQLException(cause);
        if (sqlException.filter(SQLTimeoutException.class::isInstance).isPresent()) {
            return MCPDiagnosticCategory.EXECUTION_TIMEOUT;
        }
        if (sqlException.filter(SQLFeatureNotSupportedException.class::isInstance).isPresent()) {
            return MCPDiagnosticCategory.UNSUPPORTED_DATABASE_CAPABILITY;
        }
        if (sqlException.filter(each -> each instanceof SQLTransientConnectionException || each instanceof SQLNonTransientConnectionException).isPresent()) {
            return MCPDiagnosticCategory.CONNECTION_INTERRUPTED;
        }
        if (sqlException.map(SQLException::getSQLState).filter(MCPQueryRecoveryPayloadFactory::isConnectionInterruptedSQLState).isPresent()) {
            return MCPDiagnosticCategory.CONNECTION_INTERRUPTED;
        }
        if (sqlException.map(SQLException::getSQLState).filter(MCPQueryRecoveryPayloadFactory::isInsufficientPrivilegesSQLState).isPresent()) {
            return MCPDiagnosticCategory.INSUFFICIENT_PRIVILEGES;
        }
        if (sqlException.map(SQLException::getSQLState).filter(MCPQueryRecoveryPayloadFactory::isObjectNotVisibleSQLState).isPresent()) {
            return MCPDiagnosticCategory.OBJECT_NOT_VISIBLE;
        }
        if (cause instanceof SQLSyntaxErrorException || sqlException.filter(SQLSyntaxErrorException.class::isInstance).isPresent()
                || sqlException.map(SQLException::getSQLState).filter(MCPQueryRecoveryPayloadFactory::isSyntaxErrorSQLState).isPresent()) {
            return MCPDiagnosticCategory.SQL_SYNTAX_ERROR;
        }
        return MCPDiagnosticCategory.QUERY_FAILED;
    }
    
    private static Optional<SQLException> findSQLException(final Throwable cause) {
        Throwable current = cause;
        while (null != current) {
            if (current instanceof SQLException) {
                return Optional.of((SQLException) current);
            }
            current = current.getCause();
        }
        return Optional.empty();
    }
    
    private static boolean isConnectionInterruptedSQLState(final String sqlState) {
        return null != sqlState && sqlState.startsWith("08");
    }
    
    private static boolean isInsufficientPrivilegesSQLState(final String sqlState) {
        return null != sqlState && ("42501".equals(sqlState) || sqlState.startsWith("28"));
    }
    
    private static boolean isObjectNotVisibleSQLState(final String sqlState) {
        return null != sqlState && List.of("3F000", "42P01", "42703", "42704", "42S02", "42S22").contains(sqlState);
    }
    
    private static boolean isSyntaxErrorSQLState(final String sqlState) {
        return null != sqlState && sqlState.startsWith("42");
    }
    
    private static String createModelAction(final String category) {
        switch (category) {
            case MCPDiagnosticCategory.SQL_SYNTAX_ERROR:
                return "Ask the user for one corrected SQL statement before retrying.";
            case MCPDiagnosticCategory.OBJECT_NOT_VISIBLE:
                return "Read visible metadata resources or search metadata before retrying with a visible object.";
            case MCPDiagnosticCategory.INSUFFICIENT_PRIVILEGES:
                return "Ask the operator to check runtime database privileges outside MCP before retrying.";
            case MCPDiagnosticCategory.EXECUTION_TIMEOUT:
                return "Retry only with a narrower query or a valid timeout after checking runtime status.";
            case MCPDiagnosticCategory.CONNECTION_INTERRUPTED:
                return "Check runtime database availability and connectivity before retrying.";
            case MCPDiagnosticCategory.UNSUPPORTED_DATABASE_CAPABILITY:
                return "Read MCP capabilities and choose a supported SQL or metadata operation.";
            default:
                return "Inspect runtime status and visible metadata before deciding whether to retry.";
        }
    }
    
    private static List<Map<String, Object>> createNextActions(final String category) {
        switch (category) {
            case MCPDiagnosticCategory.SQL_SYNTAX_ERROR:
                return List.of(MCPNextActionUtils.askUser("Ask for one corrected SQL statement.", List.of("sql")));
            case MCPDiagnosticCategory.OBJECT_NOT_VISIBLE:
                return List.of(MCPNextActionUtils.readResource("shardingsphere://databases", "Read visible logical databases and metadata before retrying."));
            case MCPDiagnosticCategory.INSUFFICIENT_PRIVILEGES:
                return List.of(MCPNextActionUtils.askUser("Ask the operator to verify database privileges outside MCP.", List.of("database_privileges")));
            case MCPDiagnosticCategory.UNSUPPORTED_DATABASE_CAPABILITY:
                return List.of(MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read supported SQL and metadata capabilities before retrying."));
            default:
                return List.of(MCPNextActionUtils.readResource("shardingsphere://runtime", "Read runtime status before retrying."));
        }
    }
    
    private static boolean usesResourceHint(final String category) {
        return MCPDiagnosticCategory.OBJECT_NOT_VISIBLE.equals(category) || MCPDiagnosticCategory.CONNECTION_INTERRUPTED.equals(category)
                || MCPDiagnosticCategory.EXECUTION_TIMEOUT.equals(category) || MCPDiagnosticCategory.UNSUPPORTED_DATABASE_CAPABILITY.equals(category)
                || MCPDiagnosticCategory.QUERY_FAILED.equals(category);
    }
    
    private static List<Map<String, Object>> createResourcesToRead(final String category) {
        if (MCPDiagnosticCategory.OBJECT_NOT_VISIBLE.equals(category)) {
            return MCPRecoveryPayloadSupport.createResourceHintList("shardingsphere://databases", "logical-database", "Read visible metadata before retrying.");
        }
        return MCPDiagnosticCategory.UNSUPPORTED_DATABASE_CAPABILITY.equals(category)
                ? MCPRecoveryPayloadSupport.createResourceHintList("shardingsphere://capabilities", "capability", "Read supported capabilities before retrying.")
                : MCPRecoveryPayloadSupport.createResourceHintList("shardingsphere://runtime", "runtime", "Read runtime status before retrying.");
    }
}

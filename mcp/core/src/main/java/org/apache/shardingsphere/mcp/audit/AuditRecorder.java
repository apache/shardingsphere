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

package org.apache.shardingsphere.mcp.audit;

import org.apache.shardingsphere.mcp.protocol.MCPError.MCPErrorCode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * Record MCP audit events for metadata and query activity.
 */
public final class AuditRecorder {
    
    /**
     * Record one query-execution audit event.
     *
     * @param sessionId session identifier
     * @param databaseName logical database name
     * @param sql SQL text
     * @param success success flag
     * @param transactionMarker optional transaction marker
     * @return recorded audit entry
     */
    public AuditRecord recordQueryExecution(final String sessionId, final String databaseName, final String sql, final boolean success, final String transactionMarker) {
        return record(sessionId, databaseName, sql, success, false, MCPErrorCode.INVALID_REQUEST, transactionMarker);
    }
    
    /**
     * Record one query-execution audit event with one error code.
     *
     * @param sessionId session identifier
     * @param databaseName logical database name
     * @param sql SQL text
     * @param success success flag
     * @param errorCode error code
     * @param transactionMarker optional transaction marker
     * @return recorded audit entry
     */
    public AuditRecord recordQueryExecution(final String sessionId, final String databaseName, final String sql, final boolean success,
                                            final MCPErrorCode errorCode, final String transactionMarker) {
        return record(sessionId, databaseName, sql, success, true, errorCode, transactionMarker);
    }
    
    private AuditRecord record(final String sessionId, final String databaseName, final String operationSource,
                               final boolean success, final boolean errorCodePresent, final MCPErrorCode errorCode, final String transactionMarker) {
        return new AuditRecord(sessionId, databaseName, OperationClass.QUERY_EXECUTION, digest(operationSource), success, errorCodePresent, errorCode, transactionMarker, Instant.now().toString());
    }
    
    private String digest(final String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] actualDigest = messageDigest.digest(value.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(actualDigest.length * 2);
            for (byte each : actualDigest) {
                result.append(String.format("%02x", each));
            }
            return result.toString();
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }
}

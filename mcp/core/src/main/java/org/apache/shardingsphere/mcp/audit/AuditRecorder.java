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

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Record MCP audit events for metadata and query activity.
 */
public final class AuditRecorder {
    
    private final List<AuditRecord> records = Collections.synchronizedList(new LinkedList<>());
    
    /**
     * Record one resource-read audit event.
     *
     * @param sessionId session identifier
     * @param database logical database name
     * @param resourcePath resource path
     * @param success success flag
     * @param transactionMarker optional transaction marker
     * @return recorded audit entry
     */
    public AuditRecord recordResourceRead(final String sessionId, final String database, final String resourcePath, final boolean success,
                                          final String transactionMarker) {
        return record(sessionId, database, OperationClass.RESOURCE_READ, resourcePath, success, false, ErrorCode.INVALID_REQUEST, transactionMarker);
    }
    
    /**
     * Record one resource-read audit event with one error code.
     *
     * @param sessionId session identifier
     * @param database logical database name
     * @param resourcePath resource path
     * @param success success flag
     * @param errorCode error code
     * @param transactionMarker optional transaction marker
     * @return recorded audit entry
     */
    public AuditRecord recordResourceRead(final String sessionId, final String database, final String resourcePath, final boolean success,
                                          final ErrorCode errorCode, final String transactionMarker) {
        return record(sessionId, database, OperationClass.RESOURCE_READ, resourcePath, success, true, Objects.requireNonNull(errorCode, "errorCode cannot be null"), transactionMarker);
    }
    
    /**
     * Record one metadata-tool audit event.
     *
     * @param sessionId session identifier
     * @param database logical database name
     * @param toolCallSummary tool call summary
     * @param success success flag
     * @param transactionMarker optional transaction marker
     * @return recorded audit entry
     */
    public AuditRecord recordMetadataTool(final String sessionId, final String database, final String toolCallSummary, final boolean success,
                                          final String transactionMarker) {
        return record(sessionId, database, OperationClass.METADATA_TOOL, toolCallSummary, success, false, ErrorCode.INVALID_REQUEST, transactionMarker);
    }
    
    /**
     * Record one metadata-tool audit event with one error code.
     *
     * @param sessionId session identifier
     * @param database logical database name
     * @param toolCallSummary tool call summary
     * @param success success flag
     * @param errorCode error code
     * @param transactionMarker optional transaction marker
     * @return recorded audit entry
     */
    public AuditRecord recordMetadataTool(final String sessionId, final String database, final String toolCallSummary, final boolean success,
                                          final ErrorCode errorCode, final String transactionMarker) {
        return record(sessionId, database, OperationClass.METADATA_TOOL, toolCallSummary, success, true, Objects.requireNonNull(errorCode, "errorCode cannot be null"), transactionMarker);
    }
    
    /**
     * Record one query-execution audit event.
     *
     * @param sessionId session identifier
     * @param database logical database name
     * @param sql SQL text
     * @param success success flag
     * @param transactionMarker optional transaction marker
     * @return recorded audit entry
     */
    public AuditRecord recordQueryExecution(final String sessionId, final String database, final String sql, final boolean success,
                                            final String transactionMarker) {
        return record(sessionId, database, OperationClass.QUERY_EXECUTION, sql, success, false, ErrorCode.INVALID_REQUEST, transactionMarker);
    }
    
    /**
     * Record one query-execution audit event with one error code.
     *
     * @param sessionId session identifier
     * @param database logical database name
     * @param sql SQL text
     * @param success success flag
     * @param errorCode error code
     * @param transactionMarker optional transaction marker
     * @return recorded audit entry
     */
    public AuditRecord recordQueryExecution(final String sessionId, final String database, final String sql, final boolean success,
                                            final ErrorCode errorCode, final String transactionMarker) {
        return record(sessionId, database, OperationClass.QUERY_EXECUTION, sql, success, true, Objects.requireNonNull(errorCode, "errorCode cannot be null"), transactionMarker);
    }
    
    /**
     * Get a stable snapshot of audit records.
     *
     * @return immutable audit record snapshot
     */
    public List<AuditRecord> snapshot() {
        synchronized (records) {
            return Collections.unmodifiableList(new LinkedList<>(records));
        }
    }
    
    private AuditRecord record(final String sessionId, final String database, final OperationClass operationClass, final String operationSource,
                               final boolean success, final boolean errorCodePresent, final ErrorCode errorCode, final String transactionMarker) {
        AuditRecord result = new AuditRecord(sessionId, database, operationClass, digest(operationSource), success, errorCodePresent, errorCode, transactionMarker, Instant.now().toString());
        records.add(result);
        return result;
    }
    
    private String digest(final String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] actualDigest = messageDigest.digest(Objects.requireNonNull(value, "value cannot be null").trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(actualDigest.length * 2);
            for (byte each : actualDigest) {
                result.append(String.format("%02x", each));
            }
            return result.toString();
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }
    
    /**
     * Audit operation classes.
     */
    public enum OperationClass {
        
        RESOURCE_READ, METADATA_TOOL, QUERY_EXECUTION
    }
    
    /**
     * Audit record projection.
     */
    @Getter
    public static final class AuditRecord {
        
        private final String sessionId;
        
        private final String database;
        
        private final OperationClass operationClass;
        
        private final String operationDigest;
        
        private final boolean success;
        
        @Getter(AccessLevel.NONE)
        private final boolean errorCodePresent;
        
        @Getter(AccessLevel.NONE)
        private final ErrorCode errorCode;
        
        private final String transactionMarker;
        
        private final String timestamp;
        
        /**
         * Construct an audit record projection.
         *
         * @param sessionId session identifier
         * @param database logical database name
         * @param operationClass operation class
         * @param operationDigest operation digest
         * @param success success flag
         * @param errorCodePresent error-code presence flag
         * @param errorCode error code
         * @param transactionMarker transaction marker
         * @param timestamp timestamp
         */
        public AuditRecord(final String sessionId, final String database, final OperationClass operationClass, final String operationDigest,
                           final boolean success, final boolean errorCodePresent, final ErrorCode errorCode, final String transactionMarker, final String timestamp) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null");
            this.database = Objects.requireNonNull(database, "database cannot be null");
            this.operationClass = Objects.requireNonNull(operationClass, "operationClass cannot be null");
            this.operationDigest = Objects.requireNonNull(operationDigest, "operationDigest cannot be null");
            this.success = success;
            this.errorCodePresent = errorCodePresent;
            this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
            this.transactionMarker = Objects.requireNonNull(transactionMarker, "transactionMarker cannot be null");
            this.timestamp = Objects.requireNonNull(timestamp, "timestamp cannot be null");
        }
        
        /**
         * Get the error code when one exists.
         *
         * @return optional error code
         */
        public Optional<ErrorCode> getErrorCode() {
            return errorCodePresent ? Optional.of(errorCode) : Optional.empty();
        }
    }
}

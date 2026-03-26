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
import org.apache.shardingsphere.mcp.protocol.ErrorCode;

import java.util.Optional;

/**
 * Audit record projection.
 */
@Getter
public final class AuditRecord {
    
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
        this.sessionId = sessionId;
        this.database = database;
        this.operationClass = operationClass;
        this.operationDigest = operationDigest;
        this.success = success;
        this.errorCodePresent = errorCodePresent;
        this.errorCode = errorCode;
        this.transactionMarker = transactionMarker;
        this.timestamp = timestamp;
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

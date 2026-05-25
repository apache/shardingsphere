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

package org.apache.shardingsphere.mcp.core.tool.handler.execute.trace;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * Create SQL execution trace records.
 */
public final class SQLExecutionTraceFactory {
    
    /**
     * Create one SQL execution trace record.
     *
     * @param sessionId session identifier
     * @param databaseName logical database name
     * @param sql SQL text
     * @param success success flag
     * @param statementMarker optional statement marker
     * @return SQL execution trace record
     */
    public SQLExecutionTraceRecord create(final String sessionId, final String databaseName, final String sql, final boolean success, final String statementMarker) {
        return new SQLExecutionTraceRecord(sessionId, databaseName, digest(sql), success, statementMarker, Instant.now().toString());
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

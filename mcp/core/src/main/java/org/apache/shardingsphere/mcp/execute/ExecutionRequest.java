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

package org.apache.shardingsphere.mcp.execute;

import lombok.Getter;

import java.util.Objects;

/**
 * Execute-query request contract.
 */
@Getter
public final class ExecutionRequest {
    
    private final String sessionId;
    
    private final String database;
    
    private final String databaseType;
    
    private final String schema;
    
    private final String sql;
    
    private final int maxRows;
    
    private final int timeoutMs;
    
    private final DatabaseRuntime databaseRuntime;
    
    /**
     * Construct an execute-query request.
     *
     * @param sessionId session identifier
     * @param database logical database name
     * @param databaseType database type
     * @param schema schema name
     * @param sql SQL text
     * @param maxRows max rows
     * @param timeoutMs timeout milliseconds
     * @param databaseRuntime database runtime
     */
    public ExecutionRequest(final String sessionId, final String database, final String databaseType, final String schema,
                            final String sql, final int maxRows, final int timeoutMs, final DatabaseRuntime databaseRuntime) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null");
        this.database = Objects.requireNonNull(database, "database cannot be null");
        this.databaseType = Objects.requireNonNull(databaseType, "databaseType cannot be null");
        this.schema = Objects.requireNonNull(schema, "schema cannot be null");
        this.sql = Objects.requireNonNull(sql, "sql cannot be null");
        this.maxRows = maxRows;
        this.timeoutMs = timeoutMs;
        this.databaseRuntime = Objects.requireNonNull(databaseRuntime, "databaseRuntime cannot be null");
    }
}

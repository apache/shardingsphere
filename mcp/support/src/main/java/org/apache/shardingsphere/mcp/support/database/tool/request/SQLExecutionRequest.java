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

package org.apache.shardingsphere.mcp.support.database.tool.request;

import lombok.Getter;

/**
 * SQL execution request.
 *
 * <p>The logical database is the only strong execution boundary.
 * The optional schema field is a namespace hint for unqualified object names.</p>
 */
@Getter
public final class SQLExecutionRequest {

    private final String sessionId;

    private final String database;

    private final String schema;

    private final String sql;

    private final int maxRows;

    private final int timeoutMs;

    private final boolean readOnlyExecution;

    public SQLExecutionRequest(final String sessionId, final String database, final String schema, final String sql, final int maxRows, final int timeoutMs) {
        this(sessionId, database, schema, sql, maxRows, timeoutMs, false);
    }

    public SQLExecutionRequest(final String sessionId, final String database, final String schema, final String sql, final int maxRows, final int timeoutMs, final boolean readOnlyExecution) {
        this.sessionId = sessionId;
        this.database = database;
        this.schema = schema;
        this.sql = sql;
        this.maxRows = maxRows;
        this.timeoutMs = timeoutMs;
        this.readOnlyExecution = readOnlyExecution;
    }
}

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

package org.apache.shardingsphere.mcp.support.diagnostic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Stable MCP diagnostic category names.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPDiagnosticCategory {
    
    public static final String NO_RUNTIME_DATABASE = "no_runtime_database";
    
    public static final String UNKNOWN_DATABASE = "unknown_database";
    
    public static final String DATABASE_NOT_VISIBLE = "database_not_visible";
    
    public static final String SCHEMA_NOT_VISIBLE = "schema_not_visible";
    
    public static final String OBJECT_NOT_VISIBLE = "object_not_visible";
    
    public static final String INSUFFICIENT_PRIVILEGES = "insufficient_privileges";
    
    public static final String EMPTY_SCOPE = "empty_scope";
    
    public static final String NOT_FOUND = "not_found";
    
    public static final String ORIGIN_NOT_ALLOWED = "origin_not_allowed";
    
    public static final String SESSION_ATTRIBUTION_MISMATCH = "session_attribution_mismatch";
    
    public static final String SQL_SYNTAX_ERROR = "sql_syntax_error";
    
    public static final String EXECUTION_TIMEOUT = "execution_timeout";
    
    public static final String CONNECTION_INTERRUPTED = "connection_interrupted";
    
    public static final String UNSUPPORTED_DATABASE_CAPABILITY = "unsupported_database_capability";
    
    public static final String QUERY_FAILED = "query_failed";
}

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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPDiagnosticCategoryTest {
    
    @Test
    void assertCategories() {
        List<String> actual = List.of(
                MCPDiagnosticCategory.NO_RUNTIME_DATABASE,
                MCPDiagnosticCategory.UNKNOWN_DATABASE,
                MCPDiagnosticCategory.DATABASE_NOT_VISIBLE,
                MCPDiagnosticCategory.SCHEMA_NOT_VISIBLE,
                MCPDiagnosticCategory.OBJECT_NOT_VISIBLE,
                MCPDiagnosticCategory.INSUFFICIENT_PRIVILEGES,
                MCPDiagnosticCategory.EMPTY_SCOPE,
                MCPDiagnosticCategory.NOT_FOUND,
                MCPDiagnosticCategory.ORIGIN_NOT_ALLOWED,
                MCPDiagnosticCategory.SESSION_ATTRIBUTION_MISMATCH,
                MCPDiagnosticCategory.SQL_SYNTAX_ERROR,
                MCPDiagnosticCategory.EXECUTION_TIMEOUT,
                MCPDiagnosticCategory.CONNECTION_INTERRUPTED,
                MCPDiagnosticCategory.UNSUPPORTED_DATABASE_CAPABILITY,
                MCPDiagnosticCategory.QUERY_FAILED);
        assertThat(actual, is(List.of(
                "no_runtime_database",
                "unknown_database",
                "database_not_visible",
                "schema_not_visible",
                "object_not_visible",
                "insufficient_privileges",
                "empty_scope",
                "not_found",
                "origin_not_allowed",
                "session_attribution_mismatch",
                "sql_syntax_error",
                "execution_timeout",
                "connection_interrupted",
                "unsupported_database_capability",
                "query_failed")));
    }
}

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

package org.apache.shardingsphere.mcp.core.tool.response;

import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.database.tool.result.RuntimeDatabaseValidationCheckResult;
import org.apache.shardingsphere.mcp.support.database.tool.result.RuntimeDatabaseValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RuntimeDatabaseValidationResponseTest {
    
    @Test
    void assertReady() {
        Map<String, Object> actual = RuntimeDatabaseValidationResponse.from(RuntimeDatabaseValidationResult.ready(
                "logic_db", List.of(RuntimeDatabaseValidationCheckResult.passed("configuration", "Resolved the configured runtime database.")))).toPayload();
        assertThat(actual, is(Map.of(
                "response_mode", "validation",
                "summary", "Runtime database `logic_db` passed validation.",
                "status", "ready",
                "database", "logic_db",
                "checks", List.of(Map.of(
                        "name", "configuration",
                        "status", "passed",
                        "category", "ready",
                        "message", "Resolved the configured runtime database.")),
                "category", "ready",
                "recovery", Map.of())));
    }
    
    @Test
    void assertFailed() {
        Map<String, Object> actual = RuntimeDatabaseValidationResponse.from(RuntimeDatabaseValidationResult.failed(
                "logic_db", List.of(RuntimeDatabaseValidationCheckResult.failed("configuration", "invalid_configuration", "Database is not configured.")),
                RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION)).toPayload();
        assertThat(actual.get("summary"), is("Runtime database `logic_db` failed validation with category `invalid_configuration`."));
        assertThat(actual.get("status"), is("failed"));
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("database"), is("logic_db"));
        assertThat(actualRecovery.get("category"), is("invalid_configuration"));
        assertFalse(actualRecovery.containsKey("request_id"));
        assertThat(actual.get("next_actions"), is(actualRecovery.get("next_actions")));
    }
    
    @Test
    void assertFailedWithoutDatabase() {
        Map<String, Object> actual = RuntimeDatabaseValidationResponse.from(RuntimeDatabaseValidationResult.failed(
                "", List.of(RuntimeDatabaseValidationCheckResult.failed("configuration", "invalid_configuration", "Database is required.")),
                RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION)).toPayload();
        assertThat(actual.get("database"), is(""));
        assertFalse(((Map<?, ?>) actual.get("recovery")).containsKey("database"));
    }
    
    @Test
    void assertStablePayload() {
        RuntimeDatabaseValidationResponse response = RuntimeDatabaseValidationResponse.from(RuntimeDatabaseValidationResult.failed(
                "logic_db", List.of(RuntimeDatabaseValidationCheckResult.failed("database_visibility", "database_not_visible", "Database is not visible.")),
                RuntimeDatabaseConnectionException.CATEGORY_DATABASE_NOT_VISIBLE));
        Map<String, Object> actual = response.toPayload();
        assertThat(response.toPayload(), is(actual));
    }
}

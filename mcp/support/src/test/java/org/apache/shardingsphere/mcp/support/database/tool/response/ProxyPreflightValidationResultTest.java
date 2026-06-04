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

package org.apache.shardingsphere.mcp.support.database.tool.response;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProxyPreflightValidationResultTest {
    
    @Test
    void assertReady() {
        ProxyPreflightValidationResult actual = ProxyPreflightValidationResult.ready("logic_db", List.of(ProxyPreflightCheckResult.passed("configuration", "Validated the request.")));
        assertThat(actual.getStatus(), is("ready"));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getCategory(), is("ready"));
        assertThat(actual.getRecovery(), is(Map.of()));
    }
    
    @Test
    void assertFailed() {
        Map<String, Object> recovery = Map.of("category", "connection_failed");
        ProxyPreflightValidationResult actual = ProxyPreflightValidationResult.failed("logic_db",
                List.of(ProxyPreflightCheckResult.failed("jdbc_connectivity", "connection_failed", "Failed to open a JDBC connection.")), "connection_failed", recovery);
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getCategory(), is("connection_failed"));
        assertThat(actual.getRecovery(), is(recovery));
    }
    
    @Test
    void assertToPayload() {
        Map<String, Object> actual = ProxyPreflightValidationResult.failed("logic_db",
                List.of(ProxyPreflightCheckResult.failed("metadata_read", "connection_failed", "Failed to read metadata.")),
                "connection_failed", Map.of("category", "connection_failed")).toPayload();
        assertThat(actual.get("response_mode"), is("validation"));
        assertThat(actual.get("status"), is("failed"));
        assertThat(actual.get("database"), is("logic_db"));
        assertThat(actual.get("category"), is("connection_failed"));
        assertThat(actual.get("recovery"), is(Map.of("category", "connection_failed")));
        assertThat(actual.get("checks"), is(List.of(Map.of(
                "name", "metadata_read",
                "status", "failed",
                "category", "connection_failed",
                "message", "Failed to read metadata."))));
    }
}

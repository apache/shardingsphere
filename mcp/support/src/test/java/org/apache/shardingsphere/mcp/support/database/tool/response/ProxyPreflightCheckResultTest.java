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

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProxyPreflightCheckResultTest {
    
    @Test
    void assertPassed() {
        ProxyPreflightCheckResult actual = ProxyPreflightCheckResult.passed("jdbc_connectivity", "Opened a JDBC connection.");
        assertThat(actual.getName(), is("jdbc_connectivity"));
        assertThat(actual.getStatus(), is("passed"));
        assertThat(actual.getCategory(), is("ready"));
        assertThat(actual.getMessage(), is("Opened a JDBC connection."));
    }
    
    @Test
    void assertFailed() {
        ProxyPreflightCheckResult actual = ProxyPreflightCheckResult.failed("jdbc_driver", "missing_jdbc_driver", "Failed to load the configured JDBC driver.");
        assertThat(actual.getName(), is("jdbc_driver"));
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getCategory(), is("missing_jdbc_driver"));
        assertThat(actual.getMessage(), is("Failed to load the configured JDBC driver."));
    }
    
    @Test
    void assertSkipped() {
        ProxyPreflightCheckResult actual = ProxyPreflightCheckResult.skipped("database_visibility", "Skipped because no database was provided.");
        assertThat(actual.getName(), is("database_visibility"));
        assertThat(actual.getStatus(), is("skipped"));
        assertThat(actual.getCategory(), is("skipped"));
        assertThat(actual.getMessage(), is("Skipped because no database was provided."));
    }
    
    @Test
    void assertToPayload() {
        Map<String, Object> actual = ProxyPreflightCheckResult.failed("metadata_read", "connection_failed", "Failed to read metadata.").toPayload();
        assertThat(actual, is(Map.of(
                "name", "metadata_read",
                "status", "failed",
                "category", "connection_failed",
                "message", "Failed to read metadata.")));
    }
}

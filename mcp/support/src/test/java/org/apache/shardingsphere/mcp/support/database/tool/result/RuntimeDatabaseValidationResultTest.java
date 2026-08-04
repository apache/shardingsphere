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

package org.apache.shardingsphere.mcp.support.database.tool.result;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class RuntimeDatabaseValidationResultTest {
    
    @Test
    void assertReady() {
        List<RuntimeDatabaseValidationCheckResult> checks = List.of(RuntimeDatabaseValidationCheckResult.passed("configuration", "Validated the request."));
        RuntimeDatabaseValidationResult actual = RuntimeDatabaseValidationResult.ready("logic_db", checks);
        assertThat(actual.getStatus(), is("ready"));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getChecks(), is(checks));
        assertThat(actual.getCategory(), is("ready"));
    }
    
    @Test
    void assertFailed() {
        List<RuntimeDatabaseValidationCheckResult> checks = List.of(
                RuntimeDatabaseValidationCheckResult.failed("jdbc_connectivity", "connection_failed", "Failed to open a JDBC connection."));
        RuntimeDatabaseValidationResult actual = RuntimeDatabaseValidationResult.failed("logic_db", checks, "connection_failed");
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getChecks(), is(checks));
        assertThat(actual.getCategory(), is("connection_failed"));
    }
}

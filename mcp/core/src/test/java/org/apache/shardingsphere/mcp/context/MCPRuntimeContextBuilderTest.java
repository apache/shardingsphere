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

package org.apache.shardingsphere.mcp.context;

import org.apache.shardingsphere.mcp.jdbc.H2RuntimeTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPRuntimeContextBuilderTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertBuild() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "runtime-context-assembler");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        MCPRuntimeContextBuilder builder = new MCPRuntimeContextBuilder();
        MCPRuntimeContext actual = builder.build(H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl));
        assertNotNull(actual.getSessionManager());
        assertNotNull(actual.getSessionExecutionCoordinator());
        assertThat(actual.getDatabaseMetadataSnapshots().getDatabaseTypes().get("logic_db"), is("H2"));
    }
    
    @Test
    void assertBuildWithEmptyRuntimeDatabases() {
        MCPRuntimeContextBuilder builder = new MCPRuntimeContextBuilder();
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> builder.build(Map.of()));
        assertThat(actual.getMessage(), is("At least one runtime database must be configured."));
    }
}

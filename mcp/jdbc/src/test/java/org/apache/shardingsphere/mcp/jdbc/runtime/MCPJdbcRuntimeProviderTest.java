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

package org.apache.shardingsphere.mcp.jdbc.runtime;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPJdbcRuntimeProviderTest {
    
    @TempDir
    private Path tempDir;
    
    private final MCPJdbcRuntimeContextFactory runtimeContextFactory = new MCPJdbcRuntimeContextFactory();
    
    @Test
    void assertCreate() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "provider-runtime-context");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        MCPSessionManager sessionManager = new MCPSessionManager();
        
        MCPRuntimeContext actual = runtimeContextFactory.create(sessionManager, H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl));
        
        assertThat(actual.getSessionManager(), is(sessionManager));
        assertThat(actual.getMetadataCatalog().getDatabaseTypes().get("logic_db"), is("H2"));
    }
    
    @Test
    void assertCreateWithEmptyRuntimeDatabases() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> runtimeContextFactory.create(new MCPSessionManager(), Map.of()));
        
        assertThat(actual.getMessage(), is("At least one runtime database must be configured."));
    }
}

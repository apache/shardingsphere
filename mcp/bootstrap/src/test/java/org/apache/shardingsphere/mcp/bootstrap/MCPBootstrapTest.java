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

package org.apache.shardingsphere.mcp.bootstrap;

import org.apache.shardingsphere.mcp.bootstrap.runtime.H2RuntimeTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MCPBootstrapTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertMain() throws IOException {
        Path configFile = createLegacyConfigFile();
        
        assertDoesNotThrow(() -> MCPBootstrap.main(new String[]{configFile.toString()}));
    }
    
    @Test
    void assertMainWithRuntimeDatabases() throws IOException {
        Path configFile = createRuntimeDatabasesConfigFile();
        
        assertDoesNotThrow(() -> MCPBootstrap.main(new String[]{configFile.toString()}));
    }
    
    private Path createLegacyConfigFile() throws IOException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "bootstrap");
        initializeDatabase(jdbcUrl);
        Path result = tempDir.resolve("mcp.yaml");
        Files.writeString(result, "transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "runtime:\n"
                + "  props:\n"
                + "    databaseName: logic_db\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: '" + jdbcUrl + "'\n"
                + "    driverClassName: org.h2.Driver\n"
                + "    supportsCrossSchemaSql: true\n"
                + "    supportsExplainAnalyze: false\n");
        return result;
    }
    
    private Path createRuntimeDatabasesConfigFile() throws IOException {
        String firstJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "bootstrap-orders");
        String secondJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "bootstrap-analytics");
        initializeDatabase(firstJdbcUrl);
        initializeDatabase(secondJdbcUrl);
        Path result = tempDir.resolve("mcp-runtime-databases.yaml");
        Files.writeString(result, "transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "runtime:\n"
                + "  databaseDefaults:\n"
                + "    databaseType: H2\n"
                + "    driverClassName: org.h2.Driver\n"
                + "  databases:\n"
                + "    orders:\n"
                + "      jdbcUrl: '" + firstJdbcUrl + "'\n"
                + "    analytics:\n"
                + "      jdbcUrl: '" + secondJdbcUrl + "'\n");
        return result;
    }
    
    private void initializeDatabase(final String jdbcUrl) {
        try {
            H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

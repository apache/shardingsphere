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

package org.apache.shardingsphere.mcp.bootstrap.lifecycle;

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.TransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.runtime.H2RuntimeTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionRuntimeLauncherTest {
    
    @TempDir
    private Path tempDir;
    
    private LaunchState launchState;
    
    @AfterEach
    void tearDown() {
        if (null != launchState) {
            if (launchState.getHttpServer().isPresent()) {
                launchState.getHttpServer().get().stop();
            }
            if (launchState.getStdioServer().isPresent()) {
                launchState.getStdioServer().get().stop();
            }
            launchState.getServerRegistry().stop();
            launchState = null;
        }
    }
    
    @Test
    void assertLaunchWithH2Runtime() throws SQLException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "launcher");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        launchState = runtimeLauncher.launch(new MCPLaunchConfiguration(createTransportConfiguration(false, true, "/mcp"),
                H2RuntimeTestSupport.createRuntimeProps("logic_db", jdbcUrl), Map.of()));
        assertTrue(launchState.getStdioServer().isPresent());
        assertTrue(launchState.getRuntimeServices().getCapabilityAssembler().assembleDatabaseCapability("logic_db", "H2").isPresent());
        assertTrue(launchState.getRuntimeServices().getCapabilityAssembler().assembleServiceCapability().getSupportedTools().contains("execute_query"));
    }
    
    @Test
    void assertLaunchWithInvalidRuntimeProps() {
        Properties props = new Properties();
        props.setProperty("databaseType", "H2");
        MCPRuntimeLauncher runtimeLauncher = new MCPRuntimeLauncher();
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> runtimeLauncher.launch(new MCPLaunchConfiguration(createTransportConfiguration(false, true, "/mcp"),
                        props, Map.of())));
        assertThat(actual.getMessage(), is("Runtime property `databaseName` is required."));
    }
    
    private TransportConfiguration createTransportConfiguration(final boolean httpEnabled, final boolean stdioEnabled, final String endpointPath) {
        return new TransportConfiguration(new HttpTransportConfiguration(httpEnabled, "127.0.0.1", 0, endpointPath), new StdioTransportConfiguration(stdioEnabled));
    }
}

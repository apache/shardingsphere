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

package org.apache.shardingsphere.mcp.bootstrap.runtime;

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.TransportConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPLaunchRuntimeLoaderTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertLoad() throws SQLException {
        MCPLaunchRuntimeLoader launchRuntimeLoader = new MCPLaunchRuntimeLoader();
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "production-runtime-loader");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        LoadedRuntime actual = launchRuntimeLoader.load(createRuntimeConfiguration(H2RuntimeTestSupport.createRuntimeProps("logic_db", jdbcUrl)));
        assertThat(actual.getMetadataCatalog().getDatabaseTypes().size(), is(1));
        assertTrue(actual.getMetadataCatalog().getDatabaseTypes().containsKey("logic_db"));
        assertThat(actual.getMetadataCatalog().getMetadataObjects().size(), greaterThan(0));
    }
    
    @Test
    void assertLoadWithRuntimeDatabases() throws SQLException {
        MCPLaunchRuntimeLoader launchRuntimeLoader = new MCPLaunchRuntimeLoader();
        String firstJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "production-runtime-loader-first");
        String secondJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "production-runtime-loader-second");
        H2RuntimeTestSupport.initializeDatabase(firstJdbcUrl);
        H2RuntimeTestSupport.initializeDatabase(secondJdbcUrl);
        LoadedRuntime actual = launchRuntimeLoader.load(new MCPLaunchConfiguration(createTransportConfiguration(true, false, "/mcp"), new Properties(),
                Map.of(
                        "logic_db", createRuntimeDatabaseConfiguration(firstJdbcUrl),
                        "analytics_db", createRuntimeDatabaseConfiguration(secondJdbcUrl))));
        assertThat(actual.getMetadataCatalog().getDatabaseTypes().size(), is(2));
        assertTrue(actual.getMetadataCatalog().getDatabaseTypes().containsKey("logic_db"));
        assertTrue(actual.getMetadataCatalog().getDatabaseTypes().containsKey("analytics_db"));
    }
    
    @Test
    void assertLoadWithoutRuntimeConfiguration() {
        MCPLaunchRuntimeLoader launchRuntimeLoader = new MCPLaunchRuntimeLoader();
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> launchRuntimeLoader.load(new MCPLaunchConfiguration(createTransportConfiguration(true, false, "/mcp"),
                        new Properties(), Map.of())));
        assertThat(actual.getMessage(), is("MCP runtime properties or runtime databases must be configured for the default launch path."));
    }
    
    @Test
    void assertLoadWithInvalidRuntimeProps() {
        MCPLaunchRuntimeLoader launchRuntimeLoader = new MCPLaunchRuntimeLoader();
        Properties props = new Properties();
        props.setProperty("databaseType", "H2");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> launchRuntimeLoader.load(createRuntimeConfiguration(props)));
        assertThat(actual.getMessage(), is("Runtime property `databaseName` is required."));
    }
    
    @Test
    void assertLoadWithLegacyRuntimeProps() {
        MCPLaunchRuntimeLoader launchRuntimeLoader = new MCPLaunchRuntimeLoader();
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> launchRuntimeLoader.load(createRuntimeConfiguration(PropertiesBuilder.build(new Property("databaseNames", "logic_db")))));
        assertThat(actual.getMessage(),
                is("Runtime property `databaseNames` is no longer supported. Configure a single database with `databaseName`, `databaseType`, and `jdbcUrl`."));
    }
    
    private MCPLaunchConfiguration createRuntimeConfiguration(final Properties runtimeProps) {
        return new MCPLaunchConfiguration(createTransportConfiguration(true, false, "/mcp"), runtimeProps, Map.of());
    }
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String jdbcUrl) {
        return new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", "org.h2.Driver", "public", "public", true, false);
    }
    
    private TransportConfiguration createTransportConfiguration(final boolean httpEnabled, final boolean stdioEnabled, final String endpointPath) {
        return new TransportConfiguration(new HttpTransportConfiguration(httpEnabled, "127.0.0.1", 0, endpointPath), new StdioTransportConfiguration(stdioEnabled));
    }
}

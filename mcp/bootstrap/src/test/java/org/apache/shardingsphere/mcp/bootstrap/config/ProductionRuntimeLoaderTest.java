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

package org.apache.shardingsphere.mcp.bootstrap.config;

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mcp.bootstrap.lifecycle.MCPRuntimeLauncher.RuntimeConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.lifecycle.MCPRuntimeLauncher.RuntimeConfiguration.ServerConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.runtime.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.bootstrap.runtime.MCPRuntimeProvider.LoadedRuntime;
import org.apache.shardingsphere.mcp.bootstrap.runtime.ProductionRuntimeLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionRuntimeLoaderTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertLoad() throws SQLException {
        ProductionRuntimeLoader productionRuntimeLoader = new ProductionRuntimeLoader();
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "production-runtime-loader");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        LoadedRuntime actual = productionRuntimeLoader.load(createRuntimeConfiguration(H2RuntimeTestSupport.createRuntimeProps("logic_db", jdbcUrl)));
        assertThat(actual.getMetadataCatalog().getDatabaseTypes().size(), is(1));
        assertTrue(actual.getMetadataCatalog().getDatabaseTypes().containsKey("logic_db"));
        assertThat(actual.getMetadataCatalog().getMetadataObjects().size(), greaterThan(0));
    }
    
    @Test
    void assertLoadWithoutRuntimeProps() {
        ProductionRuntimeLoader productionRuntimeLoader = new ProductionRuntimeLoader();
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> productionRuntimeLoader.load(new RuntimeConfiguration(new ServerConfiguration("127.0.0.1", 0, "/mcp"), true, false)));
        assertThat(actual.getMessage(), is("MCP runtime properties are required for the default launch path."));
    }
    
    @Test
    void assertLoadWithInvalidRuntimeProps() {
        ProductionRuntimeLoader productionRuntimeLoader = new ProductionRuntimeLoader();
        Properties props = new Properties();
        props.setProperty("databaseType", "H2");
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> productionRuntimeLoader.load(createRuntimeConfiguration(props)));
        assertThat(actual.getMessage(), is("Failed to initialize MCP runtime from configured properties."));
        assertThat(actual.getCause().getMessage(), is("Runtime property `databaseName` is required."));
    }
    
    @Test
    void assertLoadWithLegacyRuntimeProps() {
        ProductionRuntimeLoader productionRuntimeLoader = new ProductionRuntimeLoader();
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> productionRuntimeLoader.load(createRuntimeConfiguration(PropertiesBuilder.build(new Property("databaseNames", "logic_db")))));
        assertThat(actual.getMessage(), is("Failed to initialize MCP runtime from configured properties."));
        assertThat(actual.getCause().getMessage(),
                is("Runtime property `databaseNames` is no longer supported. Configure a single database with `databaseName`, `databaseType`, and `jdbcUrl`."));
    }
    
    private RuntimeConfiguration createRuntimeConfiguration(final Properties runtimeProps) {
        return new RuntimeConfiguration(new ServerConfiguration("127.0.0.1", 0, "/mcp"), true, false, runtimeProps);
    }
}

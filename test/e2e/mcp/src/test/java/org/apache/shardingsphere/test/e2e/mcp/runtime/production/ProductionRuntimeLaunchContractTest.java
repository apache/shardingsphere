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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import org.apache.shardingsphere.mcp.bootstrap.MCPRuntimeLauncher;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionRuntimeLaunchContractTest {
    
    @Test
    void assertRejectLaunchWithoutRuntimeDatabases() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new MCPRuntimeLauncher().launch(createLaunchConfiguration(Map.of())));
        
        assertThat(actual.getMessage(), is("At least one runtime database must be configured."));
    }
    
    @Test
    void assertRejectLaunchWithInvalidJdbcDriver() {
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> new MCPRuntimeLauncher().launch(createLaunchConfiguration(Map.of(
                        "logic_db", new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:invalid-driver", "", "", "org.example.MissingDriver")))));
        
        assertThat(actual.getMessage(), is("JDBC driver `org.example.MissingDriver` is not available for database `logic_db`."));
    }
    
    private MCPLaunchConfiguration createLaunchConfiguration(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        return new MCPLaunchConfiguration(new HttpTransportConfiguration(true, "127.0.0.1", false, 0, "/gateway"), new StdioTransportConfiguration(false), runtimeDatabases);
    }
}

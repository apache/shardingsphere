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
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPLaunchConfigurationTest {
    
    @Test
    void assertGetRuntimeProps() {
        Properties runtimeProps = PropertiesBuilder.build(new Property("databaseName", "logic_db"));
        MCPLaunchConfiguration launchConfiguration = createLaunchConfiguration(runtimeProps, new RuntimeTopologyConfiguration(Map.of()));
        
        assertThat(launchConfiguration.getRuntimeProps().getProperty("databaseName"), is("logic_db"));
    }
    
    @Test
    void assertGetRuntimeTopologyConfiguration() {
        RuntimeTopologyConfiguration runtimeTopologyConfiguration = new RuntimeTopologyConfiguration(Map.of("logic_db",
                new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver", "public", "public", true, false)));
        MCPLaunchConfiguration launchConfiguration = createLaunchConfiguration(new Properties(), runtimeTopologyConfiguration);
        
        assertThat(launchConfiguration.getRuntimeTopologyConfiguration().getDatabases().get("logic_db").getDatabaseType(), is("H2"));
    }
    
    private MCPLaunchConfiguration createLaunchConfiguration(final Properties runtimeProps, final RuntimeTopologyConfiguration runtimeTopologyConfiguration) {
        return new MCPLaunchConfiguration(new HttpServerConfiguration("127.0.0.1", 0, "/mcp"), true, false, runtimeProps, runtimeTopologyConfiguration);
    }
}

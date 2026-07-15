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

package org.apache.shardingsphere.mcp.bootstrap.config.yaml.swapper;

import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlRuntimeDatabaseConfigurationSwapperTest {
    
    private final YamlRuntimeDatabaseConfigurationSwapper swapper = new YamlRuntimeDatabaseConfigurationSwapper();
    
    @Test
    void assertSwapToObject() {
        RuntimeDatabaseConfiguration actual = swapper.swapToObject(createYamlConfig());
        assertThat(actual.getJdbcUrl(), is("jdbc:mysql://localhost:3306/logic_db"));
        assertThat(actual.getUsername(), is(" demo "));
        assertThat(actual.getPassword(), is(" secret "));
        assertThat(actual.getDriverClassName(), is(" com.mysql.cj.jdbc.Driver "));
    }
    
    @Test
    void assertSwapToObjectWithJdbcUrlMissing() {
        YamlRuntimeDatabaseConfiguration yamlConfig = createYamlConfig();
        yamlConfig.setJdbcUrl(null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("MCP runtime database configuration property `jdbcUrl` is required."));
    }
    
    @Test
    void assertSwapToObjectWithBlankJdbcUrl() {
        YamlRuntimeDatabaseConfiguration yamlConfig = createYamlConfig();
        yamlConfig.setJdbcUrl("   ");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("MCP runtime database configuration property `jdbcUrl` is required."));
    }
    
    @Test
    void assertSwapToObjectWithUsernameMissing() {
        YamlRuntimeDatabaseConfiguration yamlConfig = createYamlConfig();
        yamlConfig.setUsername(null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("MCP runtime database configuration property `username` is required."));
    }
    
    @Test
    void assertSwapToObjectWithBlankUsername() {
        YamlRuntimeDatabaseConfiguration yamlConfig = createYamlConfig();
        yamlConfig.setUsername("   ");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("MCP runtime database configuration property `username` is required."));
    }
    
    @Test
    void assertSwapToObjectWithPasswordMissing() {
        YamlRuntimeDatabaseConfiguration yamlConfig = createYamlConfig();
        yamlConfig.setPassword(null);
        RuntimeDatabaseConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual.getPassword(), is(""));
    }
    
    @Test
    void assertSwapToObjectWithDriverClassNameMissing() {
        YamlRuntimeDatabaseConfiguration yamlConfig = createYamlConfig();
        yamlConfig.setDriverClassName(null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("MCP runtime database configuration property `driverClassName` is required."));
    }
    
    @Test
    void assertSwapToObjectWithBlankDriverClassName() {
        YamlRuntimeDatabaseConfiguration yamlConfig = createYamlConfig();
        yamlConfig.setDriverClassName("   ");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("MCP runtime database configuration property `driverClassName` is required."));
    }
    
    @Test
    void assertSwapToObjectWithNullConfiguration() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(null));
        assertThat(actual.getMessage(), is("MCP runtime database configuration cannot be null."));
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlRuntimeDatabaseConfiguration actual = swapper.swapToYamlConfiguration(
                new RuntimeDatabaseConfiguration("jdbc:mysql://localhost:3306/logic_db", "demo", "", "com.mysql.cj.jdbc.Driver"));
        assertThat(actual.getJdbcUrl(), is("jdbc:mysql://localhost:3306/logic_db"));
        assertThat(actual.getUsername(), is("demo"));
        assertThat(actual.getPassword(), is(""));
        assertThat(actual.getDriverClassName(), is("com.mysql.cj.jdbc.Driver"));
    }
    
    private YamlRuntimeDatabaseConfiguration createYamlConfig() {
        YamlRuntimeDatabaseConfiguration result = new YamlRuntimeDatabaseConfiguration();
        result.setJdbcUrl("jdbc:mysql://localhost:3306/logic_db");
        result.setUsername(" demo ");
        result.setPassword(" secret ");
        result.setDriverClassName(" com.mysql.cj.jdbc.Driver ");
        return result;
    }
}

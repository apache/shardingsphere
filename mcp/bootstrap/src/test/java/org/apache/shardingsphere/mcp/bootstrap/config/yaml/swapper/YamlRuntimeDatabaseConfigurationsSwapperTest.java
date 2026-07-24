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

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlRuntimeDatabaseConfigurationsSwapperTest {
    
    private final YamlRuntimeDatabaseConfigurationsSwapper swapper = new YamlRuntimeDatabaseConfigurationsSwapper();
    
    @Test
    void assertSwapToObject() {
        Map<String, RuntimeDatabaseConfiguration> actual = swapper.swapToObject(Map.of(
                "logic_db", createYamlRuntimeDatabaseConfiguration("jdbc:mysql://localhost:3306/logic_db", "demo", "", "com.mysql.cj.jdbc.Driver")));
        
        assertThat(actual.get("logic_db").getJdbcUrl(), is("jdbc:mysql://localhost:3306/logic_db"));
    }
    
    @Test
    void assertSwapToObjectWithPasswordMissing() {
        Map<String, RuntimeDatabaseConfiguration> actual = swapper.swapToObject(Map.of(
                "logic_db", createYamlRuntimeDatabaseConfiguration("jdbc:mysql://localhost:3306/logic_db", "demo", null, "com.mysql.cj.jdbc.Driver")));
        
        assertThat(actual.get("logic_db").getPassword(), is(""));
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeConfiguration() {
        Map<String, RuntimeDatabaseConfiguration> actual = swapper.swapToObject(null);
        
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeDatabaseConfiguration() {
        Map<String, YamlRuntimeDatabaseConfiguration> yamlRuntimeConfiguration = new LinkedHashMap<>(1, 1F);
        yamlRuntimeConfiguration.put("logic_db", null);
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlRuntimeConfiguration));
        
        assertThat(actual.getMessage(), is("MCP runtime database configuration cannot be null."));
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        Map<String, YamlRuntimeDatabaseConfiguration> actual = swapper.swapToYamlConfiguration(Map.of(
                "logic_db", new RuntimeDatabaseConfiguration("jdbc:mysql://localhost:3306/logic_db", "demo", "", "com.mysql.cj.jdbc.Driver")));
        
        assertThat(actual.get("logic_db").getDriverClassName(), is("com.mysql.cj.jdbc.Driver"));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithNullRuntimeDatabaseConfiguration() {
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = new LinkedHashMap<>(1, 1F);
        runtimeDatabases.put("logic_db", null);
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToYamlConfiguration(runtimeDatabases));
        
        assertThat(actual.getMessage(), is("Runtime database configuration cannot be null."));
    }
    
    private YamlRuntimeDatabaseConfiguration createYamlRuntimeDatabaseConfiguration(final String jdbcUrl, final String username, final String password, final String driverClassName) {
        YamlRuntimeDatabaseConfiguration result = new YamlRuntimeDatabaseConfiguration();
        result.setJdbcUrl(jdbcUrl);
        result.setUsername(username);
        result.setPassword(password);
        result.setDriverClassName(driverClassName);
        return result;
    }
}

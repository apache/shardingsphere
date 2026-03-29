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

package org.apache.shardingsphere.mcp.jdbc.config.yaml.swapper;

import org.apache.shardingsphere.mcp.runtime.RuntimeDatabaseConfiguration;
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
        Map<String, RuntimeDatabaseConfiguration> actual = swapper.swapToObject(Map.of("logic_db", Map.of(
                "databaseType", "H2",
                "jdbcUrl", "jdbc:h2:mem:logic",
                "username", "",
                "password", "",
                "driverClassName", "org.h2.Driver")));
        
        assertThat(actual.get("logic_db").getDatabaseType(), is("H2"));
        assertThat(actual.get("logic_db").getJdbcUrl(), is("jdbc:h2:mem:logic"));
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeConfiguration() {
        Map<String, RuntimeDatabaseConfiguration> actual = swapper.swapToObject(null);
        
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertSwapToObjectWithNullRuntimeDatabaseConfiguration() {
        Map<String, Map<String, Object>> yamlRuntimeConfiguration = new LinkedHashMap<>(1, 1F);
        yamlRuntimeConfiguration.put("logic_db", null);
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlRuntimeConfiguration));
        
        assertThat(actual.getMessage(), is("Runtime database configuration cannot be null."));
    }
    
    @Test
    void assertSwapToObjectWithUnsupportedRuntimeDatabaseProperty() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(Map.of("logic_db", Map.of(
                "databaseType", "H2",
                "jdbcUrl", "jdbc:h2:mem:logic",
                "username", "",
                "password", "",
                "driverClassName", "org.h2.Driver",
                "unsupported", true))));
        
        assertThat(actual.getMessage(), is("Unsupported runtime database property `unsupported`."));
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        Map<String, Map<String, Object>> actual = swapper.swapToYamlConfiguration(Map.of(
                "logic_db", new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver")));
        
        assertThat(String.valueOf(actual.get("logic_db").get("databaseType")), is("H2"));
        assertThat(String.valueOf(actual.get("logic_db").get("driverClassName")), is("org.h2.Driver"));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithNullRuntimeDatabaseConfiguration() {
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = new LinkedHashMap<>(1, 1F);
        runtimeDatabases.put("logic_db", null);
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToYamlConfiguration(runtimeDatabases));
        
        assertThat(actual.getMessage(), is("Runtime database configuration cannot be null."));
    }
}

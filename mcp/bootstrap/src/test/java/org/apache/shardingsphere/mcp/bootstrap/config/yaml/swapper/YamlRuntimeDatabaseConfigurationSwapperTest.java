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
import org.apache.shardingsphere.mcp.jdbc.config.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlRuntimeDatabaseConfigurationSwapperTest {
    
    private final YamlRuntimeDatabaseConfigurationSwapper swapper = new YamlRuntimeDatabaseConfigurationSwapper();
    
    @Test
    void assertSwapToObject() {
        RuntimeDatabaseConfiguration actual = swapper.swapToObject(createYamlConfig());
        assertThat(actual.getDatabaseType(), is("H2"));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:logic"));
        assertThat(actual.getUsername(), is(" demo "));
        assertThat(actual.getPassword(), is(" secret "));
        assertThat(actual.getDriverClassName(), is(" org.h2.Driver "));
    }
    
    @Test
    void assertSwapToObjectWithDatabaseTypeMissing() {
        YamlRuntimeDatabaseConfiguration yamlConfig = createYamlConfig();
        yamlConfig.setDatabaseType(null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("Runtime database property `databaseType` is required."));
    }
    
    @Test
    void assertSwapToObjectWithUsernameMissing() {
        YamlRuntimeDatabaseConfiguration yamlConfig = createYamlConfig();
        yamlConfig.setUsername(null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        assertThat(actual.getMessage(), is("Runtime database property `username` is required. Use an empty string when no value is needed."));
    }
    
    @Test
    void assertSwapToObjectWithNullConfiguration() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(null));
        assertThat(actual.getMessage(), is("Runtime database configuration cannot be null."));
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlRuntimeDatabaseConfiguration actual = swapper.swapToYamlConfiguration(
                new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver"));
        assertThat(actual.getDatabaseType(), is("H2"));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:logic"));
        assertThat(actual.getUsername(), is(""));
        assertThat(actual.getPassword(), is(""));
        assertThat(actual.getDriverClassName(), is("org.h2.Driver"));
    }
    
    private YamlRuntimeDatabaseConfiguration createYamlConfig() {
        YamlRuntimeDatabaseConfiguration result = new YamlRuntimeDatabaseConfiguration();
        result.setDatabaseType("H2");
        result.setJdbcUrl("jdbc:h2:mem:logic");
        result.setUsername(" demo ");
        result.setPassword(" secret ");
        result.setDriverClassName(" org.h2.Driver ");
        return result;
    }
}

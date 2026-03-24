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

import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlRuntimeConfigurationSwapperTest {
    
    private final YamlRuntimeConfigurationSwapper swapper = new YamlRuntimeConfigurationSwapper();
    
    @Test
    void assertSwapToObjectWithRuntimeDatabases() {
        YamlRuntimeDatabaseConfiguration databaseDefaults = new YamlRuntimeDatabaseConfiguration();
        databaseDefaults.setDatabaseType("H2");
        databaseDefaults.setDriverClassName("org.h2.Driver");
        YamlRuntimeConfiguration yamlConfig = new YamlRuntimeConfiguration();
        yamlConfig.setDatabaseDefaults(databaseDefaults);
        YamlRuntimeDatabaseConfiguration firstDatabaseConfig = new YamlRuntimeDatabaseConfiguration();
        firstDatabaseConfig.setJdbcUrl("jdbc:h2:mem:logic");
        yamlConfig.getDatabases().put(" logic_db ", firstDatabaseConfig);
        YamlRuntimeDatabaseConfiguration secondDatabaseConfig = new YamlRuntimeDatabaseConfiguration();
        secondDatabaseConfig.setJdbcUrl("jdbc:h2:mem:analytics");
        yamlConfig.getDatabases().put("analytics_db", secondDatabaseConfig);
        
        Map<String, RuntimeDatabaseConfiguration> actual = swapper.swapToObject(yamlConfig);
        
        assertThat(actual.get("logic_db").getDatabaseType(), is("H2"));
        assertThat(actual.get("logic_db").getDriverClassName(), is("org.h2.Driver"));
        assertThat(actual.get("analytics_db").getDatabaseType(), is("H2"));
    }
    
    @Test
    void assertSwapToObjectWithBlankDatabaseName() {
        YamlRuntimeConfiguration yamlConfig = new YamlRuntimeConfiguration();
        YamlRuntimeDatabaseConfiguration databaseDefaults = new YamlRuntimeDatabaseConfiguration();
        databaseDefaults.setDatabaseType("H2");
        databaseDefaults.setJdbcUrl("jdbc:h2:mem:logic");
        yamlConfig.setDatabaseDefaults(databaseDefaults);
        yamlConfig.getDatabases().put(" ", new YamlRuntimeDatabaseConfiguration());
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        
        assertThat(actual.getMessage(), is("Runtime logical database name cannot be blank."));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> databases = new LinkedHashMap<>(1, 1F);
        databases.put("logic_db", new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver"));
        YamlRuntimeConfiguration actual = swapper.swapToYamlConfiguration(databases);
        
        assertThat(actual.getDatabases().get("logic_db").getJdbcUrl(), is("jdbc:h2:mem:logic"));
    }
}

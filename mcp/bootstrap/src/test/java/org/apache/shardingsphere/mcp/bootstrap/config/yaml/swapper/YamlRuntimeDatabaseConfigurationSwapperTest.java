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
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlRuntimeDatabaseConfigurationSwapperTest {
    
    private final YamlRuntimeDatabaseConfigurationSwapper swapper = new YamlRuntimeDatabaseConfigurationSwapper();
    
    @Test
    void assertSwapToObject() {
        RuntimeDatabaseConfiguration actual = swapper.swapToObject(createYamlConfig());
        
        assertThat(actual.getDatabaseType(), is("H2"));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:logic"));
        assertThat(actual.getDriverClassName(), is("org.h2.Driver"));
        assertTrue(actual.isLegacySupportsCrossSchemaSqlConfigured());
        assertTrue(actual.isLegacySupportsCrossSchemaSql());
    }
    
    @Test
    void assertSwapToObjectWithRuntimeDefaults() {
        YamlRuntimeDatabaseConfiguration runtimeDefaults = new YamlRuntimeDatabaseConfiguration();
        runtimeDefaults.setDatabaseType("H2");
        runtimeDefaults.setJdbcUrl("jdbc:h2:mem:logic");
        runtimeDefaults.setDriverClassName("org.h2.Driver");
        Map<String, String> legacyRuntimeDefaults = new LinkedHashMap<>(1, 1F);
        legacyRuntimeDefaults.put("supportsCrossSchemaSql", "true");
        
        RuntimeDatabaseConfiguration actual = swapper.swapToObject("logic_db", new YamlRuntimeDatabaseConfiguration(), runtimeDefaults, legacyRuntimeDefaults);
        
        assertThat(actual.getDatabaseType(), is("H2"));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:logic"));
        assertThat(actual.getDriverClassName(), is("org.h2.Driver"));
        assertTrue(actual.isLegacySupportsCrossSchemaSqlConfigured());
        assertTrue(actual.isLegacySupportsCrossSchemaSql());
    }
    
    @Test
    void assertSwapToObjectWithExplicitBooleanOverridesRuntimeDefaults() {
        YamlRuntimeDatabaseConfiguration runtimeDefaults = new YamlRuntimeDatabaseConfiguration();
        runtimeDefaults.setDatabaseType("H2");
        runtimeDefaults.setJdbcUrl("jdbc:h2:mem:logic");
        runtimeDefaults.setDriverClassName("org.h2.Driver");
        Map<String, String> legacyRuntimeDefaults = new LinkedHashMap<>(1, 1F);
        legacyRuntimeDefaults.put("supportsCrossSchemaSql", "true");
        YamlRuntimeDatabaseConfiguration yamlConfig = new YamlRuntimeDatabaseConfiguration();
        yamlConfig.setSupportsCrossSchemaSql(false);
        
        RuntimeDatabaseConfiguration actual = swapper.swapToObject("logic_db", yamlConfig, runtimeDefaults, legacyRuntimeDefaults);
        
        assertTrue(actual.isLegacySupportsCrossSchemaSqlConfigured());
        assertFalse(actual.isLegacySupportsCrossSchemaSql());
    }
    
    @Test
    void assertSwapToObjectWithRequiredFieldMissing() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> swapper.swapToObject("logic_db", new YamlRuntimeDatabaseConfiguration(), new YamlRuntimeDatabaseConfiguration(), new LinkedHashMap<>()));
        
        assertThat(actual.getMessage(), is("Runtime database `logic_db` property `databaseType` is required."));
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlRuntimeDatabaseConfiguration actual = swapper.swapToYamlConfiguration(
                new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver", false, false, false, false));
        
        assertThat(actual.getDatabaseType(), is("H2"));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:logic"));
        assertThat(actual.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actual.getSupportsExplainAnalyze(), is((Boolean) null));
        assertThat(actual.getSupportsCrossSchemaSql(), is((Boolean) null));
    }
    
    private YamlRuntimeDatabaseConfiguration createYamlConfig() {
        YamlRuntimeDatabaseConfiguration result = new YamlRuntimeDatabaseConfiguration();
        result.setDatabaseType("H2");
        result.setJdbcUrl("jdbc:h2:mem:logic");
        result.setUsername("");
        result.setPassword("");
        result.setDriverClassName("org.h2.Driver");
        result.setSupportsCrossSchemaSql(true);
        result.setSupportsExplainAnalyze(false);
        return result;
    }
}

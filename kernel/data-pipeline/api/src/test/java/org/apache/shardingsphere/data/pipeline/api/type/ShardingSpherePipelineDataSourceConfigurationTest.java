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

package org.apache.shardingsphere.data.pipeline.api.type;

import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSpherePipelineDataSourceConfigurationTest {
    
    @Test
    void assertCreate() {
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(getDataSourceYaml(), YamlRootConfiguration.class, true);
        Map<String, Object> backupDs0 = new HashMap<>(rootConfig.getDataSources().get("ds_0"));
        Map<String, Object> backupDs1 = new HashMap<>(rootConfig.getDataSources().get("ds_1"));
        ShardingSpherePipelineDataSourceConfiguration actual = new ShardingSpherePipelineDataSourceConfiguration(rootConfig);
        assertParameterUnchanged(backupDs0, rootConfig.getDataSources().get("ds_0"));
        assertParameterUnchanged(backupDs1, rootConfig.getDataSources().get("ds_1"));
        assertGetConfig(actual);
    }
    
    private void assertParameterUnchanged(final Map<String, Object> backup, final Map<String, Object> handled) {
        assertThat(handled.size(), is(backup.size()));
        for (Entry<String, Object> entry : backup.entrySet()) {
            Object actual = handled.get(entry.getKey());
            assertNotNull(actual, "value of '" + entry.getKey() + "' doesn't exist");
            assertThat("value of '" + entry.getKey() + "' doesn't match", actual, is(entry.getValue()));
        }
    }
    
    private void assertGetConfig(final ShardingSpherePipelineDataSourceConfiguration actual) {
        assertThat(actual.getDatabaseType().getType(), is("MySQL"));
        assertThat(actual.getType(), is(ShardingSpherePipelineDataSourceConfiguration.TYPE));
        assertThat(actual.getDataSourceConfiguration(), instanceOf(YamlRootConfiguration.class));
        Map<String, Map<String, Object>> dataSources = actual.getRootConfig().getDataSources();
        assertThat(dataSources.size(), is(2));
        assertTrue(dataSources.containsKey("ds_0"));
        assertTrue(dataSources.containsKey("ds_1"));
        for (Map<String, Object> queryProps : dataSources.values()) {
            for (String each : Arrays.asList("minPoolSize", "minimumIdle")) {
                assertThat(queryProps.get(each), is("1"));
            }
        }
    }
    
    private String getDataSourceYaml() {
        return "dataSources:\n"
                + "  ds_1:\n"
                + "    minPoolSize: 20\n"
                + "    minimumIdle: 20\n"
                + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "    url: jdbc:mysql://192.168.0.2:3306/ds_1?useSSL=false\n"
                + "  ds_0:\n"
                + "    minPoolSize: 20\n"
                + "    minimumIdle: 20\n"
                + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "    url: jdbc:mysql://192.168.0.1:3306/ds_0?useSSL=false\n";
    }
}

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

import org.apache.shardingsphere.data.pipeline.spi.JdbcQueryPropertiesExtension;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.file.SystemResourceFileUtils;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class ShardingSpherePipelineDataSourceConfigurationTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertNewInstance() {
        JdbcQueryPropertiesExtension queryPropsExtension = mock(JdbcQueryPropertiesExtension.class);
        when(DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType)).thenReturn(Optional.of(queryPropsExtension));
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        Map<String, Object> backupDataSource0 = new HashMap<>(rootConfig.getDataSources().get("ds_0"));
        Map<String, Object> backupDataSource1 = new HashMap<>(rootConfig.getDataSources().get("ds_1"));
        ShardingSpherePipelineDataSourceConfiguration actual = new ShardingSpherePipelineDataSourceConfiguration(rootConfig);
        assertParameterUnchanged(backupDataSource0, rootConfig.getDataSources().get("ds_0"));
        assertParameterUnchanged(backupDataSource1, rootConfig.getDataSources().get("ds_1"));
        assertPipelineDataSourceConfiguration(actual);
    }
    
    private void assertParameterUnchanged(final Map<String, Object> backupDataSource, final Map<String, Object> handledDataSource) {
        assertThat(handledDataSource.size(), is(backupDataSource.size()));
        for (Entry<String, Object> entry : backupDataSource.entrySet()) {
            Object actual = handledDataSource.get(entry.getKey());
            assertNotNull(actual, "value of '" + entry.getKey() + "' doesn't exist");
            assertThat("value of '" + entry.getKey() + "' doesn't match", actual, is(entry.getValue()));
        }
    }
    
    private void assertPipelineDataSourceConfiguration(final ShardingSpherePipelineDataSourceConfiguration actual) {
        assertThat(actual.getDatabaseType().getType(), is("FIXTURE"));
        assertThat(actual.getType(), is(ShardingSpherePipelineDataSourceConfiguration.TYPE));
        assertThat(actual.getDataSourceConfiguration(), isA(YamlRootConfiguration.class));
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
    
    @Test
    void assertGetActualDataSourceConfiguration() {
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        ShardingSpherePipelineDataSourceConfiguration config = new ShardingSpherePipelineDataSourceConfiguration(rootConfig);
        StandardPipelineDataSourceConfiguration actual = config.getActualDataSourceConfiguration("ds_0");
        assertThat(actual.getDatabaseType().getType(), is("FIXTURE"));
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/ds_0"));
    }
}

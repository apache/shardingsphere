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
import org.apache.shardingsphere.database.connector.core.jdbcurl.appender.JdbcUrlAppender;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.file.SystemResourceFileUtils;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
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
        rootConfig.getDataSources().get("ds_0").put("dataSourceProperties", Collections.singletonMap("foo", "bar"));
        Map<String, Object> backupDataSource0 = new HashMap<>(rootConfig.getDataSources().get("ds_0"));
        Map<String, Object> backupDataSource1 = new HashMap<>(rootConfig.getDataSources().get("ds_1"));
        ShardingSpherePipelineDataSourceConfiguration actual = new ShardingSpherePipelineDataSourceConfiguration(rootConfig);
        assertParameterUnchanged(backupDataSource0, rootConfig.getDataSources().get("ds_0"));
        assertParameterUnchanged(backupDataSource1, rootConfig.getDataSources().get("ds_1"));
        assertPipelineDataSourceConfiguration(actual);
        YamlRootConfiguration persisted = YamlEngine.unmarshal(actual.getParameter(), YamlRootConfiguration.class, true);
        assertFalse(persisted.getDataSources().get("ds_0").containsKey("dataSourceProperties"));
        assertThat(persisted.getDataSources().get("ds_0").get("minPoolSize"), is(20));
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
        assertThat(actual.getDataSourceConfiguration(), isA(ShardingSpherePipelineDataSourceConfiguration.class));
        Map<String, DataSourcePoolProperties> dataSources = actual.getDataSourcePoolPropertiesMap();
        assertThat(dataSources.size(), is(2));
        assertTrue(dataSources.containsKey("ds_0"));
        assertTrue(dataSources.containsKey("ds_1"));
        for (DataSourcePoolProperties dataSourcePoolProps : dataSources.values()) {
            assertThat(dataSourcePoolProps.getPoolPropertySynonyms().getStandardProperties().get("minPoolSize"), is("1"));
            assertThat(dataSourcePoolProps.getAllLocalProperties().get("minimumIdle"), is("1"));
        }
    }
    
    @Test
    void assertGetActualDataSourceConfiguration() {
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        rootConfig.getDataSources().get("ds_0").put("customPoolProps", Collections.singletonMap("connectionTimeout", 30000));
        rootConfig.getDataSources().get("ds_0").put("maximumPoolSize", 12);
        ShardingSpherePipelineDataSourceConfiguration config = new ShardingSpherePipelineDataSourceConfiguration(rootConfig);
        StandardPipelineDataSourceConfiguration actual = config.getActualDataSourceConfiguration("ds_0");
        assertThat(actual.getDatabaseType().getType(), is("FIXTURE"));
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/ds_0"));
        Map<?, ?> parameter = YamlEngine.unmarshal(actual.getParameter(), Map.class);
        assertThat(((Map<?, ?>) parameter.get("customPoolProps")).get("connectionTimeout"), is(30000));
        assertThat(parameter.get("minPoolSize"), is("1"));
        assertThat(parameter.get("maximumPoolSize"), is(12));
        DataSourcePoolProperties actualPoolProps = (DataSourcePoolProperties) actual.getDataSourceConfiguration();
        assertThat(actualPoolProps.getAllLocalProperties().get("connectionTimeout"), is(30000));
        assertThat(actualPoolProps.getAllLocalProperties().get("maximumPoolSize"), is(12));
    }
    
    @Test
    void assertGetActualDataSourceConfigurationWithJdbcUrl() {
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        Map<String, Object> dataSource = rootConfig.getDataSources().get("ds_0");
        dataSource.put("jdbcUrl", dataSource.remove("url"));
        StandardPipelineDataSourceConfiguration actual = new ShardingSpherePipelineDataSourceConfiguration(rootConfig).getActualDataSourceConfiguration("ds_0");
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/ds_0"));
        Map<?, ?> parameter = YamlEngine.unmarshal(actual.getParameter(), Map.class);
        assertThat(parameter.get("jdbcUrl"), is("jdbc:mock://127.0.0.1/ds_0"));
    }
    
    @Test
    void assertCustomPoolPropertiesOverrideAdjustedPoolSize() {
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        rootConfig.getDataSources().get("ds_0").put("customPoolProps", Collections.singletonMap("minimumIdle", 7));
        ShardingSpherePipelineDataSourceConfiguration config = new ShardingSpherePipelineDataSourceConfiguration(rootConfig);
        assertThat(config.getDataSourcePoolPropertiesMap().get("ds_0").getAllLocalProperties().get("minimumIdle"), is(7));
        StandardPipelineDataSourceConfiguration actual = config.getActualDataSourceConfiguration("ds_0");
        assertThat(((DataSourcePoolProperties) actual.getDataSourceConfiguration()).getAllLocalProperties().get("minimumIdle"), is(7));
        Map<?, ?> parameter = YamlEngine.unmarshal(actual.getParameter(), Map.class);
        assertThat(parameter.get("minimumIdle"), is("1"));
        assertThat(((Map<?, ?>) parameter.get("customPoolProps")).get("minimumIdle"), is(7));
    }
    
    @Test
    void assertRuntimeReadsDoNotParsePersistedParameter() {
        ShardingSpherePipelineDataSourceConfiguration config = new ShardingSpherePipelineDataSourceConfiguration(
                SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"));
        try (MockedStatic<YamlEngine> yamlEngine = mockStatic(YamlEngine.class, CALLS_REAL_METHODS)) {
            config.getDataSourceConfiguration();
            config.getDataSourcePoolPropertiesMap();
            config.getRuleConfigurations();
            config.getCreationRuleConfigurations();
            config.getActualDataSourceConfiguration("ds_0");
            yamlEngine.verify(() -> YamlEngine.unmarshal(eq(config.getParameter()), eq(YamlRootConfiguration.class), eq(true)), never());
        }
    }
    
    @Test
    void assertPersistedParameterRestoresRuntimeConfiguration() {
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        rootConfig.getDataSources().get("ds_0").put("customPoolProps", Collections.singletonMap("connectionTimeout", 30000));
        ShardingSpherePipelineDataSourceConfiguration original = new ShardingSpherePipelineDataSourceConfiguration(rootConfig);
        ShardingSpherePipelineDataSourceConfiguration actual = new ShardingSpherePipelineDataSourceConfiguration(original.getParameter());
        assertThat(actual.getDataSourcePoolPropertiesMap().get("ds_0").getAllLocalProperties().get("connectionTimeout"), is(30000));
        assertThat(actual.getActualDataSourceConfiguration("ds_0").getUrl(), is("jdbc:mock://127.0.0.1/ds_0"));
        YamlRootConfiguration persisted = YamlEngine.unmarshal(actual.getParameter(), YamlRootConfiguration.class, true);
        assertThat(persisted.getDataSources().get("ds_0").get("minPoolSize"), is(20));
        assertThat(((Map<?, ?>) persisted.getDataSources().get("ds_0").get("customPoolProps")).get("connectionTimeout"), is(30000));
    }
    
    @Test
    void assertAppendJdbcQueryPropertiesAfterPersistingParameter() {
        JdbcQueryPropertiesExtension queryPropsExtension = mock(JdbcQueryPropertiesExtension.class);
        doAnswer(invocation -> {
            Properties props = invocation.getArgument(0);
            props.setProperty("foo", "bar");
            return null;
        }).when(queryPropsExtension).extendQueryProperties(any(Properties.class));
        when(DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType)).thenReturn(Optional.of(queryPropsExtension));
        try (
                MockedConstruction<JdbcUrlAppender> ignored = mockConstruction(JdbcUrlAppender.class,
                        (mock, context) -> when(mock.appendQueryProperties(anyString(), any(Properties.class))).thenAnswer(invocation -> invocation.getArgument(0) + "?foo=bar"))) {
            ShardingSpherePipelineDataSourceConfiguration actual = new ShardingSpherePipelineDataSourceConfiguration(
                    SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"));
            assertThat(actual.getDataSourcePoolPropertiesMap().get("ds_0").getConnectionPropertySynonyms().getStandardProperties().get("url"), is("jdbc:mock://127.0.0.1/ds_0?foo=bar"));
            YamlRootConfiguration persisted = YamlEngine.unmarshal(actual.getParameter(), YamlRootConfiguration.class, true);
            assertThat(persisted.getDataSources().get("ds_0").get("url"), is("jdbc:mock://127.0.0.1/ds_0"));
            assertThat(actual.getActualDataSourceConfiguration("ds_0").getUrl(), is("jdbc:mock://127.0.0.1/ds_0?foo=bar?foo=bar"));
            YamlRootConfiguration jdbcUrlConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"),
                    YamlRootConfiguration.class, true);
            jdbcUrlConfig.getDataSources().get("ds_0").put("jdbcUrl", jdbcUrlConfig.getDataSources().get("ds_0").remove("url"));
            ShardingSpherePipelineDataSourceConfiguration jdbcUrlActual = new ShardingSpherePipelineDataSourceConfiguration(jdbcUrlConfig);
            assertThat(jdbcUrlActual.getDataSourcePoolPropertiesMap().get("ds_0").getAllLocalProperties().get("jdbcUrl"),
                    is("jdbc:mock://127.0.0.1/ds_0?foo=bar"));
            assertThat(jdbcUrlActual.getActualDataSourceConfiguration("ds_0").getUrl(), is("jdbc:mock://127.0.0.1/ds_0?foo=bar?foo=bar"));
        }
    }
}

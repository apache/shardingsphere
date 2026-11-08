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

package org.apache.shardingsphere.data.pipeline.core.datasource.config;

import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.JdbcQueryPropertiesExtension;
import org.apache.shardingsphere.database.connector.core.jdbcurl.appender.JdbcUrlAppender;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.file.SystemResourceFileUtils;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class PipelineDataSourceConfigurationFactoryTest {
    
    private static final String JDBC_URL = "jdbc:mock://127.0.0.1/foo_ds";
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "123456";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertNewInstanceForStandardPipelineDataSourceConfiguration() {
        assertThat(PipelineDataSourceConfigurationFactory.newInstance(StandardPipelineDataSourceConfiguration.TYPE, "url: jdbc:mock://127.0.0.1/foo_db"),
                isA(StandardPipelineDataSourceConfiguration.class));
    }
    
    @Test
    void assertNewInstanceForShardingSpherePipelineDataSourceConfiguration() {
        assertThat(PipelineDataSourceConfigurationFactory.newInstance(ShardingSpherePipelineDataSourceConfiguration.TYPE,
                "dataSources:\n" + "  foo_ds:\n" + "    url: jdbc:mock://127.0.0.1/foo_db\n" + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource"),
                isA(ShardingSpherePipelineDataSourceConfiguration.class));
    }
    
    @Test
    void assertNewInstanceForUnsupportedType() {
        assertThrows(UnsupportedSQLOperationException.class, () -> PipelineDataSourceConfigurationFactory.newInstance("Invalid", ""));
    }
    
    @Test
    void assertNewInstanceForStandardWithYaml() {
        assertStandardConfiguration((StandardPipelineDataSourceConfiguration) PipelineDataSourceConfigurationFactory.newInstance(StandardPipelineDataSourceConfiguration.TYPE,
                SystemResourceFileUtils.readFile("yaml/standard-pipeline-datasource-config.yaml")));
    }
    
    private void assertStandardConfiguration(final StandardPipelineDataSourceConfiguration actual) {
        assertThat(actual.getDatabaseType().getType(), is("FIXTURE"));
        assertThat(actual.getType(), is(StandardPipelineDataSourceConfiguration.TYPE));
        DataSourcePoolProperties props = (DataSourcePoolProperties) actual.getDataSourceConfiguration();
        assertThat(actual.getUrl(), is(JDBC_URL));
        assertThat(props.getPoolClassName(), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(actual.getUsername(), is(USERNAME));
        assertThat(actual.getPassword(), is(PASSWORD));
        assertThat(new YamlDataSourceConfigurationSwapper().swapToMap(props).get("minPoolSize"), is("1"));
    }
    
    @Test
    void assertNewInstanceForStandardWithMap() {
        JdbcQueryPropertiesExtension queryPropsExtension = mock(JdbcQueryPropertiesExtension.class);
        when(DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType)).thenReturn(Optional.of(queryPropsExtension));
        Map<String, Object> dataSourceConfig = new HashMap<>(5, 1F);
        dataSourceConfig.put("url", JDBC_URL);
        dataSourceConfig.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
        dataSourceConfig.put("username", USERNAME);
        dataSourceConfig.put("password", PASSWORD);
        dataSourceConfig.put("minPoolSize", "20");
        Map<String, Object> backupDataSource = new HashMap<>(dataSourceConfig);
        StandardPipelineDataSourceConfiguration actual = PipelineDataSourceConfigurationFactory.newInstance(dataSourceConfig);
        assertParameterUnchanged(backupDataSource, dataSourceConfig);
        assertStandardConfiguration(actual);
    }
    
    private void assertParameterUnchanged(final Map<String, Object> backupDataSource, final Map<String, Object> handledDataSource) {
        assertThat(handledDataSource.size(), is(backupDataSource.size()));
        for (Entry<String, Object> entry : backupDataSource.entrySet()) {
            Object actual = handledDataSource.get(entry.getKey());
            assertNotNull(actual, "value of '" + entry.getKey() + "' doesn't exist");
            assertThat("value of '" + entry.getKey() + "' doesn't match", actual, is(entry.getValue()));
        }
    }
    
    @Test
    void assertNewInstanceForShardingSphereWithRootConfiguration() {
        JdbcQueryPropertiesExtension queryPropsExtension = mock(JdbcQueryPropertiesExtension.class);
        when(DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType)).thenReturn(Optional.of(queryPropsExtension));
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        rootConfig.getDataSources().get("ds_0").put("dataSourceProperties", Collections.singletonMap("foo", "bar"));
        Map<String, Object> backupDataSource0 = new HashMap<>(rootConfig.getDataSources().get("ds_0"));
        Map<String, Object> backupDataSource1 = new HashMap<>(rootConfig.getDataSources().get("ds_1"));
        ShardingSpherePipelineDataSourceConfiguration actual = PipelineDataSourceConfigurationFactory.newInstance(rootConfig);
        assertParameterUnchanged(backupDataSource0, rootConfig.getDataSources().get("ds_0"));
        assertParameterUnchanged(backupDataSource1, rootConfig.getDataSources().get("ds_1"));
        assertShardingSphereConfiguration(actual);
        YamlRootConfiguration persisted = YamlEngine.unmarshal(actual.getParameter(), YamlRootConfiguration.class, true);
        assertFalse(persisted.getDataSources().get("ds_0").containsKey("dataSourceProperties"));
        assertThat(persisted.getDataSources().get("ds_0").get("minPoolSize"), is(20));
    }
    
    private void assertShardingSphereConfiguration(final ShardingSpherePipelineDataSourceConfiguration actual) {
        assertThat(actual.getDatabaseType().getType(), is("FIXTURE"));
        assertThat(actual.getType(), is(ShardingSpherePipelineDataSourceConfiguration.TYPE));
        assertThat(actual.getDataSourceConfiguration(), isA(ShardingSpherePipelineDataSourceConfiguration.class));
        Map<String, DataSourcePoolProperties> dataSources = actual.getDataSourcePoolPropertiesMap();
        assertThat(dataSources.size(), is(2));
        assertTrue(dataSources.containsKey("ds_0"));
        assertTrue(dataSources.containsKey("ds_1"));
        for (DataSourcePoolProperties each : dataSources.values()) {
            assertThat(each.getPoolPropertySynonyms().getStandardProperties().get("minPoolSize"), is("1"));
            assertThat(each.getAllLocalProperties().get("minimumIdle"), is("1"));
        }
    }
    
    @Test
    void assertNewInstanceForShardingSphereWithCustomPoolProperties() {
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        rootConfig.getDataSources().get("ds_0").put("customPoolProps", Collections.singletonMap("minimumIdle", 7));
        ShardingSpherePipelineDataSourceConfiguration actual = PipelineDataSourceConfigurationFactory.newInstance(rootConfig);
        assertThat(actual.getDataSourcePoolPropertiesMap().get("ds_0").getAllLocalProperties().get("minimumIdle"), is(7));
    }
    
    @Test
    void assertNewInstanceForShardingSphereWithoutCustomPoolProperties() {
        ShardingSpherePipelineDataSourceConfiguration actual = (ShardingSpherePipelineDataSourceConfiguration) PipelineDataSourceConfigurationFactory.newInstance(
                ShardingSpherePipelineDataSourceConfiguration.TYPE, SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"));
        assertFalse(actual.getCustomPoolPropertiesKeys().containsKey("ds_0"));
    }
    
    @Test
    void assertNewInstanceForShardingSphereWithEmptyCustomPoolProperties() {
        String yaml = "dataSources:\n  ds_0:\n    url: jdbc:mock://127.0.0.1/ds_0\n    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n    customPoolProps: {}";
        ShardingSpherePipelineDataSourceConfiguration actual = (ShardingSpherePipelineDataSourceConfiguration) PipelineDataSourceConfigurationFactory.newInstance(
                ShardingSpherePipelineDataSourceConfiguration.TYPE, yaml);
        assertTrue(actual.getCustomPoolPropertiesKeys().containsKey("ds_0"));
        assertTrue(actual.getCustomPoolPropertiesKeys().get("ds_0").isEmpty());
    }
    
    @Test
    void assertNewInstanceForShardingSphereWithPersistedParameter() {
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        rootConfig.getDataSources().get("ds_0").put("customPoolProps", Collections.singletonMap("connectionTimeout", 30000));
        ShardingSpherePipelineDataSourceConfiguration original = PipelineDataSourceConfigurationFactory.newInstance(rootConfig);
        ShardingSpherePipelineDataSourceConfiguration actual = (ShardingSpherePipelineDataSourceConfiguration) PipelineDataSourceConfigurationFactory.newInstance(
                ShardingSpherePipelineDataSourceConfiguration.TYPE, original.getParameter());
        assertThat(actual.getDataSourcePoolPropertiesMap().get("ds_0").getAllLocalProperties().get("connectionTimeout"), is(30000));
        YamlRootConfiguration persisted = YamlEngine.unmarshal(actual.getParameter(), YamlRootConfiguration.class, true);
        assertThat(persisted.getDataSources().get("ds_0").get("minPoolSize"), is(20));
        assertThat(((Map<?, ?>) persisted.getDataSources().get("ds_0").get("customPoolProps")).get("connectionTimeout"), is(30000));
    }
    
    @Test
    void assertNewInstanceForShardingSphereWithExtendedUrl() {
        mockJdbcQueryPropertiesExtension();
        try (
                MockedConstruction<JdbcUrlAppender> ignored = mockConstruction(JdbcUrlAppender.class,
                        (mock, context) -> when(mock.appendQueryProperties(anyString(), any(Properties.class))).thenAnswer(invocation -> invocation.getArgument(0) + "?foo=bar"))) {
            ShardingSpherePipelineDataSourceConfiguration actual = (ShardingSpherePipelineDataSourceConfiguration) PipelineDataSourceConfigurationFactory.newInstance(
                    ShardingSpherePipelineDataSourceConfiguration.TYPE, SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"));
            assertThat(actual.getDataSourcePoolPropertiesMap().get("ds_0").getConnectionPropertySynonyms().getStandardProperties().get("url"), is("jdbc:mock://127.0.0.1/ds_0?foo=bar"));
            YamlRootConfiguration persisted = YamlEngine.unmarshal(actual.getParameter(), YamlRootConfiguration.class, true);
            assertThat(persisted.getDataSources().get("ds_0").get("url"), is("jdbc:mock://127.0.0.1/ds_0"));
        }
    }
    
    private void mockJdbcQueryPropertiesExtension() {
        JdbcQueryPropertiesExtension queryPropsExtension = mock(JdbcQueryPropertiesExtension.class);
        doAnswer(invocation -> {
            Properties props = invocation.getArgument(0);
            props.setProperty("foo", "bar");
            return null;
        }).when(queryPropsExtension).extendQueryProperties(any(Properties.class));
        when(DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType)).thenReturn(Optional.of(queryPropsExtension));
    }
    
    @Test
    void assertNewInstanceForShardingSphereWithExtendedJdbcUrl() {
        mockJdbcQueryPropertiesExtension();
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        rootConfig.getDataSources().get("ds_0").put("jdbcUrl", rootConfig.getDataSources().get("ds_0").remove("url"));
        try (
                MockedConstruction<JdbcUrlAppender> ignored = mockConstruction(JdbcUrlAppender.class,
                        (mock, context) -> when(mock.appendQueryProperties(anyString(), any(Properties.class))).thenAnswer(invocation -> invocation.getArgument(0) + "?foo=bar"))) {
            ShardingSpherePipelineDataSourceConfiguration actual = PipelineDataSourceConfigurationFactory.newInstance(rootConfig);
            assertThat(actual.getDataSourcePoolPropertiesMap().get("ds_0").getAllLocalProperties().get("jdbcUrl"), is("jdbc:mock://127.0.0.1/ds_0?foo=bar"));
            YamlRootConfiguration persisted = YamlEngine.unmarshal(actual.getParameter(), YamlRootConfiguration.class, true);
            assertThat(persisted.getDataSources().get("ds_0").get("jdbcUrl"), is("jdbc:mock://127.0.0.1/ds_0"));
        }
    }
}

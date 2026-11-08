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

package org.apache.shardingsphere.data.pipeline.core.util;

import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.spi.JdbcQueryPropertiesExtension;
import org.apache.shardingsphere.database.connector.core.jdbcurl.appender.JdbcUrlAppender;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
@StaticMockSettings({DatabaseTypedSPILoader.class, DatabaseTypeFactory.class})
class PipelineDataSourceConfigurationUtilsTest {
    
    private static final String JDBC_URL = "jdbc:mock://127.0.0.1/foo_ds";
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "123456";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertTransformPipelineDataSourceConfiguration() {
        Map<String, Object> dataSourceProps = new LinkedHashMap<>(2, 1F);
        dataSourceProps.put("maxPoolSize", 2);
        dataSourceProps.put("maximumPoolSize", 3);
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = mock(ShardingSpherePipelineDataSourceConfiguration.class);
        Map<String, DataSourcePoolProperties> dataSources = new LinkedHashMap<>(1, 1F);
        dataSources.put("ds_0", new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", dataSourceProps));
        when(pipelineDataSourceConfig.getDataSourcePoolPropertiesMap()).thenReturn(dataSources);
        PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration("foo_job", pipelineDataSourceConfig, Collections.singletonMap("ds_0", mockStorageUnit()));
        assertThat(dataSources.get("ds_0").getPoolPropertySynonyms().getStandardProperties().get("maxPoolSize"), is(10));
    }
    
    @Test
    void assertTransformPipelineDataSourceConfigurationWithUnnamedStandardDataSource() {
        mockDatabaseTypeFactory();
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(createStandardDataSourceProperties(2, 3));
        assertThat(PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration("foo_job", pipelineDataSourceConfig, Collections.singletonMap("ds_0", mock(StorageUnit.class))),
                sameInstance(pipelineDataSourceConfig));
    }
    
    @Test
    void assertTransformPipelineDataSourceConfigurationWithNullStorageUnits() {
        mockDatabaseTypeFactory();
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(createStandardDataSourceProperties(2, 3));
        assertThat(PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration("foo_job", pipelineDataSourceConfig, null), sameInstance(pipelineDataSourceConfig));
    }
    
    @Test
    void assertTransformPipelineDataSourceConfigurationWithEmptyStorageUnits() {
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = mock(ShardingSpherePipelineDataSourceConfiguration.class);
        assertThat(PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration("foo_job", pipelineDataSourceConfig, Collections.emptyMap()), sameInstance(pipelineDataSourceConfig));
    }
    
    @Test
    void assertTransformPipelineDataSourceConfigurationWithStandardDataSource() {
        mockDatabaseTypeFactory();
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(createStandardDataSourceProperties(2, 3));
        StorageUnit storageUnit = mockStorageUnit(10, 20);
        StandardPipelineDataSourceConfiguration actual = (StandardPipelineDataSourceConfiguration) PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration(
                "foo_job", "ds_0", pipelineDataSourceConfig, Collections.singletonMap("ds_0", storageUnit));
        DataSourcePoolProperties actualProps = (DataSourcePoolProperties) actual.getDataSourceConfiguration();
        assertThat(actualProps.getPoolPropertySynonyms().getStandardProperties().get("maxPoolSize"), is(10));
    }
    
    @Test
    void assertTransformPipelineDataSourceConfigurationWithCurrentStandardDataSource() {
        mockDatabaseTypeFactory();
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(createStandardDataSourceProperties(2, 3));
        StandardPipelineDataSourceConfiguration actual = (StandardPipelineDataSourceConfiguration) PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration(
                "foo_job", "ds_0", pipelineDataSourceConfig, new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", createStandardDataSourceProperties(10, 20)));
        DataSourcePoolProperties actualProps = (DataSourcePoolProperties) actual.getDataSourceConfiguration();
        assertThat(actualProps.getPoolPropertySynonyms().getStandardProperties().get("maxPoolSize"), is(10));
    }
    
    @Test
    void assertTransformPipelineDataSourceConfigurationWithSameStandardDataSource() {
        mockDatabaseTypeFactory();
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(createStandardDataSourceProperties(10, 20));
        assertThat(PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration(
                "foo_job", "ds_0", pipelineDataSourceConfig, new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", createStandardDataSourceProperties(10, 20))),
                sameInstance(pipelineDataSourceConfig));
    }
    
    @Test
    void assertTransformPipelineDataSourceConfigurationWithAbsentStorageUnit() {
        mockDatabaseTypeFactory();
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(createStandardDataSourceProperties(2, 3));
        assertThat(PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration("foo_job", "ds_0", pipelineDataSourceConfig, Collections.emptyMap()), is(pipelineDataSourceConfig));
    }
    
    @Test
    void assertGetActualDataSourceConfiguration() {
        mockDatabaseTypeFactory();
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        rootConfig.getDataSources().get("ds_0").put("customPoolProps", Collections.singletonMap("connectionTimeout", 30000));
        rootConfig.getDataSources().get("ds_0").put("maximumPoolSize", 12);
        ShardingSpherePipelineDataSourceConfiguration config = PipelineDataSourceConfigurationFactory.newInstance(rootConfig);
        StandardPipelineDataSourceConfiguration actual = PipelineDataSourceConfigurationUtils.getActualDataSourceConfiguration(config, "ds_0");
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
        mockDatabaseTypeFactory();
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        Map<String, Object> dataSource = rootConfig.getDataSources().get("ds_0");
        dataSource.put("jdbcUrl", dataSource.remove("url"));
        ShardingSpherePipelineDataSourceConfiguration config = PipelineDataSourceConfigurationFactory.newInstance(rootConfig);
        StandardPipelineDataSourceConfiguration actual = PipelineDataSourceConfigurationUtils.getActualDataSourceConfiguration(config, "ds_0");
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/ds_0"));
        Map<?, ?> parameter = YamlEngine.unmarshal(actual.getParameter(), Map.class);
        assertThat(parameter.get("jdbcUrl"), is("jdbc:mock://127.0.0.1/ds_0"));
    }
    
    @Test
    void assertGetActualDataSourceConfigurationWithCustomPoolProperties() {
        mockDatabaseTypeFactory();
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        rootConfig.getDataSources().get("ds_0").put("customPoolProps", Collections.singletonMap("minimumIdle", 7));
        ShardingSpherePipelineDataSourceConfiguration config = PipelineDataSourceConfigurationFactory.newInstance(rootConfig);
        StandardPipelineDataSourceConfiguration actual = PipelineDataSourceConfigurationUtils.getActualDataSourceConfiguration(config, "ds_0");
        assertThat(((DataSourcePoolProperties) actual.getDataSourceConfiguration()).getAllLocalProperties().get("minimumIdle"), is(7));
        Map<?, ?> parameter = YamlEngine.unmarshal(actual.getParameter(), Map.class);
        assertThat(parameter.get("minimumIdle"), is("1"));
        assertThat(((Map<?, ?>) parameter.get("customPoolProps")).get("minimumIdle"), is(7));
    }
    
    @Test
    void assertGetActualDataSourceConfigurationWithoutCustomPoolProperties() {
        mockDatabaseTypeFactory();
        ShardingSpherePipelineDataSourceConfiguration config = (ShardingSpherePipelineDataSourceConfiguration) PipelineDataSourceConfigurationFactory.newInstance(
                ShardingSpherePipelineDataSourceConfiguration.TYPE, SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"));
        StandardPipelineDataSourceConfiguration actual = PipelineDataSourceConfigurationUtils.getActualDataSourceConfiguration(config, "ds_0");
        Map<?, ?> parameter = YamlEngine.unmarshal(actual.getParameter(), Map.class);
        assertFalse(parameter.containsKey("customPoolProps"));
    }
    
    @Test
    void assertGetActualDataSourceConfigurationAfterPoolUpdate() {
        mockDatabaseTypeFactory();
        ShardingSpherePipelineDataSourceConfiguration config = (ShardingSpherePipelineDataSourceConfiguration) PipelineDataSourceConfigurationFactory.newInstance(
                ShardingSpherePipelineDataSourceConfiguration.TYPE, SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"));
        PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration("foo_job", config, Collections.singletonMap("ds_0", mockStorageUnit()));
        StandardPipelineDataSourceConfiguration actual = PipelineDataSourceConfigurationUtils.getActualDataSourceConfiguration(config, "ds_0");
        DataSourcePoolProperties actualPoolProps = (DataSourcePoolProperties) actual.getDataSourceConfiguration();
        assertThat(actualPoolProps.getPoolPropertySynonyms().getStandardProperties().get("maxPoolSize"), is(10));
    }
    
    @Test
    void assertGetActualDataSourceConfigurationWithoutParsingPersistedParameter() {
        mockDatabaseTypeFactory();
        ShardingSpherePipelineDataSourceConfiguration config = (ShardingSpherePipelineDataSourceConfiguration) PipelineDataSourceConfigurationFactory.newInstance(
                ShardingSpherePipelineDataSourceConfiguration.TYPE, SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"));
        try (MockedStatic<YamlEngine> yamlEngine = mockStatic(YamlEngine.class, CALLS_REAL_METHODS)) {
            PipelineDataSourceConfigurationUtils.getActualDataSourceConfiguration(config, "ds_0");
            yamlEngine.verify(() -> YamlEngine.unmarshal(eq(config.getParameter()), eq(YamlRootConfiguration.class), eq(true)), never());
        }
    }
    
    @Test
    void assertGetActualDataSourceConfigurationFromPersistedParameter() {
        mockDatabaseTypeFactory();
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        rootConfig.getDataSources().get("ds_0").put("customPoolProps", Collections.singletonMap("connectionTimeout", 30000));
        ShardingSpherePipelineDataSourceConfiguration original = PipelineDataSourceConfigurationFactory.newInstance(rootConfig);
        ShardingSpherePipelineDataSourceConfiguration restored = (ShardingSpherePipelineDataSourceConfiguration) PipelineDataSourceConfigurationFactory.newInstance(
                ShardingSpherePipelineDataSourceConfiguration.TYPE, original.getParameter());
        StandardPipelineDataSourceConfiguration actual = PipelineDataSourceConfigurationUtils.getActualDataSourceConfiguration(restored, "ds_0");
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/ds_0"));
        assertThat(((DataSourcePoolProperties) actual.getDataSourceConfiguration()).getAllLocalProperties().get("connectionTimeout"), is(30000));
        Map<?, ?> parameter = YamlEngine.unmarshal(actual.getParameter(), Map.class);
        assertThat(((Map<?, ?>) parameter.get("customPoolProps")).get("connectionTimeout"), is(30000));
    }
    
    @Test
    void assertGetActualDataSourceConfigurationWithExtendedUrl() {
        when(DatabaseTypeFactory.get(anyString())).thenReturn(databaseType);
        mockJdbcQueryPropertiesExtension();
        try (
                MockedConstruction<JdbcUrlAppender> ignored = mockConstruction(JdbcUrlAppender.class,
                        (mock, context) -> when(mock.appendQueryProperties(anyString(), any(Properties.class))).thenAnswer(invocation -> invocation.getArgument(0) + "?foo=bar"))) {
            ShardingSpherePipelineDataSourceConfiguration config = (ShardingSpherePipelineDataSourceConfiguration) PipelineDataSourceConfigurationFactory.newInstance(
                    ShardingSpherePipelineDataSourceConfiguration.TYPE, SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"));
            StandardPipelineDataSourceConfiguration actual = PipelineDataSourceConfigurationUtils.getActualDataSourceConfiguration(config, "ds_0");
            assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/ds_0?foo=bar?foo=bar"));
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
    void assertGetActualDataSourceConfigurationWithExtendedJdbcUrl() {
        when(DatabaseTypeFactory.get(anyString())).thenReturn(databaseType);
        mockJdbcQueryPropertiesExtension();
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(SystemResourceFileUtils.readFile("yaml/shardingsphere-pipeline-datasource-config.yaml"), YamlRootConfiguration.class, true);
        rootConfig.getDataSources().get("ds_0").put("jdbcUrl", rootConfig.getDataSources().get("ds_0").remove("url"));
        try (
                MockedConstruction<JdbcUrlAppender> ignored = mockConstruction(JdbcUrlAppender.class,
                        (mock, context) -> when(mock.appendQueryProperties(anyString(), any(Properties.class))).thenAnswer(invocation -> invocation.getArgument(0) + "?foo=bar"))) {
            ShardingSpherePipelineDataSourceConfiguration config = PipelineDataSourceConfigurationFactory.newInstance(rootConfig);
            StandardPipelineDataSourceConfiguration actual = PipelineDataSourceConfigurationUtils.getActualDataSourceConfiguration(config, "ds_0");
            assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/ds_0?foo=bar?foo=bar"));
        }
    }
    
    private void mockDatabaseTypeFactory() {
        when(DatabaseTypeFactory.get(anyString())).thenReturn(databaseType);
        when(DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType)).thenReturn(Optional.empty());
    }
    
    private Map<String, Object> createStandardDataSourceProperties(final int maxPoolSize, final int maximumPoolSize) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("url", JDBC_URL);
        result.put("username", USERNAME);
        result.put("password", PASSWORD);
        result.put("maxPoolSize", maxPoolSize);
        result.put("maximumPoolSize", maximumPoolSize);
        return result;
    }
    
    private StorageUnit mockStorageUnit() {
        Map<String, Object> standardProps = new LinkedHashMap<>(2, 1F);
        standardProps.put("maxPoolSize", 10);
        standardProps.put("maximumPoolSize", 20);
        StorageUnit result = mock(StorageUnit.class);
        DataSourcePoolProperties dataSourcePoolProps = mock(DataSourcePoolProperties.class);
        PoolPropertySynonyms poolPropertySynonyms = mock(PoolPropertySynonyms.class);
        when(result.getDataSourcePoolProperties()).thenReturn(dataSourcePoolProps);
        when(dataSourcePoolProps.getPoolPropertySynonyms()).thenReturn(poolPropertySynonyms);
        when(poolPropertySynonyms.getStandardProperties()).thenReturn(standardProps);
        return result;
    }
    
    private StorageUnit mockStorageUnit(final int maxPoolSize, final int maximumPoolSize) {
        StorageUnit result = mock(StorageUnit.class);
        when(result.getDataSourcePoolProperties())
                .thenReturn(new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", createStandardDataSourceProperties(maxPoolSize, maximumPoolSize)));
        return result;
    }
}

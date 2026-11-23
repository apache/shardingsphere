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
import org.apache.shardingsphere.data.pipeline.spi.JdbcQueryPropertiesExtension;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.fixture.connector.MockedConnectionPropertiesParser;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = mockShardingSphereDataSourceConfig();
        PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration("foo_job", pipelineDataSourceConfig, Collections.singletonMap("ds_0", mockStorageUnit(10, 20)));
        assertThat(pipelineDataSourceConfig.getDataSourcePoolPropertiesMap().get("ds_0").getPoolPropertySynonyms().getStandardProperties().get("maxPoolSize"), is(10));
    }
    
    private ShardingSpherePipelineDataSourceConfiguration mockShardingSphereDataSourceConfig() {
        ShardingSpherePipelineDataSourceConfiguration result = mock(ShardingSpherePipelineDataSourceConfiguration.class);
        Map<String, DataSourcePoolProperties> dataSources = new LinkedHashMap<>(1, 1F);
        dataSources.put("ds_0", new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", createStandardDataSourceProperties(2, 3)));
        when(result.getDataSourcePoolPropertiesMap()).thenReturn(dataSources);
        when(result.getParameter()).thenReturn("dataSources:\n  ds_0:\n    url: " + JDBC_URL);
        return result;
    }
    
    @Test
    void assertTransformShardingSphereDataSourceRetainsSnapshotWhenNameIsReused() {
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = mockShardingSphereDataSourceConfig();
        Map<String, Object> liveProps = createStandardDataSourceProperties(1, 1);
        liveProps.put("url", "jdbc:mock://127.0.0.2/other_ds");
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getDataSourcePoolProperties()).thenReturn(new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", liveProps));
        ShardingSpherePipelineDataSourceConfiguration actual = PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration("foo_job", pipelineDataSourceConfig,
                Collections.singletonMap("ds_0", storageUnit));
        assertThat(actual.getDataSourcePoolPropertiesMap().get("ds_0").getPoolPropertySynonyms().getStandardProperties().get("maxPoolSize"), is(2));
    }
    
    @Test
    void assertTransformPipelineDataSourceConfigurationWithUnnamedStandardDataSource() {
        mockDatabaseTypeFactory();
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = new StandardPipelineDataSourceConfiguration(createStandardDataSourceProperties(2, 3));
        assertThat(PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration("foo_job", pipelineDataSourceConfig, Collections.singletonMap("ds_0", mock(StorageUnit.class))),
                sameInstance(pipelineDataSourceConfig));
    }
    
    @Test
    void assertTransformPipelineDataSourceConfigurationWithNullStorageUnits() {
        mockDatabaseTypeFactory();
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = new StandardPipelineDataSourceConfiguration(createStandardDataSourceProperties(2, 3));
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
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = new StandardPipelineDataSourceConfiguration(createStandardDataSourceProperties(2, 3));
        StorageUnit storageUnit = mockStorageUnit(10, 20);
        StandardPipelineDataSourceConfiguration actual = (StandardPipelineDataSourceConfiguration) PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration(
                "foo_job", "ds_0", pipelineDataSourceConfig, Collections.singletonMap("ds_0", storageUnit));
        DataSourcePoolProperties actualProps = (DataSourcePoolProperties) actual.getDataSourceConfiguration();
        assertThat(actualProps.getPoolPropertySynonyms().getStandardProperties().get("maxPoolSize"), is(10));
    }
    
    @Test
    void assertTransformPipelineDataSourceConfigurationWithCurrentStandardDataSource() {
        mockDatabaseTypeFactory();
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = new StandardPipelineDataSourceConfiguration(createStandardDataSourceProperties(2, 3));
        StandardPipelineDataSourceConfiguration actual = (StandardPipelineDataSourceConfiguration) PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration(
                "foo_job", "ds_0", pipelineDataSourceConfig, new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", createStandardDataSourceProperties(10, 20)));
        DataSourcePoolProperties actualProps = (DataSourcePoolProperties) actual.getDataSourceConfiguration();
        assertThat(actualProps.getPoolPropertySynonyms().getStandardProperties().get("maxPoolSize"), is(10));
    }
    
    @Test
    void assertTransformStandardDataSourceUsesPoolDefault() {
        mockDatabaseTypeFactory();
        JdbcQueryPropertiesExtension extension = mock(JdbcQueryPropertiesExtension.class);
        when(DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType)).thenReturn(Optional.of(extension));
        when((Object) DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType)).thenReturn(new MockedConnectionPropertiesParser());
        doAnswer(invocation -> {
            Properties queryProps = invocation.getArgument(0);
            queryProps.setProperty("useServerPrepStmts", "true");
            return null;
        }).when(extension).extendQueryProperties(any(Properties.class));
        StandardPipelineDataSourceConfiguration jobConfig = new StandardPipelineDataSourceConfiguration(createStandardDataSourceProperties(2, 3));
        Map<String, Object> liveProps = createStandardDataSourceProperties(2, 3);
        liveProps.remove("maxPoolSize");
        liveProps.remove("maximumPoolSize");
        liveProps.put("username", "other_user");
        DataSourcePoolProperties livePoolProps = new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", liveProps);
        DataSourcePoolMetaData poolMetaData = mock(DataSourcePoolMetaData.class);
        when(poolMetaData.getPropertySynonyms()).thenReturn(Collections.singletonMap("maxPoolSize", "maximumPoolSize"));
        when(poolMetaData.getDefaultProperties()).thenReturn(Collections.singletonMap("maximumPoolSize", 50));
        when(poolMetaData.getType()).thenReturn(livePoolProps.getPoolClassName());
        try (MockedStatic<TypedSPILoader> ignored = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS)) {
            when(TypedSPILoader.findService(DataSourcePoolMetaData.class, livePoolProps.getPoolClassName())).thenReturn(Optional.of(poolMetaData));
            StandardPipelineDataSourceConfiguration actual = (StandardPipelineDataSourceConfiguration) PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration("foo_job", "ds_0",
                    jobConfig, livePoolProps);
            DataSourcePoolProperties actualPoolProps = (DataSourcePoolProperties) actual.getDataSourceConfiguration();
            assertThat(actualPoolProps.getPoolPropertySynonyms().getStandardProperties().get("maxPoolSize"), is(50));
            assertThat(actual.getUsername(), is(USERNAME));
            assertThat(actual.getUrl(), is(JDBC_URL + "?useServerPrepStmts=true"));
        }
    }
    
    @Test
    void assertTransformStandardDataSourceRetainsSnapshotWhenNameIsReused() {
        mockDatabaseTypeFactory();
        StandardPipelineDataSourceConfiguration jobConfig = new StandardPipelineDataSourceConfiguration(createStandardDataSourceProperties(2, 3));
        Map<String, Object> liveProps = createStandardDataSourceProperties(10, 20);
        liveProps.put("url", "jdbc:mock://127.0.0.2/other_ds");
        DataSourcePoolProperties livePoolProps = new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", liveProps);
        assertThat(PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration("foo_job", "ds_0", jobConfig, livePoolProps), sameInstance(jobConfig));
    }
    
    @Test
    void assertTransformPipelineDataSourceConfigurationWithSameStandardDataSource() {
        mockDatabaseTypeFactory();
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = new StandardPipelineDataSourceConfiguration(createStandardDataSourceProperties(10, 20));
        assertThat(PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration(
                "foo_job", "ds_0", pipelineDataSourceConfig, new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", createStandardDataSourceProperties(10, 20))),
                sameInstance(pipelineDataSourceConfig));
    }
    
    @Test
    void assertTransformPipelineDataSourceConfigurationWithAbsentStorageUnit() {
        mockDatabaseTypeFactory();
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = new StandardPipelineDataSourceConfiguration(createStandardDataSourceProperties(2, 3));
        assertThat(PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration("foo_job", "ds_0", pipelineDataSourceConfig, Collections.emptyMap()), is(pipelineDataSourceConfig));
    }
    
    private void mockDatabaseTypeFactory() {
        when(DatabaseTypeFactory.get(JDBC_URL)).thenReturn(databaseType);
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
    
    private StorageUnit mockStorageUnit(final int maxPoolSize, final int maximumPoolSize) {
        StorageUnit result = mock(StorageUnit.class);
        when(result.getDataSourcePoolProperties())
                .thenReturn(new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", createStandardDataSourceProperties(maxPoolSize, maximumPoolSize)));
        return result;
    }
}

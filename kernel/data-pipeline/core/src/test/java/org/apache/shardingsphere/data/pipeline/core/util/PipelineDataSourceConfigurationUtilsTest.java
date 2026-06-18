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
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
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
        when(pipelineDataSourceConfig.getRootConfig()).thenReturn(createYamlRootConfiguration(dataSourceProps));
        PipelineDataSourceConfigurationUtils.transformPipelineDataSourceConfiguration("foo_job", pipelineDataSourceConfig, Collections.singletonMap("ds_0", mockStorageUnit()));
        assertThat(dataSourceProps.get("maxPoolSize"), is(10));
        assertThat(dataSourceProps.get("maximumPoolSize"), is(20));
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
    
    private YamlRootConfiguration createYamlRootConfiguration(final Map<String, Object> dataSourceProps) {
        YamlRootConfiguration result = new YamlRootConfiguration();
        result.setDatabaseName("foo_db");
        result.setDataSources(Collections.singletonMap("ds_0", dataSourceProps));
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

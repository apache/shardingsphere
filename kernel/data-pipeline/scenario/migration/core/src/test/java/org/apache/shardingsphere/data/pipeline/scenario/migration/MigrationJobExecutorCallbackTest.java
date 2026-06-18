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

package org.apache.shardingsphere.data.pipeline.scenario.migration;

import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MigrationJobExecutorCallbackTest {
    
    private static final String DATABASE_NAME = "foo_db";
    
    private static final String SOURCE_DATA_SOURCE_NAME = "source_ds";
    
    private static final String TARGET_DATA_SOURCE_NAME = "target_ds";
    
    private static final String SOURCE_JDBC_URL = "jdbc:mock://127.0.0.1/source_ds";
    
    private static final String TARGET_JDBC_URL = "jdbc:mock://127.0.0.1/target_ds";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBuildJobItemContextWithTransformedDataSourceConfigurations() {
        try (
                MockedStatic<DatabaseTypeFactory> databaseTypeFactory = mockStatic(DatabaseTypeFactory.class);
                MockedStatic<PipelineAPIFactory> pipelineAPIFactory = mockStatic(PipelineAPIFactory.class);
                MockedStatic<PipelineContextManager> pipelineContextManager = mockStatic(PipelineContextManager.class)) {
            mockDatabaseTypeFactory(databaseTypeFactory);
            mockGovernanceFacade(pipelineAPIFactory, createSourceDataSourceYaml(10, 20));
            mockProxyContext(pipelineContextManager);
            MigrationJobItemContext actual = new MigrationJobExecutorCallback().buildJobItemContext(createJobConfiguration(), 0, null, mockProcessContext(),
                    mock(PipelineDataSourceManager.class));
            MigrationTaskConfiguration actualTaskConfig = actual.getTaskConfig();
            assertThat(getMaxPoolSize((StandardPipelineDataSourceConfiguration) actualTaskConfig.getDumperContext().getCommonContext().getDataSourceConfig()), is("10"));
            ImporterConfiguration actualImporterConfig = actualTaskConfig.getImporterConfig();
            assertThat(getRootDataSourceMaxPoolSize((ShardingSpherePipelineDataSourceConfiguration) actualImporterConfig.getDataSourceConfig()), is("30"));
            CreateTableConfiguration actualCreateTableConfig = actualTaskConfig.getCreateTableConfigurations().iterator().next();
            assertThat(getMaxPoolSize((StandardPipelineDataSourceConfiguration) actualCreateTableConfig.getSourceDataSourceConfig()), is("10"));
            assertThat(getRootDataSourceMaxPoolSize((ShardingSpherePipelineDataSourceConfiguration) actualCreateTableConfig.getTargetDataSourceConfig()), is("30"));
        }
    }
    
    @Test
    void assertBuildJobItemContextWithoutMigrationSourceStorageUnit() {
        try (
                MockedStatic<DatabaseTypeFactory> databaseTypeFactory = mockStatic(DatabaseTypeFactory.class);
                MockedStatic<PipelineAPIFactory> pipelineAPIFactory = mockStatic(PipelineAPIFactory.class);
                MockedStatic<PipelineContextManager> pipelineContextManager = mockStatic(PipelineContextManager.class)) {
            mockDatabaseTypeFactory(databaseTypeFactory);
            mockGovernanceFacade(pipelineAPIFactory, "");
            Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
            storageUnits.put(SOURCE_DATA_SOURCE_NAME, mock(StorageUnit.class));
            storageUnits.put(TARGET_DATA_SOURCE_NAME, mockStorageUnit(30, 40));
            mockProxyContext(pipelineContextManager, storageUnits);
            MigrationJobItemContext actual = new MigrationJobExecutorCallback().buildJobItemContext(createJobConfiguration(), 0, null, mockProcessContext(),
                    mock(PipelineDataSourceManager.class));
            MigrationTaskConfiguration actualTaskConfig = actual.getTaskConfig();
            assertThat(getMaxPoolSize((StandardPipelineDataSourceConfiguration) actualTaskConfig.getDumperContext().getCommonContext().getDataSourceConfig()), is("2"));
            ImporterConfiguration actualImporterConfig = actualTaskConfig.getImporterConfig();
            assertThat(getRootDataSourceMaxPoolSize((ShardingSpherePipelineDataSourceConfiguration) actualImporterConfig.getDataSourceConfig()), is("30"));
            CreateTableConfiguration actualCreateTableConfig = actualTaskConfig.getCreateTableConfigurations().iterator().next();
            assertThat(getMaxPoolSize((StandardPipelineDataSourceConfiguration) actualCreateTableConfig.getSourceDataSourceConfig()), is("2"));
            assertThat(getRootDataSourceMaxPoolSize((ShardingSpherePipelineDataSourceConfiguration) actualCreateTableConfig.getTargetDataSourceConfig()), is("30"));
        }
    }
    
    private void mockDatabaseTypeFactory(final MockedStatic<DatabaseTypeFactory> databaseTypeFactory) {
        databaseTypeFactory.when(() -> DatabaseTypeFactory.get(anyString())).thenReturn(databaseType);
    }
    
    private void mockGovernanceFacade(final MockedStatic<PipelineAPIFactory> pipelineAPIFactory, final String dataSourcesYaml) {
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS);
        when(governanceFacade.getMetaDataFacade().getDataSource().load(new MigrationJobType().getType())).thenReturn(dataSourcesYaml);
        pipelineAPIFactory.when(() -> PipelineAPIFactory.getPipelineGovernanceFacade(any(PipelineContextKey.class))).thenReturn(governanceFacade);
    }
    
    private String createSourceDataSourceYaml(final int maxPoolSize, final int maximumPoolSize) {
        return SOURCE_DATA_SOURCE_NAME + ":\n"
                + "  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "  url: " + SOURCE_JDBC_URL + "\n"
                + "  username: root\n"
                + "  password: root\n"
                + "  maxPoolSize: " + maxPoolSize + "\n"
                + "  maximumPoolSize: " + maximumPoolSize + "\n";
    }
    
    private void mockProxyContext(final MockedStatic<PipelineContextManager> pipelineContextManager) {
        mockProxyContext(pipelineContextManager, Collections.singletonMap(TARGET_DATA_SOURCE_NAME, mockStorageUnit(30, 40)));
    }
    
    private void mockProxyContext(final MockedStatic<PipelineContextManager> pipelineContextManager, final Map<String, StorageUnit> storageUnits) {
        ContextManager contextManager = mock(ContextManager.class);
        when(contextManager.getStorageUnits(DATABASE_NAME)).thenReturn(storageUnits);
        pipelineContextManager.when(PipelineContextManager::getProxyContext).thenReturn(contextManager);
    }
    
    private StorageUnit mockStorageUnit(final int maxPoolSize, final int maximumPoolSize) {
        StorageUnit result = mock(StorageUnit.class);
        when(result.getDataSourcePoolProperties())
                .thenReturn(new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", createDataSourceProperties(TARGET_JDBC_URL, maxPoolSize, maximumPoolSize)));
        return result;
    }
    
    private MigrationJobConfiguration createJobConfiguration() {
        Map<String, PipelineDataSourceConfiguration> sources = new LinkedHashMap<>(1, 1F);
        sources.put(SOURCE_DATA_SOURCE_NAME, new StandardPipelineDataSourceConfiguration(createDataSourceProperties(SOURCE_JDBC_URL, 2, 3)));
        return new MigrationJobConfiguration(createJobId(), DATABASE_NAME, databaseType, databaseType, sources,
                new ShardingSpherePipelineDataSourceConfiguration(createRootConfiguration(4, 5)), Collections.singletonList("t_order"),
                Collections.singletonMap("t_order", "foo_schema"), createJobDataNodeLine(), Collections.singletonList(createJobDataNodeLine()), 1, 3);
    }
    
    private String createJobId() {
        return PipelineJobIdUtils.marshal(new MigrationJobId(new PipelineContextKey(InstanceType.PROXY), Collections.singletonList(createJobDataNodeLine().marshal())));
    }
    
    private JobDataNodeLine createJobDataNodeLine() {
        return new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("t_order", Collections.singletonList(new DataNode(SOURCE_DATA_SOURCE_NAME + ".t_order")))));
    }
    
    private YamlRootConfiguration createRootConfiguration(final int maxPoolSize, final int maximumPoolSize) {
        YamlRootConfiguration result = new YamlRootConfiguration();
        result.setDatabaseName(DATABASE_NAME);
        result.setDataSources(Collections.singletonMap(TARGET_DATA_SOURCE_NAME, createDataSourceProperties(TARGET_JDBC_URL, maxPoolSize, maximumPoolSize)));
        result.setRules(Collections.emptyList());
        return result;
    }
    
    private Map<String, Object> createDataSourceProperties(final String jdbcUrl, final int maxPoolSize, final int maximumPoolSize) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("url", jdbcUrl);
        result.put("username", "root");
        result.put("password", "root");
        result.put("maxPoolSize", maxPoolSize);
        result.put("maximumPoolSize", maximumPoolSize);
        return result;
    }
    
    private TransmissionProcessContext mockProcessContext() {
        TransmissionProcessContext result = mock(TransmissionProcessContext.class);
        when(result.getProcessConfiguration()).thenReturn(new PipelineProcessConfiguration(new PipelineReadConfiguration(1, 10, 10, null),
                new PipelineWriteConfiguration(1, 50, null), null));
        return result;
    }
    
    private String getMaxPoolSize(final StandardPipelineDataSourceConfiguration dataSourceConfig) {
        DataSourcePoolProperties poolProps = (DataSourcePoolProperties) dataSourceConfig.getDataSourceConfiguration();
        return String.valueOf(poolProps.getPoolPropertySynonyms().getStandardProperties().get("maxPoolSize"));
    }
    
    private String getRootDataSourceMaxPoolSize(final ShardingSpherePipelineDataSourceConfiguration dataSourceConfig) {
        Collection<Map<String, Object>> dataSources = dataSourceConfig.getRootConfig().getDataSources().values();
        return String.valueOf(dataSources.iterator().next().get("maxPoolSize"));
    }
}

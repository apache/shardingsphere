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

import lombok.SneakyThrows;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.config.process.PipelineProcessConfigurationUtil;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.MemoryPipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPIImpl;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationProcessContext;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.yaml.process.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.process.YamlPipelineProcessConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.yaml.process.YamlPipelineReadConfiguration;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryFactory;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class PipelineContextUtil {
    
    private static final ExecuteEngine EXECUTE_ENGINE = ExecuteEngine.newCachedThreadInstance(PipelineContextUtil.class.getSimpleName());
    
    private static final PipelineChannelCreator PIPELINE_CHANNEL_CREATOR = new MemoryPipelineChannelCreator();
    
    private static final ClusterPersistRepositoryConfiguration PERSIST_REPOSITORY_CONFIG;
    
    private static final LazyInitializer<ClusterPersistRepository> PERSIST_REPOSITORY_LAZY_INITIALIZER;
    
    static {
        PERSIST_REPOSITORY_CONFIG = new ClusterPersistRepositoryConfiguration("Zookeeper", "test", EmbedTestingServer.getConnectionString(), new Properties());
        PERSIST_REPOSITORY_LAZY_INITIALIZER = new LazyInitializer<ClusterPersistRepository>() {
            
            @Override
            protected ClusterPersistRepository initialize() {
                return ClusterPersistRepositoryFactory.getInstance(PERSIST_REPOSITORY_CONFIG);
            }
        };
    }
    
    /**
     * Mock mode configuration and context manager.
     */
    public static void mockModeConfigAndContextManager() {
        EmbedTestingServer.start();
        mockModeConfig();
        mockContextManager();
    }
    
    private static void mockModeConfig() {
        if (null == PipelineContext.getModeConfig()) {
            PipelineContext.initModeConfig(createModeConfig());
        }
    }
    
    private static ModeConfiguration createModeConfig() {
        return new ModeConfiguration("Cluster", PERSIST_REPOSITORY_CONFIG);
    }
    
    private static void mockContextManager() {
        if (null != PipelineContext.getContextManager()) {
            return;
        }
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(
                ConfigurationFileUtil.readFile("config_sharding_sphere_jdbc_source.yaml"));
        ShardingSphereDataSource dataSource = (ShardingSphereDataSource) PipelineDataSourceFactory.newInstance(pipelineDataSourceConfig).getDataSource();
        ContextManager contextManager = getContextManager(dataSource);
        MetaDataPersistService persistService = new MetaDataPersistService(getClusterPersistRepository());
        MetaDataContexts metaDataContexts = renewMetaDataContexts(contextManager.getMetaDataContexts(), persistService);
        PipelineContext.initContextManager(new ContextManager(metaDataContexts, contextManager.getInstanceContext()));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static ContextManager getContextManager(final ShardingSphereDataSource dataSource) {
        Field field = ShardingSphereDataSource.class.getDeclaredField("contextManager");
        field.setAccessible(true);
        return (ContextManager) field.get(dataSource);
    }
    
    @SneakyThrows(ConcurrentException.class)
    private static ClusterPersistRepository getClusterPersistRepository() {
        return PERSIST_REPOSITORY_LAZY_INITIALIZER.get();
    }
    
    private static MetaDataContexts renewMetaDataContexts(final MetaDataContexts old, final MetaDataPersistService persistService) {
        Map<String, ShardingSphereTable> tables = new HashMap<>(3, 1);
        tables.put("t_order", new ShardingSphereTable("t_order", Arrays.asList(new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false),
                new ShardingSphereColumn("user_id", Types.VARCHAR, false, false, false, true, false)), Collections.emptyList(), Collections.emptyList()));
        old.getMetaData().getDatabase(DefaultDatabase.LOGIC_NAME).getSchema(DefaultDatabase.LOGIC_NAME).putAll(tables);
        return new MetaDataContexts(persistService, old.getMetaData());
    }
    
    /**
     * Get execute engine.
     *
     * @return execute engine
     */
    public static ExecuteEngine getExecuteEngine() {
        return EXECUTE_ENGINE;
    }
    
    /**
     * Get pipeline channel factory.
     *
     * @return channel factory
     */
    public static PipelineChannelCreator getPipelineChannelCreator() {
        return PIPELINE_CHANNEL_CREATOR;
    }
    
    /**
     * Mock migration job item context.
     *
     * @param jobConfig job configuration
     * @return job item context
     */
    public static MigrationJobItemContext mockMigrationJobItemContext(final MigrationJobConfiguration jobConfig) {
        PipelineProcessConfiguration processConfig = mockPipelineProcessConfiguration();
        MigrationProcessContext processContext = new MigrationProcessContext(jobConfig.getJobId(), processConfig);
        int jobShardingItem = 0;
        MigrationTaskConfiguration taskConfig = new MigrationJobAPIImpl().buildTaskConfiguration(jobConfig, jobShardingItem, processConfig);
        return new MigrationJobItemContext(jobConfig, jobShardingItem, null, processContext, taskConfig, new DefaultPipelineDataSourceManager());
    }
    
    private static PipelineProcessConfiguration mockPipelineProcessConfiguration() {
        YamlPipelineReadConfiguration yamlReadConfig = YamlPipelineReadConfiguration.buildWithDefaultValue();
        yamlReadConfig.setShardingSize(10);
        YamlPipelineProcessConfiguration yamlProcessConfig = new YamlPipelineProcessConfiguration();
        yamlProcessConfig.setRead(yamlReadConfig);
        PipelineProcessConfigurationUtil.fillInDefaultValue(yamlProcessConfig);
        return new YamlPipelineProcessConfigurationSwapper().swapToObject(yamlProcessConfig);
    }
}

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

package org.apache.shardingsphere.test.it.data.pipeline.core.util;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.common.config.process.yaml.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.yaml.YamlPipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.yaml.swapper.YamlPipelineProcessConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.common.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationProcessContext;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlModeConfigurationSwapper;
import org.apache.shardingsphere.metadata.persist.MetaDataBasedPersistService;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.NewMetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.test.it.data.pipeline.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.test.util.ConfigurationFileUtils;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Pipeline context utility class.
 */
public final class PipelineContextUtils {
    
    private static final PipelineContextKey CONTEXT_KEY = PipelineContextKey.buildForProxy();
    
    private static final ExecuteEngine EXECUTE_ENGINE = ExecuteEngine.newCachedThreadInstance(PipelineContextUtils.class.getSimpleName());
    
    private static final PipelineChannelCreator PIPELINE_CHANNEL_CREATOR = TypedSPILoader.getService(PipelineChannelCreator.class, "MEMORY");
    
    /**
     * Mock mode configuration and context manager.
     */
    @SneakyThrows
    public static void mockModeConfigAndContextManager() {
        EmbedTestingServer.start();
        PipelineContextKey contextKey = getContextKey();
        if (null != PipelineContextManager.getContext(contextKey)) {
            return;
        }
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(
                ConfigurationFileUtils.readFileAndIgnoreComments("config_sharding_sphere_jdbc_source.yaml"));
        YamlRootConfiguration rootConfig = (YamlRootConfiguration) pipelineDataSourceConfig.getDataSourceConfiguration();
        ModeConfiguration modeConfig = new YamlModeConfigurationSwapper().swapToObject(rootConfig.getMode());
        ShardingSphereDataSource dataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSourceWithoutCache(rootConfig);
        ContextManager contextManager = getContextManager(dataSource);
        ClusterPersistRepository persistRepository = getClusterPersistRepository((ClusterPersistRepositoryConfiguration) modeConfig.getRepository());
        MetaDataBasedPersistService persistService = "Cluster".equals(modeConfig.getType()) ? new NewMetaDataPersistService(persistRepository) : new MetaDataPersistService(persistRepository);
        MetaDataContexts metaDataContexts = renewMetaDataContexts(contextManager.getMetaDataContexts(), persistService);
        PipelineContext pipelineContext = new PipelineContext(modeConfig, new ContextManager(metaDataContexts, contextManager.getInstanceContext()));
        PipelineContextManager.putContext(contextKey, pipelineContext);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static ContextManager getContextManager(final ShardingSphereDataSource dataSource) {
        return (ContextManager) Plugins.getMemberAccessor().get(ShardingSphereDataSource.class.getDeclaredField("contextManager"), dataSource);
    }
    
    private static ClusterPersistRepository getClusterPersistRepository(final ClusterPersistRepositoryConfiguration repositoryConfig) {
        ClusterPersistRepository result = TypedSPILoader.getService(ClusterPersistRepository.class, repositoryConfig.getType(), repositoryConfig.getProps());
        result.init(repositoryConfig);
        return result;
    }
    
    private static MetaDataContexts renewMetaDataContexts(final MetaDataContexts old, final MetaDataBasedPersistService persistService) {
        Map<String, ShardingSphereTable> tables = new HashMap<>(3, 1F);
        tables.put("t_order", new ShardingSphereTable("t_order", Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_order_item", new ShardingSphereTable("t_order_item", Arrays.asList(
                new ShardingSphereColumn("item_id", Types.INTEGER, true, false, false, true, false),
                new ShardingSphereColumn("order_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false)),
                Collections.emptyList(), Collections.emptyList()));
        old.getMetaData().getDatabase("logic_db").getSchema("logic_db").putAll(tables);
        return new MetaDataContexts(persistService, old.getMetaData());
    }
    
    /**
     * Get create order table schema.
     *
     * @return order table schema
     */
    public static String getCreateOrderTableSchema() {
        return "CREATE TABLE IF NOT EXISTS t_order (order_id INT PRIMARY KEY, user_id INT, status VARCHAR(32))";
    }
    
    /**
     * Mock order_id column meta data.
     *
     * @return mocked column meta data
     */
    public static PipelineColumnMetaData mockOrderIdColumnMetaData() {
        return new PipelineColumnMetaData(1, "order_id", Types.INTEGER, "int", false, true, true);
    }
    
    /**
     * Get context key.
     *
     * @return context key
     */
    public static PipelineContextKey getContextKey() {
        return CONTEXT_KEY;
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
        MigrationTaskConfiguration taskConfig = new MigrationJobAPI().buildTaskConfiguration(jobConfig, jobShardingItem, processConfig);
        return new MigrationJobItemContext(jobConfig, jobShardingItem, null, processContext, taskConfig, new DefaultPipelineDataSourceManager());
    }
    
    private static PipelineProcessConfiguration mockPipelineProcessConfiguration() {
        YamlPipelineReadConfiguration yamlReadConfig = YamlPipelineReadConfiguration.buildWithDefaultValue();
        yamlReadConfig.setShardingSize(10);
        YamlPipelineProcessConfiguration yamlProcessConfig = new YamlPipelineProcessConfiguration();
        yamlProcessConfig.setRead(yamlReadConfig);
        PipelineProcessConfigurationUtils.fillInDefaultValue(yamlProcessConfig);
        return new YamlPipelineProcessConfigurationSwapper().swapToObject(yamlProcessConfig);
    }
}

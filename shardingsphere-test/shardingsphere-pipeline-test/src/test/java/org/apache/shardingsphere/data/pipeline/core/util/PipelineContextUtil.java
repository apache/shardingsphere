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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.data.pipeline.core.spi.ingest.channel.MemoryPipelineChannelFactory;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryFactory;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public final class PipelineContextUtil {
    
    private static final ExecuteEngine EXECUTE_ENGINE = ExecuteEngine.newCachedThreadInstance();
    
    private static final PipelineChannelFactory PIPELINE_CHANNEL_FACTORY = new MemoryPipelineChannelFactory();
    
    private static final ClusterPersistRepositoryConfiguration PERSIST_REPOSITORY_CONFIG;
    
    private static final LazyInitializer<ClusterPersistRepository> PERSIST_REPOSITORY_LAZY_INITIALIZER;
    
    static {
        PERSIST_REPOSITORY_CONFIG = new ClusterPersistRepositoryConfiguration("Zookeeper", "test", EmbedTestingServer.getConnectionString(), new Properties());
        PERSIST_REPOSITORY_LAZY_INITIALIZER = new LazyInitializer<ClusterPersistRepository>() {
            
            @Override
            protected ClusterPersistRepository initialize() {
                return ClusterPersistRepositoryFactory.newInstance(PERSIST_REPOSITORY_CONFIG, new InstanceDefinition(InstanceType.PROXY));
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
        if (null != PipelineContext.getModeConfig()) {
            return;
        }
        PipelineContext.initModeConfig(createModeConfig());
    }
    
    private static ModeConfiguration createModeConfig() {
        return new ModeConfiguration("Cluster", PERSIST_REPOSITORY_CONFIG, true);
    }
    
    private static void mockContextManager() {
        if (null != PipelineContext.getContextManager()) {
            return;
        }
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(
                ConfigurationFileUtil.readFile("config_sharding_sphere_jdbc_source.yaml"));
        ShardingSphereDataSource shardingSphereDataSource = (ShardingSphereDataSource) new PipelineDataSourceFactory().newInstance(pipelineDataSourceConfig).getDataSource();
        ContextManager contextManager = shardingSphereDataSource.getContextManager();
        MetaDataPersistService metaDataPersistService = new MetaDataPersistService(getClusterPersistRepository());
        MetaDataContexts metaDataContexts = renewMetaDataContexts(contextManager.getMetaDataContexts(), metaDataPersistService);
        contextManager.init(metaDataContexts, contextManager.getTransactionContexts(), contextManager.getInstanceContext(), contextManager.getLockContext());
        PipelineContext.initContextManager(contextManager);
    }
    
    @SneakyThrows(ConcurrentException.class)
    private static ClusterPersistRepository getClusterPersistRepository() {
        return PERSIST_REPOSITORY_LAZY_INITIALIZER.get();
    }
    
    private static MetaDataContexts renewMetaDataContexts(final MetaDataContexts old, final MetaDataPersistService metaDataPersistService) {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(3, 1);
        tableMetaDataMap.put("t_order", new TableMetaData("t_order", Arrays.asList(new ColumnMetaData("order_id", Types.INTEGER, true, false, false),
                new ColumnMetaData("user_id", Types.VARCHAR, false, false, false)), Collections.emptyList(), Collections.emptyList()));
        old.getMetaDataMap().get("logic_db").getDefaultSchema().putAll(tableMetaDataMap);
        return new MetaDataContexts(metaDataPersistService, old.getMetaDataMap(), old.getGlobalRuleMetaData(), old.getExecutorEngine(), old.getOptimizerContext(), old.getProps());
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
    public static PipelineChannelFactory getPipelineChannelFactory() {
        return PIPELINE_CHANNEL_FACTORY;
    }
}

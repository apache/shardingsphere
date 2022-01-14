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
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.data.pipeline.core.spi.ingest.channel.MemoryPipelineChannelFactory;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;

import java.util.Properties;

@Slf4j
public final class PipelineContextUtil {
    
    private static final ExecuteEngine EXECUTE_ENGINE = ExecuteEngine.newCachedThreadInstance();
    
    private static final PipelineChannelFactory PIPELINE_CHANNEL_FACTORY = new MemoryPipelineChannelFactory();
    
    /**
     * Mock mode configuration.
     */
    @SneakyThrows
    public static void mockModeConfig() {
        PipelineContext.initModeConfig(createModeConfig());
    }
    
    private static ModeConfiguration createModeConfig() {
        return new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("Zookeeper", "test", EmbedTestingServer.getConnectionString(), new Properties()), true);
    }
    
    /**
     * Mock context manager.
     */
    public static void mockContextManager() {
        ShardingSpherePipelineDataSourceConfiguration pipelineDataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(
                ResourceUtil.readFileToString("/config_sharding_sphere_jdbc_source.yaml"));
        ShardingSphereDataSource shardingSphereDataSource = (ShardingSphereDataSource) new PipelineDataSourceFactory().newInstance(pipelineDataSourceConfig).getDataSource();
        ContextManager contextManager = shardingSphereDataSource.getContextManager();
        PipelineContext.initContextManager(contextManager);
        try {
            shardingSphereDataSource.close();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("close data source failed", ex);
        }
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

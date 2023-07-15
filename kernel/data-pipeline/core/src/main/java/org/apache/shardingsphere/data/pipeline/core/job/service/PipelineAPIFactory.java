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

package org.apache.shardingsphere.data.pipeline.core.job.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.elasticjob.CoordinatorRegistryCenterInitializer;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobOperateAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobStatisticsAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.operate.JobOperateAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.settings.JobConfigurationAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.statistics.JobStatisticsAPIImpl;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pipeline API factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineAPIFactory {
    
    private static final Map<PipelineContextKey, LazyInitializer<GovernanceRepositoryAPI>> GOVERNANCE_REPOSITORY_API_MAP = new ConcurrentHashMap<>();
    
    /**
     * Get governance repository API.
     *
     * @param contextKey context key
     * @return governance repository API
     */
    @SneakyThrows(ConcurrentException.class)
    public static GovernanceRepositoryAPI getGovernanceRepositoryAPI(final PipelineContextKey contextKey) {
        return GOVERNANCE_REPOSITORY_API_MAP.computeIfAbsent(contextKey, key -> new LazyInitializer<GovernanceRepositoryAPI>() {
            
            @Override
            protected GovernanceRepositoryAPI initialize() {
                ContextManager contextManager = PipelineContextManager.getContext(contextKey).getContextManager();
                return new GovernanceRepositoryAPIImpl((ClusterPersistRepository) contextManager.getMetaDataContexts().getPersistService().getRepository());
            }
        }).get();
    }
    
    /**
     * Get job statistics API.
     *
     * @param contextKey context key
     * @return job statistics API
     */
    public static JobStatisticsAPI getJobStatisticsAPI(final PipelineContextKey contextKey) {
        return ElasticJobAPIHolder.getInstance(contextKey).jobStatisticsAPI;
    }
    
    /**
     * Get job configuration API.
     *
     * @param contextKey context key
     * @return job configuration API
     */
    public static JobConfigurationAPI getJobConfigurationAPI(final PipelineContextKey contextKey) {
        return ElasticJobAPIHolder.getInstance(contextKey).jobConfigurationAPI;
    }
    
    /**
     * Get job operate API.
     *
     * @param contextKey context key
     * @return job operate API
     */
    public static JobOperateAPI getJobOperateAPI(final PipelineContextKey contextKey) {
        return ElasticJobAPIHolder.getInstance(contextKey).jobOperateAPI;
    }
    
    /**
     * Get registry center.
     *
     * @param contextKey context key
     * @return Coordinator registry center
     */
    public static CoordinatorRegistryCenter getRegistryCenter(final PipelineContextKey contextKey) {
        return RegistryCenterHolder.getInstance(contextKey).registryCenter;
    }
    
    private static final class ElasticJobAPIHolder {
        
        private static final Map<PipelineContextKey, ElasticJobAPIHolder> INSTANCE_MAP = new ConcurrentHashMap<>();
        
        private final JobStatisticsAPI jobStatisticsAPI;
        
        private final JobConfigurationAPI jobConfigurationAPI;
        
        private final JobOperateAPI jobOperateAPI;
        
        private ElasticJobAPIHolder(final PipelineContextKey contextKey) {
            CoordinatorRegistryCenter registryCenter = getRegistryCenter(contextKey);
            jobStatisticsAPI = new JobStatisticsAPIImpl(registryCenter);
            jobConfigurationAPI = new JobConfigurationAPIImpl(registryCenter);
            jobOperateAPI = new JobOperateAPIImpl(registryCenter);
        }
        
        public static ElasticJobAPIHolder getInstance(final PipelineContextKey contextKey) {
            return INSTANCE_MAP.computeIfAbsent(contextKey, key -> new ElasticJobAPIHolder(contextKey));
        }
    }
    
    private static final class RegistryCenterHolder {
        
        private static final Map<PipelineContextKey, RegistryCenterHolder> INSTANCE_MAP = new ConcurrentHashMap<>();
        
        private final CoordinatorRegistryCenter registryCenter;
        
        private RegistryCenterHolder(final PipelineContextKey contextKey) {
            registryCenter = createRegistryCenter(contextKey);
        }
        
        private CoordinatorRegistryCenter createRegistryCenter(final PipelineContextKey contextKey) {
            CoordinatorRegistryCenterInitializer registryCenterInitializer = new CoordinatorRegistryCenterInitializer();
            PipelineContext pipelineContext = PipelineContextManager.getContext(contextKey);
            ModeConfiguration modeConfig = pipelineContext.getModeConfig();
            String elasticJobNamespace = PipelineMetaDataNode.getElasticJobNamespace();
            String clusterType = modeConfig.getRepository().getType();
            if ("ZooKeeper".equals(clusterType)) {
                return registryCenterInitializer.createZookeeperRegistryCenter(modeConfig, elasticJobNamespace);
            } else {
                throw new IllegalArgumentException("Unsupported cluster type: " + clusterType);
            }
        }
        
        public static RegistryCenterHolder getInstance(final PipelineContextKey contextKey) {
            return INSTANCE_MAP.computeIfAbsent(contextKey, key -> new RegistryCenterHolder(contextKey));
        }
    }
}

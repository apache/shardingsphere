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

package org.apache.shardingsphere.data.pipeline.core.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.data.pipeline.core.api.impl.GovernanceRepositoryAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.registry.CoordinatorRegistryCenterInitializer;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobAPIFactory;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobOperateAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobStatisticsAPI;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pipeline API factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineAPIFactory {
    
    private static final LazyInitializer<GovernanceRepositoryAPI> REPOSITORY_API_LAZY_INITIALIZER = new LazyInitializer<GovernanceRepositoryAPI>() {
        
        @Override
        protected GovernanceRepositoryAPI initialize() {
            return new GovernanceRepositoryAPIImpl((ClusterPersistRepository) PipelineContext.getContextManager().getMetaDataContexts().getPersistService().getRepository());
        }
    };
    
    /**
     * Get governance repository API.
     *
     * @return governance repository API
     */
    @SneakyThrows(ConcurrentException.class)
    public static GovernanceRepositoryAPI getGovernanceRepositoryAPI() {
        return REPOSITORY_API_LAZY_INITIALIZER.get();
    }
    
    /**
     * Get job statistics API.
     *
     * @param contextKey context key
     * @return job statistics API
     */
    public static JobStatisticsAPI getJobStatisticsAPI(final PipelineContextKey contextKey) {
        return ElasticJobAPIHolder.getInstance(contextKey).getJobStatisticsAPI();
    }
    
    /**
     * Get job configuration API.
     *
     * @param contextKey context key
     * @return job configuration API
     */
    public static JobConfigurationAPI getJobConfigurationAPI(final PipelineContextKey contextKey) {
        return ElasticJobAPIHolder.getInstance(contextKey).getJobConfigurationAPI();
    }
    
    /**
     * Get job operate API.
     *
     * @param contextKey context key
     * @return job operate API
     */
    public static JobOperateAPI getJobOperateAPI(final PipelineContextKey contextKey) {
        return ElasticJobAPIHolder.getInstance(contextKey).getJobOperateAPI();
    }
    
    /**
     * Get registry center.
     *
     * @return Coordinator registry center
     */
    public static CoordinatorRegistryCenter getRegistryCenter() {
        return RegistryCenterHolder.getInstance();
    }
    
    @Getter
    private static final class ElasticJobAPIHolder {
        
        private static final Map<PipelineContextKey, ElasticJobAPIHolder> INSTANCE_MAP = new ConcurrentHashMap<>();
        
        private final PipelineContextKey contextKey;
        
        private final JobStatisticsAPI jobStatisticsAPI;
        
        private final JobConfigurationAPI jobConfigurationAPI;
        
        private final JobOperateAPI jobOperateAPI;
        
        private ElasticJobAPIHolder(final PipelineContextKey contextKey) {
            this.contextKey = contextKey;
            ClusterPersistRepositoryConfiguration repositoryConfig = (ClusterPersistRepositoryConfiguration) PipelineContextManager.getContext(contextKey).getModeConfig().getRepository();
            String namespace = repositoryConfig.getNamespace() + PipelineMetaDataNode.getElasticJobNamespace();
            jobStatisticsAPI = JobAPIFactory.createJobStatisticsAPI(repositoryConfig.getServerLists(), namespace, null);
            jobConfigurationAPI = JobAPIFactory.createJobConfigurationAPI(repositoryConfig.getServerLists(), namespace, null);
            jobOperateAPI = JobAPIFactory.createJobOperateAPI(repositoryConfig.getServerLists(), namespace, null);
        }
        
        public static ElasticJobAPIHolder getInstance(final PipelineContextKey contextKey) {
            ElasticJobAPIHolder result = INSTANCE_MAP.get(contextKey);
            if (null != result) {
                return result;
            }
            synchronized (INSTANCE_MAP) {
                result = INSTANCE_MAP.get(contextKey);
                if (null == result) {
                    result = new ElasticJobAPIHolder(contextKey);
                    INSTANCE_MAP.put(contextKey, result);
                }
            }
            return result;
        }
    }
    
    private static final class RegistryCenterHolder {
        
        private static volatile CoordinatorRegistryCenter instance;
        
        public static CoordinatorRegistryCenter getInstance() {
            if (null == instance) {
                synchronized (PipelineAPIFactory.class) {
                    if (null == instance) {
                        instance = createRegistryCenter();
                    }
                }
            }
            return instance;
        }
        
        private static CoordinatorRegistryCenter createRegistryCenter() {
            CoordinatorRegistryCenterInitializer registryCenterInitializer = new CoordinatorRegistryCenterInitializer();
            ModeConfiguration modeConfig = PipelineContext.getModeConfig();
            return registryCenterInitializer.createRegistryCenter(modeConfig, PipelineMetaDataNode.getElasticJobNamespace());
        }
    }
}

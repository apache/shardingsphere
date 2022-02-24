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
import org.apache.shardingsphere.data.pipeline.core.api.impl.GovernanceRepositoryAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.registry.CoordinatorRegistryCenterInitializer;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobAPIFactory;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobOperateAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobStatisticsAPI;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

/**
 * Pipeline API factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineAPIFactory {
    
    /**
     * Get governance repository API.
     *
     * @return governance repository API
     */
    public static GovernanceRepositoryAPI getGovernanceRepositoryAPI() {
        return GovernanceRepositoryAPIHolder.getInstance();
    }
    
    /**
     * Get job statistics API.
     *
     * @return job statistics API
     */
    public static JobStatisticsAPI getJobStatisticsAPI() {
        return ElasticJobAPIHolder.getInstance().getJobStatisticsAPI();
    }
    
    /**
     * Get job configuration API.
     *
     * @return job configuration API
     */
    public static JobConfigurationAPI getJobConfigurationAPI() {
        return ElasticJobAPIHolder.getInstance().getJobConfigurationAPI();
    }
    
    /**
     * Get job operate API.
     *
     * @return job operate API
     */
    public static JobOperateAPI getJobOperateAPI() {
        return ElasticJobAPIHolder.getInstance().getJobOperateAPI();
    }
    
    /**
     * Get registry center.
     *
     * @return Coordinator registry center
     */
    public static CoordinatorRegistryCenter getRegistryCenter() {
        return RegistryCenterHolder.getInstance();
    }
    
    private static final class GovernanceRepositoryAPIHolder {
        
        private static volatile GovernanceRepositoryAPI instance;
        
        static {
            ShardingSphereServiceLoader.register(ClusterPersistRepository.class);
        }
        
        public static GovernanceRepositoryAPI getInstance() {
            if (null == instance) {
                synchronized (PipelineAPIFactory.class) {
                    if (null == instance) {
                        instance = createGovernanceRepositoryAPI();
                    }
                }
            }
            return instance;
        }
        
        private static GovernanceRepositoryAPI createGovernanceRepositoryAPI() {
            ClusterPersistRepositoryConfiguration repositoryConfig = (ClusterPersistRepositoryConfiguration) PipelineContext.getModeConfig().getRepository();
            ClusterPersistRepository repository = TypedSPIRegistry.getRegisteredService(ClusterPersistRepository.class, repositoryConfig.getType(), repositoryConfig.getProps());
            repository.init(repositoryConfig);
            return new GovernanceRepositoryAPIImpl(repository);
        }
    }
    
    @Getter
    private static final class ElasticJobAPIHolder {
        
        private static volatile ElasticJobAPIHolder instance;
        
        private final JobStatisticsAPI jobStatisticsAPI;
        
        private final JobConfigurationAPI jobConfigurationAPI;
        
        private final JobOperateAPI jobOperateAPI;
        
        private ElasticJobAPIHolder() {
            ClusterPersistRepositoryConfiguration repositoryConfig = (ClusterPersistRepositoryConfiguration) PipelineContext.getModeConfig().getRepository();
            String namespace = repositoryConfig.getNamespace() + DataPipelineConstants.DATA_PIPELINE_ROOT;
            jobStatisticsAPI = JobAPIFactory.createJobStatisticsAPI(repositoryConfig.getServerLists(), namespace, null);
            jobConfigurationAPI = JobAPIFactory.createJobConfigurationAPI(repositoryConfig.getServerLists(), namespace, null);
            jobOperateAPI = JobAPIFactory.createJobOperateAPI(repositoryConfig.getServerLists(), namespace, null);
        }
        
        public static ElasticJobAPIHolder getInstance() {
            if (null == instance) {
                synchronized (PipelineAPIFactory.class) {
                    if (null == instance) {
                        instance = new ElasticJobAPIHolder();
                    }
                }
            }
            return instance;
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
            return registryCenterInitializer.createRegistryCenter(modeConfig, DataPipelineConstants.DATA_PIPELINE_ROOT);
        }
    }
}

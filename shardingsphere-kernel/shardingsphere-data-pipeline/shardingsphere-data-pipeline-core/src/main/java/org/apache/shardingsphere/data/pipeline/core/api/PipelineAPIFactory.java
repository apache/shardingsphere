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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.api.impl.GovernanceRepositoryAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredContext;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobAPIFactory;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobOperateAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobStatisticsAPI;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import java.util.Properties;

/**
 * Pipeline API factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
// TODO separate methods
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
    
    private static void checkServerConfig() {
        ModeConfiguration modeConfig = RuleAlteredContext.getModeConfig();
        Preconditions.checkNotNull(modeConfig, "Mode configuration is required.");
        Preconditions.checkArgument("Cluster".equals(modeConfig.getType()), "Mode must be `Cluster`.");
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
            checkServerConfig();
            ClusterPersistRepositoryConfiguration repositoryConfig = (ClusterPersistRepositoryConfiguration) RuleAlteredContext.getModeConfig().getRepository();
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
            checkServerConfig();
            ClusterPersistRepositoryConfiguration repositoryConfig = (ClusterPersistRepositoryConfiguration) RuleAlteredContext.getModeConfig().getRepository();
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
            CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(getZookeeperConfig());
            result.init();
            return result;
        }
        
        private static ZookeeperConfiguration getZookeeperConfig() {
            checkServerConfig();
            ClusterPersistRepositoryConfiguration repositoryConfig = (ClusterPersistRepositoryConfiguration) RuleAlteredContext.getModeConfig().getRepository();
            ZookeeperConfiguration result = new ZookeeperConfiguration(repositoryConfig.getServerLists(), repositoryConfig.getNamespace() + DataPipelineConstants.DATA_PIPELINE_ROOT);
            Properties props = repositoryConfig.getProps();
            result.setMaxSleepTimeMilliseconds(getProperty(props, "max.sleep.time.milliseconds", result.getMaxSleepTimeMilliseconds()));
            result.setBaseSleepTimeMilliseconds(getProperty(props, "base.sleep.time.milliseconds", result.getBaseSleepTimeMilliseconds()));
            result.setConnectionTimeoutMilliseconds(getProperty(props, "connection.timeout.milliseconds", result.getConnectionTimeoutMilliseconds()));
            result.setSessionTimeoutMilliseconds(getProperty(props, "session.timeout.milliseconds", result.getSessionTimeoutMilliseconds()));
            return result;
        }
        
        private static int getProperty(final Properties props, final String key, final int defaultValue) {
            return Strings.isNullOrEmpty(props.getProperty(key)) ? defaultValue : Integer.parseInt(props.getProperty(key));
        }
    }
}

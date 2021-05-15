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

package org.apache.shardingsphere.scaling.core.api;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobAPIFactory;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobOperateAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobStatisticsAPI;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.scaling.core.api.impl.GovernanceRepositoryAPIImpl;
import org.apache.shardingsphere.scaling.core.api.impl.ScalingAPIImpl;
import org.apache.shardingsphere.scaling.core.common.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;

import java.util.Properties;

/**
 * Scaling API factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScalingAPIFactory {
    
    /**
     * Get scaling API.
     *
     * @return scaling API
     */
    public static ScalingAPI getScalingAPI() {
        return ScalingAPIHolder.getInstance();
    }
    
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
        ServerConfiguration serverConfig = ScalingContext.getInstance().getServerConfig();
        Preconditions.checkNotNull(serverConfig, "Scaling server configuration is required.");
        Preconditions.checkNotNull(serverConfig.getGovernanceConfig(), "Governance configuration is required.");
    }
    
    private static final class ScalingAPIHolder {
        
        private static volatile ScalingAPI instance;
        
        public static ScalingAPI getInstance() {
            if (null == instance) {
                synchronized (ScalingAPIFactory.class) {
                    if (null == instance) {
                        checkServerConfig();
                        instance = new ScalingAPIImpl();
                    }
                }
            }
            return instance;
        }
    }
    
    private static final class GovernanceRepositoryAPIHolder {
        
        private static volatile GovernanceRepositoryAPI instance;
        
        static {
            ShardingSphereServiceLoader.register(RegistryCenterRepository.class);
        }
        
        public static GovernanceRepositoryAPI getInstance() {
            if (null == instance) {
                synchronized (ScalingAPIFactory.class) {
                    if (null == instance) {
                        instance = createGovernanceRepositoryAPI();
                    }
                }
            }
            return instance;
        }
        
        private static GovernanceRepositoryAPI createGovernanceRepositoryAPI() {
            checkServerConfig();
            GovernanceConfiguration governanceConfig = ScalingContext.getInstance().getServerConfig().getGovernanceConfig();
            RegistryCenterConfiguration registryCenterConfig = governanceConfig.getRegistryCenterConfiguration();
            RegistryCenterRepository registryCenterRepository = TypedSPIRegistry.getRegisteredService(RegistryCenterRepository.class, registryCenterConfig.getType(), registryCenterConfig.getProps());
            registryCenterRepository.init(governanceConfig.getName(), registryCenterConfig);
            return new GovernanceRepositoryAPIImpl(registryCenterRepository);
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
            GovernanceConfiguration governanceConfig = ScalingContext.getInstance().getServerConfig().getGovernanceConfig();
            String namespace = governanceConfig.getName() + ScalingConstant.SCALING_ROOT;
            jobStatisticsAPI = JobAPIFactory.createJobStatisticsAPI(governanceConfig.getRegistryCenterConfiguration().getServerLists(), namespace, null);
            jobConfigurationAPI = JobAPIFactory.createJobConfigurationAPI(governanceConfig.getRegistryCenterConfiguration().getServerLists(), namespace, null);
            jobOperateAPI = JobAPIFactory.createJobOperateAPI(governanceConfig.getRegistryCenterConfiguration().getServerLists(), namespace, null);
        }
        
        public static ElasticJobAPIHolder getInstance() {
            if (null == instance) {
                synchronized (ScalingAPIFactory.class) {
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
                synchronized (ScalingAPIFactory.class) {
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
            GovernanceConfiguration governanceConfig = ScalingContext.getInstance().getServerConfig().getGovernanceConfig();
            ZookeeperConfiguration result = new ZookeeperConfiguration(governanceConfig.getRegistryCenterConfiguration().getServerLists(),
                    governanceConfig.getName() + ScalingConstant.SCALING_ROOT);
            Properties props = governanceConfig.getRegistryCenterConfiguration().getProps();
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

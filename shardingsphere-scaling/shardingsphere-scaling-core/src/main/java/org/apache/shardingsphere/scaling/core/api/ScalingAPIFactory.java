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
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobStatisticsAPI;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.scaling.core.api.impl.RegistryRepositoryAPIImpl;
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
    
    static {
        ServerConfiguration serverConfig = ScalingContext.getInstance().getServerConfig();
        Preconditions.checkArgument(null != serverConfig, "Scaling server configuration is required.");
        Preconditions.checkArgument(null != serverConfig.getGovernanceConfig(), "Governance configuration is required.");
    }
    
    /**
     * Get scaling API.
     *
     * @return scaling API
     */
    public static ScalingAPI getScalingAPI() {
        return ScalingAPIHolder.INSTANCE;
    }
    
    /**
     * Get registry repository API.
     *
     * @return registry repository API
     */
    public static RegistryRepositoryAPI getRegistryRepositoryAPI() {
        return RegistryRepositoryAPIHolder.INSTANCE;
    }
    
    /**
     * Get job statistics API.
     *
     * @return job statistics API
     */
    public static JobStatisticsAPI getJobStatisticsAPI() {
        return ElasticJobAPIHolder.INSTANCE.getJobStatisticsAPI();
    }
    
    /**
     * Get job configuration API.
     *
     * @return job configuration API
     */
    public static JobConfigurationAPI getJobConfigurationAPI() {
        return ElasticJobAPIHolder.INSTANCE.getJobConfigurationAPI();
    }
    
    /**
     * Get registry center.
     *
     * @return Coordinator registry center
     */
    public static CoordinatorRegistryCenter getRegistryCenter() {
        return RegistryCenterHolder.INSTANCE;
    }
    
    private static final class ScalingAPIHolder {
        
        private static final ScalingAPI INSTANCE = new ScalingAPIImpl();
    }
    
    private static final class RegistryRepositoryAPIHolder {
        
        private static final RegistryRepositoryAPI INSTANCE;
        
        static {
            ShardingSphereServiceLoader.register(RegistryRepository.class);
            ShardingSphereServiceLoader.register(ConfigurationRepository.class);
            GovernanceConfiguration governanceConfig = ScalingContext.getInstance().getServerConfig().getGovernanceConfig();
            GovernanceCenterConfiguration registryCenterConfig = governanceConfig.getRegistryCenterConfiguration();
            RegistryRepository registryRepository = TypedSPIRegistry.getRegisteredService(RegistryRepository.class, registryCenterConfig.getType(), registryCenterConfig.getProps());
            registryRepository.init(governanceConfig.getName(), registryCenterConfig);
            INSTANCE = new RegistryRepositoryAPIImpl(registryRepository);
        }
    }
    
    @Getter
    private static final class ElasticJobAPIHolder {
        
        private static final ElasticJobAPIHolder INSTANCE = new ElasticJobAPIHolder();
        
        private final JobStatisticsAPI jobStatisticsAPI;
        
        private final JobConfigurationAPI jobConfigurationAPI;
        
        private ElasticJobAPIHolder() {
            GovernanceConfiguration governanceConfig = ScalingContext.getInstance().getServerConfig().getGovernanceConfig();
            String namespace = governanceConfig.getName() + ScalingConstant.SCALING_ROOT;
            jobStatisticsAPI = JobAPIFactory.createJobStatisticsAPI(governanceConfig.getRegistryCenterConfiguration().getServerLists(), namespace, null);
            jobConfigurationAPI = JobAPIFactory.createJobConfigurationAPI(governanceConfig.getRegistryCenterConfiguration().getServerLists(), namespace, null);
        }
    }
    
    private static final class RegistryCenterHolder {
        
        private static final CoordinatorRegistryCenter INSTANCE = createRegistryCenter();
        
        private static CoordinatorRegistryCenter createRegistryCenter() {
            CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(getZookeeperConfig());
            result.init();
            return result;
        }
        
        private static ZookeeperConfiguration getZookeeperConfig() {
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

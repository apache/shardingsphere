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

package org.apache.shardingsphere.data.pipeline.core.registry;

import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.props.ZookeeperProperties;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.props.ZookeeperPropertyKey;

import java.util.Properties;

/**
 * {@linkplain CoordinatorRegistryCenter} initializer.
 */
public final class CoordinatorRegistryCenterInitializer {
    
    /**
     * Create registry center instance.
     *
     * @param modeConfig mode configuration
     * @param namespaceRelativePath namespace relative path
     * @return registry center instance
     */
    public CoordinatorRegistryCenter createRegistryCenter(final ModeConfiguration modeConfig, final String namespaceRelativePath) {
        ClusterPersistRepositoryConfiguration repositoryConfig = (ClusterPersistRepositoryConfiguration) modeConfig.getRepository();
        String clusterType = modeConfig.getRepository().getType();
        if ("ZooKeeper".equalsIgnoreCase(clusterType)) {
            CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(getZookeeperConfig(repositoryConfig, namespaceRelativePath));
            result.init();
            return result;
        }
        throw new IllegalArgumentException("Unsupported clusterType=" + clusterType);
    }
    
    // TODO Merge registry center code in ElasticJob and ShardingSphere mode; Use spi to load impl;
    private ZookeeperConfiguration getZookeeperConfig(final ClusterPersistRepositoryConfiguration repositoryConfig, final String namespaceRelativePath) {
        Properties props = repositoryConfig.getProps();
        ZookeeperProperties zookeeperProps = new ZookeeperProperties(props);
        String namespace = repositoryConfig.getNamespace() + (null != namespaceRelativePath ? namespaceRelativePath : "");
        ZookeeperConfiguration result = new ZookeeperConfiguration(repositoryConfig.getServerLists(), namespace);
        int retryIntervalMilliseconds = zookeeperProps.getValue(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS);
        result.setBaseSleepTimeMilliseconds(retryIntervalMilliseconds);
        int maxRetries = zookeeperProps.getValue(ZookeeperPropertyKey.MAX_RETRIES);
        result.setMaxRetries(maxRetries);
        result.setMaxSleepTimeMilliseconds(retryIntervalMilliseconds * maxRetries);
        int timeToLiveSeconds = zookeeperProps.getValue(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS);
        if (0 != timeToLiveSeconds) {
            result.setSessionTimeoutMilliseconds(timeToLiveSeconds * 1000);
        }
        int operationTimeoutMilliseconds = zookeeperProps.getValue(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS);
        if (0 != operationTimeoutMilliseconds) {
            result.setConnectionTimeoutMilliseconds(operationTimeoutMilliseconds);
        }
        String digest = zookeeperProps.getValue(ZookeeperPropertyKey.DIGEST);
        result.setDigest(digest);
        return result;
    }
}

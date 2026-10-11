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

package org.apache.shardingsphere.mode.repository.cluster.zookeeper;

import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.props.ZookeeperProperties;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.props.ZookeeperPropertyKey;
import org.apache.shardingsphere.schedule.spi.CoordinatorRegistryCenterProvider;

/**
 * ZooKeeper coordinator registry center provider.
 */
public final class ZookeeperCoordinatorRegistryCenterProvider implements CoordinatorRegistryCenterProvider {
    
    @Override
    public CoordinatorRegistryCenter create(final ClusterPersistRepositoryConfiguration repositoryConfig, final String namespaceRelativePath) {
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(createConfiguration(repositoryConfig, namespaceRelativePath));
        try {
            result.init();
            return result;
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            try {
                result.close();
                // CHECKSTYLE:OFF
            } catch (final RuntimeException closeEx) {
                // CHECKSTYLE:ON
                ex.addSuppressed(closeEx);
            }
            throw ex;
        }
    }
    
    private ZookeeperConfiguration createConfiguration(final ClusterPersistRepositoryConfiguration repositoryConfig, final String namespaceRelativePath) {
        ZookeeperProperties zookeeperProps = new ZookeeperProperties(repositoryConfig.getProps());
        String namespace = repositoryConfig.getNamespace() + (null == namespaceRelativePath ? "" : namespaceRelativePath);
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
        result.setDigest(zookeeperProps.getValue(ZookeeperPropertyKey.DIGEST));
        return result;
    }
    
    @Override
    public String getType() {
        return "ZooKeeper";
    }
}

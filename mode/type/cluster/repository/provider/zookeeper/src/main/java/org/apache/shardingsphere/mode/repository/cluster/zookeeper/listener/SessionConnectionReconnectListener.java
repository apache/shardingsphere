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

package org.apache.shardingsphere.mode.repository.cluster.zookeeper.listener;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.service.ComputeNodeStatusService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Properties;

/**
 * Session connection reconnect listener.
 */
@Slf4j
public final class SessionConnectionReconnectListener implements ConnectionStateListener {
    
    private static final long RECONNECT_INTERVAL_SECONDS = 5L;
    
    private final InstanceContext instanceContext;
    
    private final ComputeNodeStatusService computeNodeStatusService;
    
    public SessionConnectionReconnectListener(final InstanceContext instanceContext, final ClusterPersistRepository repository) {
        this.instanceContext = instanceContext;
        computeNodeStatusService = new ComputeNodeStatusService(repository);
    }
    
    @Override
    public void stateChanged(final CuratorFramework client, final ConnectionState connectionState) {
        if (ConnectionState.LOST != connectionState) {
            return;
        }
        boolean isReconnectFailed;
        do {
            isReconnectFailed = !reconnect(client);
        } while (isReconnectFailed);
        log.info("Instance reconnect success, instance ID: {}", instanceContext.getInstance().getMetaData().getId());
    }
    
    private boolean reconnect(final CuratorFramework client) {
        try {
            if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                if (isNeedGenerateWorkerId()) {
                    instanceContext.generateWorkerId(new Properties());
                }
                reRegisterInstanceComputeNode();
                return true;
            }
            sleepInterval();
            return false;
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return true;
        }
    }
    
    private boolean isNeedGenerateWorkerId() {
        return -1 != instanceContext.getInstance().getWorkerId();
    }
    
    private void reRegisterInstanceComputeNode() {
        computeNodeStatusService.registerOnline(instanceContext.getInstance().getMetaData());
        computeNodeStatusService.persistInstanceLabels(instanceContext.getInstance().getMetaData().getId(), instanceContext.getInstance().getLabels());
        computeNodeStatusService.persistInstanceState(instanceContext.getInstance().getMetaData().getId(), instanceContext.getInstance().getState());
    }
    
    @SneakyThrows(InterruptedException.class)
    private void sleepInterval() {
        Thread.sleep(RECONNECT_INTERVAL_SECONDS * 1000L);
    }
}

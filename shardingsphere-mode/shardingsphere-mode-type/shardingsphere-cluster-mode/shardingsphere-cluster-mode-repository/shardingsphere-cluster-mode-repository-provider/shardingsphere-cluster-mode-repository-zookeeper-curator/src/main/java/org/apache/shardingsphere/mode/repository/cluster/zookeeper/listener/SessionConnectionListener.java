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

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.mode.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.handler.CuratorZookeeperExceptionHandler;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Session connection state listener.
 */
@Slf4j
@RequiredArgsConstructor
public final class SessionConnectionListener implements ConnectionStateListener {
    
    private static final int RECONNECT_INTERVAL_SECONDS = 5;
    
    private final InstanceContext instanceContext;
    
    private final ClusterPersistRepository repository;
    
    @Override
    public void stateChanged(final CuratorFramework client, final ConnectionState connectionState) {
        if (ConnectionState.LOST == connectionState) {
            boolean reRegistered;
            do {
                reRegistered = reRegister(client);
            } while (!reRegistered);
            log.debug("instance re-register success instance id: {}", instanceContext.getInstance().getCurrentInstanceId());
        }
    }
    
    private boolean reRegister(final CuratorFramework client) {
        try {
            if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                if (isNeedGenerateWorkerId()) {
                    instanceContext.generateWorkerId(new Properties());
                }
                repository.persistEphemeral(ComputeNode.getOnlineInstanceNodePath(instanceContext.getInstance().getCurrentInstanceId(),
                        instanceContext.getInstance().getMetaData().getType()), instanceContext.getInstance().getMetaData().getAttributes());
                return true;
            }
            sleepInterval();
            return false;
        } catch (final InterruptedException ex) {
            CuratorZookeeperExceptionHandler.handleException(ex);
            return true;
        }
    }
    
    private boolean isNeedGenerateWorkerId() {
        return -1L != instanceContext.getInstance().getWorkerId();
    }
    
    @SneakyThrows(InterruptedException.class)
    private void sleepInterval() {
        TimeUnit.SECONDS.sleep(RECONNECT_INTERVAL_SECONDS);
    }
}

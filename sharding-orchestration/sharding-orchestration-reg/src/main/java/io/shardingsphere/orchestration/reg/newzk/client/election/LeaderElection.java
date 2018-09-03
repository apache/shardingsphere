/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.reg.newzk.client.election;

import io.shardingsphere.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.WatcherCreator;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.ZookeeperEventListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

/**
 * Competition of node write permission.
 * It is not recommended to be used as a global variable.
 *
 * @author lidongbo
 */
@Slf4j
public abstract class LeaderElection {
    
    private int retryCount = ZookeeperConstants.NODE_ELECTION_RETRY;
    
    private boolean done;
    
    /**
     * Listener will be register when the contention of the path is unsuccessful.
     *
     * @param nodeBeContend node be contend
     * @param provider provider
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
    */
    public void executeContention(final String nodeBeContend, final IProvider provider) throws KeeperException, InterruptedException {
        boolean canBegin;
        final String realNode = provider.getRealPath(nodeBeContend);
        String contendNode = PathUtil.getRealPath(realNode, ZookeeperConstants.CHANGING_KEY);
        canBegin = contend(contendNode, provider, new ZookeeperEventListener(contendNode) {
            
            @Override
            public void process(final WatchedEvent event) {
                try {
                    retryCount--;
                    if (retryCount < 0) {
                        return;
                    }
                    executeContention(realNode, provider);
                } catch (final KeeperException | InterruptedException ex) {
                    log.error("Listener Exception executeContention:{}", ex.getMessage(), ex);
                }
            }
        });
        if (canBegin) {
            try {
                action();
                done = true;
                callback();
            } catch (final KeeperException | InterruptedException ex) {
                log.error("action Exception executeContention:{}", ex.getMessage(), ex);
            }
            provider.delete(contendNode);
        }
    }
    
    private boolean contend(final String node, final IProvider provider, final ZookeeperEventListener zookeeperEventListener) throws KeeperException, InterruptedException {
        boolean result = false;
        try {
            // TODO EPHEMERAL_SEQUENTIAL check index value
            provider.create(node, ZookeeperConstants.CLIENT_ID, CreateMode.EPHEMERAL);
            result = true;
        } catch (final KeeperException.NodeExistsException ex) {
            log.info("contend not result");
            // TODO or changing_key node value == current client id
            provider.exists(node, WatcherCreator.deleteWatcher(zookeeperEventListener));
        }
        return result;
    }
    
    /**
     * Wait done.
     */
    public void waitDone() {
        while (!done) {
            try {
                Thread.sleep(10L);
            } catch (final InterruptedException ex) {
                log.error("waitDone:{}", ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Contend execute.
     *
     * @throws KeeperException zookeeper exception
     * @throws InterruptedException interrupted exception
     */
    public abstract void action() throws KeeperException, InterruptedException;
    
    /**
     * Callback.
     */
    public void callback() {
    }
}

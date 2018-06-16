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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.election;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.Constants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Listener;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.WatcherCreator;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * competition of node write permission
 * It is not recommended to be used as a global variable.
 *
 * @author lidongbo
 */
public abstract class LeaderElection {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderElection.class);
    
    private boolean done;
    
    private int retryCount;
    
    public LeaderElection() {
        retryCount = Constants.NODE_ELECTION_RETRY;
    }

    private boolean contend(final String node, final IProvider provider, final Listener listener) throws KeeperException, InterruptedException {
        boolean success = false;
        try {
            // todo EPHEMERAL_SEQUENTIAL check index value
            provider.create(node, Constants.CLIENT_ID, CreateMode.EPHEMERAL);
            success = true;
        } catch (KeeperException.NodeExistsException e) {
            LOGGER.info("contend not success");
            // TODO or changing_key node value == current client id
            provider.exists(node, WatcherCreator.deleteWatcher(listener));
        }
        return success;
    }
    
    /**
     * listener will be register when the contention of the path is unsuccessful.
     *
     * @param nodeBeContend nodeBeContend
     * @param provider provider
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
    */
    public void executeContention(final String nodeBeContend, final IProvider provider) throws KeeperException, InterruptedException {
        boolean canBegin;
        final String realNode = provider.getRealPath(nodeBeContend);
        final String contendNode = PathUtil.getRealPath(realNode, Constants.CHANGING_KEY);
        canBegin = this.contend(contendNode, provider, new Listener(contendNode) {
            @Override
            public void process(final WatchedEvent event) {
                try {
                    retryCount--;
                    if (retryCount < 0) {
                        LOGGER.info("Election node exceed retry count");
                        return;
                    }
                    executeContention(realNode, provider);
                    // CHECKSTYLE:OFF
                } catch (Exception ee){
                    // CHECKSTYLE:ON
                    LOGGER.error("Listener Exception executeContention:{}", ee.getMessage(), ee);
                }
            }
        });
    
        if (canBegin) {
            try {
                action();
                done = true;
                callback();
                // CHECKSTYLE:OFF
            } catch (Exception e){
                // CHECKSTYLE:ON
                LOGGER.error("action Exception executeContention:{}", e.getMessage(), e);
            }
            provider.delete(contendNode);
        }
    }
    
    /**
     * wait done.
     */
    public void waitDone() {
        while (!done) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                LOGGER.error("waitDone:{}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * contend exec.
     *
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public abstract void action() throws KeeperException, InterruptedException;
    
    /**
     * callback.
     */
    public void callback() {
        
    }
}

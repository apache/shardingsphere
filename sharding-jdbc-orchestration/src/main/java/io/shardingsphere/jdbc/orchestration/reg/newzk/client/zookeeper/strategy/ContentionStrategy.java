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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.strategy;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.Callback;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.election.LeaderElection;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.Constants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Stack;

/*
 * The ContentionStrategy is effective only when all the clients of the node which be competitive are using ContentionStrategy.
 *
 * @author lidongbo
 */
public class ContentionStrategy extends UsualStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentionStrategy.class);
    
    public ContentionStrategy(final IProvider provider) {
        super(provider);
    }
    
    @Override
    /*
    * Don't use this if you don't have to use it.
    */
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        LeaderElection election = new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                getProvider().getData(getProvider().getRealPath(key), callback, ctx);
                LOGGER.debug("ContentionStrategy getData action:{}", key);
            }
        };
        getProvider().executeContention(election);
        LOGGER.debug("ContentionStrategy getData executeContention");
    }

    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        LeaderElection election = buildCreateElection(key, value, createMode, null);
        getProvider().executeContention(election);
        LOGGER.debug("ContentionStrategy createCurrentOnly executeContention");
        election.waitDone();
    }
    
    private LeaderElection buildCreateElection(final String key, final String value, final CreateMode createMode, final Callback callback) {
        return new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                getProvider().create(getProvider().getRealPath(key), value, createMode);
            }
            
            @Override
            public void callback() {
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        LeaderElection election = buildUpdateElection(key, value, null);
        getProvider().executeContention(election);
        LOGGER.debug("ContentionStrategy update executeContention");
        election.waitDone();
    }
    
    private LeaderElection buildUpdateElection(final String key, final String value, final Callback callback) {
        return new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                getProvider().update(getProvider().getRealPath(key), value);
                LOGGER.debug("ContentionStrategy update action:{},value:{}", key, value);
            }
            
            @Override
            public void callback() {
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        LeaderElection election = buildDeleteElection(key, null);
        getProvider().executeContention(election);
        LOGGER.debug("ContentionStrategy deleteOnlyCurrent executeContention");
        election.waitDone();
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        getProvider().executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                getProvider().delete(getProvider().getRealPath(key), callback, ctx);
                LOGGER.debug("ContentionStrategy deleteOnlyCurrent action:{},ctx:{}", key, ctx);
            }
        });
    }
    
    private LeaderElection buildDeleteElection(final String key, final Callback callback) {
        return new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                getProvider().delete(getProvider().getRealPath(key));
            }
            
            @Override
            public void callback() {
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        LeaderElection election = buildCreateAllNeedElection(key, value, createMode, null);
        getProvider().executeContention(election);
        LOGGER.debug("ContentionStrategy createAllNeedPath executeContention");
        election.waitDone();
    }
    
    private LeaderElection buildCreateAllNeedElection(final String key, final String value, final CreateMode createMode, final Callback callback) {
        return new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                LOGGER.debug("ContentionStrategy createAllNeedPath action:{}", key);
                createBegin(getProvider().getRealPath(key), value, createMode);
            }
            
            @Override
            public void callback() {
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }
    
    private void createBegin(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1) {
            getProvider().create(key, value, createMode);
            return;
        }
        List<String> nodes = getProvider().getNecessaryPaths(key);
        for (int i = 0; i < nodes.size(); i++) {
            if (getProvider().exists(nodes.get(i))) {
                LOGGER.info("create node exist:{}", nodes.get(i));
                continue;
            }
            LOGGER.debug("create node not exist:", nodes.get(i));
            if (i == nodes.size() - 1) {
                getProvider().create(nodes.get(i), value, createMode);
            } else {
                getProvider().create(nodes.get(i), Constants.NOTHING_VALUE, createMode);
            }
        }
    }

    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        getProvider().executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                deleteChildren(getProvider().getRealPath(key), true);
            }
        });
        LOGGER.debug("ContentionStrategy deleteAllChildren executeContention");
    }
    
    private void deleteChildren(final String key, final boolean deleteCurrentNode) throws KeeperException, InterruptedException {
        List<String> children = getProvider().getChildren(key);
        LOGGER.debug("deleteChildren:{}", children);
        for (int i = 0; i < children.size(); i++) {
            String child = PathUtil.getRealPath(key, children.get(i));
            if (!getProvider().exists(child)) {
                LOGGER.info("delete not exist:{}", child);
                continue;
            }
            LOGGER.debug("deleteChildren:{}", child);
            deleteChildren(child, true);
        }
        if (deleteCurrentNode) {
            getProvider().delete(key);
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        getProvider().executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                deleteBranch(getProvider().getRealPath(key));
            }
        });
        LOGGER.debug("ContentionStrategy deleteCurrentBranch executeContention");
    }
    
    private void deleteBranch(final String key) throws KeeperException, InterruptedException {
        deleteChildren(key, false);
        Stack<String> pathStack = getProvider().getDeletingPaths(key);
        while (!pathStack.empty()) {
            String node = pathStack.pop();
            // contrast cache
            if (getProvider().exists(node)) {
                try {
                    getProvider().delete(node);
                } catch (KeeperException.NotEmptyException e) {
                    LOGGER.warn("deleteBranch {} exist other children:{}", node, this.getChildren(node));
                    LOGGER.debug(e.getMessage());
                    return;
                }
            }
            LOGGER.info("deleteBranch node not exist:{}", node);
        }
    }
    
    //todo Use arbitrary competitive nodes
    //IExecStrategy convert to ContentionStrategy
    /*public void createCurrentOnly(final String key, final String value, final CreateMode createMode, final Callback callback) throws KeeperException, InterruptedException {
        getProvider().executeContention(buildCreateElection(key, value, createMode, callback));
    }
    
    public void update(final String key, final String value, final Callback callback) throws KeeperException, InterruptedException {
        getProvider().executeContention(buildUpdateElection(key, value, null));
    }

    public void deleteOnlyCurrent(final String key, final Callback callback) throws KeeperException, InterruptedException {
        getProvider().executeContention(buildDeleteElection(key, null));
    }*/
}

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

package io.shardingsphere.orchestration.reg.newzk.client.zookeeper.strategy;

import io.shardingsphere.orchestration.reg.newzk.client.action.ContentionCallback;
import io.shardingsphere.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.orchestration.reg.newzk.client.election.LeaderElection;
import io.shardingsphere.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.List;
import java.util.Stack;

/**
 * The contention strategy is effective only when all the clients of the node which be competitive are using contention strategy.
 *
 * @author lidongbo
 */
@Slf4j
public class ContentionStrategy extends UsualStrategy {
    
    public ContentionStrategy(final IProvider provider) {
        super(provider);
    }
    
    /*
    * Don't use this if you don't have to use it.
    */
    @Override
    public final void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        getProvider().executeContention(new LeaderElection() {
            
            @Override
            public void action() throws KeeperException, InterruptedException {
                getProvider().getData(getProvider().getRealPath(key), callback, ctx);
            }
        });
    }

    @Override
    public final void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        LeaderElection election = buildCreateElection(key, value, createMode, null);
        getProvider().executeContention(election);
        election.waitDone();
    }
    
    private LeaderElection buildCreateElection(final String key, final String value, final CreateMode createMode, final ContentionCallback contentionCallback) {
        return new LeaderElection() {
            
            @Override
            public void action() throws KeeperException, InterruptedException {
                getProvider().create(getProvider().getRealPath(key), value, createMode);
            }
            
            @Override
            public void callback() {
                if (null != contentionCallback) {
                    contentionCallback.processResult();
                }
            }
        };
    }
    
    @Override
    public final void update(final String key, final String value) throws KeeperException, InterruptedException {
        LeaderElection election = buildUpdateElection(key, value, null);
        getProvider().executeContention(election);
        election.waitDone();
    }
    
    private LeaderElection buildUpdateElection(final String key, final String value, final ContentionCallback contentionCallback) {
        return new LeaderElection() {
            
            @Override
            public void action() throws KeeperException, InterruptedException {
                getProvider().update(getProvider().getRealPath(key), value);
            }
            
            @Override
            public void callback() {
                if (null != contentionCallback) {
                    contentionCallback.processResult();
                }
            }
        };
    }
    
    @Override
    public final void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        LeaderElection election = buildDeleteElection(key, null);
        getProvider().executeContention(election);
        election.waitDone();
    }
    
    @Override
    public final void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        getProvider().executeContention(new LeaderElection() {
            
            @Override
            public void action() throws KeeperException, InterruptedException {
                getProvider().delete(getProvider().getRealPath(key), callback, ctx);
            }
        });
    }
    
    private LeaderElection buildDeleteElection(final String key, final ContentionCallback contentionCallback) {
        return new LeaderElection() {
            
            @Override
            public void action() throws KeeperException, InterruptedException {
                getProvider().delete(getProvider().getRealPath(key));
            }
            
            @Override
            public void callback() {
                if (null != contentionCallback) {
                    contentionCallback.processResult();
                }
            }
        };
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        LeaderElection election = buildCreateAllNeedElection(key, value, createMode, null);
        getProvider().executeContention(election);
        election.waitDone();
    }
    
    private LeaderElection buildCreateAllNeedElection(final String key, final String value, final CreateMode createMode, final ContentionCallback contentionCallback) {
        return new LeaderElection() {
            
            @Override
            public void action() throws KeeperException, InterruptedException {
                createBegin(getProvider().getRealPath(key), value, createMode);
            }
            
            @Override
            public void callback() {
                if (null != contentionCallback) {
                    contentionCallback.processResult();
                }
            }
        };
    }
    
    private void createBegin(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        // TODO start with /
        if (!key.contains(ZookeeperConstants.PATH_SEPARATOR)) {
            getProvider().create(key, value, createMode);
            return;
        }
        List<String> nodes = getProvider().getNecessaryPaths(key);
        for (int i = 0; i < nodes.size(); i++) {
            if (getProvider().exists(nodes.get(i))) {
                continue;
            }
            if (i == nodes.size() - 1) {
                getProvider().create(nodes.get(i), value, createMode);
            } else {
                getProvider().create(nodes.get(i), ZookeeperConstants.NOTHING_VALUE, createMode);
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
    }
    
    private void deleteChildren(final String key, final boolean deleteCurrentNode) throws KeeperException, InterruptedException {
        List<String> children = getProvider().getChildren(key);
        for (String aChildren : children) {
            String child = PathUtil.getRealPath(key, aChildren);
            if (!getProvider().exists(child)) {
                continue;
            }
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
                    log.warn("deleteBranch {} exist other children: {}", node, this.getChildren(node));
                    return;
                }
            }
            log.info("deleteBranch node not exist: {}", node);
        }
    }
    
    //TODO Use arbitrary competitive nodes
    //IExecStrategy convert to ContentionStrategy
    /*public void createCurrentOnly(final String key, final String value, final CreateMode createMode, final ContentionCallback callback) throws KeeperException, InterruptedException {
        getProvider().executeContention(buildCreateElection(key, value, createMode, callback));
    }
    
    public void update(final String key, final String value, final ContentionCallback callback) throws KeeperException, InterruptedException {
        getProvider().executeContention(buildUpdateElection(key, value, null));
    }

    public void deleteOnlyCurrent(final String key, final ContentionCallback callback) throws KeeperException, InterruptedException {
        getProvider().executeContention(buildDeleteElection(key, null));
    }*/
}

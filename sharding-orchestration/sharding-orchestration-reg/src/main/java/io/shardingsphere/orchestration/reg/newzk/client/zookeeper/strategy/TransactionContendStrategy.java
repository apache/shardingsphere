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
import io.shardingsphere.orchestration.reg.newzk.client.action.ITransactionProvider;
import io.shardingsphere.orchestration.reg.newzk.client.election.LeaderElection;
import io.shardingsphere.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.provider.BaseProvider;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.transaction.ZooKeeperTransaction;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.List;
import java.util.Stack;

/**
 * ContentionStrategy with transaction.
 *
 * @author lidongbo
 * @since zookeeper 3.4.0
 */
public final class TransactionContendStrategy extends ContentionStrategy {
    
    public TransactionContendStrategy(final ITransactionProvider provider) {
        super(provider);
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
                ZooKeeperTransaction transaction = new ZooKeeperTransaction(((BaseProvider) getProvider()).getRootNode(), ((BaseProvider) getProvider()).getHolder());
                createBegin(key, value, createMode, transaction);
                transaction.commit();
            }
            
            @Override
            public void callback() {
                if (contentionCallback != null) {
                    contentionCallback.processResult();
                }
            }
        };
    }

    private void createBegin(final String key, final String value, final CreateMode createMode, final ZooKeeperTransaction transaction) throws KeeperException, InterruptedException {
        if (!key.contains(ZookeeperConstants.PATH_SEPARATOR)) {
            ((ITransactionProvider) getProvider()).createInTransaction(key, value, createMode, transaction);
            return;
        }
        List<String> nodes = getProvider().getNecessaryPaths(key);
        for (int i = 0; i < nodes.size(); i++) {
            if (getProvider().exists(nodes.get(i))) {
                continue;
            }
            if (i == nodes.size() - 1) {
                ((ITransactionProvider) getProvider()).createInTransaction(nodes.get(i), value, createMode, transaction);
            } else {
                ((ITransactionProvider) getProvider()).createInTransaction(nodes.get(i), ZookeeperConstants.NOTHING_VALUE, createMode, transaction);
            }
        }
    }

    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        getProvider().executeContention(new LeaderElection() {
            
            @Override
            public void action() throws KeeperException, InterruptedException {
                ZooKeeperTransaction transaction = new ZooKeeperTransaction(((BaseProvider) getProvider()).getRootNode(), ((BaseProvider) getProvider()).getHolder());
                deleteChildren(getProvider().getRealPath(key), true, transaction);
                transaction.commit();
            }
        });
    }
    
    private void deleteChildren(final String key, final boolean deleteCurrentNode, final ZooKeeperTransaction transaction) throws KeeperException, InterruptedException {
        List<String> children = getProvider().getChildren(key);
        for (String each : children) {
            String child = PathUtil.getRealPath(key, each);
            if (!getProvider().exists(child)) {
                continue;
            }
            deleteChildren(child, true, transaction);
        }
        if (deleteCurrentNode) {
            transaction.delete(key, ZookeeperConstants.VERSION);
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        getProvider().executeContention(new LeaderElection() {
            
            @Override
            public void action() throws KeeperException, InterruptedException {
                ZooKeeperTransaction transaction = new ZooKeeperTransaction(((BaseProvider) getProvider()).getRootNode(), ((BaseProvider) getProvider()).getHolder());
                deleteBranch(getProvider().getRealPath(key), transaction);
                transaction.commit();
            }
        });
    }

    private void deleteBranch(final String key, final ZooKeeperTransaction transaction) throws KeeperException, InterruptedException {
        deleteChildren(key, false, transaction);
        Stack<String> pathStack = getProvider().getDeletingPaths(key);
        String prePath = key;
        while (!pathStack.empty()) {
            String node = pathStack.pop();
            // contrast cache
            // Performance needs testing
            List<String> children = getProvider().getChildren(node);
            boolean canDelete = children.isEmpty() || 1 == children.size();
            if (1 == children.size()) {
                if (!PathUtil.getRealPath(node, children.get(0)).equals(prePath)) {
                    canDelete = false;
                }
            }
            if (getProvider().exists(node) && canDelete) {
                transaction.delete(node, ZookeeperConstants.VERSION);
            }
            prePath = node;
        }
    }
}
